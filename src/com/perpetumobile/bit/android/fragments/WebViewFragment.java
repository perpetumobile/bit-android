package com.perpetumobile.bit.android.fragments;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.R;
import com.perpetumobile.bit.android.clients.BitWebChromeClient;
import com.perpetumobile.bit.android.clients.BitWebViewClient;
import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewFragment extends BitFragment {

	static final public String LAYOUT_ID_CONFIG_KEY = "WebViewFragment.Layout.Id";
	static final public String LAYOUT_ID_DEFAULT = "@layout/webview_fragment";
	
	static final public String WEB_VIEW_ID_CONFIG_KEY = "WebViewFragment.WebView.Id";
	static final public String WEB_VIEW_ID_DEFAULT = "@id/web_view";
	
	static final public String PROGRESS_BAR_ID_CONFIG_KEY = "WebViewFragment.ProgressBar.Id";
	static final public String PROGRESS_BAR_ID_DEFAULT = "@id/progress_bar";
	
	static final public String PROTOCOL_CONFIG_KEY = "WebViewActivityHandler.Protocol";
	static final public String HOST_CONFIG_KEY = "WebViewActivityHandler.Host";
	static final public String URL_EXTRA_CONFIG_KEY = "WebViewActivityHandler.URL.Extra";

	static final public String PROTOCOL_DEFAULT = "http://";
	static final public String HOST_DEFAULT = "www.perpetumobile.com";
	static final public String URL_EXTRA_DEFAULT = "";

	protected String webViewId;
	protected String progressBarId;
	
	protected ProgressBar progressBar;
	protected WebView webView;

	public WebViewFragment() {
	}

	public WebViewFragment(String configName) {
		super(configName);
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
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);		
		TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.WebViewFragment);
		webViewId = a.getString(R.styleable.WebViewFragment_web_view_id);
		progressBarId = a.getString(R.styleable.WebViewFragment_progress_bar_id);
		a.recycle();
	}
	
	@Override
	@SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (webView != null) {
			webView.destroy();
		}
		
		if(Util.nullOrEmptyString(layoutId)) {
			layoutId = Config.getInstance().getClassProperty(configName, LAYOUT_ID_CONFIG_KEY, LAYOUT_ID_DEFAULT);
		}
		layout = inflater.inflate(RUtil.getResourceId(layoutId), container, false);
		
		if(Util.nullOrEmptyString(webViewId)) {
			webViewId = Config.getInstance().getClassProperty(configName, WEB_VIEW_ID_CONFIG_KEY, WEB_VIEW_ID_DEFAULT);
		}
        webView = (WebView)layout.findViewById(RUtil.getResourceId(webViewId));
        
        if(Util.nullOrEmptyString(progressBarId)) {
			progressBarId = Config.getInstance().getClassProperty(configName, PROGRESS_BAR_ID_CONFIG_KEY, PROGRESS_BAR_ID_DEFAULT);
		}
        progressBar = (ProgressBar)layout.findViewById(RUtil.getResourceId("id", "progress_bar"));
        
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
