package kpcg.kedamaOnlineRecorder.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientComponentConfig {

	public int thread;
	
	public class SQLiteConfig {
		
		public String file;
		
		public int queue;
		
		public int timeout;
	}
	
	@JsonProperty("mojang_api")
	public String mojangAPI;
	
	public SQLiteConfig sqlite = new SQLiteConfig();
	
	public IRCListenerClientConfig irc = new IRCListenerClientConfig();
	
	public MinecraftPingConfig ping = new MinecraftPingConfig();
	
}
