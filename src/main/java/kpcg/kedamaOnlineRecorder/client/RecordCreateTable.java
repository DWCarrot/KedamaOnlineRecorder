package kpcg.kedamaOnlineRecorder.client;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordCreateTable implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordCreateTable.class);
	
	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
//		sqlStmt.execute(SQLBuilder.get()
//				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
//				.table("player_list").keyword('(')
//				.column("uuid").keyword("CHAR").split('(').value(32).split(')').split(' ').keyword("NOT NULL").split(',')
//				.column("name").keyword("TEXT").keyword("NOT NULL").split(',')
//				.keyword("PRIMARY KEY").split('(').column("uuid").split(')')
//				.split(')').split(' ')
//				.keyword("WITHOUT").keyword("ROWID")
//				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_record").keyword('(')
				.column("uuid").keyword("CHAR").split('(').value(32).split(')').split(' ').keyword("NOT NULL").split(',')
				.column("name").keyword("TEXT").keyword("NOT NULL").split(',')
				.column("timestamp2").keyword("INTEGER").split(',')
				.column("timestamp1").keyword("INTEGER").split(',')
				.column("integrity").keyword("BOOLEAN")
				.split(')')
				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_count").keyword('(')
				.column("timestamp").keyword("INTEGER").split(',')
				.column("online").keyword("INTEGER").split(',')
				.column("bref").keyword("TEXT").split(',')
				.column("integrity").keyword("BOOLEAN")
				.split(')')
				.toString());
		//TODO log
		logger.info("> record: create table excute");
	}
	
	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		throw (Exception)cause;
	}

	@Override
	public boolean reserve(boolean queueFull) {
		// TODO Auto-generated method stub
		return true;
	}

}
