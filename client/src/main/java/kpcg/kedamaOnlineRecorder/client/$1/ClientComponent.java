package kpcg.kedamaOnlineRecorder.client.$1;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.client.$1.ClientComponentConfig.SQLiteConfig.SQLSplit;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.util.RecorderComponent;
import kpcg.kedamaOnlineRecorder.util.Util;

public class ClientComponent implements RecorderComponent {

	public static ClientComponent instance;
	
	public static ClientComponent getInstance() {
		if(instance == null)
			instance = new ClientComponent();
		return instance;
	}
	
	public static final String configFile = "client-config.json";
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ClientComponent.class);


	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	
	public ClientComponentConfig config;
	
	//0x01
	public SQLiteManager mgr;
	
	public PlayerList list;
	
	public ScheduledFuture<?> sqlSplit;
	
	public ScheduledExecutorService service;
		
	//0x02
	public IRCListenerClient client;
	
	public IRCListenerClientConfig ircCfg;
	
	//0x03
	public MinecraftPing pinger;
	
	public MinecraftPingConfig mcCfg;
	
	
	
	
	
	
	public ClientComponent() {
		
	}
	
	public void updateConfigs() throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException, SQLException {
		config = Util.mapper.readValue(new File(configFile), ClientComponentConfig.class);
	}
	
	public void initIRC() {
		ircCfg = config.irc;
		client = new IRCListenerClient();
		client.setConfig(ircCfg);
		client.setExecutor(service);
	}
	
	public void startIRC() {
		client.init(2);
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
		service.execute(pinger);
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
			logger.warn(e);
		}
	}
	
	@Override
	public boolean start() {
		try {
			logger.info("> start");
			updateConfigs();
			mgr = new SQLiteManagerWithLog(new File(config.sqlite.file), config.sqlite.queue);
			list = new PlayerList();
			list.setRecord(mgr);
			list.setSqliteAddTimeout(config.sqlite.timeout * 1000L);
			
			
			GetPlayerInfo.set(config.mojangAPI);
			service = Executors.newScheduledThreadPool(config.thread);
			service.execute(mgr);
			
			list.init(5000);
			
			startSplitService();
			
			//TODO log
//			System.out.println("initial " + list.getList());
			logger.info("> list: initialized {}", list.getList());
		} catch (ClassNotFoundException | IOException | SQLException e) {
			// TODO Auto-generated catch block
			logger.warn(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.debug(e);
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
			stopSplitServeice();
			if(service != null) {
				service.shutdown();
				//TODO log
				logger.info("> shutdown");
				service = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug(e);
			
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
				case "status":
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void startSplitService() {
		SQLSplit splitCfg = config.sqlite.split;
		long start = LocalDateTime.parse(splitCfg.start, formatter).toInstant(Util.offset).toEpochMilli();
		long period = splitCfg.period * 1000L;	//ms
		int p = splitCfg.period * 2;	//s
		long initialDelay = start - System.currentTimeMillis();	//ms
		if(initialDelay < 0)
			initialDelay += ((-initialDelay) / period + 1) * period;
		sqlSplit = service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(mgr != null)
					mgr.add(new RecordSplitTable2(p), config.sqlite.timeout * 1000);
				if(list != null)
					list.fliteTable();
			}
		}, initialDelay, period, TimeUnit.MILLISECONDS);
		logger.info("> sql: split service start ({}+...{}ms)", initialDelay, period);
	}
	
	public void stopSplitServeice() {
		if(sqlSplit != null) {
			boolean b = sqlSplit.cancel(true);
			logger.info("> sql: split service stop ({})", b);
		}
	}
}
