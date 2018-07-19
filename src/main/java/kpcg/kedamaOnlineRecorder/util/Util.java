package kpcg.kedamaOnlineRecorder.util;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	public static final ObjectMapper mapper = new ObjectMapper();
	
	public static final ZoneId zone = ZoneId.systemDefault();
	
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	public static final Charset charset = Charset.forName("UTF-8");
	
	public static final ZoneOffset offset = OffsetDateTime.now().getOffset();
}
