package kpcg.kedamaOnlineRecorder.client;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class IRCMessage {

	protected long time;
	
	protected String nick;
	
	protected String username;
	
	protected String host;
	
	protected String command;
	
	protected List<String> middles;
	
	protected String trailing;

	public IRCMessage() {

	}
	
	public IRCMessage(long time, String nick, String username, String host, String command, String... para) {
		if(command == null || command.isEmpty())
			return;
		this.time = time;
		this.nick = nick;
		this.username = username;
		this.host = host;
		this.command = command;
		trailing = "";
		middles = new ArrayList<>(para.length);
		for(String p : para) {
			if(p == null || p.isEmpty())
				continue;
			if(p.charAt(0) == ':') {
				trailing = p.substring(1);
				break;
			}
			middles.add(p);
		}			
	}
	
	public IRCMessage(long time, String msg) throws IOException {
		this.time = time;
		parse(msg);
	}
	
	public void prasePrefix(String prefix) {
		nick = username = host = "";
		int i = prefix.indexOf('!');
		if(i > 0) {
			nick = prefix.substring(0, i++);
			int j = prefix.indexOf('@', i);
			if(j > i) {
				username = prefix.substring(i, j++);
				host = prefix.substring(j);
			} else {
				username = prefix.substring(i);
			}
		} else {
			nick = prefix;
		}
	}
	
	public void parse(String msg) throws IOException {
		int j = 0, i = 0;
		if(msg.startsWith("null"))
			i = 4;
		if(msg.charAt(i) == ':') {
			j = msg.indexOf(' ', i);
			if(j < i)
				throw new IOException("Invalid IRC message: " + msg);	
			prasePrefix(msg.substring(i + 1, j));
			i = j + 1;
		} else {
			nick = "";
			username = "";
			host = "";
		}
		j = msg.indexOf(' ', i);
		if(j < 0)
			throw new IOException("Invalid IRC message: " + msg);
		command = msg.substring(i, j);
		i = j + 1;
		middles = new ArrayList<>(10);
		for(j = msg.indexOf(' ', i); msg.charAt(i) != ':'; j = msg.indexOf(' ', i)) {
			if(j < 0) {
				middles.add(msg.substring(i));
				trailing = "";
				return;
			}
			middles.add(msg.substring(i, j));
			i = j + 1;
		}
		trailing = msg.substring(i + 1);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if(command == null || command.isEmpty())
			return null;
		if(nick != null && !nick.isEmpty()) {
			s.append(':').append(nick);
			if(username != null && !username.isEmpty())
				s.append('!').append(username);
			if(host != null && !host.isEmpty())
				s.append('@').append(host);
			s.append(' ');
		}
		s.append(command);
		if(middles != null)
			for(String m : middles)
				s.append(' ').append(m);
		if(trailing != null && !trailing.isEmpty())
			s.append(' ').append(':').append(trailing);
		return s.toString();
	}
	
	public IRCMessage setTime(long time) {
		this.time = time;
		return this;
	}

	public IRCMessage setNick(String nick) {
		this.nick = nick;
		return this;
	}

	public IRCMessage setUsername(String username) {
		this.username = username;
		return this;
	}

	public IRCMessage setHost(String host) {
		this.host = host;
		return this;
	}

	public IRCMessage setCommand(String command) {
		this.command = command;
		return this;
	}

	public IRCMessage setMiddles(List<String> middles) {
		this.middles = middles;
		return this;
	}

	public IRCMessage addMiddles(String middle) {
		if(middle == null || middle.isEmpty())
			return this;
		if(middles == null)
			middles = new ArrayList<>();
		middles.add(middle);
		return this;
	}
	
	public IRCMessage setTrailing(String trailing) {
		this.trailing = trailing;
		return this;
	}
}
