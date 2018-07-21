package kpcg.kedamaOnlineRecorder;

import java.util.Scanner;

import io.netty.util.Version;
import kpcg.kedamaOnlineRecorder.client.ClientComponent;

/**
 * Hello world!
 *
 */
public class App {
	
	public static Integer version = 4;
	
	public static void main(String[] args) {
		System.out.println("Hello World!" + " $" + version);
		RecorderComponent client = ClientComponent.getInstance();
    	Scanner s;
    	String p;
    	try {
    		s = new Scanner(System.in);
    		while(s.hasNext()) {
    			p = s.nextLine();
    			if(p.equals("/quit"))
    				break;
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
	}
}
