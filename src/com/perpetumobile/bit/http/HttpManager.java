package com.perpetumobile.bit.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.CookieSyncManager;

import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.TaskCallback;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class HttpManager {
	static private HttpManager instance = new HttpManager();
	static public HttpManager getInstance(){ return instance; }

	static private Logger logger = new Logger(HttpManager.class);
	
	static final public String NEW_LINE = System.getProperty("line.separator");
	
	private HttpManager() {
		CookieHandler.setDefault(new WebkitCookieManager());
	}
	
	protected void readResponse(HttpURLConnection c, HttpResponseDocument result) throws IOException {	
		int code = c.getResponseCode();
		result.setStatusCode(code);
		result.setContentType(c.getContentType());
		result.setContentLenght(c.getContentLength());
		result.setHeaderFields(c.getHeaderFields());
		
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
	
	protected void readImageResponse(HttpURLConnection c, HttpResponseDocument result) throws IOException {	
		int code = c.getResponseCode();
		if(code != 200) {
			readResponse(c, result);
		} else {
			result.setStatusCode(code);
			result.setContentType(c.getContentType());
			result.setContentLenght(c.getContentLength());
			result.setHeaderFields(c.getHeaderFields());
			
			BufferedInputStream in = new BufferedInputStream(c.getInputStream());
			Bitmap bitmap = BitmapFactory.decodeStream(in);		
			result.setBitmap(bitmap);
			
			in.close();
			
			// sync cookies
			CookieSyncManager.getInstance().sync();
		}
	}

	protected HttpResponseDocument getImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = new HttpResponseDocument(httpRequest.getUrl());
		HttpURLConnection c = null;
		try {
			URL u = new URL(httpRequest.getUrl());
			c = (HttpURLConnection) u.openConnection();
			httpRequest.prepareConnection(c);
			
			// read response
			if(httpRequest.method == HttpMethod.GET_IMAGE) { 
				readImageResponse(c, result);
			} else {
				readResponse(c, result);
			}
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
				c.setDoOutput(true);
				c.setChunkedStreamingMode(0);
				c.setRequestProperty("content-type", httpRequest.getContentType());
				BufferedOutputStream out = new BufferedOutputStream(c.getOutputStream());
				out.write(httpRequest.getContent().getBytes());
				out.close();
			}
			
			// read response
			if(httpRequest.method == HttpMethod.POST_IMAGE) { 
				readImageResponse(c, result);
			} else {
				readResponse(c, result);
			}
		} catch (Exception e) {
			logger.error("HttpManager.postImpl exception", e);
		} finally {
			if(c != null) c.disconnect();
		}
		return result;
	}
	
	public HttpResponseDocument executeImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = null;
		switch(httpRequest.method) {
			case GET:
				result = getImpl(httpRequest);
				break;
			case POST:
				result = postImpl(httpRequest);
				break;
			case GET_IMAGE:
				result = getImpl(httpRequest);
				break;
			case POST_IMAGE:
				result = postImpl(httpRequest);
				break;
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
				ThreadPoolManager.getInstance().run(ThreadPoolManager.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
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
	 */
	public void execute(TaskCallback<HttpTask> callback, HttpRequest httpRequest) {
		execute(callback, httpRequest, null);
	}
	
	/**
	 * Operation is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void execute(TaskCallback<HttpTask> callback, HttpRequest httpRequest, String threadPoolManagerConfigName) {
		HttpTask task = new HttpTask();
		task.setHttpRequest(httpRequest);
		task.setCallback(callback);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(ThreadPoolManager.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}	
		} catch (Exception e) {
			logger.error("HttpManager.execute exception", e);
		}
	}
}
