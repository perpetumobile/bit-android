package com.perpetumobile.bit.orm.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.json.simple.parser.JSONParser;

import com.perpetumobile.bit.android.BitBroadcastManager;
import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class JSONParserManager extends BitBroadcastManager {
	static private JSONParserManager instance = new JSONParserManager();
	static public JSONParserManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(JSONParserManager.class);
	
	static final public String JSON_PARSER_MANAGER_INTENT_ACTION_PREFIX = "com.perpetumobile.bit.orm.json.JSON_PARSER_MANAGER_INTENT_ACTION.";
	
	private GenericObjectPool pool = null;
	
	private JSONParserManager() {
		super(JSON_PARSER_MANAGER_INTENT_ACTION_PREFIX);
		init();
	}
	
	private void init() {
		pool = new GenericObjectPool(new JSONParserFactory());
		pool.setMaxActive(-1);
		pool.setMaxIdle(10);
		pool.setTestOnBorrow(true);
	}
	
	public JSONParser getJSONParser() throws Exception {
		return (JSONParser)pool.borrowObject();
	}
	
	public void returnJSONParser(JSONParser parser) {
		if(parser != null) {
			try {
				pool.returnObject(parser);
			} catch (Exception e) {
				logger.error("JSONParserManager.returnJSONParser exception", e);
			}
		}
	}
	
	public void invalidateJSONParser(JSONParser parser) {
		if(parser != null) {
			try {
				pool.invalidateObject(parser);
			} catch (Exception e) {
				logger.error("JSONParserManager.invalidateJSONParser exception", e);
			}
		}
	}
	
	public JSONRecord parseImpl(Reader in, String configName) {
		JSONParser parser = null;
		JSONRecordHandler handler = null;
		JSONRecord result = null;  
			
		try {
			parser = getJSONParser();
			handler = new JSONRecordHandler(configName, parser, in);
			parser.parse(in, handler);
			result = handler.getJSONRecord();
		} catch (Exception e) {
			invalidateJSONParser(parser);
			logger.error("JSONParserManager.parseImpl exception", e);
			parser = null;
		} finally {
			returnJSONParser(parser);
		}
		
		return result;
	}
	
	public JSONRecord parseImpl(HttpRequest httpRequest, String configName, StatementLogger stmtLogger) {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, httpRequest.getUrl());	
		try {
			String response = HttpManager.getInstance().executeImpl(httpRequest).getPageSource();
			if(!Util.nullOrEmptyString(response)) {
				StringReader in = new StringReader(response);
				result = parseImpl(in, configName);
			}
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("JSONParserManager.parseImpl exception", e);
		}
		return result;
	}
	
	public JSONRecord parseImpl(File file, String configName, StatementLogger stmtLogger) {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, file.getAbsolutePath());	
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			result = parseImpl(in, configName);
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("JSONParserManager.parseImpl exception", e);
		}
		return result;
	}
	
	public int start(StatementLogger stmtLogger, String uri) {
		if (stmtLogger != null) {
			StatementLog stmtLog = new StatementLog(uri);
			return stmtLogger.startStatement(stmtLog);
		}
		return -1;
	}

	public void end(StatementLogger stmtLogger, int index) {
		if (stmtLogger != null) {
			stmtLogger.endStatement(index);
		}
	}
	
	public void logErrorMsg(StatementLogger stmtLogger, int index, String errorMsg) {
		if (stmtLogger != null) {
			stmtLogger.setErrorMsg(index, errorMsg);
		}
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, String configName) {
		return parseSync(httpRequest, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, String configName, String threadPoolManagerConfigName) {
		return parseSync(httpRequest, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setHttpRequest(httpRequest);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			task.isDone();
		} catch (Exception e) {
			logger.error("JSONParserManager.parse exception", e);
		}
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName) {
		return parseSync(file, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName, String threadPoolManagerConfigName) {
		return parseSync(file, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setFile(file);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			task.isDone();
		} catch (Exception e) {
			logger.error("JSONParserManager.parse exception", e);
		}
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 */
	public void parse(HttpRequest httpRequest, String configName) {
		parse(httpRequest, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 */
	public void parse(HttpRequest httpRequest, String configName, String threadPoolManagerConfigName) {
		parse(httpRequest, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configName is used as a intentActionSuffix.
	 */
	public void parse(HttpRequest httpRequest, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		// registerReceiver must not be called multiple times
		// client needs to explicitly register using registerReceiver method
		// registerReceiver(broadcastReceiver, configName);
		JSONParserTask task = new JSONParserTask();
		task.setHttpRequest(httpRequest);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		task.setIntentActionSuffix(configName);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
		} catch (Exception e) {
			logger.error("JSONParserManager.parse exception", e);
		}
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configName is used as a intentActionSuffix.
	 */
	public void parse(File file, String configName) {
		parse(file, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configName is used as a intentActionSuffix.
	 */
	public void parse(File file, String configName, String threadPoolManagerConfigName) {
		parse(file, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configName is used as a intentActionSuffix.
	 */
	public void parse(File file, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		// registerReceiver must not be called multiple times
		// client needs to explicitly register using registerReceiver method
		// registerReceiver(broadcastReceiver, configName);
		JSONParserTask task = new JSONParserTask();
		task.setFile(file);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		task.setIntentActionSuffix(configName);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
		} catch (Exception e) {
			logger.error("JSONParserManager.parse exception", e);
		}
	}
}
