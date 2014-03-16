package com.perpetumobile.bit.http;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perpetumobile.bit.util.Util;

import android.webkit.CookieManager;

/**
 * @author Zoran Dukic
 *
 */
public class WebkitCookieManager extends CookieHandler {
	
	protected CookieManager cookieManager = CookieManager.getInstance();

	public WebkitCookieManager() {
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		if (uri != null && requestHeaders != null) {
			String url = uri.toString();		
			String cookie = cookieManager.getCookie(url);
			if(!Util.nullOrEmptyString(cookie)) {
				result.put("Cookie", Arrays.asList(cookie));
			}
		}
		return Collections.unmodifiableMap(result);
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		if(uri != null && responseHeaders != null) {	
			String url = uri.toString();
			for(String key : responseHeaders.keySet()) {
				if(key != null && key.equalsIgnoreCase("Set-Cookie")) {
					for(String val : responseHeaders.get(key)) {
						cookieManager.setCookie(url, val);
					}
				}
			}
		}
	}

}
