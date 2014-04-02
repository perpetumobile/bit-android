package com.perpetumobile.bit.android.fragments;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.R;
import com.perpetumobile.bit.android.clients.BitWebChromeClient;
import com.perpetumobile.bit.android.clients.BitWebViewClient;
import com.perpetumobile.bit.config.Config;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewFragment extends Fragment {
	static final public String CONFIG_NAME = "Default";

	static final public String PROTOCOL_CONFIG_KEY = "WebViewActivityHandler.Protocol";
	static final public String HOST_CONFIG_KEY = "WebViewActivityHandler.Host";
	static final public String URL_EXTRA_CONFIG_KEY = "WebViewActivityHandler.URL.Extra";

	static final public String PROTOCOL_DEFAULT = "http://";
	static final public String HOST_DEFAULT = "www.perpetumobile.com";
	static final public String URL_EXTRA_DEFAULT = "";

	protected String configName;
	protected ProgressBar progressBar;
	protected WebView webView;

	public WebViewFragment() {
		this(CONFIG_NAME);
	}

	public WebViewFragment(String configName) {
		this.configName = configName;
	}

	public String getConfigName() {
		return configName;
	}

	protected BitActivity getBitActivity() {
		Activity a = getActivity();
		if (a != null && a instanceof BitActivity) {
			return (BitActivity)a;  
		}
		return null;
	}
	
	protected WebViewClient createWebViewClient() {
		return new BitWebViewClient(this);
	}
	
	protected WebChromeClient createWebChromeClient() {
		return new BitWebChromeClient(this);
	}
	
	/////////////////////////////////
	// life cycle methods
	/////////////////////////////////
	@Override
	@SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (webView != null) {
			webView.destroy();
		}
        View layout = inflater.inflate(R.layout.webview_fragment, container, false);
        webView = (WebView)layout.findViewById(R.id.web_view);
        progressBar = (ProgressBar)layout.findViewById(R.id.progress_bar);
        if(webView != null) {
			webView.setWebViewClient(createWebViewClient());
			webView.setWebChromeClient(createWebChromeClient());
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
		}
        return layout;
	}
		
	@Override
	public void onPause() {
		super.onPause();
		webView.onPause();
	}
	@Override
	public void onResume() {
		webView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroyView() {
		if (webView != null) {
			webView.destroy();
			webView = null;
		}
		super.onDestroyView();
	}

	public WebBackForwardList saveState(Bundle outState) {
		if(webView != null) return webView.saveState(outState);
		return null;
	}

	public WebBackForwardList restoreState(Bundle inState) {
		if(webView != null) return webView.restoreState(inState);
		return null;
	}
	
	/////////////////////////////////
	// web view methods
	/////////////////////////////////
	public void loadUrl(String url) {
		if(webView != null) {
			showProgressBar();
			webView.loadUrl(url);
		}
	}

	public void reload() {
		if(webView != null) {
			showProgressBar();
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

	/////////////////////////////////
	// progress bar methods
	/////////////////////////////////
	public void showProgressBar() {
		if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		if(progressBar != null) progressBar.setVisibility(View.GONE);
	}

	public void setProgress(int progress) {
		if(progressBar != null) progressBar.setProgress(progress);
	}

	/////////////////////////////////
	// bit protocol/activity methods
	/////////////////////////////////
	public String getProtocol() {
		return Config.getInstance().getClassProperty(configName, PROTOCOL_CONFIG_KEY, PROTOCOL_DEFAULT);
	}

	public String getHost() {
		return Config.getInstance().getClassProperty(configName, HOST_CONFIG_KEY, HOST_DEFAULT);
	}

	public String getUrlExtra() {
		return Config.getInstance().getClassProperty(configName, URL_EXTRA_CONFIG_KEY, URL_EXTRA_DEFAULT);
	}

	public void onWebViewEvent(String url) {
		BitActivity a = getBitActivity();
		if(a != null) a.onWebViewEvent(url);
	}
	
	public boolean isSearchableActivityStartEnabled() {
		BitActivity a = getBitActivity();
		if(a != null) return a.isSearchableActivityStartEnabled();
		return false;
	}
	
	public void setQuery(String query) {
		BitActivity a = getBitActivity();
		if(a != null) a.setQuery(query);
	}
}
