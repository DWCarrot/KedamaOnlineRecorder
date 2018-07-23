package kpcg.kedamaOnlineRecorder.util;

import java.io.IOException;
import java.lang.reflect.Field;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class NRClassSerializer extends JsonSerializer<Object> {

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		Class<?> klass = value.getClass();
		
		
		gen.writeStartObject();
		gen.writeStringField("class", klass.toGenericString());
		gen.writeFieldName("value");
		if(klass.isArray())
			serializeArray(value, gen);
		else
			serializeObject(value, gen);
		gen.writeFieldName("fields");
		serializeField(value, klass, gen);
		gen.writeFieldName("super");
		gen.writeStartObject();
		klass = klass.getSuperclass();
		if(klass != null) {
			gen.writeStringField("class", klass.toGenericString());
			gen.writeFieldName("fields");
			serializeField(value, klass, gen);
		}
		gen.writeEndObject();
		gen.writeEndObject();
	}

	public void serializeField(Object value, Class<?> klass, JsonGenerator gen) throws IOException {
		Object fv;
		Field[] fields = klass.getDeclaredFields();
		gen.writeStartObject();
		for(Field field : fields) {
			field.setAccessible(true);
			try {
				fv = field.get(value);
				gen.writeFieldName(field.getName());
				gen.writeStartObject();				
				gen.writeStringField("class", field.getType().toGenericString());
				if(fv == null) {
					gen.writeNullField("value");	
				} else {
					gen.writeFieldName("value");
					if(field.getType().isArray()) {
						serializeArray(fv, gen);
					} else {
						serializeObject(fv, gen);
					}
				}
				gen.writeEndObject();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gen.writeEndObject();
	}
	
	public void serializeArray(Object value, JsonGenerator gen) throws IOException {
		if(value == null)
			return;
		Class<?> klass = value.getClass();
		String cn = klass.getName();
		gen.writeStartArray();
		if(cn.lastIndexOf('[') == 0) {
			switch(cn.charAt(1)) {
			case 'Z':	//boolean
				boolean[] zs = (boolean[]) value;
				for(boolean z : zs)
					gen.writeBoolean(z);
				break;
			case 'B':	//byte
				byte[] bs = (byte[]) value;
				for(byte b : bs)
					gen.writeNumber(b);
				break;
			case 'C':	//char
				char[] cs = (char[]) value;
				for(char c : cs)
					gen.writeNumber(c);
				break;
			case 'L':	//class
				Object[] ls = (Object[]) value;
				for(Object l : ls)
					serializeObject(l, gen);
				break;
			case 'D':	//double
				double[] ds = (double[]) value;
				for(double d : ds)
					gen.writeNumber(d);
				break;
			case 'F':	//float
				float[] fs = (float[]) value;
				for(float f : fs)
					gen.writeNumber(f);
				break;
			case 'I':	//int
				int[] is = (int[]) value;
				for(int i : is)
					gen.writeNumber(i);
				break;
			case 'J':	//long
				long[] js = (long[]) value;
				for(long j : js)
					gen.writeNumber(j);
				break;
			case 'S':	//short
				short[] ss = (short[]) value;
				for(short s : ss)
					gen.writeNumber(s);
				break;
			}
		} else {
			for(Object v : (Object[])value) {
				serializeArray(v, gen);
			}
		}
		gen.writeEndArray();
	}
	
	public void serializeObject(Object value, JsonGenerator gen) throws IOException {
		if(value == null) {
			gen.writeNull();
			return;
		}
		switch (value.getClass().getName()) {
		case "java.lang.Boolean":
		case "java.lang.Byte":
		case "java.lang.Double":
		case "java.lang.Float":
		case "java.lang.Integer":
		case "java.lang.Long":
		case "java.lang.Short":
		case "java.lang.Character":
		case "java.math.BigDecimal":
		case "java.math.BigInteger":
			gen.writeRawValue(value.toString());
			break;
		default:
			gen.writeString(value.toString());
			break;
		}
	}
}
