package kpcg.kedamaOnlineRecorder.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.shaded.org.jctools.queues.CircularArrayOffsetCalculator;
import kpcg.kedamaOnlineRecorder.client.WatchDogTimer.WorkingProcess;

public class IRCPreHandler extends ChannelInboundHandlerAdapter implements WorkingProcess {

	private IRCListenerClientConfig.UserConfig cfg;
	
	private String target;
	
	private IRCHandlerMessage m;

	protected WatchDogTimer wdt;

	protected int state = 0;

	protected Channel ch;
	
	private EventLoopGroup g;
	
	IRCPreHandler setConfig(IRCListenerClientConfig cfg) {
		this.cfg = cfg.user;
		target = cfg.listener.target;
		wdt = new WatchDogTimer(cfg.MessageTimeout * 1000, this);
		return this;
	}

	IRCPreHandler setExecutor(EventLoopGroup group) {
		this.g = group;
		return this;
	}
	
	IRCPreHandler setStatus(IRCHandlerMessage msg) {
		this.m = msg;
		return this;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		
		super.handlerAdded(ctx);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {	
		if(g == null)
			new Thread(wdt).start();
		else
			g.execute(wdt);
		ch = ctx.channel();
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		wdt.stop();
		ch = null;
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		if (msg instanceof IRCMessage) {
			wdt.reset();
			IRCMessage ircMsg = (IRCMessage) msg;			
			switch (ircMsg.command) {
			case "ERROR":
				m.setMessage(IRCHandlerMessage.ERROR);
				throw new IRCException(ircMsg.toString());
			case "PING":
				pong(ctx, ircMsg);
				break;
			case "NOTICE":
				if(state == 0) {
					login(ctx);
					state = 1;
					break;
				}
				if(state == 1) {
					if(ircMsg.trailing.startsWith("You are now identified")) {
						joinChannel(ctx);
						state = 2;
					}
					break;
				}
				break;
			case "JOIN":
				if(state == 2) {
					if(ircMsg.nick.equals(cfg.nick)) {
						state = 3;
						//
						MinecraftPing pinger = ClientComponent.getInstance().pinger;
						if(pinger != null)
							pinger.triggle();
					}
					break;
				}
				if(state == 3) {
					if(ircMsg.nick.equals(target)) {
						//
						MinecraftPing pinger = ClientComponent.getInstance().pinger;
						if(pinger != null)
							pinger.triggle();
					}
				}
				break;
			case "KICK":
				if(ircMsg.middles.size() > 1 && ircMsg.middles.get(1).equals(cfg.nick)) {
					m.setMessage(IRCHandlerMessage.BE_KICKED);
					throw new IRCException(ircMsg.toString());
				}
			case "QUIT":
				if(ircMsg.nick.equals(cfg.nick)) {
					m.setMessage(IRCHandlerMessage.UNKNOWN);
					throw new IRCException(ircMsg.toString());
				}
				break;
			case "433":
				if(ircMsg.trailing.startsWith("Nickname is already in use")) {
					m.setMessage(IRCHandlerMessage.NAME_IN_USE);
					throw new IRCException(ircMsg.toString());
				}
				break;
			default:
				if(state < 3)
					break;
				super.channelRead(ctx, ircMsg);
				break;
			}
			return;
		}
		super.channelRead(ctx, msg);
	}

	protected void pong(ChannelHandlerContext ctx, IRCMessage msg) throws Exception {
		ctx.write(new IRCMessage().setCommand("PONG").addMiddles(msg.trailing));
		ctx.flush();
	}
	
	protected void login(ChannelHandlerContext ctx) throws Exception {
		ctx.write(new IRCMessage().setCommand("NICK").addMiddles(cfg.nick));
		ctx.flush();
		ctx.write(new IRCMessage().setCommand("USER").addMiddles(cfg.username).addMiddles(Integer.toString(cfg.usermode)).addMiddles("*").setTrailing(cfg.realname));
		ctx.flush();
		ctx.write(new IRCMessage().setCommand("PRIVMSG").addMiddles("NickServ").setTrailing("IDENTIFY " + cfg.pw));
		ctx.flush();
	}
	protected void joinChannel(ChannelHandlerContext ctx) {
		ctx.write(new IRCMessage().setCommand("JOIN").addMiddles(cfg.channel).addMiddles(cfg.channelpw));
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(cause instanceof IRCException) {
			ctx.disconnect();
			return;
		}
		super.exceptionCaught(ctx, cause);
	}
	
	
	@Override
	public void reboot() throws Exception {
		if(ch != null) {
			m.setMessage(IRCHandlerMessage.NO_MESSAGE);
			ch.disconnect();
		}
	}

	@Override
	public void rebootExceptionCaught(Throwable cause) throws Exception {
		//TODO log
		cause.printStackTrace();
	}
	
	
}
