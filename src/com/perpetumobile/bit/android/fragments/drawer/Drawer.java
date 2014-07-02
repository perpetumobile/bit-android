package com.perpetumobile.bit.android.fragments.drawer;

import java.util.ArrayList;

import android.app.Activity;

import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.orm.json.JSONRecord;
import com.perpetumobile.bit.util.Util;

public class Drawer extends JSONRecord {
	private static final long serialVersionUID = 1L;
	
	public Drawer() {
	}

	public int getLayoutId() {
		return RUtil.getResourceId(getFieldValue("layout"));
	}

	public int getShadowId() {
		return RUtil.getResourceId(getFieldValue("shadow"));
	}
	
	public int getIconId() {
		return RUtil.getResourceId(getFieldValue("icon"));
	}
	
	public int getOpenTextId() {
		return RUtil.getResourceId(getFieldValue("open_text"));
	}
	
	public int getCloseTextId() {
		return RUtil.getResourceId(getFieldValue("close_text"));
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<DrawerItem> getDrawerItems() {
		ArrayList<? extends JSONRecord> list = getJSONRecords("JSONDrawer", "items");
    	if(!Util.nullOrEmptyList(list)) {
    		return (ArrayList<DrawerItem>)list; 
    	}	
		return null;
	}
	
	public DrawerItem getDrawerItem(int position) {
		ArrayList<DrawerItem> list = getDrawerItems();
		if(!Util.nullOrEmptyList(list)) {
			return list.get(position);
		}
		return null;
	}
	
	public DrawerItem getDrawerItem(String id) {
		ArrayList<DrawerItem> list = getDrawerItems();
		for(DrawerItem item : list) {
			if(id.equals(item.getId())) {
				return item;
			}
		}
		return null;
	}
	
	public ArrayList<DrawerItem> getDrawerItems(String idStartsWith) {
		ArrayList<DrawerItem> result = new ArrayList<DrawerItem>();
		ArrayList<DrawerItem> list = getDrawerItems();
		for(DrawerItem item : list) {
			String itemId = item.getId(); 
			if(itemId != null && itemId.startsWith(idStartsWith)) {
				result.add(item);
			}
		}
		return result;
	}
	
	public boolean startActivity(Activity activity, int position) {
		DrawerItem item = getDrawerItem(position);
		if(item !=null) {
			return item.startActivity(activity, position);
		}
		return false;
	}
}
