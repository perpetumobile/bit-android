package com.perpetumobile.bit.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.perpetumobile.bit.android.BitBroadcastReceiver;
import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.http.HttpResponseDocument;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

public class ConfigServerManager {
	static private ConfigServerManager instance = new ConfigServerManager();
	static public ConfigServerManager getInstance() { return instance; }

	static private Logger logger = new Logger(ConfigServerManager.class);
	
	final static public String CONFIG_SERVER_MANAGER_THREAD_POOL_NAME = "ConfigService";
	
	final static public String CONFIG_SERVER_MANAGER_ENABLE_KEY = "ConfigServerManager.Enable";
	final static public String CONFIG_SERVER_MANAGER_URLS_KEY = "ConfigServerManager.URLs";
	final static public String CONFIG_SERVER_MANAGER_HTTP_REQUEST_CLASS_KEY = "ConfigServerManager.HttpRequest.Class";
	final static public String CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX = "ConfigServerManager.INTENT_ACTION_SUFFIX";

	final static public String CONFIG_SERVER_MANAGER_TIMER_CANCEL_KEY = "ConfigServerManager.Timer.Cancel";
	
	private String[] requestList = null;
	private ArrayList<HttpResponseDocument> docList = null;
	private boolean isRequestPending = false;

	private Object lock = new Object();
	
	private ConfigServerManager() {
		HttpManager.getInstance().registerReceiver(broadcastReceiver, CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX);
	}
	
	@SuppressWarnings("unchecked")
	protected void requestServerConfig() {
		boolean doRequest = false;
		
		synchronized(lock) {
			// don't request config from server if previous request pending
			// don't hold the lock to queue up the requests
			// if request is already pending release the lock and skip the request
			if(!isRequestPending && Config.getInstance().getBooleanProperty(CONFIG_SERVER_MANAGER_ENABLE_KEY, false)) {
				String urls = Config.getInstance().getProperty(CONFIG_SERVER_MANAGER_URLS_KEY, null);
				if(!Util.nullOrEmptyString(urls)) {
					// member variables are managed in synchronized section
					docList = new ArrayList<HttpResponseDocument>();
					requestList = urls.split(",");
					isRequestPending = true;
					doRequest = true;
				}
			}
		}
		
		if(doRequest) {
			String httpRequestClassName = Config.getInstance().getProperty(CONFIG_SERVER_MANAGER_HTTP_REQUEST_CLASS_KEY, null);
			for(String url : requestList) {
				HttpRequest httpRequest = null;
				if(!Util.nullOrEmptyString(httpRequestClassName)) {
					try {
						Class<? extends HttpRequest> httpRequestClass = (Class<? extends HttpRequest>)Class.forName(httpRequestClassName);
						httpRequest = httpRequestClass.newInstance();
					} catch (Exception e) {
						logger.error("ConfigServerManager.requestServerConfig cannot instantiate " + httpRequestClassName);
						httpRequest = new HttpRequest();
					}
				} else {
					httpRequest = new HttpRequest();
				}
				httpRequest.setUrl(url);
				HttpManager.getInstance().execute(httpRequest, CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX, CONFIG_SERVER_MANAGER_THREAD_POOL_NAME);
			}
		}
	}
	
	private BroadcastReceiver broadcastReceiver = new BitBroadcastReceiver() {
		@Override
		public void onHttpManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, HttpResponseDocument result) {
			if(intentActionSuffix != null && intentActionSuffix.equals(CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX)) {
				synchronized(lock) {
					if(isRequestPending) {
						docList.add(result);
						if(docList.size() == requestList.length) {
							if(writeConfig(context)) {
								Config.getInstance().reset(Config.getInstance().getBooleanProperty(CONFIG_SERVER_MANAGER_TIMER_CANCEL_KEY, false));
							}
							isRequestPending = false;
						}
					}
				}
			}
		}
	};
	
	private boolean writeConfig(Context context) {
		if(Util.nullOrEmptyList(docList)) {
			return false;
		}
			
		// all documents need to be valid
		for(HttpResponseDocument doc : docList) {
			if(!(doc != null && doc.getStatusCode() == 200 && !Util.nullOrEmptyString(doc.getPageSource()))) {
				return false;
			}
		}
		
		File dir = Config.getConfigPropertiesDir();
		Util.deleteDirectory(dir);
		// re-create directory
		dir = Config.getConfigPropertiesDir();
		
		for(HttpResponseDocument doc : docList) {
			String url = doc.getSourceUrl();
			int index = url.lastIndexOf("/");
			if(index != -1) {
				String fileName = url.substring(index+1); 
				try {
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(dir, fileName)));
					out.write(doc.getPageSource().getBytes());
					out.close();
				} catch (IOException e) {
					logger.error("ConfigServerManager.writeConfig exception.", e);
				}
			}
		}
		
		return true;
	}
}
