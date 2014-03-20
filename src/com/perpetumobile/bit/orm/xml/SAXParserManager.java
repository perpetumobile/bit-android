package com.perpetumobile.bit.orm.xml;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.xml.sax.InputSource;

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
public class SAXParserManager extends BitBroadcastManager {
	static private Logger logger = new Logger(SAXParserManager.class);
	
	static private SAXParserManager instance = new SAXParserManager();
	static public SAXParserManager getInstance() { return instance; }
	
	static final public String SAX_PARSER_MANAGER_INTENT_ACTION_PREFIX = "com.perpetumobile.bit.orm.xml.SAX_PARSER_MANAGER_INTENT_ACTION.";
	
	private GenericObjectPool pool = null;
	
	private SAXParserManager() {
		super(SAX_PARSER_MANAGER_INTENT_ACTION_PREFIX);
		init();
	}
	
	private void init() {
		pool = new GenericObjectPool(new SAXParserFactory());
		pool.setMaxActive(-1);
		pool.setMaxIdle(10);
		pool.setTestOnBorrow(true);
	}
	
	public SAXParser getSAXParser() throws Exception {
		return (SAXParser)pool.borrowObject();
	}
	
	public void returnSAXParser(SAXParser parser) {
		if(parser != null) {
			try {
				pool.returnObject(parser);
			} catch (Exception e) {
				logger.error("SAXParserManager.returnSAXParser exception", e);
			}
		}
	}
	
	public void invalidateSAXParser(SAXParser parser) {
		if(parser != null) {
			try {
				pool.invalidateObject(parser);
			} catch (Exception e) {
				logger.error("SAXParserManager.invalidateSAXParser exception", e);
			}
		}
	}
	
	public XMLRecord parseImpl(HttpRequest httpRequest, String configNamePrefix, String rootElementName, StatementLogger stmtLogger) {
		SAXParser parser = null;
		XMLRecordHandler handler = null;
		XMLRecord result = null;  
		
		int stmtLogIndex = start(stmtLogger, httpRequest.getUrl());	
		try {
			parser = getSAXParser();
			handler = new XMLRecordHandler(configNamePrefix, rootElementName, parser);
			String response = HttpManager.getInstance().executeImpl(httpRequest).getPageSource();
			if(!Util.nullOrEmptyString(response)) {
				// TODO: There must be a faster way to fix & :-) 
				response = Util.replaceAll(response, "&amp;", "&");
				response = Util.replaceAll(response, "&", "&amp;");
				parser.parse(new InputSource(new StringReader(response)), handler);
				// parser.parse(uri, handler);
				result = handler.getXMLRecord();
			}
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("SAXParserManager.parseImpl exception", e);
			invalidateSAXParser(parser);
			parser = null;
		} finally {
			returnSAXParser(parser);
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
	public XMLRecord parseSync(HttpRequest httpRequest, String configNamePrefix, String rootElementName) {
		return parseSync(httpRequest, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(HttpRequest httpRequest, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		return parseSync(httpRequest, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(HttpRequest httpRequest, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		SAXParserTask task = new SAXParserTask();
		task.setHttpRequest(httpRequest);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			task.isDone();
		} catch (Exception e) {
			logger.error("SAXParserManager.parse exception", e);
		}
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configNamePrefix is used as a intentActionSuffix.
	 */
	public void parse(HttpRequest httpRequest, String configNamePrefix, String rootElementName) {
		parse(httpRequest, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configNamePrefix is used as a intentActionSuffix.
	 */
	public void parse(HttpRequest httpRequest, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		parse(httpRequest, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * configNamePrefix is used as a intentActionSuffix.
	 */
	public void parse(HttpRequest httpRequest, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		// registerReceiver must not be called multiple times
		// client needs to explicitly register using registerReceiver method
		// registerReceiver(broadcastReceiver, configNamePrefix);
		SAXParserTask task = new SAXParserTask();
		task.setHttpRequest(httpRequest);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		task.setIntentActionSuffix(configNamePrefix);
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(DataSingleton.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
		} catch (Exception e) {
			logger.error("SAXParserManager.parse exception", e);
		}
	}
}
