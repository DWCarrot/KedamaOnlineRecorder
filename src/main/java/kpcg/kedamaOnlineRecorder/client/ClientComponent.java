package kpcg.kedamaOnlineRecorder.client;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.shaded.org.jctools.queues.CircularArrayOffsetCalculator;
import kpcg.kedamaOnlineRecorder.RecorderComponent;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.util.Util;

public class ClientComponent implements RecorderComponent {

	static ClientComponent instance;
	
	public static ClientComponent getInstance() {
		if(instance == null)
			instance = new ClientComponent();
		return instance;
	}
	
	
	
	
	public ClientComponentConfig config;
	
	//0x01
	public SQLiteManager mgr;
	
	public PlayerList list;
	
	public EventLoopGroup group;
	
	//0x02
	public IRCListenerClient client;
	
	public IRCListenerClientConfig ircCfg;
	
	//0x03
	public MinecraftPing pinger;
	
	public MinecraftPingConfig mcCfg;
	
	
	
	
	
	
	public ClientComponent() {
		
	}
	
	public void updateConfigs() throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException, SQLException {
		config = Util.mapper.readValue(new File("config.json"), ClientComponentConfig.class);
	}
	
	public void initIRC() {
		ircCfg = config.irc;
		client = new IRCListenerClient();
		client.setConfig(ircCfg);
		client.init(group);
	}
	
	public void startIRC() {
		client.start();
	}
	
	public void stopIRC() {
		if(client != null) {
			client.stop(null);
			client = null;
		}
	}
	
	public void initPinger() {
		mcCfg = config.ping;
		pinger = new MinecraftPing();
		pinger.setConfig(mcCfg);
		pinger.setHandler(list);
		pinger.init();
	}
	
	public void startPinger() {
		group.execute(pinger);
	}
	
	public void stopPinger() {
		if(pinger != null) {
			pinger.stop();
			pinger.setHandler(null);
			pinger = null;
		}
	}
	
	public void status() {
		try {
			System.out.println(Util.mapper.writeValueAsString(this));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean start() {
		try {
			updateConfigs();
			mgr = new SQLiteManager(new File(config.sqlite.file), config.sqlite.queue);
			list = new PlayerList();
			list.setRecord(mgr);
			list.setSqliteAddTimeout(config.sqlite.timeout * 1000L);
			
			
			GetPlayerInfo.set(config.mojangAPI);
			group = new NioEventLoopGroup(config.thread);
			group.execute(mgr);
			
			list.init(5000);
			//TODO log
			System.out.println("initial " + list.getList());
		} catch (ClassNotFoundException | IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean stop() {
		try {
			if(mgr != null) {
				mgr.close();
				mgr = null;
			}
			if(list != null) {
				list.clear();
				list = null;
			}
			stopIRC();
			stopPinger();
			if(group != null) {
				group.shutdownGracefully().sync();
				//TODO log
				System.out.println("shutdown");
				group = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean execute(String cmd) {
		if(cmd.charAt(0) == '#') {
			try {
				int i = cmd.indexOf(' ');
				String target = cmd.substring(1, i < 0 ? cmd.length() : i);
				String param = (i < 0 ? null : cmd.substring(i + 1));
				//TODO log
				System.out.printf("[cmd] %s %s |\n", target, param);
				switch (target) {
				case "start":
					start();
					break;
				case "stop":
					stop();
					break;
				case "irc":
					switch (param) {
					case "config":
						updateConfigs();
						break;
					case "start":
						initIRC();
						startIRC();
						break;
					case "stop":
						stopIRC();
						break;
					}
					break;
				case "ping":
					switch (param) {
					case "config":
						updateConfigs();
						break;
					case "start":
						initPinger();
						startPinger();
						break;
					case "stop":
						stopPinger();
						break;
					}
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
