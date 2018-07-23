package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public abstract class RecordGetUUID implements SQLiteOperation {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordGetUUID.class);
	
	
	private long timestamp;
	
	private String uuid;
	
	private String name;
	
	public RecordGetUUID(String name, long timestamp) {
		this.name = name;
		this.timestamp = timestamp;
	}


	/**
	 * 
	 * @param name
	 * @param timestamp
	 * @param timeout
	 * @return uuid; {@code null} if not exist or any exception
	 * @throws InterruptedException
	 */
	public abstract String getUuidFromMojang(String name, long timestamp, long timeout) throws InterruptedException;
	
	public abstract void handle(SQLiteManager mgr, String uuid, String name, long timestamp);
	
	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		String tempUUID = null;
		SQLBuilder sql;
		uuid = null;
		sql = SQLBuilder.get()
				.keyword("SELECT").column("uuid").keyword("FROM").table("player_list")
				.keyword("WHERE").column("name").keyword('=').value(name);
		if (sqlStmt.execute(sql.toString())) {
			ResultSet rs = sqlStmt.getResultSet();
			while (rs.next()) {
				uuid = rs.getString("uuid");
				if(uuid.charAt(0) != '@')
					break;
			}
		}
		if (uuid == null || (uuid.charAt(0) == '@')) {
			tempUUID = uuid;
			uuid = getUuidFromMojang(name, timestamp, 3000);
			if (uuid == null) {
				uuid = new StringBuilder(32).append('@').append(name).append('?').append(timestamp / 1000L).toString();
			} else {
				if (tempUUID != null) {
					tempUUID = tempUUID.substring(0, tempUUID.indexOf('?'));
					sql = SQLBuilder.get()
							.keyword("UPDATE").table("online_record").keyword("SET")
							.column("uuid").keyword('=').value(uuid)
							.keyword("WHERE")
							.column("uuid").keyword("LIKE").value(tempUUID);
					sqlStmt.execute(sql.toString());
					// TODO log
					logger.info("> record: upate ({},{})", uuid, name);
//					System.out.printf("\"correct\":{\"id\":\"%s\",\"name\":\"%s\"}\n", uuid, name);
				}
				sql = SQLBuilder.get()
						.keyword("UPDATE").table("player_list").keyword("SET")
						.column("name").keyword('=').value(name)
						.keyword("WHERE")
						.column("uuid").keyword('=').value(uuid);
				sqlStmt.execute(sql.toString());
				if (sqlStmt.getUpdateCount() < 1) {
					sql = SQLBuilder.get()
							.keyword("INSERT").keyword("INTO").table("player_list")
							.keyword("VALUES").keyword('(').value(uuid).split(',').value(name).split(')');
					sqlStmt.execute(sql.toString());
					// TODO log
					logger.info("> record: insert ({},{})", uuid, name);
//					System.out.printf("\"insert\":{\"id\":\"%s\",\"name\":\"%s\"}\n", uuid, name);
				} else {
					// TODO log
					logger.info("> record: upate ({},{})", uuid, name);
//					System.out.printf("\"update\":{\"id\":\"%s\",\"name\":\"%s\"}\n", uuid, name);
				}
			}
		}
		handle(mgr, uuid, name, timestamp);
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
