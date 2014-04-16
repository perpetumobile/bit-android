package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;

import com.perpetumobile.bit.android.BitBroadcastManager;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class DBStatementManager extends BitBroadcastManager {
	static private DBStatementManager instance = new DBStatementManager();
	static public DBStatementManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(DBStatementManager.class);
	
	static final public String DB_STATEMENT_MANAGER_INTENT_ACTION_PREFIX = "com.perpetumobile.bit.orm.db.DB_STATEMENT_MANAGER_INTENT_ACTION.";
	
	private DBStatementManager() {
		super(DB_STATEMENT_MANAGER_INTENT_ACTION_PREFIX);
	}

	public ArrayList<? extends DBRecord> selectImpl(String dbConfigName, DBStatement<? extends DBRecord> stmt)
	throws Exception {
		return selectImpl(dbConfigName, stmt, null);
	}
	
	public ArrayList<? extends DBRecord> selectImpl(String dbConfigName, DBStatement<? extends DBRecord> stmt, String strSQL) 
	throws Exception {
		ArrayList<? extends DBRecord> result = null; 
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			if(Util.nullOrEmptyString(strSQL)) {
				result = stmt.readDBRecords(dbConnection);
			} else {
				result = stmt.readDBRecords(dbConnection, strSQL);
			}
		} catch (Exception e) {
			logger.error("DBStatementManager.select exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public int insertImpl(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord rec) 
	throws Exception {
		int result = 0; 
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			if(method == DBStatementMethod.INSERT) {
				result = ((DBStatement<DBRecord>)stmt).insertDBRecord(dbConnection, rec);
			} else if(method == DBStatementMethod.INSERT_IGNORE) {
				result = ((DBStatement<DBRecord>)stmt).insertIgnoreDBRecord(dbConnection, rec);
			} else if(method == DBStatementMethod.REPLACE){
				result = ((DBStatement<DBRecord>)stmt).replaceDBRecord(dbConnection, rec);
			}
		} catch (Exception e) {
			logger.error("DBStatementManager.insert exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void insertImpl(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recs)
	throws Exception {	
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			if(method == DBStatementMethod.INSERT) {
				((DBStatement<DBRecord>)stmt).insertDBRecords(dbConnection, recs);
			} else if(method == DBStatementMethod.INSERT_IGNORE) {
				((DBStatement<DBRecord>)stmt).insertIgnoreDBRecords(dbConnection, recs);
			} else if(method == DBStatementMethod.REPLACE){
				((DBStatement<DBRecord>)stmt).replaceDBRecords(dbConnection, recs);
			}
		} catch (Exception e) {
			logger.error("DBStatementManager.insert exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
	}
	
	@SuppressWarnings("unchecked")
	public int updateImpl(String dbConfigName, DBStatement<? extends DBRecord> stmt, DBRecord rec)
	throws Exception {
		int result = 0; 
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			result = ((DBStatement<DBRecord>)stmt).updateDBRecords(dbConnection, rec);
		} catch (Exception e) {
			logger.error("DBStatementManager.update exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	public int deleteImpl(String dbConfigName, DBStatement<? extends DBRecord> stmt)
	throws Exception {
		int result = 0;
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConfigName);
			result = stmt.deleteDBRecords(dbConnection);
		} catch (Exception e) {
			logger.error("DBStatementManager.delete exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	protected DBStatementTask createDBStatementTask(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt) {
		DBStatementTask task = new DBStatementTask();
		task.setDBConfigName(dbConfigName);
		task.setMethod(method);
		task.setStmt(stmt);
		return task;
	}
	
	/**
	 * Select is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public ArrayList<? extends DBRecord> selectSync(String dbConfigName, DBStatement<? extends DBRecord> stmt) {
		return selectSync(dbConfigName, stmt, null, null);
	}
	
	/**
	 * Select is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public ArrayList<? extends DBRecord> selectSync(String dbConfigName, DBStatement<? extends DBRecord> stmt, String strSQL) {
		return selectSync(dbConfigName, stmt, strSQL, null);
	}
	
	/**
	 * Select is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public ArrayList<? extends DBRecord> selectSync(String dbConfigName, DBStatement<? extends DBRecord> stmt, String strSQL, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.SELECT, stmt);
		task.setSQL(strSQL);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResultList();
	}
	
	/**
	 * Select is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void select(String dbConfigName, DBStatement<? extends DBRecord> stmt) {
		select(dbConfigName, stmt, null, null);
	}
	
	/**
	 * Read is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void select(String dbConfigName, DBStatement<? extends DBRecord> stmt, String strSQL) {
		select(dbConfigName, stmt, strSQL, null);
	}
	
	/**
	 * Read is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void select(String dbConfigName, DBStatement<? extends DBRecord> stmt, String strSQL, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.SELECT, stmt);
		task.setSQL(strSQL);
		task.setIntentActionSuffix(dbConfigName);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int insertSync(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord record) {
		return insertSync(dbConfigName, method, stmt, record, null);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int insertSync(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord record, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, method, stmt);
		task.setRecord(record);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Insert is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void insert(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord record) {
		insert(dbConfigName, method, stmt, record, null);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void insert(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord record, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, method, stmt);
		task.setRecord(record);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int insertSync(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recordList) {
		return insertSync(dbConfigName, method, stmt, recordList, null);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int insertSync(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recordList, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, method, stmt);
		task.setRecordList(recordList);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Insert is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void insert(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recordList) {
		insert(dbConfigName, method, stmt, recordList, null);
	}
	
	/**
	 * Insert is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void insert(String dbConfigName, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recordList, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, method, stmt);
		task.setRecordList(recordList);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Update is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int updateSync(String dbConfigName, DBStatement<? extends DBRecord> stmt, DBRecord record) {
		return updateSync(dbConfigName, stmt, record, null);
	}
	
	/**
	 * Update is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int updateSync(String dbConfigName, DBStatement<? extends DBRecord> stmt, DBRecord record, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.UPDATE, stmt);
		task.setRecord(record);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Update is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void update(String dbConfigName, DBStatement<? extends DBRecord> stmt, DBRecord record) {
		update(dbConfigName, stmt, record, null);
	}
	
	/**
	 * Update is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void update(String dbConfigName, DBStatement<? extends DBRecord> stmt, DBRecord record, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.UPDATE, stmt);
		task.setRecord(record);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Delete is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int deleteSync(String dbConfigName, DBStatement<? extends DBRecord> stmt) {
		return deleteSync(dbConfigName, stmt, null);
	}
	
	/**
	 * Delete is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public int deleteSync(String dbConfigName, DBStatement<? extends DBRecord> stmt, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.DELETE, stmt);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Delete is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void delete(String dbConfigName, DBStatement<? extends DBRecord> stmt) {
		delete(dbConfigName, stmt, null);
	}
	
	/**
	 * Delete is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 * Broadcast will be sent to broadcast receiver after operation is completed.
	 * Broadcast receiver needs to be registered using registerReceiver method.
	 * dbConfigName is used as a intentActionSuffix.
	 */
	public void delete(String dbConfigName, DBStatement<? extends DBRecord> stmt, String threadPoolManagerConfigName) {
		DBStatementTask task = createDBStatementTask(dbConfigName, DBStatementMethod.DELETE, stmt);
		runTask(task, threadPoolManagerConfigName, false);
	}	
}
