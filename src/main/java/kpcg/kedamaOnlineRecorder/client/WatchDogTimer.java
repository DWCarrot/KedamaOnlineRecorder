package kpcg.kedamaOnlineRecorder.client;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class WatchDogTimer implements Runnable {

	public interface WorkingProcess {
		
		public void reboot() throws Exception;
		
		public void rebootExceptionCaught(Throwable cause) throws Exception;
	}

	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(WatchDogTimer.class);
	
	
	private WorkingProcess workingProcess;
	
	private Thread t;
	
	private long timeout;
	
	private boolean unfeeded;
	
	private byte[] lock;
	
	public WatchDogTimer(long timeout, WorkingProcess workingProcess) {
		this.timeout = timeout;
		this.workingProcess = workingProcess;
		lock = new byte[] {0x00};
	}
	
	public void stop() {
		if(t != null) {
			workingProcess = null;
			t.interrupt();
		}
	}
	
	public void reset() {
		synchronized (lock) {
			unfeeded = false;
			lock.notify();
		}
	}
	
	@Override
	public void run() {
		t = Thread.currentThread();
		logger.info("> wdt: start [timeout={}ms]", timeout);
		while (!t.isInterrupted()) {
			try {
				synchronized (lock) {
					unfeeded = true;
					lock.wait(timeout);
					if (unfeeded) {
						try {
							workingProcess.reboot();
						} catch (InterruptedException e) {
							// TODO log
							logger.debug(e);
							break;
						} catch (Exception e) {
							workingProcess.rebootExceptionCaught(e);
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO log
				logger.debug(e);
				break;
			} catch (Exception e) {
				// TODO: log
				logger.warn(e);
			}
		}
		unfeeded = false;
		workingProcess = null;
		t = null;
		logger.info("> wdt: stop");
	}
}
