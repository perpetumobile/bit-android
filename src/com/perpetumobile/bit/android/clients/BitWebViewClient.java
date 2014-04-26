package com.perpetumobile.bit.android.clients;

import com.perpetumobile.bit.android.fragments.WebViewFragment;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;

import android.net.Uri;
import android.net.http.SslError;
import android.view.KeyEvent;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BitWebViewClient extends WebViewClient {
	static final public String CONFIG_NAME = "Default";
	
	static final public String WEB_VIEW_EVENT_PROTOCOL_CONFIG_KEY = "BitWebViewClient.WebView.Event.Protocol";
	static final public String WEB_VIEW_EVENT_PROTOCOL_DEFAULT = "";
	
	static final public String ON_RECEIVED_SSL_ERROR_PROCEED_ENABLE_CONFIG_KEY = "BitWebViewClient.OnReceivedSslError.Proceed.Enable";
	
	protected WebViewFragment webViewFragment = null;
	
	public BitWebViewClient(WebViewFragment webViewFragment) {
		this.webViewFragment = webViewFragment;
	}
	
	public String getConfigName() {
		return webViewFragment.getConfigName();
	}
	
	public String getWebViewEventProtocol() {
		return Config.getInstance().getClassProperty(getConfigName(), WEB_VIEW_EVENT_PROTOCOL_CONFIG_KEY, WEB_VIEW_EVENT_PROTOCOL_DEFAULT);
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		String webViewEventProtocol = getWebViewEventProtocol();
		if(!Util.nullOrEmptyString(webViewEventProtocol) && url.startsWith(webViewEventProtocol)) {
			webViewFragment.onWebViewEvent(url);
			return true;
		}
		webViewFragment.showProgressBar();
		webViewFragment.getActivity().setTitle(Uri.parse(url).getHost());
		return false;
	}
	
	@Override
	public void onLoadResource(WebView view, String url) {
		// TODO Auto-generated method stub
		super.onLoadResource(view, url);
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
		webViewFragment.hideProgressBar();
		webViewFragment.getActivity().setTitle(view.getTitle());
		CookieSyncManager.getInstance().sync();
		super.onPageFinished(view, url);
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		if(Config.getInstance().getBooleanClassProperty(getConfigName(), ON_RECEIVED_SSL_ERROR_PROCEED_ENABLE_CONFIG_KEY, false)) {
			handler.proceed();
		} else {	
			super.onReceivedSslError(view, handler, error);
		}
	}
}
