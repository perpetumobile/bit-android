package com.perpetumobile.bit.android.fragments;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.R;
import com.perpetumobile.bit.android.fragments.drawer.Drawer;
import com.perpetumobile.bit.android.fragments.drawer.DrawerArrayAdapter;
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;


enum DrawerFragmentTaskAction {
	ADD_DRAWER_ITEM,
	REMOVE_DRAWER_ITEM,
	ADD_DRAWER_ITEMS,
	REMOVE_DRAWER_ITEMS,
	ADD_HEADER_VIEW,
	REMOVE_HEADER_VIEW,
	ADD_FOOTER_VIEW,
	REMOVE_FOOTER_VIEW;
}

class DrawerFragmentTask {
	
	DrawerFragmentTaskAction action;
	
	DrawerItem item;
	
	ArrayList<DrawerItem> items;
	
	// Header/Footer fields
	View view;
	Object data;
	boolean isSelectable = true;
	
	DrawerFragmentTask(DrawerFragmentTaskAction action, DrawerItem item) {
		this.action = action;
		this.item = item;
	}
	
	DrawerFragmentTask(DrawerFragmentTaskAction action, ArrayList<DrawerItem> items) {
		this.action = action;
		this.items = items;
	}
	
	DrawerFragmentTask(DrawerFragmentTaskAction action, View view) {
		this.action = action;
		this.view = view;
	}
	
	DrawerFragmentTask(DrawerFragmentTaskAction action, View view, Object data, boolean isSelectable) {
		this.action = action;
		this.view = view;
		this.data = data;
		this.isSelectable = isSelectable;
	}
}

public class DrawerFragment extends BitFragment {
	static private Logger logger = new Logger(DrawerFragment.class);
	
	static final public String FILE_NAME_CONFIG_KEY = "DrawerFragment.FileName";
	static final public String FILE_NAME_DEFAULT = "drawer.json";
	
	static final public String LAYOUT_ID_CONFIG_KEY = "DrawerFragment.Layout.Id";
	static final public String LAYOUT_ID_DEFAULT = "@layout/drawer_fragment";
	
	static final public String LIST_VIEW_ID_CONFIG_KEY = "DrawerFragment.ListView.Id";
	static final public String LIST_VIEW_ID_DEFAULT = "@id/drawer_list_view";
	
	protected String fileName;
	
	protected Handler handler;
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
	
	static public DrawerItem createDrawerItem(String drawerItemJSON) {
		DrawerItem result = null;
		Drawer drawer = null;
		try {
			String drawerJSON = "{\"items\": [\n" + drawerItemJSON + "\n]}";
			drawer = (Drawer)JSONParserManager.getInstance().parseImpl(new StringReader(drawerJSON), "JSONDrawer");
			result = drawer.getDrawerItem(0);
		} catch (Exception e) {
			logger.error("DrawerFragment.readDrawerItemImpl exception.", e);
		}
		return result;
	}
	
	static public DrawerItem readDrawerItem(String drawerItemFileName) {
		String drawerItemJSON = null;
		try {
			drawerItemJSON = FileUtil.readAssetFile("properties/"+drawerItemFileName).toString();
		} catch (IOException e) {
			logger.error("DrawerFragment.readDrawerItem exception.", e);
		}
		return createDrawerItem(drawerItemJSON);
	}
	
	public DrawerItem getDrawerItem(String id) {
		return drawer.getDrawerItem(id);
	}
	
	public ArrayList<DrawerItem> getDrawerItems(String idStartsWith) {
		return drawer.getDrawerItems(idStartsWith);
	}
	
	protected DrawerArrayAdapter getDrawerArrayAdapter() {
		Adapter a = drawerListView.getAdapter();
		if(a instanceof WrapperListAdapter) {
			return (DrawerArrayAdapter)((WrapperListAdapter)a).getWrappedAdapter();
		}
		return (DrawerArrayAdapter)a;
	}
	
