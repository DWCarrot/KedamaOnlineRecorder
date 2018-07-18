package kpcg.kedamaOnlineRecorder.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;



public class SQLiteManager implements Runnable {
	
	static public Class<?> jdbc = null;
		
	protected Connection sqlConn = null;
	
	protected BlockingQueue<SQLiteOperation> queue = null;
	
	protected Thread t;
	
	protected boolean dBLocked = false;
	
	private long blockedWait = 3000;
	
	private int queueCapacity;
	
	public SQLiteManager(File dbFile, int capacity) throws SQLException, ClassNotFoundException {
		if(jdbc == null)
			jdbc = Class.forName("org.sqlite.JDBC");
		sqlConn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
		sqlConn.setAutoCommit(false);
		queueCapacity = capacity;
		queue = new ArrayBlockingQueue<>(queueCapacity);
	}

	public SQLiteManager setBlockedWait(long blockedWait) {
		if(blockedWait > 0)
			this.blockedWait = blockedWait;
		return this;
	}
	
	public boolean add(SQLiteOperation operation, long timeout) {
		boolean success = false;
		try {
			success = queue.offer(operation, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();// TODO Auto-generated catch block
		}
		return success;
	}
	
	public boolean close() {
		try {
			if(t != null) {
				t.interrupt();
				t = null;
			}
			if (sqlConn != null) {
				sqlConn.close();
				sqlConn = null;
			}
			return true;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}

	public String getSQLErrorMessage(SQLException e) {
		String msg = e.getMessage();
		int i = msg.indexOf('(');
		int j = msg.lastIndexOf(')');
		if (j < i || i < 0)
			return "";
		else
			return msg.substring(i + 1, j);
	}
	
	public boolean isDBLocked() {
		return dBLocked;
	}
	
	public boolean isDBLocked(Exception e) {
		if(e instanceof SQLException)
			return e.getMessage().contains("(database is locked)");
		else
			return false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		SQLiteOperation operation = null;
		Statement sqlStmt = null;
		t = Thread.currentThread();
		while(!t.isInterrupted()) {
			try {
				try {
					if(operation == null || !(operation.reserve(queue.size() >= queueCapacity) && dBLocked)) {
						operation = queue.take();
						sqlStmt = sqlConn.createStatement();
					}
					dBLocked = false;
					operation.operate(this, sqlStmt);
					sqlConn.commit();
					operation = null;
					sqlStmt.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				} catch (Exception e) {
					sqlConn.rollback();
					dBLocked = isDBLocked(e);
					operation.sqliteOperationExceptionCaught(this, e);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dBLocked = isDBLocked(e);
			}
			if(dBLocked) {
				//TODO log
				System.err.println("database locked");
				try {
					synchronized (sqlConn) {
						sqlConn.wait(blockedWait);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;			
				}
			}
		}
		close();
		queue.clear();
		dBLocked = false;
		t = null;
	}
	
	public boolean blockedExcute(String sql, long timeout, long interval) throws SQLException {
		boolean hasRs;
		long start = System.currentTimeMillis();
		String sqlState = null;
		Statement sqlStmt = null;
		while (true) {
			try {
				sqlStmt = sqlConn.createStatement();
				hasRs = sqlStmt.execute(sql);
				break;
			} catch (SQLException e) {
				if (isDBLocked(e))
					sqlState = e.getSQLState();
				else
					throw e;
			}
			try {
				synchronized (sqlStmt) {
					sqlStmt.wait(interval);
					if (System.currentTimeMillis() - start > timeout)
						throw new SQLTimeoutException("database is locked", sqlState);
				}
			} catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return hasRs;
	}

}
