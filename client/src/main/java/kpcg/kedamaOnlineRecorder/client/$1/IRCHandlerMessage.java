package kpcg.kedamaOnlineRecorder.client.$1;

public class IRCHandlerMessage {

	public static final int CALLED_STOP = 1;
	
	public static final int NAME_IN_USE = 2;
	
	public static final int NO_MESSAGE = 3;
	
	public static final int BE_KICKED = 4;
	
	public static final int ERROR = 5;
	
	public static final int UNKNOWN = 6;
	
	public int message;
	
	public IRCHandlerMessage() {
		message = 0;
	}
	
	public void setMessage(int msg) {
		message = msg;
	}
	
	public void clear() {
		message = 0;
	}
}
