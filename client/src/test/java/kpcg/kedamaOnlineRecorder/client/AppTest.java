package kpcg.kedamaOnlineRecorder.client;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;
import kpcg.kedamaOnlineRecorder.client.$1.RecordCreateTable;
import kpcg.kedamaOnlineRecorder.client.$1.RecordAboutJoin;
import kpcg.kedamaOnlineRecorder.client.$1.RecordSplitTable;
import kpcg.kedamaOnlineRecorder.client.$1.RecordAboutLeave;
/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    static class MGR extends kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager {
    	
    	private static final InternalLogger logger = InternalLoggerFactory.getInstance(SQLiteManager.class);
    	
    	private String fileName;
    	
    	public MGR(File dbFile, int capacity) throws SQLException, ClassNotFoundException {
    		super(dbFile, capacity);
    		fileName = dbFile.getAbsolutePath();
    	}
    	
    	@Override
    	public void run() {
    		logger.info("> sql: start @{}", fileName);
    		SQLiteOperation operation = null;
    		Statement sqlStmt = null;
    		t = Thread.currentThread();
    		long t1,t2;
    		while(!t.isInterrupted()) {
    			try {
    				try {
    					if(operation == null || !(operation.reserve(queue.size() >= queueCapacity) && dBLocked)) {
    						operation = queue.take();
    						sqlStmt = sqlConn.createStatement();
    					}
    					dBLocked = false;
    					t2 = System.nanoTime();
    					operation.operate(this, sqlStmt);
    					sqlConn.commit();
    					t1 = System.nanoTime();
    					operation = null;
    					sqlStmt.close();
    					logger.info("> sql: execute ({}ns)", t1 - t2);
    				} catch (InterruptedException e) {
    					logger.debug(e);
    					break;
    				} catch (Exception e) {
    					sqlConn.rollback();
    					dBLocked = isDBLocked(e);
    					operation.sqliteOperationExceptionCaught(this, e);
    				}
    			} catch (InterruptedException e) {
    				logger.debug(e);
    				break;
    			} catch (Exception e) {
    				dBLocked = isDBLocked(e);
    				if(!dBLocked)
    					logger.warn(e);
    			}
    			if(dBLocked) {
    				logger.warn("> sql: database is locked");
    				try {
    					synchronized (sqlConn) {
    						sqlConn.wait(blockedWait);
    					}
    				} catch (InterruptedException e) {
    					logger.debug(e);
    					break;			
    				}
    			}
    		}
    		close();
    		queue.clear();
    		dBLocked = false;
    		t = null;
    		logger.info("> sql: stop");
    	}

    	
    }

    public void testApp2()
    {
    	System.out.println("sqlite test");
    	SQLiteManager mgr;
    	Thread t;
    	int times = 100;
		try {
			mgr = new MGR(new File("testSQL.db"), times * 3);
			mgr.add(new RecordCreateTable(), 1000);
			t = new Thread(mgr);
			t.start();
			
	    	for(int i = 100000; i < times + 100000; ++i) {
	    		UUID u = UUID.randomUUID();
	    		String uuid = Long.toHexString(u.getMostSignificantBits()) + Long.toHexString(u.getLeastSignificantBits());
	    		String name = u.toString();
	    		long timestamp1 = System.currentTimeMillis();
	    		long timestamp2 = timestamp1 + 60000;
	    		System.out.println("i = " + i);
	    		mgr.add(new RecordAboutJoin(uuid, name, timestamp1, true), 3000);
	    		if((i & 1023) == 1023)
	    			mgr.add(new RecordSplitTable(10, "_" + i), 3000);
	    		mgr.add(new RecordAboutLeave(uuid, name, timestamp2, timestamp1, null), 3000);	    		
	    	}
	    	System.out.println("add ok");
	    	//while(System.in.read() != '#');
	    	mgr.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        assertTrue( true );
    }
}
