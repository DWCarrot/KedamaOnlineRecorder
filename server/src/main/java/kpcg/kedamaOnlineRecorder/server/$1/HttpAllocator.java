package kpcg.kedamaOnlineRecorder.server.$1;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import kpcg.kedamaOnlineRecorder.server.$1.DataServerConfig.HttpConfig;

@Sharable
public class HttpAllocator extends ChannelInboundHandlerAdapter {
	
	public interface HttpHandler {
		
		public void channelRead(ChannelHandlerContext ctx, FullHttpRequest req, URI uri) throws Exception;
		
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
	}
	
	private static Charset charset = Charset.forName("UTF-8");
	
	private static DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
	
	
	private File resp404;
	
	private File resp50x;
	
	private Map<String, HttpHandler> handlers;
	
	private HttpConfig cfg;
	
	public HttpAllocator() {
		handlers = new HashMap<>();
		resp404 = new File("static/404.html");
		resp50x = new File("static/50x.html");
	}
	
	public void register(String path, HttpHandler handler) {
		handlers.put(path, handler);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
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
			FullHttpRequest req = (FullHttpRequest) msg;
			URI uri = new URI(req.uri());
			HttpHandler h = handlers.get(uri.getPath());
			if(h != null) {
				h.channelRead(ctx, req, uri);
			} else {
				ctx.write(buildTinyFileResponse(resp404, HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, ctx.alloc()));
			}
			return;
		}
		super.channelRead(ctx, msg);
	}
	
	public FullHttpResponse buildTinyFileResponse(File file, HttpVersion version, HttpResponseStatus status, ByteBufAllocator allocator) {
		ByteBuf content = null;
		try(InputStream ifile = new FileInputStream(file)) {
			content = allocator.buffer(256, cfg.maxSendSize);
			content.writeBytes(ifile, Math.min(ifile.available(), content.writableBytes()));
			ifile.close();
		} catch (IOException e) {
			content = allocator.buffer(256, cfg.maxSendSize);
			content.writeCharSequence(status.codeAsText(), charset);
		}
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(version, status, content);
		HttpHeaders headers = resp.headers();
		headers.add(HttpHeaderNames.DATE, LocalDateTime.now().format(formatter));
		headers.add(HttpHeaderNames.CONTENT_TYPE, "text/javascript; charset=utf-8");
		headers.add(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
		return resp;
	}
}
