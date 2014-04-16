package com.perpetumobile.bit.orm.db;

import com.perpetumobile.bit.util.Task;

public class DBScriptTask extends Task {

	protected String filePath;
	protected String dbConfigName = null;
	protected DBScriptMethod method = null;
	protected String dbRecordConfigName = null;
	protected int batchSize = 100;
	
	protected String intentActionSuffix = null;

	public DBScriptTask() {
	}

	@Override
	public void runImpl() throws Exception {
		if(method == DBScriptMethod.LOAD) {
			DBScriptManager.getInstance().loadImpl(filePath, dbConfigName, dbRecordConfigName, batchSize);
		} else if(method == DBScriptMethod.EXECUTE) {
			DBScriptManager.getInstance().executeImpl(filePath, dbConfigName);
		}
	}

	@Override
	public void run() {
		super.run();
		DBScriptManager.getInstance().sendBroadcast(intentActionSuffix, this);
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setDBConfigName(String dbConfigName) {
		this.dbConfigName = dbConfigName;
	}
	
	public void setMethod(DBScriptMethod method) {
		this.method = method;
	}

	public void setDBRecordConfigName(String dbRecordConfigName) {
		this.dbRecordConfigName = dbRecordConfigName;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setIntentActionSuffix(String intentActionSuffix) {
		this.intentActionSuffix = intentActionSuffix;
	}
}
