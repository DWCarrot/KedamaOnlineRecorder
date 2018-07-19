package kpcg.kedamaOnlineRecorder.client;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledFuture;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class SQLiteManagerWithLog extends SQLiteManager {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(SQLiteManager.class);
	
	private String fileName;
	
	public SQLiteManagerWithLog(File dbFile, int capacity) throws SQLException, ClassNotFoundException {
		super(dbFile, capacity);
		fileName = dbFile.getAbsolutePath();
	}
	
	@Override
	public void run() {
		logger.info("> sql: start @{}", fileName);
		SQLiteOperation operation = null;
		Statement sqlStmt = null;
		t = Thread.currentThread();
		while(!t.isInterrupted()) {
			try {
				try {
					if(operation == null || !(operation.reserve(queue.size() >= queueCapacity) && dBLocked)) {
						operation = queue.take();
						sqlStmt = sqlConn.createStatement();
					}
					dBLocked = false;
					operation.operate(this, sqlStmt);
					sqlConn.commit();
					operation = null;
					sqlStmt.close();
				} catch (InterruptedException e) {
					logger.debug(e);
					break;
				} catch (Exception e) {
					sqlConn.rollback();
					dBLocked = isDBLocked(e);
					operation.sqliteOperationExceptionCaught(this, e);
				}
			} catch (InterruptedException e) {
				logger.debug(e);
				break;
			} catch (Exception e) {
				dBLocked = isDBLocked(e);
				if(!dBLocked)
					logger.warn(e);
			}
			if(dBLocked) {
				logger.warn("> sql: database is locked");
				try {
					synchronized (sqlConn) {
						sqlConn.wait(blockedWait);
					}
				} catch (InterruptedException e) {
					logger.debug(e);
					break;			
				}
			}
		}
		close();
		queue.clear();
		dBLocked = false;
		t = null;
		logger.info("> sql: stop");
	}

	
	
}
