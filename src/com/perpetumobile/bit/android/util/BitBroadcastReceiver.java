package com.perpetumobile.bit.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.perpetumobile.bit.http.HttpResponseDocument;
import com.perpetumobile.bit.orm.db.DBStatementTask;
import com.perpetumobile.bit.orm.json.JSONRecord;
import com.perpetumobile.bit.orm.xml.XMLRecord;


@Deprecated
public class BitBroadcastReceiver extends BroadcastReceiver {
	
	public BitBroadcastReceiver(){
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	/*
    	String action = intent.getAction();
    	if(action != null && action.startsWith(HttpManager.HTTP_MANAGER_INTENT_ACTION_PREFIX)){
        	// HttpResponseDocument doc = (HttpResponseDocument) intent.getParcelableExtra(HttpManager.HTTP_MANAGER_INTENT_EXTRA);
    		String intentActionSuffix = action.substring(HttpManager.HTTP_MANAGER_INTENT_ACTION_PREFIX.length());
        	HttpResponseDocument result = (HttpResponseDocument)DataSingleton.getInstance().getIntentExtra(intent.getStringExtra(HttpManager.BROADCAST_INTENT_EXTRA_KEY));
        	if(result != null) onHttpManagerBroadcastReceive(context, intent, intentActionSuffix, result);
        } else if(action != null && action.startsWith(SAXParserManager.SAX_PARSER_MANAGER_INTENT_ACTION_PREFIX)) {
        	String intentActionSuffix = action.substring(SAXParserManager.SAX_PARSER_MANAGER_INTENT_ACTION_PREFIX.length());
        	XMLRecord result = (XMLRecord)DataSingleton.getInstance().getIntentExtra(intent.getStringExtra(SAXParserManager.BROADCAST_INTENT_EXTRA_KEY));
        	if(result != null) onSAXParserManagerBroadcastReceive(context, intent, intentActionSuffix, result);
        } else if(action != null && action.startsWith(JSONParserManager.JSON_PARSER_MANAGER_INTENT_ACTION_PREFIX)) {
        	String intentActionSuffix = action.substring(JSONParserManager.JSON_PARSER_MANAGER_INTENT_ACTION_PREFIX.length());
        	JSONRecord result = (JSONRecord)DataSingleton.getInstance().getIntentExtra(intent.getStringExtra(JSONParserManager.BROADCAST_INTENT_EXTRA_KEY));
        	if(result != null) onJSONParserManagerBroadcastReceive(context, intent, intentActionSuffix, result);
        } else if(action != null && action.startsWith(DBStatementManager.DB_STATEMENT_MANAGER_INTENT_ACTION_PREFIX)) {
        	String intentActionSuffix = action.substring(DBStatementManager.DB_STATEMENT_MANAGER_INTENT_ACTION_PREFIX.length());
        	DBStatementTask result = (DBStatementTask)DataSingleton.getInstance().getIntentExtra(intent.getStringExtra(DBStatementManager.BROADCAST_INTENT_EXTRA_KEY));
        	if(result != null) onDBStatementManagerBroadcastReceive(context, intent, intentActionSuffix, result);
        }
        */
    }
    
    public void onHttpManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, HttpResponseDocument result) {
    }
    
    public void onSAXParserManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, XMLRecord result) {
    }
    
    public void onJSONParserManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, JSONRecord result) {
    }
    
    public void onDBStatementManagerBroadcastReceive(Context context, Intent intent, String intentActionSuffix, DBStatementTask result) {
    }
}
