package com.perpetumobile.bit.android.fragments.drawer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DrawerArrayAdapter extends ArrayAdapter<DrawerItem> {
	
	public DrawerArrayAdapter(Context context, ArrayList<DrawerItem> drawerItems) {
		super(context, 0, drawerItems);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DrawerItem item = getItem(position);
		View view = inflater.inflate(item.getLayoutId(), parent, false);
		item.setView(view);	
		return view;
	}
}
