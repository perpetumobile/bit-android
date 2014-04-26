package com.perpetumobile.bit.android.fragments;

import com.perpetumobile.bit.android.BitActivity;
import com.perpetumobile.bit.android.R;
import com.perpetumobile.bit.util.Util;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

public class BitFragment extends Fragment {
	static final public String DEFAULT_CONFIG_NAME = "Default";
	
	protected String configName;
	protected String layoutId;
	
	protected View layout; 
	
	public BitFragment() {
	}

	public BitFragment(String configName) {
		this.configName = configName;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public View getLayout() {
		return layout;
	}
	
	protected BitActivity getBitActivity() {
		Activity a = getActivity();
		if (a != null && a instanceof BitActivity) {
			return (BitActivity)a;  
		}
		return null;
	}
		
	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);		
		TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.BitFragment);
		configName = a.getString(R.styleable.BitFragment_config_name);
		if(Util.nullOrEmptyString(configName)) {
			configName = DEFAULT_CONFIG_NAME;
		}
		layoutId = a.getString(R.styleable.BitFragment_layout_id);;
		a.recycle();
	}	
}
