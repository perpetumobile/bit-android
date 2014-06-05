package com.perpetumobile.bit.http;

import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

/**
 * @author Zoran Dukic
 *
 */
public class HttpResponseDocument {
	
	protected String sourceUrl = null;
	protected String destinationUrl = null;
	protected int statusCode = -1;
	protected Map<String, List<String>> headerFields = null;
	protected String contentType = null;
	protected long contentLenght = -1;
	protected String pageSource = null;
	protected Bitmap bitmap = null;
	
	public HttpResponseDocument(String url) {
		sourceUrl = url;
		destinationUrl = url;
	}
	
	protected void reset() {
		// reset all generated fields
		statusCode = -1;
		headerFields = null;
		pageSource = null;
		bitmap = null;
		contentLenght = -1;
	}
	
	/**
	 * @return Returns the sourceUrl.
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}
	
	/**
	 * @param sourceUrl The sourceUrl to set.
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	/**
	 * @return Returns the destinationUrl.
	 */
	public String getDestinationUrl() {
		return destinationUrl;
	}
	
	/**
	 * @param destinationUrl The destinationUrl to set.
	 */
	public void setDestinationUrl(String destinationUrl) {
		this.destinationUrl = destinationUrl;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}

	public void setHeaderFields(Map<String, List<String>> headerFields) {
		this.headerFields = headerFields;
	}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the contentLenght
	 */
	public long getContentLenght() {
		return contentLenght;
	}

	/**
	 * @param contentLenght the contentLenght to set
	 */
	public void setContentLenght(long contentLenght) {
		this.contentLenght = contentLenght;
	}
	
	/**
	 * @return Returns the pageSource.
	 */
	public String getPageSource() {
		return pageSource;
	}
	
	/**
	 * @param pageSource The pageSource to set.
	 */
	public void setPageSource(String pageSource) {
		this.pageSource = pageSource;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}
