package kpcg.kedamaOnlineRecorder.server.$1;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

@Sharable
public class HttpAllocator extends ChannelInboundHandlerAdapter {
	
	public static class HttpRequestPair {
		public FullHttpRequest req;		
		public URI uri;
		public HttpRequestPair(FullHttpRequest req) throws URISyntaxException {
			this.req = req;
			this.uri = new URI(req.uri());
		}
	}
	
	private Map<String, ChannelInboundHandler> handlers;
	
	public HttpAllocator() {
		handlers = new HashMap<>();
	}
	
	public void register(String path, ChannelInboundHandler handler) {
		handlers.put(path, handler);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		for(ChannelInboundHandler handler : handlers.values())
			handler.handlerAdded(ctx);
		super.handlerAdded(ctx);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerRemoved(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof FullHttpRequest) {
			HttpRequestPair pair = new HttpRequestPair((FullHttpRequest) msg);
			ChannelInboundHandler h = handlers.get(pair.uri.getPath());
			h.channelRead(ctx, pair);
			return;
		}
		super.channelRead(ctx, msg);
	}
	
}
