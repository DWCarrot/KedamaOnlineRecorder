package kpcg.kedamaOnlineRecorder.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientComponentConfig {

	public int thread;
	
	@JsonProperty("mojang_api")
	public String mojangAPI;
	
	public static class SQLiteConfig {
		
		public String file;
		
		public int queue;
		
		public int timeout;
		
		public class SQLSplit {
			
			public String start;
			
			public int period;
		}
		public SQLSplit split = new SQLSplit();
	}
	public SQLiteConfig sqlite = new SQLiteConfig();
	
	public IRCListenerClientConfig irc = new IRCListenerClientConfig();
	
	public MinecraftPingConfig ping = new MinecraftPingConfig();
	
}
