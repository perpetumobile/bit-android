package com.perpetumobile.bit.android.clients;

import com.perpetumobile.bit.android.handlers.WebViewActivityHandler;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class BitWebChromeClient extends WebChromeClient {

	protected WebViewActivityHandler webView = null;
	protected ProgressBar progressBar = null;

	public BitWebChromeClient(WebViewActivityHandler webView, ProgressBar progressBar) {
		this.webView = webView;
		this.progressBar = progressBar;
	}

	public String getConfigName() {
		return webView.getConfigName();
	}
	
	@Override
	public void onProgressChanged(WebView view, int progress) {
		if(progressBar != null) {
			progressBar.setProgress(progress);
		}
		super.onProgressChanged(view, progress);
	}
}
