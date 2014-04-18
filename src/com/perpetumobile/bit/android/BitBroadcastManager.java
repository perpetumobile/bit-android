package com.perpetumobile.bit.android;

import java.util.UUID;

import com.perpetumobile.bit.util.Util;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

@Deprecated
public class BitBroadcastManager {

	static final public String BROADCAST_INTENT_EXTRA_KEY = "com.perpetumobile.bit.android.BROADCAST_INTENT_EXTRA_KEY";
	
	protected String intentActionPrefix = null;
			
	public BitBroadcastManager(String intentActionPrefix) {
		this.intentActionPrefix = intentActionPrefix;
	}
	
	public String getIntentAction(String intentActionSuffix) {
		StringBuilder buf = new StringBuilder(intentActionPrefix);
		buf.append(intentActionSuffix);
		return buf.toString();
	}
	
	public void registerReceiver(BroadcastReceiver broadcastReceiver, String intentActionSuffix) {
		if(broadcastReceiver != null) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(getIntentAction(intentActionSuffix));
			LocalBroadcastManager.getInstance(DataSingleton.getInstance().getAppContext()).registerReceiver(broadcastReceiver, intentFilter);
		}
	}
	
	public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
		if(broadcastReceiver != null) {
			LocalBroadcastManager.getInstance(DataSingleton.getInstance().getAppContext()).unregisterReceiver(broadcastReceiver);
		}
	}
	
	public void sendBroadcast(String intentActionSuffix, Object value) {
		if(value != null && !Util.nullOrEmptyString(intentActionSuffix)) {
			Intent intent = new Intent(getIntentAction(intentActionSuffix));
			String uuid = UUID.randomUUID().toString();
			DataSingleton.getInstance().putIntentExtra(uuid, value);
			intent.putExtra(BROADCAST_INTENT_EXTRA_KEY, uuid);
			LocalBroadcastManager.getInstance(DataSingleton.getInstance().getAppContext()).sendBroadcastSync(intent);
		}
	}
}
