package com.perpetumobile.bit.android.handlers;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.clients.BitWebChromeClient;
import com.perpetumobile.bit.android.clients.BitWebViewClient;
import com.perpetumobile.bit.config.Config;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class WebViewActivityHandler {
	static final public String CONFIG_NAME = "Default";
	
	static final public String PROTOCOL_CONFIG_KEY = "WebViewActivityHandler.Protocol";
	static final public String HOST_CONFIG_KEY = "WebViewActivityHandler.Host";
	static final public String URL_EXTRA_CONFIG_KEY = "WebViewActivityHandler.URL.Extra";
	
	static final public String PROTOCOL_DEFAULT = "http://";
	static final public String HOST_DEFAULT = "www.perpetumobile.com";
	static final public String URL_EXTRA_DEFAULT = "";
	
	protected String configName;
	protected BitActivity activity;
	protected ProgressBar progressBar;
	protected WebView webView;
	
	public WebViewActivityHandler(BitActivity activity) {
		this(CONFIG_NAME, activity);
	}
	
	public WebViewActivityHandler(String configName, BitActivity activity) {
		this.configName = configName;
		this.activity = activity;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public BitActivity getActivity() {
		return activity;
	}
	
	public String getProtocol() {
		return Config.getInstance().getClassProperty(configName, PROTOCOL_CONFIG_KEY, PROTOCOL_DEFAULT);
	}
	
	public String getHost() {
		return Config.getInstance().getClassProperty(configName, HOST_CONFIG_KEY, HOST_DEFAULT);
	}
	
	public String getUrlExtra() {
		return Config.getInstance().getClassProperty(configName, URL_EXTRA_CONFIG_KEY, URL_EXTRA_DEFAULT);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState, int webViewId, int progressBarId) {
		webView = (WebView)activity.findViewById(webViewId);
		progressBar = (ProgressBar)activity.findViewById(progressBarId);
		if(webView != null) {
			webView.setWebViewClient(new BitWebViewClient(this, progressBar));
			webView.setWebChromeClient(new BitWebChromeClient(this, progressBar));
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
		}
	}
	
	public WebBackForwardList saveState(Bundle outState) {
		if(webView != null) return webView.saveState(outState);
		return null;
	}
	
	public WebBackForwardList restoreState(Bundle inState) {
		if(webView != null) return webView.restoreState(inState);
		return null;
	}
	
	public void loadUrl(String url) {
		if(webView != null) {
			if(progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
			}
			webView.loadUrl(url);
		}
	}
	
	public void reload() {
		if(webView != null) {
			if(progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
			}
			webView.reload();
		}
	}
	
	public boolean canGoBack() {
		if(webView != null) return webView.canGoBack();
		return false;
	}
	
	public void goBack() {
		if(webView != null) webView.goBack();
	}
}
