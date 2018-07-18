package kpcg.kedamaOnlineRecorder.client;

import java.sql.Statement;

import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordAboutJoin implements SQLiteOperation {

	private String uuid;
	
	private String name;
	
	private long timestamp;
	
	public RecordAboutJoin(String uuid, String name, long timestamp) {
		this.uuid = uuid;
		this.name = name;
		this.timestamp = timestamp;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		// TODO Auto-generated method stub
		SQLBuilder sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_record")
				.keyword('(').column("uuid").split(',').column("name").split(',').column("timestamp1").keyword(')')
				.keyword("VALUES")
				.keyword('(').value(uuid).split(',').value(name).split(',').value(timestamp).split(')');
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