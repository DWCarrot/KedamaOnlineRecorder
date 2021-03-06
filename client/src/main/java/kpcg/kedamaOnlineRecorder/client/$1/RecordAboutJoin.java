package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordAboutJoin implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordAboutJoin.class);
	
	
	private String uuid;
	
	private String name;
	
	private long timestamp;
	
	private Boolean continuity;
	
	public RecordAboutJoin(String uuid, String name, long timestamp, Boolean continuity) {
		this.uuid = uuid;
		this.name = name;
		this.timestamp = timestamp;
		this.continuity = continuity;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		// TODO Auto-generated method stub
		SQLBuilder sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_record")
				.keyword('(').column("uuid").split(',').column("name").split(',').column("timestamp1");
		if(continuity != null) {
			sql.split(',').column("continuity");
		}
		sql.keyword(')')
				.keyword("VALUES")
				.keyword('(').value(uuid).split(',').value(name).split(',').value(timestamp / 1000L);
		if(continuity != null) {
			sql.split(',').value(continuity);
		}
		sql.split(')');
		sqlStmt.execute(sql.toString());
		//TODO log
		logger.info("> record: join ({},{})", uuid, name);
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
