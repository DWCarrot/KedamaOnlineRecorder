package kpcg.kedamaOnlineRecorder.client;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;

import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordInitPlayerList implements SQLiteOperation {

	
	private Map<String,Instant> list;
		
	public RecordInitPlayerList(Map<String, Instant> list) {
		this.list = list;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		SQLBuilder sql = SQLBuilder.get()
				.keyword("SELECT").column("name").split(',').column("timestamp1").keyword("FROM").table("online_record")
				.keyword("WHERE").column("timestamp2").keyword("IS NULL");
		synchronized (list) {
			if(sqlStmt.execute(sql.toString())) {
				ResultSet rs = sqlStmt.getResultSet();
				int i = -1, j = -1;
				while(rs.next()) {
					if(i < 0 || j < 0) {
						i = rs.findColumn("name");
						j = rs.findColumn("timestamp1");
					}
					list.put(rs.getString(i), Instant.ofEpochMilli(rs.getLong(j)));
				}
			}
			list.notify();
		}	
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		if(!mgr.isDBLocked())
			cause.printStackTrace();
	}

	@Override
	public boolean reserve(boolean queueFull) {
		return false;
	}

}
