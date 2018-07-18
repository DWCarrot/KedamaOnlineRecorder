package kpcg.kedamaOnlineRecorder.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class IRCListenerHandler extends ChannelInboundHandlerAdapter {
	
	IRCListenerClientConfig.ListenerConfig cfg;
	
	Pattern chat;
	
	Pattern join;
	
	Pattern part;
	
	IRCListenerHandler setConfig(IRCListenerClientConfig cfg) {
		this.cfg = cfg.listener;
		chat = Pattern.compile(this.cfg.chatpattern);
		join = Pattern.compile(this.cfg.joinpattern);
		part = Pattern.compile(this.cfg.partpattern);
		return this;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof IRCMessage) {
			IRCMessage ircMsg = (IRCMessage) msg;
			Matcher m;
			if(ircMsg.nick.equals(cfg.target)) {
				m = chat.matcher(ircMsg.trailing);
				if(m.find()) {
					super.channelRead(ctx, new KedamaMessage(ircMsg, m.group(1), m.group(2), m.group(3)));
				} else {
					m = join.matcher(ircMsg.trailing);
					if(m.find()) {
						handleJoin(m.group(1), ircMsg.time);
					} else {
						m = part.matcher(ircMsg.trailing);
						if(m.find()) {
							handlePart(m.group(1), ircMsg.time);
						}
					}
				}
			} else {
				super.channelRead(ctx, msg);
			}
			return;
		}
		super.channelRead(ctx, msg);
	}
	
	public void handleJoin(String name, long timestamp) {
		PlayerList list = ClientComponent.getInstance().list;
		if(list != null) {
			list.add(name, timestamp);
		}
		System.out.println("join: " + name);
	}
	
	public void handlePart(String name, long timestamp) {
		PlayerList list = ClientComponent.getInstance().list;
		if(list != null) {
			list.remove(name, timestamp);
		}
		System.out.println("part: " + name);
	}
}
