package kpcg.kedamaOnlineRecorder.server.$1;

public class DataServerConfig {

	public int thread;
	
	public class HttpConfig {
		
		public int maxInitialLineLength;
		
		public int maxHeaderSize;
		
		public int maxChunkSize;
		
		public int maxSendSize;
		
		public int compressionLevel;
		
		public int windowBits; 
		
		public int memLevel;
		
	}
	public HttpConfig http = new HttpConfig();
	
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
