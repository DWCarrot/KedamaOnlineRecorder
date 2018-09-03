package kpcg.kedamaOnlineRecorder.server.$1;

public class DataServerConfig {

	public int thread;
	
	public class NetConfig {
		
		public String host;
		
		public int port;
		
		public class SSL {
			
			public String keystore;
			
			public String keystorepw;
		}
		public SSL ssl = new SSL();
	}
	public NetConfig net = new NetConfig();
	
	
}
