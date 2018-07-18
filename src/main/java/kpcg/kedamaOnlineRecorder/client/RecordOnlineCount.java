package kpcg.kedamaOnlineRecorder.client;

import java.sql.Statement;

import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordOnlineCount implements SQLiteOperation {

	private long timestamp;
	
	private int online;
	
	private boolean integrity;
	
	private String bref;
	
	public RecordOnlineCount(long timestamp, int online, boolean integrity, String bref) {
		this.timestamp = timestamp;
		this.online = online;
		this.integrity = integrity;
		this.bref = bref;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		// TODO Auto-generated method stub
		SQLBuilder sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_count")
				.keyword("VALUES").keyword('(')
				.value(timestamp).split(',')
				.value(online).split(',')
				.value(bref).split(',')
				.value(integrity)
				.split(')');
		sqlStmt.execute(sql.toString());
		//TODO log
		System.out.println("#record " + this.getClass());
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		if(!mgr.isDBLocked())
			cause.printStackTrace();
	}

	@Override
	public boolean reserve(boolean queueFull) {
		// TODO Auto-generated method stub
		return !queueFull;
	}

}
