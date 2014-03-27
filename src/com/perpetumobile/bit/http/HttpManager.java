package com.perpetumobile.bit.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;

import android.webkit.CookieSyncManager;

import com.perpetumobile.bit.android.BitBroadcastManager;
import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class HttpManager extends BitBroadcastManager {
	static private HttpManager instance = new HttpManager();
	static public HttpManager getInstance(){ return instance; }

	static private Logger logger = new Logger(HttpManager.class);
	
	static final public String NEW_LINE = System.getProperty("line.separator");

	static final public String HTTP_MANAGER_INTENT_ACTION_PREFIX = "com.perpetumobile.bit.http.HTTP_MANAGER_INTENT_ACTION";
	
	private HttpManager() {
		super(HTTP_MANAGER_INTENT_ACTION_PREFIX);
		CookieHandler.setDefault(new WebkitCookieManager());
	}
	
	protected void readResponse(HttpURLConnection c, HttpResponseDocument result) throws IOException {	
		int code = c.getResponseCode();
		result.setStatusCode(code);
		result.setContentLenght(c.getContentLength());
		
		InputStream stream = null;
		if(code < 400) {
			stream = c.getInputStream();
		} else {
			stream = c.getErrorStream();
		}
		
		// read stream 
		StringBuilder buf = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String inputLine;
		while ((inputLine = in.readLine()) != null) { 
			buf.append(inputLine);
			buf.append(NEW_LINE);
		}
		result.setPageSource(buf.toString());
		
		in.close();
		
		// sync cookies
		CookieSyncManager.getInstance().sync();
	}

	protected HttpResponseDocument getImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = new HttpResponseDocument(httpRequest.getUrl());
		HttpURLConnection c = null;
		try {
			URL u = new URL(httpRequest.getUrl());
			c = (HttpURLConnection) u.openConnection();
			httpRequest.prepareConnection(c);
			// read response
			readResponse(c, result);
		} catch (Exception e) {
			logger.error("HttpManager.getImpl exception", e);
		} finally {
			if(c != null) c.disconnect();
		}
		return result;
	}
	
	protected HttpResponseDocument postImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = new HttpResponseDocument(httpRequest.getUrl());
		HttpURLConnection c = null;
		try {
			URL u = new URL(httpRequest.getUrl());
			c = (HttpURLConnection) u.openConnection();
			httpRequest.prepareConnection(c);
			c.setRequestMethod("POST");
			// write content
			if(!Util.nullOrEmptyString(httpRequest.getContent())) {
				c.setDoInput(true);
				c.setChunkedStreamingMode(0);
				BufferedOutputStream out = new BufferedOutputStream(c.getOutputStream());
				out.write(httpRequest.getContent().getBytes());
				out.close();
			}
			// read response
			readResponse(c, result);
		} catch (Exception e) {
			logger.error("HttpManager.postImpl exception", e);
		} finally {
			if(c != null) c.disconnect();
		}
		return result;
	}
	
	public HttpResponseDocument executeImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = null;
		if(httpRequest.method == HttpMethod.GET) {
			result = getImpl(httpRequest);
		} else if(httpRequest.method == HttpMethod.POST) {
			result = postImpl(httpRequest);
		}
		return result;
	}
	
	/**
	 * Operation is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public HttpResponseDocument executeSync(HttpRequest httpRequest) {
		return executeSync(httpRequest, null);
	}
	
	/**
	 * Operation is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public HttpResponseDocument executeSync(HttpRequest httpRequest, String threadPoolManagerConfigName) {
		HttpTask task = new HttpTask();
		task.setHttpRequest(httpRequest);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			task.isDone();
		} catch (Exception e) {
			logger.error("HttpManager.execute exception", e);
		}
		return task.getResult();
	}
	
	/**
	 * Operation is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 */
	public void execute(HttpRequest httpRequest, String intentActionSuffix) {
		execute(httpRequest, intentActionSuffix, null);
	}
	
	/**
	 * Operation is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 */
	public void execute(HttpRequest httpRequest, String intentActionSuffix, String threadPoolManagerConfigName) {
		// registerReceiver must not be called multiple times
		// client needs to explicitly register using registerReceiver method
		// registerReceiver(broadcastReceiver, httpRequest.getUrl());
		HttpTask task = new HttpTask();
		task.setHttpRequest(httpRequest);
		if(!Util.nullOrEmptyString(intentActionSuffix)) {
			task.setIntentActionSuffix(intentActionSuffix);
		} else {
			task.setIntentActionSuffix(httpRequest.getUrl());
		}
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}	
		} catch (Exception e) {
			logger.error("HttpManager.execute exception", e);
		}
	}
}
