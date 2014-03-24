package com.perpetumobile.bit.android.clients;

import com.perpetumobile.bit.android.handlers.WebViewActivityHandler;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;

import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class BitWebViewClient extends WebViewClient {
	static final public String CONFIG_NAME = "Default";
	
	static final public String WEB_VIEW_EVENT_PROTOCOL_CONFIG_KEY = "BitWebViewClient.WebView.Event.Protocol";
	static final public String WEB_VIEW_EVENT_PROTOCOL_DEFAULT = "";
	
	protected WebViewActivityHandler webView = null;
	protected ProgressBar progressBar = null;
	
	public BitWebViewClient(WebViewActivityHandler webView, ProgressBar progressBar) {
		this.webView = webView;
		this.progressBar = progressBar;
	}
	
	public String getConfigName() {
		return webView.getConfigName();
	}
	
	public String getWebViewEventProtocol() {
		return Config.getInstance().getClassProperty(getConfigName(), WEB_VIEW_EVENT_PROTOCOL_CONFIG_KEY, WEB_VIEW_EVENT_PROTOCOL_DEFAULT);
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		String webViewEventProtocol = getWebViewEventProtocol();
		if(!Util.nullOrEmptyString(webViewEventProtocol) && url.startsWith(webViewEventProtocol)) {
			webView.getActivity().onWebViewEvent(url);
			return true;
		}
		if(progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
		}
		webView.getActivity().setTitle(Uri.parse(url).getHost());
		return false;
	}

	@Override
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		// TODO: This doesn't seem to be called by framework, maybe activity needs to call it

		// Check if the key event was the Back button and if there's history
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && view.canGoBack()) {
			view.goBack();
			return false;
		}
		// If it wasn't the Back key or there's no web page history, bubble up to the default
		// system behavior (probably exit the activity)
		return true;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		if(progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
		webView.getActivity().setTitle(view.getTitle());
		CookieSyncManager.getInstance().sync();
		super.onPageFinished(view, url);
	}
}
