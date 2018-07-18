package kpcg.kedamaOnlineRecorder.client;

public class KedamaMessage extends IRCMessage {
	
	String world;
	
	String player;
	
	String message;

	public KedamaMessage(IRCMessage origin, String world, String player, String message) {
		this.nick = origin.nick;
		this.username = origin.username;
		this.host = origin.host;
		this.command = origin.command;
		this.middles = origin.middles;
		this.trailing = origin.trailing;
		this.world = world;
		this.player = player;
		this.message = message;
	}
}
