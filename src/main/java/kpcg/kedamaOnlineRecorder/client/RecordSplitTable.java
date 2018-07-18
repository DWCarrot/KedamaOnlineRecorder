package kpcg.kedamaOnlineRecorder.client;

import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordSplitTable implements SQLiteOperation {

	private static DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
	private static ZoneId zone = ZoneId.systemDefault();

	private int ignoreBeforeDays = 7;	//days
	
	
	public RecordSplitTable(int ignoreBeforeDays) {
		this.ignoreBeforeDays = ignoreBeforeDays;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		// TODO Auto-generated method stub
		Instant now = Instant.now();
		LocalDateTime time = LocalDateTime.ofInstant(now, zone );
		String newTableName = "online_record" + time.format(formatter);
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
				.column("integrity").keyword("BOOLEAN")
				.split(')');
		sqlStmt.execute(sql.toString());
		query = SQLBuilder.get()
				.keyword("WHERE")
				.column("timestamp2").keyword("IS NULL")
				.keyword("AND")
				.column("timestamp1").keyword('>').value((now.getEpochSecond() - ignoreBeforeDays * 24 * 60 * 60) * 1000L);
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
		
		newTableName = "online_count" + time.format(formatter);
		sql = SQLBuilder.get().keyword("ALTER").keyword("TABLE").table("online_count").keyword("RENAME TO").table(newTableName);
		sqlStmt.execute(sql.toString());
		sql = SQLBuilder.get()
				.keyword("CREATE").keyword("TABLE").table("online_count").keyword('(')
				.column("timestamp").keyword("INTEGER").split(',')
				.column("online").keyword("INTEGER").split(',')
				.column("integrity").keyword("BOOLEAN")
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
		return !queueFull;
	}

}
