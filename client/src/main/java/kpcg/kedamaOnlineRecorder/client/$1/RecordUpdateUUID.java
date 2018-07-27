package kpcg.kedamaOnlineRecorder.client.$1;

import java.sql.Statement;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLBuilder;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;

public class RecordUpdateUUID implements SQLiteOperation {

	private InternalLogger logger = InternalLoggerFactory.getInstance(RecordUpdateUUID.class);
	
	
	private String tempUUID;
	
	private String uuid;
		
	public RecordUpdateUUID(String tempUUID, String uuid) {
		this.tempUUID = tempUUID;
		this.uuid = uuid;
	}

	@Override
	public void operate(SQLiteManager mgr, Statement sqlStmt) throws Exception {
		SQLBuilder sql = SQLBuilder.get()
				.keyword("UPDATE").table("online_record").keyword("SET")
				.column("uuid").keyword('=').value(uuid)
				.keyword("WHERE")
				.column("uuid").keyword("LIKE").value(tempUUID);
		sqlStmt.execute(sql.toString());
		// TODO log
		logger.info("> record: update ({} => {})", tempUUID, uuid);		
	}

	@Override
	public void sqliteOperationExceptionCaught(SQLiteManager mgr, Throwable cause) throws Exception {
		if(!mgr.isDBLocked())
			logger.warn(cause);
	}

	@Override
	public boolean reserve(boolean queueFull) {
		return !queueFull;
	}

}
