package com.perpetumobile.bit.http;

import com.perpetumobile.bit.util.Task;

/**
 * @author Zoran Dukic
 *
 */
public class HttpTask extends Task {
	
	protected HttpRequest httpRequest = new HttpRequest(); 
	protected String intentActionSuffix = null;
	
	protected HttpResponseDocument result = null;
	
	public HttpTask() {
	}
	
	@Override
	public void runImpl() {
		result = HttpManager.getInstance().executeImpl(httpRequest);
	}
	
	@Override
	public void run() {
		super.run();
		HttpManager.getInstance().sendBroadcast(intentActionSuffix, result);
	}

	public HttpResponseDocument getResult() {
		return result;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setIntentActionSuffix(String intentActionSuffix) {
		this.intentActionSuffix = intentActionSuffix;
	}
}
