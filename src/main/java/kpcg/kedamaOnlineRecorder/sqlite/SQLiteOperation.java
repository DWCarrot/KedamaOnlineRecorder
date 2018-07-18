package kpcg.kedamaOnlineRecorder.sqlite;

import java.sql.Statement;

public interface SQLiteOperation {
	
	void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception;
	
	void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception;
	
	boolean reserve(boolean queueFull);
}