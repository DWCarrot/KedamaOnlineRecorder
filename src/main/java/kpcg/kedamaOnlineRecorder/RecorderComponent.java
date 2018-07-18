package kpcg.kedamaOnlineRecorder;

public interface RecorderComponent {

	public boolean start();
	
	public boolean stop();
	
	public boolean execute(String cmd);
}
