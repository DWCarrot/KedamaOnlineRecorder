package kpcg.kedamaOnlineRecorder.client;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.ConfigFeature;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kpcg.kedamaOnlineRecorder.client.IRCListenerClientConfig.ResponseConfig;
import kpcg.kedamaOnlineRecorder.util.CreepyClass;
import kpcg.kedamaOnlineRecorder.util.Util;

public class IRCResponseHandler extends ChannelInboundHandlerAdapter {

	public class Container {
		
		@JsonSerialize(using = kpcg.kedamaOnlineRecorder.util.NRClassSerializer.class)
		public Object obj;
	}
	
	IRCListenerClientConfig config;
	
	String keyAll;
	
	public IRCResponseHandler() {
		
	}

	public IRCResponseHandler setConfig(IRCListenerClientConfig config) {
		this.config = config;
		keyAll = new StringBuilder().append('@').append(' ').append(config.user.nick).append(' ').toString();
		return this;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof IRCMessage) {
			IRCMessage ircMsg = (IRCMessage) msg;
			if(ircMsg.command.equals("PRIVMSG")) {
				String message = ircMsg.trailing;
				boolean isPublic = false;
				int i = message.indexOf(keyAll);
				int j;
				String key;
				String param;
				if(i >= 0) {
					i += keyAll.length();
					j = message.indexOf(' ', i);
					if(j > i) {
						key = message.substring(i, j);
						i = j + 1;
						j = message.indexOf(' ', i);
						if(j > i)
							param = message.substring(i, j);
						else
							param = message.substring(i);
					} else {
						key = message.substring(i);
						param = "";
					}
					IRCMessage resp = new IRCMessage();
					resp.command = "PRIVMSG";
					if(ircMsg.middles.get(0).equals(config.user.channel)) {
						resp.middles.add(config.user.channel);
						isPublic = true;
					} else {
						resp.addMiddles(ircMsg.nick);
						isPublic = false;
					}
					if(msg instanceof KedamaMessage)
						resp.trailing = new StringBuilder().append('@').append(' ').append(((KedamaMessage) msg).player).append(' ').toString();
					else
						resp.trailing = "";
					do {
						if(key.equals(config.response.pingKey)) {
							resp.trailing += ping(param, isPublic);
							break;
						}
						if(key.equals(config.response.listKey)) {
							resp.trailing += list(param, isPublic);
							break;
						}
						if(key.equals(config.response.checkKey)) {
							resp.trailing += check(param, isPublic);
							break;
						}
					} while(false);
					ctx.writeAndFlush(resp);
				}
			}
			return;
		}
		super.channelRead(ctx, msg);
	}
	
	private String check(String param, boolean isPublic) {
		if(isPublic)
			return "******";
		String res = null;
		int i = param.indexOf('.');
		if(param.charAt(0) == '#') {
			try {
				if(i < 0)
					i = param.length();
				res = param.substring(0, i);
				param = param.substring(i);
				if(res.equals("#Client")) {
					Container c = new Container();
					c.obj = CreepyClass.getObject(ClientComponent.getInstance(), param);
					res = Util.mapper.writeValueAsString(c);
				} else {
					throw new IllegalArgumentException();
				}
			} catch (Exception e) {
				res = e.toString();
			}
		}
		return res;
	}

	private String list(String param, boolean isPublic) {
		try {
			return Util.mapper.writeValueAsString(ClientComponent.getInstance().list.getList().keySet());
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	public String ping(String param, boolean isPub) {
		StringBuilder s = new StringBuilder(config.response.pong);
		int i;
		String p;
		p = "%T";
		i = s.indexOf(p);
		if(i >= 0)
			s.replace(i, i + p.length(), LocalDateTime.now().format(Util.formatter));
		p = "%S";
		i = s.indexOf(p);
		if(i >= 0)
			s.replace(i, i + p.length(), LocalDateTime.now().format(Util.formatter));
		return s.toString();
	}
}