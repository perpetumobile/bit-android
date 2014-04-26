package com.perpetumobile.bit.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.perpetumobile.bit.android.util.FileUtil;
import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.http.HttpResponseDocument;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.TaskCallback;
import com.perpetumobile.bit.util.Util;

abstract public class BatchHttpRequest {
	static private Logger logger = new Logger(BatchHttpRequest.class);
	
	private ArrayList<String> requestList = null;
	private HashMap<String, String> requestMap = null;
	private ArrayList<HttpResponseDocument> docList = null;
	
	private String httpRequestClassName = null;
	
	private String threadPoolManagerConfigName = null;
	
	private boolean isRequestPending = false;
	private Object lock = new Object();
	
	public BatchHttpRequest(String httpRequestClassName, String threadPoolManagerConfigName) {
		this.httpRequestClassName = httpRequestClassName;
		this.threadPoolManagerConfigName = threadPoolManagerConfigName;
	}
	
	@SuppressWarnings("unchecked")
	public void request(String requestURLs) {
		boolean doRequest = false;
		
		synchronized(lock) {
			// don't request again from server if previous request pending
			// don't hold the lock to queue up the requests
			// if request is already pending release the lock and skip the request
			if(!isRequestPending && !Util.nullOrEmptyString(requestURLs)) {
				// member variables are managed in synchronized section
				docList = new ArrayList<HttpResponseDocument>();
				requestList = new ArrayList<String>();
				requestMap = new HashMap<String, String>();
				String[] requests = requestURLs.split(",");
				for(String r : requests) {
					r = r.trim();
					requestList.add(r);
					requestMap.put(r, "1");
				}
				isRequestPending = true;
				doRequest = true;
			}
		}
		
		if(doRequest) {
			for(String url : requestList) {
				HttpRequest httpRequest = null;
				if(!Util.nullOrEmptyString(httpRequestClassName)) {
					try {
						Class<? extends HttpRequest> httpRequestClass = (Class<? extends HttpRequest>)Class.forName(httpRequestClassName);
						httpRequest = httpRequestClass.newInstance();
					} catch (Exception e) {
						logger.error("BatchHttpRequest.request cannot instantiate " + httpRequestClassName);
						httpRequest = new HttpRequest();
					}
				} else {
					httpRequest = new HttpRequest();
				}
				httpRequest.setUrl(url);
				HttpManager.getInstance().execute(callback, httpRequest, threadPoolManagerConfigName);
			}
		}
	}
	
	private TaskCallback<HttpTask> callback = new TaskCallback<HttpTask>() {
		@Override
		public void onTaskDone(HttpTask task) {
			if(task != null && task.isSuccess()) {
				HttpResponseDocument result = task.getResult();
				synchronized(lock) {
					if(isRequestPending) {
						if(requestMap.containsKey(result.getSourceUrl())) {
							docList.add(result);
						}
						if(docList.size() == requestList.size()) {
							onResponse(docList);
							isRequestPending = false;
						}
					}
				}
			}
		}
	};
	
	abstract protected void onResponse(ArrayList<HttpResponseDocument> docList);
	
	protected boolean save(String directoryPath, ArrayList<HttpResponseDocument> docList) {
		if(Util.nullOrEmptyList(docList)) {
			return false;
		}
			
		// all documents need to be valid
		for(HttpResponseDocument doc : docList) {
			if(!(doc != null && doc.getStatusCode() == 200 && !Util.nullOrEmptyString(doc.getPageSource()))) {
				return false;
			}
		}
		
		// delete directory
		FileUtil.deleteDir(directoryPath);
		// re-create directory
		File dir = FileUtil.getDir(directoryPath);
		
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
					logger.error("BatchHttpRequest.save exception.", e);
				}
			}
		}
		
		return true;
	}
}