package com.perpetumobile.bit.config;

import java.io.BufferedOutputStream;
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
	
	final static public String CONFIG_SERVER_URLS_KEY = "ConfigServerManager.URLs";
	final static public String CONFIG_SERVER_HTTP_REQUEST_CLASS_KEY = "ConfigServerManager.HttpRequest.Class";
	final static public String CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX = "ConfigServerManager.INTENT_ACTION_SUFFIX";

	final static public String CONFIG_SERVER_TIMER_CANCEL_KEY = "ConfigServerManager.Timer.Cancel";
	
	private String[] requestList = null;
	private ArrayList<HttpResponseDocument> docList = null;
	private boolean isRequestPending = false;

	private Object lock = new Object();
	
	private ConfigServerManager() {
		HttpManager.getInstance().registerReceiver(broadcastReceiver, CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX);
	}
	
	@SuppressWarnings("unchecked")
	protected void requestServerConfig() {
		synchronized(lock) {
			// don't request config from server if previous request pending
			if(!isRequestPending) {
				String urls = Config.getInstance().getProperty(CONFIG_SERVER_URLS_KEY, null);
				if(!Util.nullOrEmptyString(urls)) {
					docList = new ArrayList<HttpResponseDocument>();
					requestList = urls.split(",");
					String httpRequestClassName = Config.getInstance().getProperty(CONFIG_SERVER_HTTP_REQUEST_CLASS_KEY, null);
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
						HttpManager.getInstance().execute(httpRequest, CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX);
					}
					isRequestPending = true;
				}
			}
		}
	}
	
	private BroadcastReceiver broadcastReceiver = new BitBroadcastReceiver() {
		@Override
		public void onHttpManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, HttpResponseDocument result) {
			if(intentActionSuffix != null && intentActionSuffix.equals(CONFIG_SERVER_MANAGER_INTENT_ACTION_SUFFIX)) {
				synchronized(lock) {
					docList.add(result);
					if(docList.size() == requestList.length) {
						if(writeConfig(context)) {
							Config.getInstance().reset(Config.getInstance().getBooleanProperty(CONFIG_SERVER_TIMER_CANCEL_KEY, false));
						}
						isRequestPending = false;
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
		
		// clean previous config files
		String[] fileList = context.fileList();
		for(String file : fileList) {
			if(file.endsWith(".config.txt") && !file.equals(Config.CONFIG_LOCAL_FILE)) {
				context.deleteFile(file);
			}
		}
		
		for(HttpResponseDocument doc : docList) {
			String url = doc.getSourceUrl();
			int index = url.lastIndexOf("/");
			if(index != -1) {
				String fileName = url.substring(index+1); 
				try {
					BufferedOutputStream out = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
					out.write(doc.getPageSource().getBytes());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
}
