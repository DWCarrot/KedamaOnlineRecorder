package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordOnlineCount implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordOnlineCount.class);
	
	
	private long timestamp;
	
	private int online;
	
	private boolean continuity;
	
	private String bref;
	
	public RecordOnlineCount(long timestamp, int online, boolean continuity, String bref) {
		this.timestamp = timestamp;
		this.online = online;
		this.continuity = continuity;
		this.bref = bref;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		// TODO Auto-generated method stub
		SQLBuilder sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_count")
				.keyword("VALUES").keyword('(')
				.value(timestamp / 1000L).split(',')
				.value(online).split(',')
				.value(bref).split(',')
				.value(continuity)
				.split(')');
		sqlStmt.execute(sql.toString());
		//TODO log
		logger.info("> record: online_count ({})", online);
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
