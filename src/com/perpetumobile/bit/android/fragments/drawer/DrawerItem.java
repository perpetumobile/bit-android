package com.perpetumobile.bit.android.fragments.drawer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.orm.json.JSONRecord;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


public class DrawerItem extends JSONRecord {
	static private Logger logger = new Logger(DrawerItem.class);
	
	public DrawerItem() {
	}

	public String getTitle() {
		String title = getFieldValue("title");
		if(title != null && title.startsWith("@")) {
			title = RUtil.getResources().getString(RUtil.getResourceId(title));
		} 
		return title;
	}
	
	public int getLayoutId() {
		return RUtil.getResourceId(getFieldValue("layout"));
	}
	
	public String getActivityClassName() {
		return getFieldValue("activity");
	}
	
	public String getActivityExtra() {
		return getFieldValue("activity_extra");
	}
	
	public String getWebViewUrl() {
		return getFieldValue("webview_url");
	}
	
	public boolean isSelectable() {
		return getIntFieldValue("is_selectable") == 1;
	}
	
	@SuppressWarnings("unchecked")
	public boolean startActivity(Activity activity, int position) {
		String activityClassName = getActivityClassName();
		if(!Util.nullOrEmptyString(activityClassName)) {
			try {		
				Class<? extends Activity> activityClass = (Class<? extends Activity>)Class.forName(activity.getApplicationContext().getPackageName() + activityClassName);
				Intent intent = new Intent(activity, activityClass);
				intent.putExtra(BitActivity.ACTIVITY_EXTRA, getActivityExtra());
				intent.putExtra(BitActivity.ACTIVITY_URL, getWebViewUrl());
				intent.putExtra(BitActivity.DRAWER_POSITION, position);
				activity.startActivity(intent);
				return true;
			} catch (ClassNotFoundException e) {
				logger.error("DrawerItem.startActivity exception for activity class name: " + activityClassName, e);
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<DrawerItemField> getItemFields() {
		ArrayList<? extends JSONRecord> list = new ArrayList<DrawerItemField>();
		getJSONRecords("JSONDrawer", "items.fields", (ArrayList<JSONRecord>)list);
    	if(!Util.nullOrEmptyList(list)) {
    		return (ArrayList<DrawerItemField>)list; 
    	}	
		return null;
	}
	
	public void setView(View view) {
		ArrayList<DrawerItemField> fields = getItemFields();
		for(DrawerItemField f : fields) {
			f.setView(view);
		}
	}
}
