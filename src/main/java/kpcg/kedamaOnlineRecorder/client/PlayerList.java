package kpcg.kedamaOnlineRecorder.client;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.util.Util;

@JsonSerialize(using = kpcg.kedamaOnlineRecorder.client.PlayerListSerializer.class)
public class PlayerList implements MinecraftPing.MinecraftPingHandler {
	
	
	private static final Comparator<Entry<String, String>> comparator = new Comparator<Map.Entry<String,String>>() {	
		@Override
		public int compare(Entry<String, String> o1, Entry<String, String> o2) {
			 //<name, uuid>
			return o1.getValue().compareTo(o1.getValue());
		}
	};
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(PlayerList.class);
	
	private Decoder decoder;
	
	private long sqliteAddTimeout = 1000;
	
	private long netTimeout = 3000;
	
	private SQLiteManager record;

	private Map<String, Long> list;	//<name, time1>
	
	private Map<String, String> table; //<name, uuid>
	
	int realOnline;
	
	public PlayerList() {
		list = new HashMap<>();
		table = new HashMap<>(2048);
	}
	
	public void setRecord(SQLiteManager record) {
		this.record = record;
		
	}
	
	public Map<String, Long> getList() {
		return list;
	}
	
	public void init(long timeout) throws InterruptedException {
		if(record != null) {
			record.add(new RecordCreateTable(), sqliteAddTimeout);
			synchronized (list) {
				record.add(new RecordInitPlayerList(list, table), sqliteAddTimeout);
				list.wait(timeout);
				if(list.isEmpty())
					list = new HashMap<>();
				realOnline = list.size();
			}
		}
	}
	
	public void setSqliteAddTimeout(long sqliteAddTimeout) {
		this.sqliteAddTimeout = sqliteAddTimeout;
	}
	
	public String getUUid(String name, long timestamp) throws InterruptedException {
		String uuid = table.get(name);
		String tempUUID = null;
		if(uuid == null || uuid.charAt(0) == '@') {
			tempUUID = uuid;
			try {
				uuid = GetPlayerInfo.getUUID(name, timestamp, netTimeout);
			} catch (InterruptedException e) {
				throw e;
			}
			if (uuid == null) {
				uuid = new StringBuilder(32).append('@').append(name).append('?').append(timestamp).toString();
			} else {
				if (tempUUID != null) {
					//TODO update
					
				}
				table.put(name, uuid);
				logger.info("> list: table append ({},{}) ({})", uuid, name, table.size());
			}
		}
		return uuid;
	}
	
	public int fliteTable() {
		int count = 0;
		synchronized (table) {
			Entry<String, String>[] collection = (Entry<String, String>[]) table.entrySet().toArray();
			Arrays.sort(collection, comparator);
			int i, j;
			boolean repeat;
			for(i = 0, j = 1, repeat = false; j < collection.length; ++j) {
				if(collection[i].getValue().equals(collection[j].getValue())) {
					repeat = true;
				} else {
					if(repeat) {
						repeat = false;
						for(int k = i; k < j; ++k, ++count)
							table.remove(collection[k].getKey());
						i = j;
					} else {
						++i;
					}
				}
			}
			if(repeat) {
				for(int k = i; k < j; ++k, ++count)
					table.remove(collection[k].getKey());
			}
		}
		logger.info("> list: table reserved ({}) removed ({})", table.size(), count);
		return count;
	}
	
	public void add(String name, long timestamp) {
		Long last = list.put(name, timestamp);
		if(record != null) {
			//online_count
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				logger.warn(e);
			}
			if(last == null)
				realOnline += 1;
			record.add(new RecordOnlineCount(timestamp, realOnline, last == null, bref), sqliteAddTimeout);			
			//leave
			if(last != null) {				
				String uuid = null;
				try {
					uuid = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				record.add(new RecordAboutLeave(uuid, name, timestamp, last.longValue(), false), sqliteAddTimeout);
			}	
			//join
			String uuid = null;
			try {
				uuid = getUUid(name, timestamp);
			} catch (InterruptedException e) {
				logger.debug(e);
				return;
			}
			record.add(new RecordAboutJoin(uuid, name, timestamp), sqliteAddTimeout);
				
		}
	}
	
