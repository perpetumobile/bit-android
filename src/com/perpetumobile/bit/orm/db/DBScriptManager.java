package com.perpetumobile.bit.orm.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;

import com.perpetumobile.bit.android.BitBroadcastManager;
import com.perpetumobile.bit.android.FileUtil;
import com.perpetumobile.bit.orm.record.RecordScriptData;
import com.perpetumobile.bit.orm.record.StatementLoggerImpl;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


public class DBScriptManager extends BitBroadcastManager {
	static private DBScriptManager instance = new DBScriptManager();
	static public DBScriptManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(DBScriptManager.class);
	
	static final public String DB_SCRIPT_MANAGER_INTENT_ACTION_PREFIX = "com.perpetumobile.bit.orm.db.DB_SCRIPT_MANAGER_INTENT_ACTION.";
	
	private DBScriptManager() {
		super(DB_SCRIPT_MANAGER_INTENT_ACTION_PREFIX);
	}
	
	/**
	 * Load tab delimited data to database. Field names need to be provided in the first row.
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void loadImpl(String filePath, String dbConfigName, String dbRecordConfigName, int batchSize) 
	throws Exception {
		DBConnection dbConnection = null;
		BufferedReader in = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
		
			DBStatement<DBRecord> stmt = new DBStatement<DBRecord>(dbRecordConfigName, new StatementLoggerImpl());
			ArrayList<DBRecord> dbRecords = new ArrayList<DBRecord>();
			
			logger.info("Load File: " + filePath);
			in = new BufferedReader(FileUtil.getFileReader(filePath));
			String[] columnNames = null;
			String line = null;
			while((line = in.readLine()) != null) {
				line = line.trim();
				if(!Util.nullOrEmptyString(line)) {
					if(columnNames == null) {
						columnNames = line.split("\t");
					} else {
						RecordScriptData rsd = new RecordScriptData(columnNames, line.split("\t"));
						DBRecord rec = new DBRecord(dbRecordConfigName);
						ArrayList<Field> fields = rec.getFields();
						for(Field f : fields) {
							String val = rsd.get(f.getFieldName());
							// all fields needs to be included for batch insert to work
							if(Util.nullOrEmptyString(val)) {	
								f.setFieldValue("");
							} else {
								f.setFieldValue(val);
							}
						}
						dbRecords.add(rec);
					}
				}
				
				if(dbRecords.size() >= batchSize) {
					stmt.insertDBRecords(dbConnection, dbRecords);
					dbRecords.clear();
				}
			}
			if(dbRecords.size() > 0) {
				stmt.insertDBRecords(dbConnection, dbRecords);
			}
		} catch (Exception e) {
			logger.error("DBScriptManager.load exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
			if(in != null) try { in.close(); } catch (IOException e) {}
		}
	}
	
	/**
	 * Execute SQL statements from the file.
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void executeImpl(String filePath, String dbConfigName) 
	throws Exception {
		DBConnection dbConnection = null;
		Statement stmt = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			stmt = dbConnection.getConnection().createStatement();
			
			String allSql = FileUtil.readFile(filePath).toString();
					
			String[] sqls = allSql.split(";");
			for (String sql : sqls) {
				sql = sql.trim();
				if (!Util.nullOrEmptyString(sql)) {	
					if(logger.isInfoEnabled()) {
						System.out.print(sql);
						System.out.println(";");
						System.out.println();
					}				
					stmt.execute(sql);
				}
			}
		} catch (Exception e) {
			logger.error("DBScriptManager.execute exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
			DBUtil.close(stmt);
		}
	}
	
	protected DBScriptTask createDBScriptTask(String filePath, String dbConfigName, DBScriptMethod method) {
		DBScriptTask task = new DBScriptTask();
		task.setFilePath(filePath);
		task.setDBConfigName(dbConfigName);
		task.setMethod(method);
		return task;
	}
	
	/**
	 * Load is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public boolean loadSync(String filePath, String dbConfigName, String dbRecordConfigName, int batchSize) {
		return loadSync(filePath, dbConfigName, dbRecordConfigName, batchSize, null);
	}
	
	/**
	 * Load is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public boolean loadSync(String filePath, String dbConfigName, String dbRecordConfigName, int batchSize, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.LOAD);
		task.setDBRecordConfigName(dbRecordConfigName);
		task.setBatchSize(batchSize);
		runTask(task, threadPoolManagerConfigName, true);
		return task.isSuccess();
	}
	
	/**
	 * Load is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:". 
	 */
	public void load(String filePath, String dbConfigName, String dbRecordConfigName, int batchSize) {
		load(filePath, dbConfigName, dbRecordConfigName, batchSize, null);
	}
	
	/**
	 * Load is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void load(String filePath, String dbConfigName, String dbRecordConfigName, int batchSize, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.LOAD);
		task.setDBRecordConfigName(dbRecordConfigName);
		task.setBatchSize(batchSize);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Execute is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:". 
	 */
	public boolean executeSync(String filePath, String dbConfigName) {
		return executeSync(filePath, dbConfigName, null);
	}
	
	/**
	 * Execute is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public boolean executeSync(String filePath, String dbConfigName, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.EXECUTE);
		runTask(task, threadPoolManagerConfigName, true);
		return task.isSuccess();
	}
	
	/**
	 * Execute is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:". 
	 */
	public void execute(String filePath, String dbConfigName) {
		execute(filePath, dbConfigName, null);
	}
	
	/**
	 * Load is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void execute(String filePath, String dbConfigName, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.EXECUTE);
		runTask(task, threadPoolManagerConfigName, false);
	}
}
