package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordSplitTable implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordSplitTable.class);
	
	private int ignoreBefore;	//seconds
	private String suffix;
	
	public RecordSplitTable(int ignoreBefore, String suffix) {
		this.ignoreBefore = ignoreBefore;
		this.suffix = suffix;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		int epoch = (int) (System.currentTimeMillis() / 1000);
		String newTableName = "online_record" + suffix;
		SQLBuilder sql;
		SQLBuilder query;
		sql = SQLBuilder.get().keyword("ALTER").keyword("TABLE").table("online_record").keyword("RENAME TO").table(newTableName);
		sqlStmt.execute(sql.toString());
		sql = SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").table("online_record").keyword('(')
				.column("uuid").keyword("CHAR").split('(').value(32).split(')').split(' ').keyword("NOT NULL").split(',')
				.column("name").keyword("TEXT").keyword("NOT NULL").split(',')
				.column("timestamp2").keyword("INTEGER").split(',')
				.column("timestamp1").keyword("INTEGER").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')');
		sqlStmt.execute(sql.toString());
		query = SQLBuilder.get()
				.keyword("WHERE")
				.column("timestamp2").keyword("IS NULL")
				.keyword("AND")
				.column("timestamp1").keyword('>').value(epoch - ignoreBefore);
		sql = SQLBuilder.get()
				.keyword("INSERT").keyword("INTO").table("online_record")
				.keyword('(').column("uuid").split(',').column("name").split(',').column("timestamp1").split(')').split(' ')
				.keyword("SELECT").column("uuid").split(',').column("name").split(',').column("timestamp1").keyword("FROM").table(newTableName)
				.append(query);				
		sqlStmt.execute(sql.toString());
		if(sqlStmt.getUpdateCount() > 0) {
			sql = SQLBuilder.get()
					.keyword("DELETE").keyword("FROM").table(newTableName).append(query);
			sqlStmt.execute(sql.toString());
		}
		
		newTableName = "online_count" + suffix;
		sql = SQLBuilder.get().keyword("ALTER").keyword("TABLE").table("online_count").keyword("RENAME TO").table(newTableName);
		sqlStmt.execute(sql.toString());
		sql = SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").table("online_count").keyword('(')
				.column("timestamp").keyword("INTEGER").split(',')
				.column("online").keyword("INTEGER").split(',')
				.column("bref").keyword("TEXT").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')');
		sqlStmt.execute(sql.toString());
		//TODO log
		logger.info("> record: split table");
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
		return !queueFull;
	}

}
