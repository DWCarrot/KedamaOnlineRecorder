package kpcg.kedamaOnlineRecorder.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EmptyStackException;
import java.util.Stack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CreepyClass {
	
	@JsonIgnore
	private Stack<Object> path;
	
	@JsonSerialize(using = kpcg.kedamaOnlineRecorder.util.NRClassSerializer.class)
	public Object object;
	
	public CreepyClass(Object obj) {		
		path = new Stack<>();
		path.push(object = obj);
	}
	
	public void push(String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(object == null)
			throw new NullPointerException("object is null");
		Class<?> klass = object.getClass();
		Field field = klass.getDeclaredField(fieldName);
		field.setAccessible(true);
		object = field.get(object);
		path.push(object);
	}
	
	public void push(int index) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(object == null)
			throw new NullPointerException("object is null");
		Class<?> klass = object.getClass();
		if(!klass.isArray())
			throw new NoSuchFieldException("objject is not an array");
		object = ((Object[])object)[index];
		path.push(object);
	}
	
	public void pop() throws EmptyStackException {
		path.pop();
		object = path.peek();
	}
	
	public String serialize() throws JsonProcessingException {	
		return Util.mapper.writeValueAsString(this);
	}
	
	public void serialize(File file) throws IOException {	
		Util.mapper.writeValue(file, this);
	}
	
	public static Object getObject(Object root, String path) throws Exception {
		int i = 0, j = 0;
		int index;
		boolean getSuper = false;
		String field;
		char[] cpath = path.toCharArray();
		char pre;
		if(cpath.length == 0)
			return root;
		try {
			for(i = 0, j = 1; j < cpath.length; ++j) {
				pre = cpath[i];
				switch(cpath[j]) {
				case '.':
					if(pre == '.') {
						field = new String(cpath, i + 1, j - i - 1);
						if(field.equals("super")) {
							getSuper = true;
						} else {
							root = getObjectField(root, field, getSuper);
							getSuper = false;
						}
						i = j;
						break;
					}
					if(pre == ']' && i == j - 1) {
						i = j;
						break;
					}
					throw new IllegalArgumentException("prase exception: '.'");				
				case '[':
					if(pre == '.') {
						field = new String(cpath, i + 1, j - i - 1);
						if(field.equals("super")) {
							getSuper = true;
						} else {
							root = getObjectField(root, field, getSuper);
							getSuper = false;
						}
						i = j;
						break;
					}
					if(pre == ']' && i == j - 1) {
						i = j;
						break;
					}
					throw new IllegalArgumentException("prase exception: '['");					
				case ']':
					if(pre == '[') {
						index = Integer.parseInt(new String(cpath, i + 1, j - i - 1), 10);
						root = getArrayElement(root, index);
						i = j;
						break;
					}
					throw new IllegalArgumentException("prase exception: ']'");
				}
			}
			pre = cpath[i];
			if(pre == ']' && i == cpath.length - 1)
				return root;
			if(pre == '.') {
				field = new String(cpath, i + 1, j - i - 1);
				if(field.equals("super")) {
					getSuper = true;
				} else {
					root = getObjectField(root, field, getSuper);
					getSuper = false;
				}
				return root;
			}
			throw new IllegalArgumentException("prase exception: end");
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder("prase failed at ");
			msg.append('{').append(cpath, i, (j < cpath.length ? j + 1 : j) - i).append('}');
			e = new Exception(msg.toString(), e);
			throw e;
		}
	}
	
	private static Object getObjectField(Object root, String field, boolean getSuper) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> c = root.getClass();
		if(getSuper)
			c = c.getSuperclass();
		Field f = c.getDeclaredField(field);
		f.setAccessible(true);
		return f.get(root);
	}
	
	private static Object getArrayElement(Object root, int index) {
		Object[] array = (Object[]) root;
		return array[index];
	}
}
