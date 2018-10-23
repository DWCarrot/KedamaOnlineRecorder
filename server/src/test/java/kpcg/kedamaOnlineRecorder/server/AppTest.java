package kpcg.kedamaOnlineRecorder.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import kpcg.kedamaOnlineRecorder.server.$1.DataServerConfig;
import kpcg.kedamaOnlineRecorder.util.Util;

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
    public void testApp1()
    {
    	
    	File f = new File("server-config.json");
    	DataServerConfig cfg = new DataServerConfig();
    	
    	try(InputStream ifile = new FileInputStream(f)) {
    		cfg = Util.mapper.readValue(ifile, cfg.getClass());
    		ifile.close();
    	} catch (Exception e) {
			try(OutputStream ofile = new FileOutputStream(f)) {
				Util.mapper.writeValue(ofile, cfg);
				ofile.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
    	System.out.println(cfg);
        assertTrue( true );
    }
}
