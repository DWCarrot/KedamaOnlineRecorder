package kpcg.kedamaOnlineRecorder.client.$1;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
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
	
	public void run() {
		logger.info("> sql: start @{}", fileName);
		SQLiteOperation operation = null;
		Statement sqlStmt = null;
		t = Thread.currentThread();
		long t1,t2;
		while(!t.isInterrupted()) {
			try {
				try {
					if(operation == null || !(operation.reserve(queue.size() >= queueCapacity) && dBLocked)) {
						operation = queue.take();
						sqlStmt = sqlConn.createStatement();
					}
					dBLocked = false;
					t2 = System.nanoTime();
					operation.operate(this, sqlStmt);
					sqlConn.commit();
					t1 = System.nanoTime();
					operation = null;
					sqlStmt.close();
					logger.info("> sql: execute ({}ms)", (t1 - t2) / 1000000.0f);
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
