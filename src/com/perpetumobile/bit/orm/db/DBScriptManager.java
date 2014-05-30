package com.perpetumobile.bit.orm.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;

import com.perpetumobile.bit.android.util.FileUtil;
import com.perpetumobile.bit.orm.record.RecordScriptData;
import com.perpetumobile.bit.orm.record.StatementLoggerImpl;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Task;
import com.perpetumobile.bit.util.TaskCallback;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;


public class DBScriptManager {
	static private DBScriptManager instance = new DBScriptManager();
	static public DBScriptManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(DBScriptManager.class);
	
	private DBScriptManager() {
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
	
	protected void runTask(Task task, String threadPoolManagerConfigName, boolean isSync) {
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(ThreadPoolManager.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			if(isSync) {
				task.isDone();
			}
		} catch (Exception e) {
			logger.error("DBScriptManager.runTask exception", e);
		}
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
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:". 
	 */
	public void load(TaskCallback<DBScriptTask>callback, String filePath, String dbConfigName, String dbRecordConfigName, int batchSize) {
		load(callback, filePath, dbConfigName, dbRecordConfigName, batchSize, null);
	}
	
	/**
	 * Load is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void load(TaskCallback<DBScriptTask>callback, String filePath, String dbConfigName, String dbRecordConfigName, int batchSize, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.LOAD);
		task.setDBRecordConfigName(dbRecordConfigName);
		task.setBatchSize(batchSize);
		task.setCallback(callback);
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
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:". 
	 */
	public void execute(TaskCallback<DBScriptTask>callback, String filePath, String dbConfigName) {
		execute(callback, filePath, dbConfigName, null);
	}
	
	/**
	 * Load is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * 
	 * If file is in the Asset directory the filePath param should be prefixed with "asset:".
	 */
	public void execute(TaskCallback<DBScriptTask>callback, String filePath, String dbConfigName, String threadPoolManagerConfigName) {
		DBScriptTask task = createDBScriptTask(filePath, dbConfigName, DBScriptMethod.EXECUTE);
		task.setCallback(callback);
		runTask(task, threadPoolManagerConfigName, false);
	}
}
