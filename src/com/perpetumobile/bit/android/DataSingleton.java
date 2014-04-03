package com.perpetumobile.bit.android;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.perpetumobile.bit.util.Logger;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;

public class DataSingleton {
	static private DataSingleton instance = new DataSingleton();
	static public DataSingleton getInstance() { return instance; }
	
	static private Logger logger = new Logger(DataSingleton.class);
	
	static final public String BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME = "BitService";
	
	static final public String QUERY_KEY = "QUERY";
	
	protected Context appContext;
	protected AssetManager assetManager;
	
	// general purpose object map
	protected HashMap<String, Object> map = new HashMap<String, Object>();
	
	// map for passing objects via intent locally
	protected HashMap<String, WeakReference<Object>> intentExtraMap = new HashMap<String, WeakReference<Object>>(); 
		
	private DataSingleton() {
	}
	
	public Context getAppContext() {
		return appContext;
	}

	public void setAppContext(Context appContext) {
		this.appContext = appContext;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}
	
	public String getAppVersion() {
		String result = null;
		try {
			Context context = getAppContext();
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			result  = pInfo.versionName;
		} catch (Exception e) {
			logger.error("DataSingleton.getAppVersion exception", e);
		}
		return (result != null ? result : "");
	}

	public Object get(String key) {
		synchronized(map) {
			return map.get(key);
		}
	}
	
	public void put(String key, Object value) {
		synchronized(map) {
			map.put(key, value);
		}
	}
	
	public void remove(String key) {
		synchronized(map) {
			map.remove(key);
		}
	}
	
	public Object getIntentExtra(String key) {
		synchronized(intentExtraMap) {
			WeakReference<Object> value = intentExtraMap.get(key); 
			if(value != null) {
				return value.get();
			}
		}
		return null;
	}
	
	public void putIntentExtra(String key, Object value) {
		synchronized(intentExtraMap) {
			intentExtraMap.put(key, new WeakReference<Object>(value));
		}
	}
	
	public void removeIntentExtra(String key) {
		synchronized(intentExtraMap) {
			intentExtraMap.remove(key);
		}
	}
}
