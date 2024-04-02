package com.dieselpoint.norm.sqlmakers;

public interface PojoInfo {

	Object getValue(Object pojo, String name);

	void putValue(Object pojo, String name, Object value);

	void putValue(Object pojo, String name, Object value, boolean ignoreIfMissing);

	String[] getGeneratedColumnNames();

	Property getProperty(String name);

}
