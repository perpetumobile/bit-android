package com.perpetumobile.bit.android.handlers;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.DataSingleton;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.SearchView;

public class SearchViewActivityHandler {

	protected BitActivity activity;
	protected SearchView searchView;

	public SearchViewActivityHandler(BitActivity activity) {
		this.activity = activity;
	}

	public BitActivity getActivity() {
		return activity;
	}

	public boolean onCreateOptionsMenu(Menu menu, int searchViewId, ComponentName searchable) {     
		// Get the SearchView and set the searchable configuration
		searchView = (SearchView) menu.findItem(searchViewId).getActionView();
		if(searchView != null) {
			SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE); 
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(searchable); 
			searchView.setSearchableInfo(searchableInfo);
			// Do not iconify the widget; expand it by default
			searchView.setIconifiedByDefault(false);

			Intent intent = activity.getIntent();
			if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
				activity.setQuery(intent.getStringExtra(SearchManager.QUERY));
			}
		}
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		String query = (String) DataSingleton.getInstance().get(DataSingleton.QUERY_KEY);
		if(query != null && searchView != null) {
			searchView.setQuery(query, false);
		}
		return true;
	}
}
