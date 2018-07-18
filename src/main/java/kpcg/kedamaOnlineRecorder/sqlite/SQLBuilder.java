package kpcg.kedamaOnlineRecorder.sqlite;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SQLBuilder  {

	private static char tableSplit = '\'';
	
	private static char columnSplit = '`';
	
	private static char textSplit = '"';
	
	private static boolean booleanAsInteger = true;
	
	private StringBuilder sql;
	
	public static SQLBuilder get() {
		return new SQLBuilder();
	}
	
	public SQLBuilder() {
		sql = new StringBuilder();
	}
	
	public SQLBuilder keyword(String s) {
		sql.append(s).append(' ');
		return this;
	}
	
	public SQLBuilder keyword(char s) {
		sql.append(s).append(' ');
		return this;
	}
	
	public SQLBuilder split(char s) {
		int i = sql.length() - 1;
		if(i > 0 && sql.charAt(i) == ' ')
			sql.setCharAt(i, s);
		else
			sql.append(s);
		return this;
	}
	
	public SQLBuilder table(String s) {
		sql.append(tableSplit).append(s).append(tableSplit).append(' ');
		return this;
	}
	
	public SQLBuilder column(String s) {
		sql.append(columnSplit).append(s).append(columnSplit).append(' ');
		return this;
	}
	
	public SQLBuilder value(String s) {
		if(s.indexOf('"') >= 0)
			sql.append('\'').append(s).append('\'').append(' ');
		else
			sql.append(textSplit).append(s).append(textSplit).append(' ');
		return this;
	}
	
	public SQLBuilder value(short s) {
		sql.append(s).append(' ');
		return this;
	}
	
	public SQLBuilder value(int s) {
		sql.append(s).append(' ');
		return this;
	}
	
	public SQLBuilder value(long s) {
		sql.append(s).append(' ');
		return this;
	}
	
	public SQLBuilder value(boolean s) {
		if(booleanAsInteger)
			sql.append(s ? '1' : '0').append(' ');
		else
			sql.append(textSplit).append(s ? "true" : "false").append(textSplit).append(' ');
		return this;
	}
	
	public SQLBuilder append(SQLBuilder s) {
		sql.append(s.sql);
		return this;
	}

	
//	/**
//	 * {@code ($column1,$column2,...,$columnN)\}
//	 * @param columns
//	 * @return
//	 */
//	public SQLBuilder constructInsertGroup(String...columns) {
//		boolean f = false;
//		sql.append('(');
//		for(String c : columns) {
//			if(f) 
//				sql.append(',');
//			else
//				f = true;
//			sql.append(columnSplit).append(c).append(columnSplit);
//		}
//		sql.append(')').append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code VALUES ($value1,$value2,...,$valueN)\}
//	 * @param values
//	 * @return
//	 */
//	public SQLBuilder constructVALUESGroup(Object...values) {		
//		boolean f = false;
//		sql.append("VALUES").append(' ');
//		sql.append('(');
//		for(Object v : values) {
//			if(f)
//				sql.append(',');
//			else
//				f = true;
//			if(v instanceof Integer || v instanceof Long || v instanceof Short) {
//				sql.append(v.toString());
//				continue;
//			}
//			if(v instanceof Boolean && booleanAsInteger) {
//				sql.append((Boolean)v ? '1' : '0');
//				continue;
//			}
//			sql.append(textSplit).append(v.toString()).append(textSplit);
//		}
//		sql.append(')').append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code $column $op [$value] \}
//	 * @param column
//	 * @param operator
//	 * @param value
//	 * @return
//	 */
//	public SQLBuilder constructConditionExpression(String column, String operator, Object value) {
//		sql.append(columnSplit).append(column).append(columnSplit).append(' ');
//		sql.append(operator).append(' ');
//		if(value != null) {
//			if(value instanceof SQLBuilder) {
//				sql.append('(').append(((SQLBuilder)value).sql).append(')');
//			} else {
//				if(value instanceof Integer || value instanceof Long || value instanceof Short)
//					sql.append(value.toString());
//				else {
//					if(value instanceof Boolean && booleanAsInteger)
//						sql.append((Boolean)value ? '1' : '0');
//					else
//						sql.append(textSplit).append(value.toString()).append(textSplit);
//				}
//			}
//			sql.append(' ');
//		}	
//		return this;
//	}
//	
//	/**
//	 * {@code GREATE TABLE [IF NOT EXISTS] $table (\}
//	 * @param table
//	 * @param checkExist
//	 * @return
//	 */
//	public SQLBuilder startCreateTable(String table, boolean checkExist) {
//		sql.append("CREATE").append(' ').append("TABLE").append(' ');
//		if(checkExist)
//			sql.append("IF").append(' ').append("NOT").append(' ').append("EXISTS").append(' ');
//		sql.append(tableSplit).append(table).append(tableSplit).append(' ');
//		sql.append('(');
//		return this;
//	}
//	
//	/**
//	 * {@code ) \} <br>auto-remove last ','
//	 * @return
//	 */
//	public SQLBuilder endCreateTable() {
//		int i = sql.length() - 1;
//		if(i > 0 && sql.charAt(i) == ',')
//			sql.setCharAt(i, ')');
//		else
//			sql.append(')');
//		sql.append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code $column $prop1 $prop2 ... $propN,\}
//	 * @param column
//	 * @param properties
//	 * @return
//	 */
//	public SQLBuilder createTableColumn(String column, String...properties) {
//		sql.append(columnSplit).append(column).append(columnSplit).append(' ');
//		for(String p : properties)
//			sql.append(p).append(' ');
//		int i = sql.length() - 1;
//		if(i > 0 && sql.charAt(i) == ' ')
//			sql.setCharAt(i, ',');
//		return this;
//	}
//	
//	/**
//	 * {@code DROP TABLE [IF EXISTS] $table \}
//	 * @param table
//	 * @param checkExist
//	 * @return
//	 */
//	public SQLBuilder dropTable(String table, boolean checkExist) {
//		sql.append("DROP").append(' ').append("TABLE").append(' ');
//		if(checkExist)
//			sql.append("IF").append(' ').append("EXISTS").append(' ');
//		sql.append(tableSplit).append(table).append(tableSplit).append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code INSERT INTO $table \}
//	 * @param table
//	 * @return
//	 */
//	public SQLBuilder startInsert(String table) {
//		sql.append("INSERT").append(' ').append("INTO").append(' ');
//		sql.append(tableSplit).append(table).append(tableSplit).append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code}
//	 * @return
//	 */
//	public SQLBuilder endInsert() {
//		return this;
//	}
//	
//	/**
//	 * {@code SELECT $column1,$column2,...,$columnN FROM $table \}
//	 * @param table
//	 * @param columns
//	 * @return
//	 */
//	public SQLBuilder startSELECT(String table, String...columns) {
//		sql.append("SELECT").append(' ');
//		boolean f = false;
//		for(String c : columns) {
//			if(f)
//				sql.append(',');
//			else
//				f = true;
//			if(c.equals("*"))
//				sql.append(c);
//			else
//				sql.append(columnSplit).append(c).append(columnSplit);
//		}
//		sql.append(' ');
//		sql.append("FROM").append(' ').append(tableSplit).append(table).append(tableSplit).append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code}
//	 * @return
//	 */
//	public SQLBuilder endSELECT() {
//		return this;
//	}
//	
//	/**
//	 * {@code UPDATE $table \}
//	 * @param table
//	 * @return
//	 */
//	public SQLBuilder startUpdate(String table) {
//		sql.append("UPDATE").append(' ').append(tableSplit).append(table).append(tableSplit).append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code}
//	 * @return
//	 */
//	public SQLBuilder endUpdate() {
//		return this;
//	}
//	
//	/**
//	 * {@code SET \} 
//	 * @return
//	 */
//	public SQLBuilder startSET() {
//		sql.append("SET").append(' ');
//		return this;
//	}
//	/**
//	 * {@code  \} <br> auto-remove ','
//	 * @return
//	 */
//	public SQLBuilder endSET() {
//		int i = sql.length() - 1;
//		if(i > 0 && sql.charAt(i) == ',')
//			sql.setCharAt(i, ' ');
//		else
//			sql.append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code $column = $value,\}
//	 * @param column
//	 * @param value
//	 * @return
//	 */
//	public SQLBuilder updateColumn(String column, Object value) {
//		sql.append(columnSplit).append(column).append(columnSplit).append(' ');
//		sql.append('=').append(' ');
//		if(value != null) {
//			if(value instanceof SQLBuilder) {
//				sql.append('(').append(((SQLBuilder)value).sql).append(')');
//			} else {
//				if(value instanceof Integer || value instanceof Long || value instanceof Short)
//					sql.append(value.toString());
//				else {
//					if(value instanceof Boolean && booleanAsInteger)
//						sql.append((Boolean)value ? '1' : '0');
//					else
//						sql.append(textSplit).append(value.toString()).append(textSplit);
//				}
//			}
//		} else {
//			sql.append("NULL");
//		}
//		sql.append(',');
//		return this;
//	} 
//	
//	/**
//	 * {@code DELETE FROM $table \}
//	 * @param table
//	 * @param columns
//	 * @return
//	 */
//	public SQLBuilder startDELETE(String table) {
//		sql.append("DELETE").append(' ');
//		sql.append("FROM").append(' ').append(tableSplit).append(table).append(tableSplit).append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code}
//	 * @return
//	 */
//	public SQLBuilder endDELETE() {
//		return this;
//	}
//	
//	/**
//	 * {@code WHERE \}
//	 * @return
//	 */
//	public SQLBuilder startWHERE() {
//		sql.append("WHERE").append(' ');
//		return this;
//	}
//	
//	/**
//	 * {@code}
//	 * @return
//	 */
//	public SQLBuilder endWHERE() {
//		return this;
//	}
//	
	public StringBuilder getStringBuilder() {
		return sql;
	}

	@Override
	public String toString() {
		return sql.toString();
	}
	
}
