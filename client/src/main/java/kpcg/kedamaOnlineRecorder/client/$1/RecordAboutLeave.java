package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordAboutLeave implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordAboutLeave.class);
	
	
	String uuid;
	
	String name;
	
	String tmpId;
	
	long timestamp2;
	
	long timestamp1;
	
	Boolean continuity;
	
	public RecordAboutLeave(String uuid, String name, long timestamp2, long timestamp1, Boolean continuity) {
		this.uuid = uuid;
		this.name = name;
		this.timestamp2 = timestamp2;
		this.timestamp1 = timestamp1;
		this.continuity = continuity;
	}

	public void setTmpId(String tmpId) {
		this.tmpId = tmpId;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		SQLBuilder sql;
		sql = SQLBuilder.get()
				.keyword("UPDATE").table("online_record").keyword("SET");
		if(tmpId != null)
			sql.column("uuid").keyword('=').value(uuid).split(',');
		sql.column("timestamp2").keyword('=').value(timestamp2 / 1000L);
		if(continuity != null) {
			sql.split(',');
			sql.column("continuity").keyword('=').value(continuity);
		}
		sql.keyword("WHERE").column("rowid").keyword('=').keyword('(')
			.keyword("SELECT").keyword("MAX").split('(').column("rowid").split(')').split(' ')
			.keyword("FROM").table("online_record")
			.keyword("WHERE")
			.column("uuid").keyword('=').value(tmpId == null ? uuid : tmpId)
			.keyword("AND")
			.column("timestamp2").keyword("IS NULL")
			.split(')');
		sqlStmt.execute(sql.toString());
		if(sqlStmt.getUpdateCount() < 1) {
			if(continuity == null)
				continuity = Boolean.FALSE;
			sql = SQLBuilder.get()
					.keyword("INSERT").keyword("INTO").table("online_record")
					.keyword("VALUES").keyword('(')
					.value(uuid).split(',')
					.value(name).split(',')
					.value(timestamp2 / 1000L).split(',')
					.value(timestamp1 / 1000L).split(',')
					.value(continuity)
					.split(')');
			sqlStmt.execute(sql.toString());
		}
		//TODO log
		logger.info("> record: part ({},{})", uuid, name);
//		System.out.println("#record " + this.getClass());
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		if(!mgr.isDBLocked())
			logger.warn(cause);
	}

	@Override
	public boolean reserve(boolean queueFull) {
		// TODO Auto-generated method stub
		return !queueFull;
	}

}
