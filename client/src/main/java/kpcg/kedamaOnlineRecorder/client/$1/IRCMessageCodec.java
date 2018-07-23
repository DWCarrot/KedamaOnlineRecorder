package kpcg.kedamaOnlineRecorder.client.$1;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.util.Util;

public class IRCMessageCodec extends ByteToMessageCodec<IRCMessage> {
	
	private final static InternalLogger logger = InternalLoggerFactory.getInstance(IRCMessageCodec.class);

	private static Charset charset = Util.charset;
	
	
	private int maxLength = 1024;	//IRC message limit to about 250/line; need not to change
	
	private int maxTrailing = 400;
	
	protected int offset = 0;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		String s = null;
		int totalLength = in.readableBytes();
		int eol = in.forEachByte(in.readerIndex() + offset, totalLength - offset, ByteProcessor.FIND_LF);
		int skip = 0;
		if (eol >= 0) {
			offset = 0;
			skip = 1;
			if (eol > 0 && in.getByte(eol - 1) == '\r') {
				--eol;
				++skip;
			}
			s = (String) in.readCharSequence(eol - in.readerIndex(), charset);
			in.skipBytes(skip);
			IRCMessage msg = new IRCMessage(System.currentTimeMillis(), s);
			out.add(msg);
			if(msg.command.equals("PING"))
				logger.debug("[=>|] {}", msg.toString());
			else
				logger.info("[=>|] {}", msg.toString());
			
		} else {
			offset = totalLength;
			if(totalLength > maxLength) {
				in.skipBytes(totalLength);
				throw new TooLongFrameException("readable buffer length: " + totalLength);
			}
		}		
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, IRCMessage msg, ByteBuf out) throws Exception {
		String s;
		if(msg.command.equals("PRIVMSG") && msg.trailing.length() > maxTrailing) {
			String t = msg.trailing;
			for(int i = 0, j = maxTrailing; i < t.length(); i = j, j += maxTrailing) {
				if(j > t.length())
					j = t.length();
				msg.trailing = t.substring(i, j);
				out.writeCharSequence(msg.toString() + "\r\n", charset);
			}
		} else {
			out.writeCharSequence(msg.toString() + "\r\n", charset);
		}
		if(msg.command.equals("PONG"))
			logger.debug("[<=|] {}", msg.toString());
		else
			logger.info("[<=|] {}", msg.toString());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {	
		if(cause instanceof TooLongFrameException || cause instanceof IndexOutOfBoundsException)
			logger.warn(cause);
		else 
			super.exceptionCaught(ctx, cause);
	}
}
