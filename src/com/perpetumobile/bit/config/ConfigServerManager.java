package com.perpetumobile.bit.config;

import java.util.ArrayList;

import android.content.Context;

import com.perpetumobile.bit.http.BatchHttpRequest;
import com.perpetumobile.bit.http.HttpResponseDocument;

public class ConfigServerManager {
	static private ConfigServerManager instance = new ConfigServerManager();
	static public ConfigServerManager getInstance() { return instance; }
	
	final static public String CONFIG_SERVER_MANAGER_THREAD_POOL_NAME = "ConfigService";
	
	final static public String CONFIG_SERVER_MANAGER_ENABLE_KEY = "ConfigServerManager.Enable";
	final static public String CONFIG_SERVER_MANAGER_URLS_KEY = "ConfigServerManager.URLs";
	final static public String CONFIG_SERVER_MANAGER_HTTP_REQUEST_CLASS_KEY = "ConfigServerManager.HttpRequest.Class";
	final static public String CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX = "ConfigServerManager.INTENT_ACTION_SUFFIX";

	final static public String CONFIG_SERVER_MANAGER_TIMER_CANCEL_KEY = "ConfigServerManager.Timer.Cancel";
	
	protected BatchHttpRequest batchHttpRequest = new BatchHttpRequest(Config.getInstance().getProperty(CONFIG_SERVER_MANAGER_HTTP_REQUEST_CLASS_KEY, null), 
			CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX,
			CONFIG_SERVER_MANAGER_THREAD_POOL_NAME) {
		@Override
		protected void onResponse(Context context, ArrayList<HttpResponseDocument> result) {
			if(save(Config.CONFIG_PROPERTIES_VERSION_DIRECTORY_PATH(), context, result)) {
				Config.getInstance().reset(Config.getInstance().getBooleanProperty(CONFIG_SERVER_MANAGER_TIMER_CANCEL_KEY, false));
			}
		}
	};
	
	private ConfigServerManager() {
	}
	
	protected void requestServerConfig() {
		if(Config.getInstance().getBooleanProperty(CONFIG_SERVER_MANAGER_ENABLE_KEY, false)) {
			batchHttpRequest.request(Config.getInstance().getProperty(CONFIG_SERVER_MANAGER_URLS_KEY, null));
		}
	}
}
