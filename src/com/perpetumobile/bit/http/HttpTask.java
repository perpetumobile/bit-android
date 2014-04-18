package com.perpetumobile.bit.http;

import com.perpetumobile.bit.util.Task;

/**
 * @author Zoran Dukic
 *
 */
public class HttpTask extends Task {
	
	protected HttpRequest httpRequest = new HttpRequest(); 
	
	protected HttpResponseDocument result = null;
	
	public HttpTask() {
	}
	
	@Override
	public void runImpl() {
		result = HttpManager.getInstance().executeImpl(httpRequest);
	}

	public HttpResponseDocument getResult() {
		return result;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
}
