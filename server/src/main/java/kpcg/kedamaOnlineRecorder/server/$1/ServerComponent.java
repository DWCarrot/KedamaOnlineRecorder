package kpcg.kedamaOnlineRecorder.server.$1;

import kpcg.kedamaOnlineRecorder.util.RecorderComponent;

public class ServerComponent implements RecorderComponent {

	public static ServerComponent instance;
	
	public static RecorderComponent getInstance() {
		if(instance == null)
			instance = new ServerComponent();
		return instance;
	}
	
	public static final String configFile = "server-config.json";
	
	
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
						//updateConfigs();
						break;
					case "start":
						//initIRC();
						//startIRC();
						break;
					case "stop":
						//stopIRC();
						break;
					}
					break;
				case "ping":
					switch (param) {
					case "config":
						//updateConfigs();
						break;
					case "start":
						//initPinger();
						//startPinger();
						break;
					case "stop":
						//stopPinger();
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

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

}
