package kpcg.kedamaOnlineRecorder.client.$1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.util.Util;

public class MinecraftPing implements Runnable {

	interface MinecraftPingHandler {
		
		public void handle(JsonNode node) throws Exception;
		
		public void pingHandleExceptionCaught(Throwable cause) throws Exception;
	}
	
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(MinecraftPing.class);
	
	
	private InetSocketAddress address;
	
	private MinecraftPingConfig cfg;
	
	private final int MaxCapability = 1024;
	
	private final Charset charset = Charset.forName("UTF-8");
	
	private ObjectMapper mapper;
	
	protected byte[] buf;
	
	protected int pointer;
	
	protected int limit;
	
	protected Thread t;
	
	protected byte[] lock;
	
	protected int state;
	
	protected long triggleTime;
	
	private MinecraftPingHandler handler;
	
	public void clearBuffer() {
		pointer = 0;
		limit = buf.length;
	}
	
	public void writeVarInt(int value) throws IOException {
		if(value >= 0 && value <= 0x7F) {
			if(pointer >= limit)
				throw new IOException("write VarInt out of bound");
			buf[pointer++] = (byte)(value & 0x7F);
			return;
		}
		while((value & 0xFFFFFF80)!= 0) {
			if(pointer >= limit)
				throw new IOException("write VarInt out of bound");
			buf[pointer++] = (byte)(value & 0x7F | 0x80);
			value >>>= 7;
		}
		if(pointer >= limit)
			throw new IOException("write VarInt out of bound");
		buf[pointer++] = (byte)(value & 0x7F);
	}
	
	public void writeString(String s) throws IOException {
		writeVarInt(s.length());
		byte[] tmp = s.getBytes(charset);
		if(pointer + tmp.length > limit)
			throw new IOException("write String out of bound");
		System.arraycopy(tmp, 0, buf, pointer, tmp.length);
		pointer += tmp.length;
	}
	
	public void writeShortBE(short value) throws IOException {
		if(pointer + 2 > limit)
			throw new IOException("write Short(BE) out of bound");
		buf[pointer++] = (byte) ((value & 0xFF00) >> 8);
		buf[pointer++] = (byte) (value & 0x00FF);
	}
	
	public int readVarInt() throws IOException {
		int value = 0;
		for(int b = buf[pointer++], i = 0; pointer < limit; b = buf[pointer++], i += 7) {
			value |= ((b & 0x7F) << i);
			if((b & 0x80) == 0x00)
				break;
		}
		if(pointer >= limit)
			throw new IOException("read VarInt out of bound");
		return value;
	}
	
	public String readString() throws IOException {
		int len = readVarInt();
		if(len + pointer > limit)
			throw new IOException("read String out of bound");
		String s = new String(buf, pointer, len, charset);
		pointer += len;
		return s;
	}
	
	public void readFrom(InputStream in) throws IOException {
		limit = 0;
		for(int i = 0, b = in.read(); b >= 0; b = in.read(), i += 7) {
			limit |= ((b & 0x7F) << i);
			if((b & 0x80) == 0x00)
				break;
		}
		pointer = 0;
		if(buf.length < limit)
			buf = new byte[limit];
		for(int n = in.read(buf, pointer, limit - pointer); n > 0 && pointer < limit; pointer += n, n = in.read(buf, pointer, limit - pointer));
		pointer = 0;
	}
	
	public void writeTo(OutputStream out) throws IOException {
		int value = pointer;
		if(value >= 0 && value <= 0xFF) {
			out.write(value);
		} else {
			while((value & 0xFFFFFF80)!= 0) {
				out.write(value & 0x7F | 0x80);
				value >>>= 7;
			}
			out.write(value & 0x7F);
		}
		out.write(buf, 0, pointer);
		pointer = 0;
		limit = buf.length;
	}
	
	public MinecraftPing setConfig(MinecraftPingConfig cfg) {
		this.cfg = cfg;
		return this;
	}
	
