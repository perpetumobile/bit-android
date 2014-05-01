package com.perpetumobile.bit.orm.db;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Zoran Dukic
 *
 */
public enum DBScriptMethod {
	LOAD("LOAD"),
	EXECUTE("EXECUTE");
	
	private static final HashMap<String,DBScriptMethod> map = new HashMap<String,DBScriptMethod>();
	static {
		for(DBScriptMethod rt : EnumSet.allOf(DBScriptMethod.class))
			map.put(rt.getMethod(), rt);
	}

	private String method;

	private DBScriptMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getMethod(){
		return method; 
	}

	static public DBScriptMethod get(String type) { 
		return map.get(type.toUpperCase()); 
	}
}
