package com.perpetumobile.bit.android.handlers;

import com.perpetumobile.bit.android.BitActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DrawerActivityHandler {
	
	static final public String DRAWER_POSITION = "com.thefind.android.DRAWER_POSITION";
	
	protected BitActivity activity;
	
	protected DrawerLayout drawerLayout;
	protected ListView drawerListView;
	protected ActionBarDrawerToggle drawerToggle;

	protected CharSequence drawerTitle;
	protected CharSequence title;

	protected String[] drawerTitles;
	protected String[] drawerActivities;
	protected String[] drawerActivityExtras;
	protected String[] drawerWebViewUrls;

	public DrawerActivityHandler(BitActivity activity) {
		this.activity = activity;
	}
	
	public BitActivity getActivity() {
		return activity;
	}
	
	public void onCreate(Bundle savedInstanceState, 
			int drawerLayoutId, int drawerListId, int drawerListItemId, 
			int drawerTitleArrayId, int drawerActivityArrayId, int drawerActivityExtraArrayId, int drawerWebViewUrlArrayId,
			int drawerOpenStrId, int drawerCloseStrId, 
			int drawerIconId, int drawerShadowId) {
		
		drawerLayout = (DrawerLayout) activity.findViewById(drawerLayoutId);
		drawerListView = (ListView) activity.findViewById(drawerListId);
		
		title = drawerTitle = activity.getTitle();
		
		drawerTitles = activity.getResources().getStringArray(drawerTitleArrayId);
		drawerActivities = activity.getResources().getStringArray(drawerActivityArrayId);
		drawerActivityExtras = activity.getResources().getStringArray(drawerActivityExtraArrayId);
		drawerWebViewUrls = activity.getResources().getStringArray(drawerWebViewUrlArrayId);

		// set a custom shadow that overlays the main content when the drawer opens
		drawerLayout.setDrawerShadow(drawerShadowId, GravityCompat.START);
				
		// set up the drawer's list view with items and click listener
		drawerListView.setAdapter(new ArrayAdapter<String>(activity, drawerListItemId, drawerTitles));
		drawerListView.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		drawerToggle = new ActionBarDrawerToggle(
				activity,				/* host Activity */
				drawerLayout,			/* DrawerLayout object */
				drawerIconId,			/* nav drawer image to replace 'Up' caret */
				drawerOpenStrId,	/* "open drawer" description for accessibility */
				drawerCloseStrId	/* "close drawer" description for accessibility */
				) {

			public void onDrawerClosed(View view) {
				activity.getActionBar().setTitle(title);
				activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				activity.getActionBar().setTitle(drawerTitle);
				activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
		
		Intent intent = activity.getIntent();
		if(intent != null) {
			int position = intent.getIntExtra(DRAWER_POSITION, 0);
			markItem(position);
		}
	}
	
	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	public void onPostCreate(Bundle savedInstanceState) {
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		// Pass any configuration change to the drawer toggle
		drawerToggle.onConfigurationChanged(newConfig);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerListView);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}
	
	public void setTitle(CharSequence title) {
		this.title = title;
		activity.setTitle(title);
	}
	
	protected void markItem(int position) {
		// update selected item and title, then close the drawer
		drawerListView.setItemChecked(position, true);
		setTitle(drawerTitles[position]);
		drawerLayout.closeDrawer(drawerListView);
	}
	
	@SuppressWarnings("unchecked")
	protected void selectItem(int position) throws ClassNotFoundException {
		if(drawerActivities[position] != null && !drawerActivities[position].equals("")) {
			Class<? extends Activity> activityClass = (Class<? extends Activity>)Class.forName(activity.getApplicationContext().getPackageName() + drawerActivities[position]);
			Intent intent = new Intent(activity, activityClass);
			intent.putExtra(BitActivity.ACTIVITY_EXTRA, drawerActivityExtras[position]);
			intent.putExtra(BitActivity.ACTIVITY_URL, drawerWebViewUrls[position]);
			intent.putExtra(DRAWER_POSITION, position);
			activity.startActivity(intent);
		} else {
			activity.setQuery("");
			activity.loadUrl(drawerWebViewUrls[position]);
		}
		markItem(position);
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				selectItem(position);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
