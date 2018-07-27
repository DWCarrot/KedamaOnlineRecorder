package kpcg.kedamaOnlineRecorder.server.$1;

import java.util.Map;

public class DataServerConfig {

	public String host;
	
	public int port;
	
	public class SSL {
		
		public String keystore;
		
		public String keystorepw;
	}
	public SSL ssl = new SSL();
	
}
