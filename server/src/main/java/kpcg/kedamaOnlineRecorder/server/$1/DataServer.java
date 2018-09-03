package kpcg.kedamaOnlineRecorder.server.$1;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.Socket;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class DataServer {

	private NioEventLoopGroup group;
	
	private ServerBootstrap b;
	
	private InetSocketAddress localAddress;
	
	private DataServerConfig cfg;
	
	private SslContext sslContext;
	
	private HttpAllocator allocatorHandler;
	
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
				p.addLast("decode", new HttpRequestDecoder());
                p.addLast("encode", new HttpResponseEncoder());
				p.addLast("aggregate", new HttpObjectAggregator(8192));
				p.addLast("allocate", allocatorHandler);
			}
		});
	}
	
	public void start() {
		
	}
}
