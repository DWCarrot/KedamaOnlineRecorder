package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordSplitTable2 implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordSplitTable2.class);
	
	private int ignoreBefore;	//seconds
	
	public RecordSplitTable2(int ignoreBefore) {
		this.ignoreBefore = ignoreBefore;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		int epoch = (int) (System.currentTimeMillis() / 1000);
		int update;
		SQLBuilder sql;
		SQLBuilder query;
		query = SQLBuilder.get()
				.keyword("WHERE")
				.column("timestamp2").keyword("IS NOT NULL")
				.keyword("OR")
				.column("timestamp1").keyword('<').value(epoch - ignoreBefore);
		//online_record		
		sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_record_static")
				.keyword("SELECT").keyword('*').keyword("FROM").table("online_record")
				.append(query);				
		sqlStmt.execute(sql.toString());
		if((update = sqlStmt.getUpdateCount()) > 0) {
			sql = SQLBuilder.get()
					.keyword("DELETE").keyword("FROM").table("online_record").append(query);
			sqlStmt.execute(sql.toString());
		}
		logger.info("> record: split table 'online_record' ({})", update);
		//online_count
		sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_count_static")
				.keyword("SELECT").keyword('*').keyword("FROM").table("online_count");				
		sqlStmt.execute(sql.toString());
		if((update = sqlStmt.getUpdateCount()) > 0) {
			sql = SQLBuilder.get()
					.keyword("DELETE").keyword("FROM").table("online_count");
			sqlStmt.execute(sql.toString());
		}
		sqlStmt.execute(sql.toString());
		logger.info("> record: split table 'online_count' ({})", update);
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		if(!mgr.isDBLocked())
			logger.warn(cause);
	}

	@Override
	public boolean reserve(boolean queueFull) {
		return !queueFull;
	}

}
