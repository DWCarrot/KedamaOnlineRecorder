package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordCreateTable2 implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordCreateTable2.class);
	
	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		//record
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_record").keyword('(')
				.column("uuid").keyword("CHAR").split('(').value(32).split(')').split(' ').keyword("NOT NULL").split(',')
				.column("name").keyword("TEXT").keyword("NOT NULL").split(',')
				.column("timestamp2").keyword("INTEGER").split(',')
				.column("timestamp1").keyword("INTEGER").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')')
				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_count").keyword('(')
				.column("timestamp").keyword("INTEGER").split(',')
				.column("online").keyword("INTEGER").split(',')
				.column("bref").keyword("TEXT").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')')
				.toString());
		//static table
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_record_static").keyword('(')
				.column("uuid").keyword("CHAR").split('(').value(32).split(')').split(' ').keyword("NOT NULL").split(',')
				.column("name").keyword("TEXT").keyword("NOT NULL").split(',')
				.column("timestamp2").keyword("INTEGER").split(',')
				.column("timestamp1").keyword("INTEGER").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')')
				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("online_count_static").keyword('(')
				.column("timestamp").keyword("INTEGER").split(',')
				.column("online").keyword("INTEGER").split(',')
				.column("bref").keyword("TEXT").split(',')
				.column("continuity").keyword("BOOLEAN")
				.split(')')
				.toString());
		//index
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("INDEX").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("index_online_record_uuid")
				.keyword("ON").table("online_record_static").keyword('(').column("uuid").split(')')
				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("INDEX").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("index_online_record_timestamp1")
				.keyword("ON").table("online_record_static").keyword('(').column("timestamp1").split(')')
				.toString());
		sqlStmt.execute(SQLBuilder.get()
				.keyword("CREATE").keyword("INDEX").keyword("IF").keyword("NOT").keyword("EXISTS")
				.table("index_online_count_timestamp")
				.keyword("ON").table("online_count_static").keyword('(').column("timestamp").split(')')
				.toString());		
		logger.info("> record: create table execute");
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