	/////////////////////////////////
	// life cycle methods
	/////////////////////////////////
	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);		
		TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.DrawerFragment);
		fileName = a.getString(R.styleable.DrawerFragment_file_name);
		a.recycle();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				DrawerFragmentTask task = (DrawerFragmentTask)msg.obj; 
				switch(task.action) {
				case ADD_DRAWER_ITEM:
					getDrawerArrayAdapter().add(task.item);
					break;
				case REMOVE_DRAWER_ITEM:
					getDrawerArrayAdapter().remove(task.item);
					break;
				case ADD_DRAWER_ITEMS:
					for(DrawerItem item : task.items) { 
						getDrawerArrayAdapter().add(item);
					}
					break;
				case REMOVE_DRAWER_ITEMS:
					for(DrawerItem item : task.items) {
						getDrawerArrayAdapter().remove(item);
					}
					break;	
				case ADD_HEADER_VIEW:
					drawerListView.addHeaderView(task.view, task.data, task.isSelectable);
					break;
				case REMOVE_HEADER_VIEW:
					drawerListView.removeHeaderView(task.view);
					break;
				case ADD_FOOTER_VIEW:
					drawerListView.addFooterView(task.view, task.data, task.isSelectable);
					break;
				case REMOVE_FOOTER_VIEW:
					drawerListView.removeFooterView(task.view);
					break;
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		Activity activity = getActivity();
		
		String layoutId = Config.getInstance().getClassProperty(configName, LAYOUT_ID_CONFIG_KEY, LAYOUT_ID_DEFAULT);
		layout = inflater.inflate(RUtil.getResourceId(layoutId), container, false);
        
		String listViewId = Config.getInstance().getClassProperty(configName, LIST_VIEW_ID_CONFIG_KEY, LIST_VIEW_ID_DEFAULT);
		drawerListView = (ListView)layout.findViewById(RUtil.getResourceId(listViewId));
		
		title = drawerTitle = activity.getTitle();
		
		drawer = readDrawer();
		
		// set up the drawer's list view with items and click listener
		drawerListView.setAdapter(new DrawerArrayAdapter(activity, drawer.getDrawerItems()));
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

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		drawerToggle = new ActionBarDrawerToggle(
				activity,				/* host Activity */
				parentLayout,			/* DrawerLayout object */
				// drawer.getIconId(),			/* nav drawer image to replace 'Up' caret */
				drawer.getOpenTextId(),	/* "open drawer" description for accessibility */
				drawer.getCloseTextId()	/* "close drawer" description for accessibility */
				) {
			
			@Override
			public void onDrawerClosed(View view) {
				getActivity().getActionBar().setTitle(title);
				getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActivity().getActionBar().setTitle(drawerTitle);
				getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		parentLayout.setDrawerListener(drawerToggle);	
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getActionBar().setHomeButtonEnabled(true);
		
		// If drawer starts new activity, position will be passed as an intent extra
		// mark the drawer using the position
		Intent intent = activity.getIntent();
		if(intent != null) {
			int position = intent.getIntExtra(BitActivity.DRAWER_POSITION, -1);
			if(position != -1) {
				markItem(position);
			}
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

	@Override
	public void onDestroyView() {
		parentLayout.setDrawerListener(null);
		drawerListView.setAdapter(null);
		parentLayout = null;
		drawerListView = null;
		super.onDestroyView();
	}

	/////////////////////////////////
	// list view methods
	/////////////////////////////////
	public void addHeaderView(View v) {
		addHeaderView(v, null, true);
	}
	
	public void addHeaderView(View v, Object data, boolean isSelectable) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.ADD_HEADER_VIEW, v, data, isSelectable));
		msg.sendToTarget();
	}
	
	public void removeHeaderView(View v) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.REMOVE_HEADER_VIEW, v));
		msg.sendToTarget();
	}
	
	public void addFooterView(View v) {
		addFooterView(v, null, true);
	}
	
	public void addFooterView(View v, Object data, boolean isSelectable) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.ADD_FOOTER_VIEW, v, data, isSelectable));
		msg.sendToTarget();
	}
	
	public void removeFooterView(View v) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.REMOVE_FOOTER_VIEW, v));
		msg.sendToTarget();
	}
	
	public void addDrawerItem(DrawerItem item) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.ADD_DRAWER_ITEM, item));
		msg.sendToTarget();
	}
	
	public void removeDrawerItem(DrawerItem item) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.REMOVE_DRAWER_ITEM, item));
		msg.sendToTarget();
	}
	
	public void addDrawerItems(ArrayList<DrawerItem> items) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.ADD_DRAWER_ITEMS, items));
		msg.sendToTarget();
	}
	
	public void removeDrawerItems(ArrayList<DrawerItem> items) {
		Message msg = handler.obtainMessage(0, new DrawerFragmentTask(DrawerFragmentTaskAction.REMOVE_DRAWER_ITEMS, items));
		msg.sendToTarget();
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DrawerItem item = (DrawerItem)parent.getAdapter().getItem(position);
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
	}
	
	/////////////////////////////////
	// bit protocol/activity methods
	/////////////////////////////////
	public void setTitle(CharSequence title) {
		this.title = title;
		getActivity().setTitle(title);
	}
	
	protected void markItem(int position) {
		DrawerItem item = (DrawerItem)drawerListView.getAdapter().getItem(position);
		if(item != null) {
			// update selected item and title, then close the drawer
			drawerListView.setItemChecked(position, true);
			setTitle(item.getTitle());
			parentLayout.closeDrawer(layout);
		}
	}
}
