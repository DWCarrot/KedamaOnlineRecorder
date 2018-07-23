package kpcg.kedamaOnlineRecorder.client.$1;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class IRCListenerClient {
	
	private final static InternalLogger logger = InternalLoggerFactory.getInstance(IRCListenerClient.class);
	
	ScheduledExecutorService s;
	
	Bootstrap b;
	
	EventLoopGroup g;
	
	InetSocketAddress ircAddr;
	
	SslContext sslContext;
	
	Channel channel;
	
	IRCListenerClientConfig config;
	
	int thread;
	
	IRCHandlerMessage handleMsg;
	
	ScheduledFuture<?> restart;
	
	public IRCListenerClient() {
		
	}
	
	public void setConfig(IRCListenerClientConfig cfg) {
		config = cfg;
	}
	
	public void setExecutor(ScheduledExecutorService service) {
		s = service;
	}
	
	protected Runnable startTask() {
		return new Runnable() {			
			@Override
			public void run() {
				init(thread);
				start();
			}
		};
	}
	
	public void init(int thread) {
		this.thread = thread;
		g = new NioEventLoopGroup(this.thread, s);
		ircAddr = new InetSocketAddress(config.net.host, config.net.port);
		handleMsg = new IRCHandlerMessage();
		sslContext = null;
		try {
			if (config.net.ssl)
				sslContext = SslContextBuilder.forClient().build();
		} catch (SSLException e) {
			logger.warn(e);
		}
		b = new Bootstrap();
		b.group(g)
		 .channel(NioSocketChannel.class)
		 .remoteAddress(ircAddr)
		 .handler(new ChannelInitializer<SocketChannel>() {
			 
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				if(sslContext != null) {
			        p.addLast("ssl", sslContext.newHandler(ch.alloc()));
				}				
				p.addLast("codec", new IRCMessageCodec());
				p.addLast("pre", new IRCPreHandler().setConfig(config).setStatus(handleMsg).setExecutor(g));
				p.addLast("listen", new IRCListenerHandler().setConfig(config));
				p.addLast("response", new IRCResponseHandler().setConfig(config));
			}			 
		 });
	}
	
	public void start() {
		
		try {
			handleMsg.clear();
			restart = null;
			ChannelFuture f = b.connect().sync().addListener((future)->{
				logger.info("> irc: client started");
			});
			
			channel = f.channel();
			channel.closeFuture().addListener((future)->{
				g.shutdownGracefully().addListener((future1)-> {
					int delay = -1;
					String reason = null;			
					if(handleMsg.message == 0)
						handleMsg.message = IRCHandlerMessage.UNKNOWN;
					switch (handleMsg.message) {
					case IRCHandlerMessage.NAME_IN_USE:
						delay = config.restartInterval.NameInUse;//.NameInUseRetryInterval;
						reason = "name in use";
						break;
					case IRCHandlerMessage.NO_MESSAGE:
						delay = config.restartInterval.NoMessage;//.NoMessageRetryInterval;
						reason = "no message";
						break;
					case IRCHandlerMessage.BE_KICKED:
						delay = config.restartInterval.BeKicked;//.BeKickedRetryInterval;
						reason = "be kicked";
						break;
					case IRCHandlerMessage.ERROR:
						reason = "irc error";
						delay = config.restartInterval.IRCError;//.IRCErrorRetryInterval;
						break;
					case IRCHandlerMessage.UNKNOWN:
						reason = "unknown";
						delay = config.restartInterval.UnknownFailure;//.UnknownFailureRetryInterval;
						break;
					case IRCHandlerMessage.CALLED_STOP:
						reason = "called stop";
						break;
					}
					logger.info("> irc: client closed ({})", reason);
					if(delay < 0) {
						logger.info("> irc: client terminated");
					} else {
						if(restart == null) {
							logger.info("> irc: client will restart after {}s", delay);
							restart = s.schedule(startTask(), delay, TimeUnit.SECONDS);
						}
					}
				});
				channel = null;
			});
		} catch (InterruptedException e) {
			logger.debug(e);
			handleMsg = null;
			logger.info("> irc: client terminated");
		} catch (Exception e) {
			logger.warn(e);
			g.shutdownGracefully().addListener((future)->{
				if(!(s == null || s.isShutdown()) && restart != null) {
					logger.info("> irc: client will restart after {}s", config.restartInterval.ConnectFailure);
					restart = s.schedule(startTask(), config.restartInterval.ConnectFailure, TimeUnit.SECONDS);
				} else {
					handleMsg = null;
					logger.info("> irc: client terminated");
				}
			});
		}
	}

	public void stop(GenericFutureListener<? extends Future<? super Void>> listener) {
		if(restart != null) {
			restart.cancel(true);
			restart = null;
			logger.info("> irc: client terminated");
			if(listener != null) {				
				try {
					listener.operationComplete(null);
				} catch (Exception e) {
					logger.warn(e);
				}			
			}
		}		
		if (channel != null) {
			handleMsg.message = IRCHandlerMessage.CALLED_STOP;		
			ChannelFuture future = channel.disconnect();
			if(listener != null)
				future.addListener(listener);
		}
	}
	
}
