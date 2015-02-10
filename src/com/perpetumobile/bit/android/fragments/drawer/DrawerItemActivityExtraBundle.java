package com.perpetumobile.bit.android.fragments.drawer;

import android.os.Bundle;

import com.perpetumobile.bit.orm.json.JSONRecord;
import com.perpetumobile.bit.orm.record.field.BooleanField;
import com.perpetumobile.bit.orm.record.field.DoubleField;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FloatField;
import com.perpetumobile.bit.orm.record.field.IntField;

public class DrawerItemActivityExtraBundle extends JSONRecord {
	private static final long serialVersionUID = 1L;
	
	protected Bundle bundle = new Bundle(); 
	
	public DrawerItemActivityExtraBundle() {
	}

	@Override
	public void setField(String key, String value) {
		super.setField(key, value);
		if(isFieldSet(key)) {
			Field f = getField(key);
			if(f instanceof BooleanField) {
				bundle.putBoolean(key, f.getBooleanFieldValue());
			} else if(f instanceof IntField) {
				bundle.putInt(key, f.getIntFieldValue());
			} else if(f instanceof FloatField) {
				bundle.putFloat(key, f.getFloatFieldValue());
			} else if(f instanceof DoubleField) {
				bundle.putDouble(key, f.getDoubleFieldValue());
			} else {
				bundle.putString(key, f.getFieldValue());
			}
		}
	}

	public Bundle getBundle() {
		return bundle;
	}
}
