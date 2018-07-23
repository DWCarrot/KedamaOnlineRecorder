package kpcg.kedamaOnlineRecorder.sqlite;

import java.sql.Statement;

public class SQLiteOperationGroup implements SQLiteOperation {

	private SQLiteOperation operation;
	
	private SQLiteOperationGroup next;
	
	private boolean done;
	
	public SQLiteOperationGroup(SQLiteOperation operation) {
		this.operation = operation;
	}
	
	public SQLiteOperationGroup setNext(SQLiteOperationGroup next) {
		this.next = next;
		return this;
	}
	
	public SQLiteOperationGroup next(SQLiteOperationGroup next) {
		this.next = next;
		return next;
	}
	
	public SQLiteOperationGroup getNext() {
		return next;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		done = false;
		operation.operate(mgr, sqlStmt);
		done = true;
		if(next != null)
			next.operate(mgr, sqlStmt);
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		if(done && next != null)
			next.sqliteOperationExceptionCaught(mgr, cause);
		else
			operation.sqliteOperationExceptionCaught(mgr, cause);
	}

	@Override
	public boolean reserve(boolean queueFull) {
		if(next != null)
			return operation.reserve(queueFull) && next.reserve(queueFull);
		else
			return operation.reserve(queueFull);
	}

}
