package com.perpetumobile.bit.android.fragments.drawer;

import java.util.EnumSet;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.perpetumobile.bit.android.util.RUtil;
import com.perpetumobile.bit.orm.json.JSONRecord;

enum DrawerItemFieldType {
	TEXT("TEXT"),
	IMAGE("IMAGE");
	
	private static final HashMap<String, DrawerItemFieldType> map = new HashMap<String, DrawerItemFieldType>();
	static {
		for(DrawerItemFieldType rt : EnumSet.allOf(DrawerItemFieldType.class))
			map.put(rt.getType(), rt);
	}

	private String type;

	private DrawerItemFieldType(String type) {
		this.type = type.toUpperCase();
	}

	public String getType(){
		return type; 
	}

	static public DrawerItemFieldType get(String type) { 
		return map.get(type.toUpperCase()); 
	}
}

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
	
	public void setView(View view) {
		DrawerItemFieldType type = DrawerItemFieldType.get(getType());		
		if(type == DrawerItemFieldType.TEXT) {
			TextView textView = (TextView)view.findViewById(getId());
			if(textView != null) {
				String value = getValue();
				if(value != null && value.startsWith("@")) {
					value = RUtil.getResources().getString(RUtil.getResourceId(value));
				}
				textView.setText(value);
			}
		}
		if(type == DrawerItemFieldType.IMAGE) {
			ImageView imageView = (ImageView)view.findViewById(getId());
			if(imageView != null) {
				Drawable drawable = RUtil.getResources().getDrawable(RUtil.getResourceId(getValue()));
				imageView.setImageDrawable(drawable);
			}
		}
	}
}
