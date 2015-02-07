package com.perpetumobile.bit.android;

import com.perpetumobile.bit.android.fragments.DrawerFragment;
import com.perpetumobile.bit.android.fragments.WebViewFragment;
import com.perpetumobile.bit.android.handlers.SearchViewActivityHandler;
import com.perpetumobile.bit.config.Config;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieSyncManager;

public class BitActivity extends Activity {

	static final public String ACTIVITY_EXTRA = "com.perpetumobile.bit.android.ACTIVITY_EXTRA";
	static final public String ACTIVITY_URL = "com.perpetumobile.bit.android.ACTIVITY_URL";
	
	static final public String DRAWER_POSITION = "com.perpetumobile.bit.android.DRAWER_POSITION";
	
	static final public String SEARCHABLE_ACTIVITY_START_ENABLED_CONFIG_KEY = "BitActivity.SearchableActivity.Start.Enabled";	
	static final public boolean SEARCHABLE_ACTIVITY_START_ENABLED_DEFAULT = false;
	
	protected DrawerFragment drawerFragment = null;
	protected SearchViewActivityHandler searchView = null;
	protected WebViewFragment webViewFragment = null;
	
	public boolean isSearchableActivityStartEnabled() {
		return webViewFragment != null && Config.getInstance().getBooleanProperty(SEARCHABLE_ACTIVITY_START_ENABLED_CONFIG_KEY, SEARCHABLE_ACTIVITY_START_ENABLED_DEFAULT);
	}
		
	protected void setDrawerFragment(Bundle savedInstanceState, int drawerFragmentId) {
		FragmentManager fragmentManager = getFragmentManager();
		drawerFragment = (DrawerFragment)fragmentManager.findFragmentById(drawerFragmentId);
	}
	
	protected void setSearchView(Menu menu, SearchViewActivityHandler searchViewActivityHandler, int searchViewId) {
		searchView = searchViewActivityHandler;
		if(searchView != null) {
			String pkg = getApplicationContext().getPackageName();
			searchView.onCreateOptionsMenu(menu, searchViewId, new ComponentName(pkg, pkg + ".SearchableActivity"));
		}
	}
	
	protected void setWebViewFragment(Bundle savedInstanceState, int webViewFragmentId) {
		FragmentManager fragmentManager = getFragmentManager();
		webViewFragment = (WebViewFragment)fragmentManager.findFragmentById(webViewFragmentId);
	}

	protected void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState);
		DataSingleton.getInstance().setAssetManager(getAssets());
		DataSingleton.getInstance().setAppContext(getApplicationContext());
		CookieSyncManager.createInstance(getApplicationContext());
		Config.getInstance();
		setContentView(layoutId);
		overridePendingTransition(android.R.anim.fade_in , android.R.anim.fade_out);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(drawerFragment != null) drawerFragment.onPostCreate(savedInstanceState);
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// now called by activity automatically 
		// if(drawerFragment != null) drawerFragment.onPrepareOptionsMenu(menu);
		if(searchView != null) searchView.onPrepareOptionsMenu(menu);
		return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(drawerFragment != null) drawerFragment.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(webViewFragment != null) webViewFragment.restoreState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(webViewFragment != null) webViewFragment.saveState(outState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webViewFragment != null && webViewFragment.canGoBack()) {
			webViewFragment.goBack();
			return true;
		}
		// If it wasn't the Back key or there's no web page history, bubble up to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(drawerFragment != null && drawerFragment.onOptionsItemSelected(item)) {
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
		if(webViewFragment != null) webViewFragment.loadUrl(url);
	}

	public void reload() {
		if(webViewFragment != null)	webViewFragment.reload();
	}
	
	public void onWebViewEvent(String url) {
	}
}
