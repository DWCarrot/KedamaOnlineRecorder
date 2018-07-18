package kpcg.kedamaOnlineRecorder.util;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	public static ObjectMapper mapper = new ObjectMapper();
	
	public static ZoneId zone = ZoneId.systemDefault();
	
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS");
	
	public static Charset charset = Charset.forName("UTF-8");
}
