package kpcg.kedamaOnlineRecorder.client;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordInitPlayerList implements SQLiteOperation {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RecordInitPlayerList.class);
	
	
	private Map<String, Long> list;
	
	private Map<String, String> table;
		
	public RecordInitPlayerList(Map<String, Long> list, Map<String, String> table) {
		this.list = list;
		this.table = table;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		SQLBuilder sql = SQLBuilder.get()
				.keyword("SELECT")
				.column("uuid").split(',')
				.column("name").split(',')
				.column("timestamp1")
				.keyword("FROM").table("online_record")
				.keyword("WHERE").column("timestamp2").keyword("IS NULL")
				.keyword("ORDER").keyword("BY").column("timestamp1").keyword("ASC");
		synchronized (list) {
			if(sqlStmt.execute(sql.toString())) {
				ResultSet rs = sqlStmt.getResultSet();
				int i = -1, j = -1, k = -1;
				while(rs.next()) {
					if(i < 0 || j < 0 || k < 0) {
						i = rs.findColumn("uuid");
						j = rs.findColumn("name");
						k = rs.findColumn("timestamp1");						
					}
					list.put(rs.getString(j), rs.getLong(k) * 1000L);
					table.put(rs.getString(j), rs.getString(i));
				}
			}
			logger.info("> record: list initialized ({})", list.size());
			list.notify();
		}
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		if(!mgr.isDBLocked())
			logger.warn(cause);
	}

	@Override
	public boolean reserve(boolean queueFull) {
		return false;
	}

}