	public void remove(String name, long timestamp) {
		Long time1 = list.remove(name);
		if(record != null) {
			//online_count
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				logger.warn(e);
			}
			if(time1 != null)
				realOnline -= 1;
			record.add(new RecordOnlineCount(timestamp, realOnline, time1 != null, bref), sqliteAddTimeout);
			//leave
			if(time1 != null) {
				String uuid = null;
				try {
					uuid = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				record.add(new RecordAboutLeave(uuid, name, timestamp, time1.longValue(), true), sqliteAddTimeout);
			}
		}
	}
	
	/**
	 * 
	 * @param players	[name=id]
	 * @param timestamp
	 */
	public void check(Set<String> players, int online, long timestamp) {
		Map<String, Long> toRemove = new HashMap<>();
		Set<String> toAdd = players;
		int rcv = players.size();
		boolean canRemove = (online == rcv || online == rcv + 1);
		for(Iterator<Entry<String, Long>> it = list.entrySet().iterator(); it.hasNext();) {
			Entry<String, Long> e = it.next();
			if(!toAdd.remove(e.getKey()) && canRemove) {
				toRemove.put(e.getKey(), e.getValue());
				it.remove();
			}
		}
		for(String name : toAdd) {
			list.put(name, timestamp);
		}
		realOnline = online;
		if(record != null && !(toAdd.isEmpty() && toRemove.isEmpty() && list.size() == realOnline)) {
			//online_record
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				logger.warn(e);
			}
			record.add(new RecordOnlineCount(timestamp, online, false, bref), sqliteAddTimeout);
			//remove
			for (Entry<String, Long> entry : toRemove.entrySet()) {
				String name = entry.getKey();
				String uuid = null;
				try {
					uuid = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				long timestamp1 = entry.getValue();
				record.add(new RecordAboutLeave(uuid, name, timestamp, timestamp1, false), sqliteAddTimeout);
			}
			//join
			for(String name : toAdd) {
				String uuid = null;
				try {
					uuid = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				record.add(new RecordAboutJoin(uuid, name, timestamp), sqliteAddTimeout);
			}
		} else {
			logger.info("> list: integrity=true");
		}
	}
	
	public void clear() {
		list.clear();
	}

	@Override
	public void handle(JsonNode node) throws Exception {
		
		if(decoder == null) {
			try {
				decoder = Base64.getMimeDecoder();
				String src = node.get("favicon").asText();
				int offset = src.indexOf(',') + 1;
				byte[] data = decoder.decode(src.substring(offset));
				OutputStream ofile = new FileOutputStream("favicon" + ".png");
				ofile.write(data);
				ofile.flush();
				ofile.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
		
		long timestamp = System.currentTimeMillis();
		JsonNode v = node.get("players");
		ArrayNode array = (ArrayNode) v.get("sample");
		int online = v.get("online").asInt();
		Set<String> players = new HashSet<>();	//name,uuid
		for(Iterator<JsonNode> it = array.iterator(); it.hasNext(); ) {
			JsonNode n = it.next();
			String name = n.get("name").asText();
			String id = n.get("id").asText();
			StringBuilder uuidB = new StringBuilder();
			int i, j;
			for(i = 0, j = id.indexOf('-', i); j > i; i = j + 1, j = id.indexOf('-', i)) {
				uuidB.append(id.substring(i, j));
			}
			if(i < id.length())
				uuidB.append(id.substring(i));
			String uuid = uuidB.toString();
			players.add(name);
			table.put(name, uuid);
		}
		//
		logger.debug("> ping: result ({})", players);
//		System.out.println("#ping " + players);
		check(players, online,timestamp);
	}

	@Override
	public void pingHandleExceptionCaught(Throwable cause) throws Exception {
		logger.warn(cause);	
	}
}
