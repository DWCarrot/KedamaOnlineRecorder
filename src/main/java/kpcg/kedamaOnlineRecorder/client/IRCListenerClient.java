package kpcg.kedamaOnlineRecorder.client;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class IRCListenerClient {
	
	Bootstrap b;
	
	EventLoopGroup g;
	
	InetSocketAddress ircAddr;
	
	SslContext sslContext;
	
	Channel channel;
	
	IRCListenerClientConfig config;
	
	IRCHandlerMessage handleMsg;
	
	public IRCListenerClient() {
		
	}
	
	public void setConfig(IRCListenerClientConfig cfg) {
		config = cfg;
	}
	
	public void init(EventLoopGroup group) {
		ircAddr = new InetSocketAddress(config.net.host, config.net.port);
		handleMsg = new IRCHandlerMessage();
		sslContext = null;
		try {
			if (config.net.ssl)
				sslContext = SslContextBuilder.forClient().build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b = new Bootstrap();
		b.group(g = group)
		 .channel(NioSocketChannel.class)
		 .remoteAddress(ircAddr)
		 .handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ChannelPipeline p = ch.pipeline();
				if(sslContext != null) {
			        p.addLast("ssl", sslContext.newHandler(ch.alloc()));
				}				
				p.addLast("codec", new IRCMessageCodec());
				p.addLast("pre", new IRCPreHandler().setConfig(config).setStatus(handleMsg).setExecutor(group));
				p.addLast("listen", new IRCListenerHandler().setConfig(config));
				p.addLast("response", new IRCResponseHandler().setConfig(config));
			}			 
		 });
	}
	
	protected Runnable startTask() {
		return new Runnable() {			
			@Override
			public void run() {
				start();
			}
		};
	}
	
	public void start() {
		
		try {
			handleMsg.clear();
			ChannelFuture f = b.connect().sync().addListener((future)->{
				//TODO log
				System.out.println("IRC Client started. ");
			});
			
			channel = f.channel();
			channel.closeFuture().addListener((future)->{
				int delay = -1;
				//TODO log
				System.out.println("IRC Client closed.");
				if(handleMsg.message == 0)
					handleMsg.message = IRCHandlerMessage.UNKNOWN;
				switch (handleMsg.message) {
				case IRCHandlerMessage.NAME_IN_USE:
					delay = config.restartInterval.NameInUse;//.NameInUseRetryInterval;
					break;
				case IRCHandlerMessage.NO_MESSAGE:
					delay = config.restartInterval.NoMessage;//.NoMessageRetryInterval;
					break;
				case IRCHandlerMessage.BE_KICKED:
					delay = config.restartInterval.BeKicked;//.BeKickedRetryInterval;
					break;
				case IRCHandlerMessage.ERROR:
					delay = config.restartInterval.IRCError;//.IRCErrorRetryInterval;
					break;
				case IRCHandlerMessage.UNKNOWN:
					delay = config.restartInterval.UnknownFailure;//.UnknownFailureRetryInterval;
					break;
				case IRCHandlerMessage.CALLED_STOP:
					break;
				}
				if(delay < 0) {
					//TODO log
					System.out.println("IRC Client terminated.");
				} else {
					//TODO log
					System.out.println("IRC Client restart after " + delay + 's');
					channel.eventLoop().schedule(startTask(), delay, TimeUnit.SECONDS);
				}
				channel = null;

			});
		} catch (InterruptedException e) {
			e.printStackTrace();
			handleMsg = null;
			//TODO log
			System.out.println("IRC Client terminated.");
		} catch (Exception e) {
			e.printStackTrace();
			if(!(g == null || g.isShutdown() || g.isShuttingDown())) {
				g.schedule(startTask(), config.restartInterval.ConnectFailure, TimeUnit.SECONDS);
			} else {
				handleMsg = null;
				//TODO log
				System.out.println("IRC Client terminated.");
			}
		}
	}

	public void stop(GenericFutureListener<? extends Future<? super Void>> listener) {
		if (channel != null) {
			handleMsg.message = IRCHandlerMessage.CALLED_STOP;
			ChannelFuture future = channel.disconnect();
			if(listener != null)
				future.addListener(listener);
		}
	}
	
}
