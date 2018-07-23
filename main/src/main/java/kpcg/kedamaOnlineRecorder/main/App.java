package kpcg.kedamaOnlineRecorder.main;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kpcg.kedamaOnlineRecorder.util.RecorderComponent;

/**
 * Hello world!
 *
 */
public class App {
	
	/**
	 * 
	 * @param args
	 * 
	 * command:<br>
	 * {@code (client|server|load) cmd}
	 */
	public static void main(String[] args) {
		System.out.println("Hello World!" + ' ' + App.class);
		System.out.println(System.getProperty("java.class.path"));
		Map<String, RecorderComponent> components = new HashMap<>();
		Scanner jin;
    	String s;
    	Matcher m;
    	String s1;
    	String s2;
    	Pattern loadp = Pattern.compile("load (\\S+) as (\\w+)");
    	int i;
    	try {
    		jin = new Scanner(System.in);
    		while(jin.hasNext()) {
    			s = jin.nextLine();
    			System.out.println(':' + s);
    			if(s == null || s.isEmpty())
    				continue;
    			if(s.equals("/quit"))
    				break;
    			i = s.indexOf(' ');
    			try {
	    			if(i > 0) {
	    				s1 = s.substring(0, i);
	    				s2 = s.substring(i + 1);
	    				switch(s1) {
	    				case "load":
	    					m = loadp.matcher(s);
	    					if(m.find()) {
	    						s1 = m.group(1);//class name
	    						s2 = m.group(2);//alias
	    						Class<?> k = Class.forName(s1);
	    						Method get = k.getMethod("getInstance");
	    						components.put(s2, (RecorderComponent) get.invoke(null));
	    						System.out.println(
	    								new StringBuilder()
	    								.append(s2)
	    								.append(' ')
	    								.append('(')
	    								.append(k)
	    								.append(')')
	    								.append(' ')
	    								.append("loaded")
	    								);
	    					}
	    					break;
	    				case "release":
	    					RecorderComponent c1 = components.remove(s2);
	    					boolean b = c1.stop();
	    					System.out.println(
    								new StringBuilder()
    								.append(s2)
    								.append(' ')
    								.append('(')
    								.append(c1.getClass())
    								.append(')')
    								.append(' ')
    								.append("removed")
    								.append(':')
    								.append(b)
    								);
	    					break;
	    				default:
	    					RecorderComponent c2 = components.get(s1);
	    					if(c2 == null) {
	    						System.out.println("can not find component: " + s1);
	    						break;
	    					}
	    					if(s2 == null || s2.isEmpty() || s2.charAt(0) != '#') {
	    						System.out.println("illegal command: " + s2);
	    						break;
	    					}
	    					c2.execute(s2);
	    					break;
	    				}
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
