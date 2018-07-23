package kpcg.kedamaOnlineRecorder.util;

public interface RecorderComponent {

	public boolean start();
	
	public boolean stop();
	
	public boolean execute(String cmd);
	
	public static RecorderComponent getInstance() {
		return null;
	};
}