	public MinecraftPing registeHandler(MinecraftPingHandler handler) {
		this.handler = handler;
		return this;
	}
	
	public void init() {
		buf = new byte[MaxCapability];
		address = new InetSocketAddress(cfg.minecrafthost, cfg.minecraftport);
		mapper = new ObjectMapper();
		lock = new byte[] {0x00};
		state = 0;
	}
	
	public void setHandler(MinecraftPingHandler handler) {
		this.handler = handler;
	}

	public JsonNode ping() throws InterruptedException {
		JsonNode node = null;
		InputStream in = null;
		OutputStream out = null;
		Socket socket = new Socket();
		try {
			socket.connect(address, cfg.timeout * 1000);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			
			clearBuffer();
			
			writeVarInt(0x00);						//	package id
			writeVarInt(cfg.protocolversion);					//	protocol version
			writeString(address.getHostName());		//	host name
			writeShortBE((short) address.getPort());;	//	port
			writeVarInt(0x01);						//	next state
			writeTo(out);
			out.flush();
			
			writeVarInt(0x00);						//	package id
			writeTo(out);
			out.flush();
			
			readFrom(in);
			int pkgid = readVarInt();
			if(pkgid != 0x00)
				throw new IOException("Unpredictable reply: package id = " + pkgid);
			String json = readString();
			//TODO record
			node = mapper.readTree(json);
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.debug(e);
//			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.warn(e);
//				e.printStackTrace();
			}
		}
		if(t == null || t.isInterrupted())
			throw new InterruptedException();
		return node;
	}

	@Override
	public void run() {
		JsonNode node;
		int continusRetryTimes = 0;
		long targetT = 0;
		long normalT = targetT;
		t = Thread.currentThread();
		state = 1;
		while(!t.isInterrupted()) {
			
			try {
				if(state == 1)
					normalT += (long)(cfg.NormalInterval * 1000);
				node = ping();
				if(node == null) {					
					continusRetryTimes++;
					if(continusRetryTimes > cfg.maxRetryTimes) {
						targetT = normalT;
						continusRetryTimes = 0;
						state = 1;
					} else {
						if(state == 2)
							targetT = triggleTime + (long)(cfg.RetryInterval * 1000);
						else
							targetT += (long)(cfg.RetryInterval * 1000);
						state = 3;
					}
				} else {
					continusRetryTimes = 0;
					targetT = normalT;
					state = 1;
					if(handler != null) {
						try {
							handler.handle(node);
						} catch (InterruptedException e) {
							//TODO log
							logger.debug(e);
//							e.printStackTrace();
							break;
						} catch (Exception e) {
							handler.pingHandleExceptionCaught(e);
						}
					} else {
						//TODO log
//						System.out.println(node);
						logger.info("> ping: result ({})", node);
					}
				}
				synchronized (lock) {
					if(targetT - System.currentTimeMillis() < cfg.MinimalInterval * 1000L) {
						long i = (cfg.MinimalInterval * 1000L - (targetT - System.currentTimeMillis())) / (cfg.NormalInterval * 1000L);
						targetT += cfg.NormalInterval * 1000L * (i + 1);
						normalT += cfg.NormalInterval * 1000L * (i + 1);
					}
					logger.debug("> ping: next @{}", LocalDateTime.ofInstant(Instant.ofEpochMilli(targetT), Util.zone).format(Util.formatter));
//					System.err.println(targetT);
					lock.wait(targetT - System.currentTimeMillis());
				}
			} catch (InterruptedException e) {
				//TODO log
				logger.debug(e);
//				e.printStackTrace();
				break;
			} catch (Exception e) {
				// TODO: handle exception
				logger.warn(e);
//				e.printStackTrace();
			}
		}
		t = null;
		handler = null;
		logger.info("> ping: stopped");
	}
	
	public void stop() {
		if(t != null)
			t.interrupt();
		handler = null;
	}
	
	public void triggle() {
		synchronized (lock) {
			state = 2;
			triggleTime = System.currentTimeMillis();
			lock.notify();
		}
	}
}
