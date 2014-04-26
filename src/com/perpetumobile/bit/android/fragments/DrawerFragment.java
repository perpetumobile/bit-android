package com.perpetumobile.bit.android.fragments;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.R;
import com.perpetumobile.bit.android.fragments.drawer.Drawer;
import com.perpetumobile.bit.android.fragments.drawer.DrawerItem;
import com.perpetumobile.bit.android.util.FileUtil;
import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.json.JSONParserManager;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DrawerFragment extends BitFragment {
	static private Logger logger = new Logger(DrawerFragment.class);
	
	static final public String FILE_NAME_CONFIG_KEY = "DrawerFragment.FileName";
	static final public String FILE_NAME_DEFAULT = "drawer.json";
	
	static final public String LAYOUT_ID_CONFIG_KEY = "DrawerFragment.Layout.Id";
	static final public String LAYOUT_ID_DEFAULT = "@layout/drawer_fragment";
	
	static final public String LIST_VIEW_ID_CONFIG_KEY = "DrawerFragment.ListView.Id";
	static final public String LIST_VIEW_ID_DEFAULT = "@id/drawer_list_view";
	
	protected String fileName;
	protected String listViewId;
	
	protected DrawerLayout parentLayout;
	
	protected ListView drawerListView;
	protected ActionBarDrawerToggle drawerToggle;
	
	protected CharSequence drawerTitle;
	protected CharSequence title;
	
	protected Drawer drawer = null;
	
	public DrawerFragment() {
	}

	public DrawerFragment(String configName) {
		super(configName);
	}
	
	protected Drawer readDrawer() {
		Drawer result = null;
		try {
			if(Util.nullOrEmptyString(fileName)) {
				fileName = Config.getInstance().getClassProperty(configName, FILE_NAME_CONFIG_KEY, FILE_NAME_DEFAULT);
			}
			result = (Drawer)JSONParserManager.getInstance().parseImpl(FileUtil.getAssetFileReader("properties/"+fileName), "JSONDrawer");
		} catch (Exception e) {
			logger.error("DrawerFragment.readDrawer exception.", e);
		}
		return result;
	}
	
	/////////////////////////////////
	// life cycle methods
	/////////////////////////////////
	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);		
		TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.DrawerFragment);
		fileName = a.getString(R.styleable.DrawerFragment_file_name);
		listViewId = a.getString(R.styleable.DrawerFragment_list_view_id);
		a.recycle();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		Activity activity = getActivity();
		
		if(Util.nullOrEmptyString(layoutId)) {
			layoutId = Config.getInstance().getClassProperty(configName, LAYOUT_ID_CONFIG_KEY, LAYOUT_ID_DEFAULT);
		}
		layout = inflater.inflate(RUtil.getResourceId(layoutId), container, false);
        
		if(Util.nullOrEmptyString(listViewId)) {
			listViewId = Config.getInstance().getClassProperty(configName, LIST_VIEW_ID_CONFIG_KEY, LIST_VIEW_ID_DEFAULT);
		}
		drawerListView = (ListView)layout.findViewById(RUtil.getResourceId(listViewId));
		
		title = drawerTitle = activity.getTitle();
		
		drawer = readDrawer();
				
		// set up the drawer's list view with items and click listener
		drawerListView.setAdapter(new ArrayAdapter<String>(activity, drawer.getLayoutId(), drawer.getTitles()));
		drawerListView.setOnItemClickListener(new DrawerItemClickListener());
		
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Activity activity = getActivity();
		
		parentLayout = (DrawerLayout)activity.findViewById(RUtil.getResourceId("id", "drawer_layout"));
		// set a custom shadow that overlays the main content when the drawer opens
		parentLayout.setDrawerShadow(drawer.getShadowId(), GravityCompat.START);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		drawerToggle = new ActionBarDrawerToggle(
				activity,				/* host Activity */
				parentLayout,			/* DrawerLayout object */
				drawer.getIconId(),			/* nav drawer image to replace 'Up' caret */
				drawer.getOpenTextId(),	/* "open drawer" description for accessibility */
				drawer.getCloseTextId()	/* "close drawer" description for accessibility */
				) {

			public void onDrawerClosed(View view) {
				getActivity().getActionBar().setTitle(title);
				getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActivity().getActionBar().setTitle(drawerTitle);
				getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		parentLayout.setDrawerListener(drawerToggle);	
		
		Intent intent = activity.getIntent();
		if(intent != null) {
			int position = intent.getIntExtra(BitActivity.DRAWER_POSITION, 0);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Pass any configuration change to the drawer toggle
		drawerToggle.onConfigurationChanged(newConfig);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerListView);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}

	/////////////////////////////////
	// list view methods
	/////////////////////////////////
	


	/////////////////////////////////
	// bit protocol/activity methods
	/////////////////////////////////
	
	public void setTitle(CharSequence title) {
		this.title = title;
		getActivity().setTitle(title);
	}
	
	protected void markItem(int position) {
		DrawerItem item = drawer.getDrawerItem(position);
		if(item != null) {
			// update selected item and title, then close the drawer
			drawerListView.setItemChecked(position, true);
			setTitle(item.getTitle());
			parentLayout.closeDrawer(layout);
		}
	}
	
	protected void selectItem(int position) throws ClassNotFoundException {
		DrawerItem item = drawer.getDrawerItem(position);
		if(item != null) {
			if(!item.startActivity(getActivity(), position)) {
				BitActivity ba = getBitActivity();
				if(ba != null) {
					ba.setQuery("");
					ba.loadUrl(item.getWebViewUrl());
				}
			}
			markItem(position);
		}
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				selectItem(position);
			} catch (ClassNotFoundException e) {
				logger.error("DrawerItemClickListener.onItemClick exception." , e);
			}
		}
	}
}
