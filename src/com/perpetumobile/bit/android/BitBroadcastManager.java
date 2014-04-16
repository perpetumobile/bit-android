package com.perpetumobile.bit.android;

import java.util.UUID;

import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Task;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class BitBroadcastManager {
	static private Logger logger = new Logger(BitBroadcastManager.class);

	static final public String BROADCAST_INTENT_EXTRA_KEY = "com.perpetumobile.bit.android.BROADCAST_INTENT_EXTRA_KEY";
	
	protected String intentActionPrefix = null;
			
	public BitBroadcastManager(String intentActionPrefix) {
		this.intentActionPrefix = intentActionPrefix;
	}
	
	protected void runTask(Task task, String threadPoolManagerConfigName, boolean isSync) {
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			if(isSync) {
				task.isDone();
			}
		} catch (Exception e) {
			logger.error("BitBroadcastManager.runTask exception", e);
		}
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
