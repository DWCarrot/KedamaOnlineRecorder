package kpcg.kedamaOnlineRecorder.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import kpcg.kedamaOnlineRecorder.util.RecorderComponent;
import kpcg.kedamaOnlineRecorder.client.$1.ClientComponent;
import kpcg.kedamaOnlineRecorder.client.$1.PlayerList;
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

    boolean install = true;
    
    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testApp2()
    {
    	/*
    	RecorderComponent client = ClientComponent.getInstance();
    	Scanner s;
    	String p;
    	
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Map<String, String> table = null;
    	PlayerList list = null;
    	

    	if(install) {
    		assertTrue( true );
    		return;
    	}
    	
    	try {
    		s = new Scanner(System.in);
    		while(s.hasNext()) {
    			p = s.nextLine();
    			if(p.equals("/quit"))
    				break;
    			if(p.equals("/load")) {
    				list = ClientComponent.getInstance().list;
    				table = ClientComponent.getInstance().list.getTable();
    				ArrayNode array = (ArrayNode) mapper.readTree(new File("players.json"));
    				for(JsonNode node : array) {
    					String uuid = node.get("uuid").asText();
    					ArrayNode names = (ArrayNode) node.get("names");
    					for(JsonNode nn : names) {
    						String name = nn.get("name").asText();
    						table.put(name, uuid);
    					}
    				}
    				System.out.println(table.size());
    				continue;
    			}
    			if(p.equals("/show")) {
    				table = ClientComponent.getInstance().list.getTable();
    				ArrayList<Entry<String, String>> collection = new ArrayList<>(table.entrySet());
    				collection.sort(PlayerList.comparator);
    				mapper.writeValue(new File("show"+System.currentTimeMillis()), collection);
    				break;
    			}
    			if(p.startsWith("/record")) { // /record+<name>
    				char op = p.charAt("/record".length());
    				p = p.substring("/record".length() + 1);
    				if(op == '+')
    					list.add(p, System.currentTimeMillis());
    				if(op == '-')
    					list.remove(p, System.currentTimeMillis());
    			}
    			try {
    				if(p.charAt(0) == '#') {
    					client.execute(p);
    				}
    			} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
        assertTrue( true );
        */
    }
}
