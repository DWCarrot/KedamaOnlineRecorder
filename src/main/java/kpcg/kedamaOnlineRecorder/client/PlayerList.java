package kpcg.kedamaOnlineRecorder.client;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.util.Util;

@JsonSerialize(using = kpcg.kedamaOnlineRecorder.client.PlayerListSerializer.class)
public class PlayerList implements MinecraftPing.MinecraftPingHandler {
	
	private SQLiteManager record;
	
	private long sqliteAddTimeout = 1000;

	private Map<String, Instant> list;	//<name, time1>
	
	public PlayerList() {
		list = new HashMap<>();
	}
	
	public void setRecord(SQLiteManager record) {
		this.record = record;
	}
	
	public Map<String, Instant> getList() {
		return list;
	}

	public void init(long timeout) throws InterruptedException {
		if(record != null) {
			record.add(new RecordCreateTable(), sqliteAddTimeout);
			synchronized (list) {
				record.add(new RecordInitPlayerList(list), sqliteAddTimeout);
				list.wait(timeout);
				if(list.isEmpty())
					list = new HashMap<>();
			}
		}
	}
	
	public void setSqliteAddTimeout(long sqliteAddTimeout) {
		this.sqliteAddTimeout = sqliteAddTimeout;
	}
	
	public void add(String name, long timestamp) {
		Instant last = list.put(name, Instant.ofEpochMilli(timestamp));
		if(record != null) {
			
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			record.add(new RecordOnlineCount(timestamp, list.size(), last == null, bref), sqliteAddTimeout);
			
			if(last != null) {
				record.add(new RecordGetUUID(name, timestamp) {
					
					final long timestamp1 = last.toEpochMilli();
					
					@Override
					public void handle(SQLiteManager mgr, String uuid, String name, long timestamp) {
						mgr.add(new RecordAboutLeave(uuid, name, timestamp, timestamp1, false), sqliteAddTimeout);
						//TODO log
					}
					
					@Override
					public String getUuidFromMojang(String name, long timestamp, long timeout) throws InterruptedException {
						return GetPlayerInfo.getUUID(name, timestamp, timeout);
					}
				}, sqliteAddTimeout);
			}
						
			record.add(new RecordGetUUID(name, timestamp) {
				
				@Override
				public void handle(SQLiteManager mgr, String uuid, String name, long timestamp) {
					mgr.add(new RecordAboutJoin(uuid, name, timestamp), sqliteAddTimeout);
					//TODO log
				}
				
				@Override
				public String getUuidFromMojang(String name, long timestamp, long timeout) throws InterruptedException {
					return GetPlayerInfo.getUUID(name, timestamp, timeout);
				}
			}, sqliteAddTimeout);
		}
	}
	
	public void remove(String name, long timestamp) {
		Instant time1 = list.remove(name);
		if(record != null) {
			
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			record.add(new RecordOnlineCount(timestamp, list.size(), time1 != null, bref), sqliteAddTimeout);
			
			if(time1 != null) {
				record.add(new RecordGetUUID(name, timestamp) {
					
					final long timestamp1 = time1.toEpochMilli();
					
					@Override
					public void handle(SQLiteManager mgr, String uuid, String name, long timestamp) {
						mgr.add(new RecordAboutLeave(uuid, name, timestamp, timestamp1, true), sqliteAddTimeout);
						//TODO log
					}
					
					@Override
					public String getUuidFromMojang(String name, long timestamp, long timeout) throws InterruptedException {
						return GetPlayerInfo.getUUID(name, timestamp, timeout);
					}
				}, sqliteAddTimeout);
			}
		}
	}
	
	/**
	 * 
	 * @param players	[name=id]
	 * @param timestamp
	 */
	public void check(Map<String, String> players, long timestamp) {
		Map<String, Instant> toRemove = new HashMap<>(list.size());
		Set<String> toAdd = players.keySet();
		for(Iterator<Entry<String, Instant>> it = list.entrySet().iterator(); it.hasNext();) {
			Entry<String, Instant> e = it.next();
			if(!toAdd.remove(e.getKey())) {
				toRemove.put(e.getKey(), e.getValue());
				it.remove();
			}
		}
		for(String name : toAdd) {
			list.put(name, Instant.ofEpochMilli(timestamp));
		}
		if(record != null && !(toAdd.isEmpty() && toRemove.isEmpty())) {
			
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			record.add(new RecordOnlineCount(timestamp, list.size(), false, bref), sqliteAddTimeout);
			
			for(Entry<String, Instant> entry : toRemove.entrySet()) {
				record.add(new RecordGetUUID(entry.getKey(), timestamp) {
					
					final long timestamp1 = entry.getValue().toEpochMilli();
					
					@Override
					public void handle(SQLiteManager mgr, String uuid, String name, long timestamp) {
						mgr.add(new RecordAboutLeave(uuid, name, timestamp, timestamp1, false), sqliteAddTimeout);
						//TODO log
					}
					
					@Override
					public String getUuidFromMojang(String name, long timestamp, long timeout) throws InterruptedException {
						return GetPlayerInfo.getUUID(name, timestamp, timeout);
					}
				}, sqliteAddTimeout);
			}
			
			for(Map.Entry<String, String> entry : players.entrySet()) {
				record.add(new RecordAboutJoin(entry.getValue(), entry.getKey(), timestamp), sqliteAddTimeout);
			}
		}
	}
	
	public void clear() {
		list.clear();
	}

	@Override
	public void handle(JsonNode node) throws Exception {
		long timestamp = System.currentTimeMillis();
		ArrayNode array = (ArrayNode) node.get("players").get("sample");
		Map<String, String> players = new HashMap<>();
		for(Iterator<JsonNode> it = array.iterator(); it.hasNext(); ) {
			JsonNode n = it.next();
			String name = n.get("name").asText();
			String id = n.get("id").asText();
			StringBuilder uuid = new StringBuilder();
			int i, j;
			for(i = 0, j = id.indexOf('-', i); j > i; i = j + 1, j = id.indexOf('-', i)) {
				uuid.append(id.substring(i, j));
			}
			if(i < id.length())
				uuid.append(id.substring(i));
			players.put(name, uuid.toString());
		}
		//TODO log
		System.out.println("#ping " + players);
		check(players, timestamp);
	}

	@Override
	public void pingHandleExceptionCaught(Throwable cause) throws Exception {
		//TODO log
		cause.printStackTrace();		
	}
}
