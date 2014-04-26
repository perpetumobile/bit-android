package com.perpetumobile.bit.android.fragments.drawer;

import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.orm.json.JSONRecord;

public class DrawerItemField extends JSONRecord {
	
	public DrawerItemField() {
	}

	public int getId() {
		return RUtil.getResourceId(getFieldValue("id"));
	}
	
	public String getType() {
		return getFieldValue("type");
	}
	
	public String getValue() {
		return getFieldValue("value");
	}
}
