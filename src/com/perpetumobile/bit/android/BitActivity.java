package com.perpetumobile.bit.android;

import com.perpetumobile.bit.android.handlers.DrawerActivityHandler;
import com.perpetumobile.bit.android.handlers.SearchViewActivityHandler;
import com.perpetumobile.bit.android.handlers.WebViewActivityHandler;
import com.perpetumobile.bit.config.Config;

import android.app.Activity;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class BitActivity extends Activity {

	static final public String ACTIVITY_EXTRA = "com.perpetumobile.bit.android.ACTIVITY_EXTRA";
	static final public String ACTIVITY_URL = "com.perpetumobile.bit.android.ACTIVITY_URL";
	
	static final public String SEARCHABLE_ACTIVITY_START_ENABLED_CONFIG_KEY = "BitActivity.SearchableActivity.Start.Enabled";	
	static final public boolean SEARCHABLE_ACTIVITY_START_ENABLED_DEFAULT = false;
	
	protected DrawerActivityHandler drawer = null;
	protected SearchViewActivityHandler searchView = null;
	protected WebViewActivityHandler webView = null;
	
	public boolean isSearchableActivityStartEnabled() {
		return webView != null && Config.getInstance().getBooleanProperty(SEARCHABLE_ACTIVITY_START_ENABLED_CONFIG_KEY, SEARCHABLE_ACTIVITY_START_ENABLED_DEFAULT);
	}
	
	protected void setDrawer(Bundle savedInstanceState, DrawerActivityHandler drawerActivityHandler, 
			int drawerLayoutId, int drawerListId, int drawerListItemId, 
			int drawerTitleArrayId, int drawerActivityArrayId, int drawerActivityExtraArrayId, int drawerWebViewUrlArrayId,
			int drawerOpenStrId, int drawerCloseStrId, 
			int drawerIconId, int drawerShadowId) {

		drawer = drawerActivityHandler;
		if(drawer != null) {
			drawer.onCreate(savedInstanceState, 
				drawerLayoutId, drawerListId, drawerListItemId, 
				drawerTitleArrayId, drawerActivityArrayId, drawerActivityExtraArrayId, drawerWebViewUrlArrayId,
				drawerOpenStrId, drawerCloseStrId, 
				drawerIconId, drawerShadowId);
		}
}
	
	protected void setSearchView(Menu menu, SearchViewActivityHandler searchViewActivityHandler, int searchViewId) {
		searchView = searchViewActivityHandler;
		if(searchView != null) {
			String pkg = getApplicationContext().getPackageName();
			searchView.onCreateOptionsMenu(menu, searchViewId, new ComponentName(pkg, pkg + ".SearchableActivity"));
		}
	}
	
	protected void setWebView(Bundle savedInstanceState, WebViewActivityHandler webViewActivityHandler, int webViewId, int progressBarId) {
		webView = webViewActivityHandler;
		if(webView != null) webView.onCreate(savedInstanceState, webViewId, progressBarId);
	}

	protected void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState);
		DataSingleton.getInstance().setAssetManager(getAssets());
		DataSingleton.getInstance().setAppContext(getApplicationContext());
		setContentView(layoutId);
		overridePendingTransition(android.R.anim.fade_in , android.R.anim.fade_out);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(drawer != null) drawer.onPostCreate(savedInstanceState);
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if(drawer != null) drawer.onPrepareOptionsMenu(menu);
		if(searchView != null) searchView.onPrepareOptionsMenu(menu);
		return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(drawer != null) drawer.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(webView != null) webView.restoreState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(webView != null) webView.saveState(outState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView != null && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		// If it wasn't the Back key or there's no web page history, bubble up to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(drawer != null && drawer.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Called when the user clicks the Settings action button */
	public void openSettings() {
	}

	@Override
	public void setTitle(CharSequence title) {
		getActionBar().setTitle(title);
	}

	public void setQuery(String query) {
		DataSingleton.getInstance().put(DataSingleton.QUERY_KEY, query);
		invalidateOptionsMenu();
	}

	public void loadUrl(String url) {
		if(webView != null) webView.loadUrl(url);
	}

	public void reload() {
		if(webView != null)	webView.reload();
	}
	
	public void onWebViewEvent(String url) {
	}
}
