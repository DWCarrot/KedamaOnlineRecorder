package kpcg.kedamaOnlineRecorder.client.$1;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import kpcg.kedamaOnlineRecorder.util.Util;

public class IRCListenerClientConfig {
	
	public static class NetConfig {
		
		public String host;
		
		public int port;
		
		public boolean ssl;
	}
	public NetConfig net = new NetConfig();
	
	public static class UserConfig {

		public String nick;
		
		public String username;
		
		public String realname;
		
		public String pw;
		
		public String channel;
		
		public String channelpw;
		
		public int usermode;
	}
	public UserConfig user = new UserConfig();
	
	public static class ListenerConfig {

		public String target;
		
		public String joinpattern;
		
		public String partpattern;
		
		public String chatpattern;
	}
	public ListenerConfig listener = new ListenerConfig();

	public static class ResponseConfig {
		// @ $nick $key params... 
		@JsonProperty("ping_key")
		public String pingKey;
		
		@JsonProperty("list_key")
		public String listKey;
		
		@JsonProperty("check_key")
		public String checkKey;
		
		public String pong;
	}
	public ResponseConfig response = new ResponseConfig();
	
	@JsonProperty("message_timeout")
	public int MessageTimeout;	//s
	
	public static class RestartIntervalSettings {
	
		@JsonProperty("name_in_use")
		public int NameInUse;
		
		@JsonProperty("connect_failure")
		public int ConnectFailure;
		
		@JsonProperty("no_message")
		public int NoMessage;
		
		@JsonProperty("be_kicked")
		public int BeKicked;
		
		@JsonProperty("irc_error")
		public int IRCError;
		
		@JsonProperty("unknown_failure")
		public int UnknownFailure;
	}
	@JsonProperty("restart_interval")
	public RestartIntervalSettings restartInterval = new RestartIntervalSettings();
	
	
	
	public static IRCListenerClientConfig loadConfig(File file) throws JsonParseException, JsonMappingException, IOException {
		
		return Util.mapper.readValue(file, IRCListenerClientConfig.class); 
	}
	
	public void storeConfig(File file) throws JsonGenerationException, JsonMappingException, IOException {
		Util.mapper.writeValue(file, this);
	}
}
