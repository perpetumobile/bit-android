package com.perpetumobile.bit.config;

import java.io.File;
import java.util.ArrayList;
import java.util.TimerTask;

import android.content.Context;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.TimerManager;
import com.perpetumobile.bit.util.Util;


/**
 *
 * @author  Zoran Dukic
 */
final public class Config {
	static private Config instance = new Config();
	static public Config getInstance() { return instance; }
	
	final static public String CONFIG_PROPERTIES_DIRECTORY_NAME = "properties";
	static public File getConfigPropertiesDir() {
		Context context  = DataSingleton.getInstance().getAppContext();
		File dir = context.getDir(CONFIG_PROPERTIES_DIRECTORY_NAME, Context.MODE_PRIVATE);
		File versionDir = dir;
		String version = DataSingleton.getInstance().getAppVersion();
		if(!Util.nullOrEmptyString(version)) {
			versionDir = new File(dir, version);
			if(!versionDir.exists()) {
				versionDir.mkdir();
			}
		}
		return versionDir;
	}
	
	final static public String CONFIG_LOCAL_FILE = "local.config.txt";
	final static public String CONFIG_ROOT_FILE = "root.config.txt";
	
	private ConfigProperties properties = null;
	
	private ArrayList<ConfigSubscriber> subscriberList = new ArrayList<ConfigSubscriber>();
	
	private Config() {
		cleanConfigPropertiesDirectory();
		reset(false);
		
		// schedule the reset task
		long delay = getLongProperty("Config.Reset.Delay", 5000);
		long period = getLongProperty("Config.Reset.Period", 60*60000);
		TimerManager.getInstance().schedule("Config.Reset", new ResetTask(), delay, period);
		
		// cannot call requestServerConfig() here since need to finish creating Config instance
		// it will be called on timer
		// requestServerConfig();
	}
	
	/**
	 * Reloads properties.
	 */
	protected void reset(boolean cancelTimer) {
		ConfigProperties tmpProperties = null;
		
		try {
			// load server config properties
			tmpProperties = new ConfigProperties(CONFIG_ROOT_FILE);
		} catch (ConfigPropertiesException e1) {
			// load asset config properties if server config properties failed 
			try {
				tmpProperties = new AssetConfigProperties(CONFIG_ROOT_FILE);
			} catch (ConfigPropertiesException e2) {
				tmpProperties = new AssetConfigProperties();
			}
		}
		try {
			// load local properties if available
			ConfigProperties localProperties = new LocalConfigProperties(CONFIG_LOCAL_FILE);
			tmpProperties.putAll(localProperties);
		} catch (ConfigPropertiesException e) {	
		} 
		
		// is this thread safe?
		properties = tmpProperties;
		
		if(cancelTimer) {
			TimerManager.getInstance().cancel("Config.Reset");
		}
		
		publish();
	}
	
	protected void cleanConfigPropertiesDirectory() {
		Context context  = DataSingleton.getInstance().getAppContext();
		String version = DataSingleton.getInstance().getAppVersion();
		File dir = context.getDir(CONFIG_PROPERTIES_DIRECTORY_NAME, Context.MODE_PRIVATE);
		File[] list = dir.listFiles();
		for(File file : list) {
			if(file.isDirectory() && !file.getName().equals(version)) {
				Util.deleteDirectory(file);
			}
		}
	}
	
	/**
	 * Request config from server.
	 */
	protected void requestServerConfig() {
		ConfigServerManager.getInstance().requestServerConfig();
	}
	
	public void subscribe(ConfigSubscriber subscriber) {
		subscriberList.add(subscriber);		
	}
	
	protected void publish() {
		for(ConfigSubscriber subscriber : subscriberList) {
			subscriber.configReset();
		}
	}
	
	final private String getPropertyImpl(String key) {
		return properties.getProperty(key, null);
	}
	
	/**
	 * Returns a property value for a given key from the local properties.
	 * Returns a property value for a given key from the global properties.
	 * Returns defaultValue otherwise.
	 */
	public String getProperty(String key, String defaultValue) {
		String result = getPropertyImpl(key);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public int getIntProperty(String key, int defaultValue) {
		return Util.toInt(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public long getLongProperty(String key, long defaultValue) {
		return Util.toLong(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public float getFloatProperty(String key, float defaultValue) {
		return Util.toFloat(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public double getDoubleProperty(String key, double defaultValue) {
		return Util.toDouble(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		return Util.toBoolean(getPropertyImpl(key), defaultValue);
	}
	
	final private String getClassPropertyImpl(String classKey, String key) {
		return properties.getClassProperty(classKey, key, null);
	}
	
	/**
	 * Returns a property value for a given classKey.key from the local properties.
	 * Returns a property value for a given key from the local properties.
	 * Returns a property value for a given classKey.key from the global properties.
	 * Returns a property value for a given key from the global properties.
	 * Returns defaultValue otherwise.
	 */
	public String getClassProperty(String classKey, String key, String defaultValue) {
		String result = getClassPropertyImpl(classKey, key);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public int getIntClassProperty(String classKey,	String key, int defaultValue) {
		return Util.toInt(getClassPropertyImpl(classKey, key),	defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public long getLongClassProperty(String classKey, String key, long defaultValue) {
		return Util.toLong(getClassPropertyImpl(classKey, key), defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public float getFloatClassProperty(String classKey,	String key, float defaultValue) {
		return Util.toFloat(getClassPropertyImpl(classKey, key), defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public double getDoubleClassProperty(String classKey, String key, double defaultValue) {
		return Util.toDouble(getClassPropertyImpl(classKey, key),	defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public boolean getBooleanClassProperty(String classKey, String key, boolean defaultValue) {
		return Util.toBoolean(getClassPropertyImpl(classKey, key), defaultValue);
	}
	
	/**
	 * Returns a list of ConfigFormOption items.
	 */
	public ArrayList<ConfigFormOption> getOptionArray(String configName) {
		ArrayList<ConfigFormOption> result = new ArrayList<ConfigFormOption>();
		int num = Config.getInstance().getIntProperty(configName+".Num", 100);
		for(int i=1; i<=num; i++) {
			StringBuffer buf = new StringBuffer();
			buf.append(i);
			buf.append(".");
			buf.append(configName);
			ConfigFormOption option = new ConfigFormOption(buf.toString());
			if(option.isValid()) {
				result.add(option);
			} else {
				break;
			}
			
		}
		return result;
	}
	
	public class ResetTask extends TimerTask {
		public void run() {
			requestServerConfig();
		}
	}
}