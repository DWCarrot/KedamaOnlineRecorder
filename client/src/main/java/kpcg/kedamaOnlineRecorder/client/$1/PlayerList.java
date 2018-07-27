package kpcg.kedamaOnlineRecorder.client.$1;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Comparator;
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

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteManager;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperation;
import kpcg.kedamaOnlineRecorder.sqlite.SQLiteOperationGroup;
import kpcg.kedamaOnlineRecorder.util.Util;

@JsonSerialize(using = kpcg.kedamaOnlineRecorder.client.$1.PlayerListSerializer.class)
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
			record.add(new RecordCreateTable2(), sqliteAddTimeout);
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
					record.add(new RecordUpdateUUID(tempUUID.substring(0, tempUUID.indexOf('?')), uuid), sqliteAddTimeout);
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
			SQLiteOperationGroup g = null;
			//join
			String uuid = null;
			try {				
				uuid = getUUid(name, timestamp);
			} catch (InterruptedException e) {
				logger.debug(e);
				return;
			}
			g = new SQLiteOperationGroup(new RecordAboutJoin(uuid, name, timestamp, true)).setNext(g);
			//leave
			if(last != null) {				
				String uuid1 = null;
				try {
					uuid1 = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				g = new SQLiteOperationGroup(new RecordAboutLeave(uuid1, name, timestamp, last.longValue(), false)).setNext(g);
			}
			//online_count
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				logger.warn(e);
			}
			if(last == null)
				realOnline += 1;
			g = new SQLiteOperationGroup(new RecordOnlineCount(timestamp, realOnline, last == null, bref)).setNext(g);
			record.add(g, sqliteAddTimeout);
		}
	}
	
	public void remove(String name, long timestamp) {
		Long time1 = list.remove(name);
		if(record != null) {
			SQLiteOperationGroup g = null;
			//leave
			if(time1 != null) {
				String uuid = null;
				try {
					uuid = getUUid(name, timestamp);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				g = new SQLiteOperationGroup(new RecordAboutLeave(uuid, name, timestamp, time1.longValue(), null)).setNext(g);
			}
			//online_count
			String bref = null;
			try {
				bref = Util.mapper.writeValueAsString(list.keySet());
			} catch (JsonProcessingException e) {
				logger.warn(e);
			}
			if(time1 != null)
				realOnline -= 1;
			g = new SQLiteOperationGroup(new RecordOnlineCount(timestamp, realOnline, time1 != null, bref)).setNext(g);
			record.add(g, sqliteAddTimeout);
		}
	}
	
	public void check(Set<String> players, int online, long timestamp) {
		Map<String, Long> toRemove = new HashMap<>();
		Set<String> toAdd = players;
		int rcv = players.size();		
		for(Entry<String, Long> entry : list.entrySet()) {
			if(!toAdd.remove(entry.getKey()))
				toRemove.put(entry.getKey(), entry.getValue());
		}
		//
		realOnline = online;
		if(record != null) {
			if(list.size() == online && toAdd.isEmpty()) {
				//necessary-insufficient condition; unable to handle other condition in next step
				logger.info("> list: continuity=true");
			} else {
				SQLiteOperationGroup g = null;
				boolean canRemove = (online == rcv) || (list.size() + toAdd.size() - toRemove.size() == online);					
				//remove
				if(canRemove) {
					for (Entry<String, Long> entry : toRemove.entrySet()) {
						String name = entry.getKey();
						list.remove(name);
						String uuid = null;
						try {
							uuid = getUUid(name, timestamp);
						} catch (InterruptedException e) {
							logger.debug(e);
							return;
						}
						long timestamp1 = entry.getValue();
						g = new SQLiteOperationGroup(new RecordAboutLeave(uuid, name, timestamp, timestamp1, false)).setNext(g);
					}
				}
				//join
				for(String name : toAdd) {
					list.put(name, timestamp);
					String uuid = null;
					try {
						uuid = getUUid(name, timestamp);
					} catch (InterruptedException e) {
						logger.debug(e);
						return;
					}
					g = new SQLiteOperationGroup(new RecordAboutJoin(uuid, name, timestamp, false)).setNext(g);
				}
				//online_record
				String bref = null;
				try {
					bref = Util.mapper.writeValueAsString(list.keySet());
				} catch (JsonProcessingException e) {
					logger.warn(e);
				}
				g = new SQLiteOperationGroup(new RecordOnlineCount(timestamp, online, false, bref)).setNext(g);
				
				record.add(g, sqliteAddTimeout);
			}
		}
	}
	
	public void clear() {
		list.clear();
	}

	@Override
	public void handle(JsonNode node) throws Exception {
		
		if(decoder == null) {
			try {
				String src = node.get("favicon").asText();
				int offset = src.indexOf(',') + 1;
				src = src.substring(offset);
				if(src.indexOf('\n') >= 0) {
					decoder = Base64.getMimeDecoder();
				} else {
					if(src.indexOf('+') >= 0 || src.indexOf('/') >= 0) {
						decoder = Base64.getDecoder();
					} else {
						decoder = Base64.getUrlDecoder();
					}
				}
				byte[] data = decoder.decode(src);
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
		if(array != null) {
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
		}
		//
		logger.info("> ping result ({}/{})", players.size(), online);
		logger.debug("> ping: sample ({})", players);
//		System.out.println("#ping " + players);
		check(players, online,timestamp);
	}

	@Override
	public void pingHandleExceptionCaught(Throwable cause) throws Exception {
		logger.warn(cause);	
	}
}
