package kpcg.kedamaOnlineRecorder.client;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import kpcg.kedamaOnlineRecorder.util.Util;

public class PlayerListSerializer extends JsonSerializer<PlayerList> {

	@Override
	public void serialize(PlayerList value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		// TODO Auto-generated method stub
		gen.writeStartArray();
		Map<String, Instant> list = value.getList();
		if (list != null) {
			for (Map.Entry<String, Instant> entry : list.entrySet()) {
				gen.writeStartObject();
				gen.writeStringField("name", entry.getKey());
				gen.writeStringField("join", LocalDateTime.ofInstant(entry.getValue(), Util.zone).format(Util.formatter));
				gen.writeEndArray();
			}
		}
		gen.writeEndArray();
	}

}
