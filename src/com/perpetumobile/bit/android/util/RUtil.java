package com.perpetumobile.bit.android.util;

import java.lang.reflect.Field;

import android.content.res.Resources;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.Logger;

public class RUtil {
	static private Logger logger = new Logger(RUtil.class);

	static public Resources getResources() {
		return DataSingleton.getInstance().getAppContext().getResources();
	}
	
	static public Class<?> getDeclaredClass(Class<?> srcClass, String innerClassName) {
		Class<?> result = null;
		Class<?>[] classes = srcClass.getDeclaredClasses();
		for(Class<?> c : classes) {
			if(c.getSimpleName().equals(innerClassName)) {
				result = c;
			}
		}
		return result;
	}
	
	static public int getInt(Class<?> srcClass, String fieldName) {
		int result = -1;
		try {
			Field f = srcClass.getDeclaredField(fieldName);
			result = f.getInt(srcClass.newInstance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	static public int getResourceId(Class<?> rClass, String encFieldName) {
		if(!encFieldName.startsWith("@")) {
			throw new RUtilException("RUtil.getInt exception. Syntax error for: " + encFieldName);
		}
		String innerClassName = null;
		String fieldName = null;
		int index = encFieldName.indexOf("/");
		if(index == -1) {
			throw new RUtilException("RUtil.getInt exception. Syntax error for: " + encFieldName);
		} else {
			innerClassName = encFieldName.substring(1, index);
			fieldName = encFieldName.substring(index+1);
		}
		return getResourceId(rClass, innerClassName, fieldName);
	}
	
	static public Class<?> getRClass(String packageName) {
		Class<?> result = null;
		try {
			result = Class.forName(packageName + ".R");
		} catch (ClassNotFoundException e) {
			logger.error("RUtil.getRClass exception.", e);
		}
		return result;
	}
	
	static public Class<?> getPackageRClass() {
		return getRClass(DataSingleton.getInstance().getAppContext().getPackageName());
	}
	
	static public int getResourceId(Class<?> rClass, String innerClassName, String fieldName) {
		return getInt(getDeclaredClass(rClass, innerClassName), fieldName);
	}
	
	static public int getResourceId(String innerClassName, String fieldName) {
		return getResourceId(getPackageRClass(), innerClassName, fieldName);
	}
	
	static public int getResourceId(String encFieldName) {
		if(!encFieldName.startsWith("@")) {
			throw new RUtilException("RUtil.getInt exception. Syntax error for: " + encFieldName);
		}
		Class<?> rClass = null;
		String packageName = null;
		String innerClassName = null;
		String fieldName = null;
		int index1 = encFieldName.indexOf(":");
		if(index1 == -1) {
			rClass = getPackageRClass();
			index1 = 0;
		} else {
			packageName = encFieldName.substring(1, index1);
			rClass = getRClass(packageName);
		}
		int index2 = encFieldName.indexOf("/");
		if(index2 == -1) {
			throw new RUtilException("RUtil.getInt exception. Syntax error for: " + encFieldName);
		} else {
			innerClassName = encFieldName.substring(index1+1, index2);
			fieldName = encFieldName.substring(index2+1);
		}
		return getResourceId(rClass, innerClassName, fieldName);
	}
}
