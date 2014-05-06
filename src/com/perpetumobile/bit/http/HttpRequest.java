package com.perpetumobile.bit.http;

import java.net.HttpURLConnection;

/**
 * @author Zoran Dukic
 *
 */
public class HttpRequest {
	
	protected HttpMethod method = HttpMethod.GET;
	protected String url = null;
	protected String content = null; 
	
	public HttpRequest() {
	}
	
	public HttpRequest(String url) {
		this.url = url;
	}
	
	public HttpRequest(HttpMethod method, String url) {
		this.url = url;
		this.method = method;
	}
	
	public HttpRequest(HttpMethod method, String url, String content) {
		this.url = url;
		this.method = method;
		this.content = content;
	}
	
	public void prepareConnection(HttpURLConnection connection) {
	}
	
	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
