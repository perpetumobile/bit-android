package com.perpetumobile.bit.android.clients;

import com.perpetumobile.bit.android.fragments.WebViewFragment;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class BitWebChromeClient extends WebChromeClient {

	protected WebViewFragment webViewFragment = null;

	public BitWebChromeClient(WebViewFragment webViewFragment) {
		this.webViewFragment = webViewFragment;
	}

	public String getConfigName() {
		return webViewFragment.getConfigName();
	}
	
	@Override
	public void onProgressChanged(WebView view, int progress) {
		webViewFragment.setProgress(progress);
		super.onProgressChanged(view, progress);
	}
}
