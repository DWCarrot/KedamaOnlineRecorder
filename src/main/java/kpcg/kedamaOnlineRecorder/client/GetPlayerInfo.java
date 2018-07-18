package kpcg.kedamaOnlineRecorder.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kpcg.kedamaOnlineRecorder.util.Util;

public class GetPlayerInfo {
	
	private static String method;
	
	private static String protocol;

	private static String host;

	private static int port;

	private static String[] patterns;

	public static void set(String pattern) throws MalformedURLException {
		int i, j, k;
		i = pattern.indexOf(' ');
		method = pattern.substring(0, i);
		URL url = new URL(pattern.substring(i + 1));
		protocol = url.getProtocol();
		host = url.getHost();
		port = url.getPort();
		if (port < 0)
			port = url.getDefaultPort();
		String file = url.getFile();
		ArrayList<String> array = new ArrayList<>();		
		for(k = 0, i = file.indexOf('<', k), j = file.indexOf('>', i); i > 0 && j > i; k = j + 1, i = file.indexOf('<', k), j = file.indexOf('>', i)) {
			array.add(file.substring(k, i));
			array.add(file.substring(i, j + 1));
		}
		array.add(file.substring(k));
		patterns = array.toArray(new String[array.size()]);	
	}

	public static String getUUID(String name, long timestamp, long timeout) throws InterruptedException {
		StringBuilder file = new StringBuilder();
		String uuid = null;
		URL url = null;
		HttpsURLConnection connection = null;
		InputStream in = null;
		timestamp /= 1000L;
		int argIndex = 0;
		for(String p : patterns) {
			if(p == null || p.isEmpty())
				continue;
			if(p.charAt(0) == '<') {
				if(argIndex == 0)
					file.append(name);
				if(argIndex == 1)
					file.append(timestamp);
				++argIndex;
			} else {
				file.append(p);
			}
		}
		try {
			url = new URL(protocol, host, port, file.toString());
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setConnectTimeout((int) timeout);
			connection.setReadTimeout((int) timeout);
			connection.connect();
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				in = connection.getInputStream();
				JsonNode node = Util.mapper.readTree(in);
				uuid = node.get("id").asText();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (connection != null)
				connection.disconnect();
			if(Thread.currentThread().isInterrupted())
				throw new InterruptedException();
		}
		return uuid;
	}

}
