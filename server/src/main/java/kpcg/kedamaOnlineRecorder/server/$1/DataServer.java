package kpcg.kedamaOnlineRecorder.server.$1;

import java.io.File;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.server.$1.DataServerConfig.HttpConfig;

public class DataServer {

	private NioEventLoopGroup group;
	
	private ServerBootstrap b;
	
	private InetSocketAddress localAddress;
	
	private DataServerConfig cfg;
	
	private SslContext sslContext;
	
	private HttpAllocator allocatorHandler;
	
	private Channel channel;
	
	private InternalLogger logger = InternalLoggerFactory.getInstance(DataServer.class);
	
	public DataServer (){
		
	}
	
	public void setConfig(DataServerConfig cfg) {
		this.cfg = cfg;
	}
	
	public void init() {
		
		allocatorHandler = new HttpAllocator();
		
		
		localAddress = new InetSocketAddress(cfg.net.host, cfg.net.port);
		if(cfg.net.ssl != null) {
			try {
				sslContext = SslContextBuilder.forServer(null, new File(cfg.net.ssl.keystore), cfg.net.ssl.keystorepw).build();	
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		group = new NioEventLoopGroup(cfg.thread);
		b = new ServerBootstrap();
		b.group(group)
		 .channel(NioServerSocketChannel.class)
		 .localAddress(localAddress)
		 .childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				if(sslContext != null) {
			        p.addLast("ssl", sslContext.newHandler(ch.alloc()));
				}
				HttpConfig hcfg = cfg.http;
				p.addLast("codec", new HttpServerCodec(hcfg.maxInitialLineLength, hcfg.maxHeaderSize, hcfg.maxChunkSize));
				p.addLast("aggregate", new HttpObjectAggregator(hcfg.maxInitialLineLength + hcfg.maxHeaderSize + hcfg.maxChunkSize + 256));
				p.addLast("compress", new HttpContentCompressor(hcfg.compressionLevel, hcfg.windowBits, hcfg.memLevel));
				p.addLast("allocate", allocatorHandler);
			}
		});
	}
	
	public void start() throws InterruptedException {
		if(b != null) {
			channel = b.bind().addListener((future)->{logger.info("$ start");;}).channel();
		}
	}
	
	public void stop() throws InterruptedException {
		if(b != null) {
			channel.close().addListener((future)->{
				logger.info("$ stop");
				b = null;
			});
			group.shutdownGracefully().addListener((future)->{logger.info("$ shutdown");});
		}
	}
}
