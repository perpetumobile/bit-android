package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;

import com.perpetumobile.bit.android.FileUtil;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.config.ConfigSubscriber;
import com.perpetumobile.bit.http.BatchHttpRequest;
import com.perpetumobile.bit.http.HttpResponseDocument;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

/**
 * DBSchemaManager supports database schema management.
 * 
 * <p>Schema db configuration needs to be specified. For example:
 * <br>Schema.Database.Database: schema
 * 
 * <p>Schema db record configuration needs to be specified. For example:
 * <br>Schema.DBRecord.Table.Name: schema
 * <br>Schema.Record.Fields: schema_id auto,\
 * <br>version varchar(64)
 * 
 * <p>DBSchemaManager.Version.Required configuration settings should be used to define required schema version.
 * <p>DBSchemaManager.Version.UpgradeFrom.[version] configuration setting should be used to specify comma delimited 
 * list of scripts ([file] [db_config_name]) for upgrading schema from given [version].
 * 
 * <p>Schema table doesn't get updated automatically, so last script in the list should update schema table. For example: 
 * <br>DBSchemaManager.Version.UpgradeFrom.0: asset:schema/schema_ddl.txt Schema  
 * 
 * <p>See db.config.txt and orm.config.txt for details and examples.
 *
 * @author Zoran Dukic
 */
public class DBSchemaManager implements ConfigSubscriber {
	static private DBSchemaManager instance = new DBSchemaManager();
	static public DBSchemaManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(DBSchemaManager.class);
	
	static final public String SCHEMA_DB_CONFIG_NAME = "Schema";
	static final public String SCHEMA_DB_RECORD_CONFIG_NAME = "Schema";
	
	static final public String AUTO_ENABLE_CONFIG_KEY = "DBSchemaManager.Auto.Enable";
	static final public boolean AUTO_ENABLE_DEFAULT = true;
	
	static final public String VERSION_REQUIRED_CONFIG_KEY = "DBSchemaManager.Version.Required";
	static final public String VERSION_REQUIRED_DEFAULT = "1.0";
	
	static final public String VERSION_UPGRADE_FROM_CONFIG_KEY_PREFIX = "DBSchemaManager.Version.UpgradeFrom.";
	
	private DBSchemaManager() {
		Config.getInstance().subscribe(this);
	}

	@Override
	public void onConfigReset() {
		if(isAutoEnabled()) {
			try {
				if(isSchemaFromServerEnabled()) {
					requestSchemaFromServer();
				} else {
					upgrade();
				}
			} catch (Exception e) {
				logger.error("DBSchemaManager.configReset exception", e);
			}
		}
	}

	public boolean isAutoEnabled() {
		return Config.getInstance().getBooleanProperty(AUTO_ENABLE_CONFIG_KEY, AUTO_ENABLE_DEFAULT);
	}
	
	public String getRequiredVersion() {
		return Config.getInstance().getProperty(VERSION_REQUIRED_CONFIG_KEY, VERSION_REQUIRED_DEFAULT);
	}
	
	public String getCurrentVersion() {
		String result = "0";
		try {
			DBStatement<? extends DBRecord> stmt = new DBStatement<DBRecord>(SCHEMA_DB_RECORD_CONFIG_NAME);
			stmt.addOrderByClause("schema_id desc");
			ArrayList<? extends DBRecord> list = DBStatementManager.getInstance().selectImpl(SCHEMA_DB_CONFIG_NAME, stmt);
			if(!Util.nullOrEmptyList(list)) {
				DBRecord rec = list.get(0);
				result = rec.getFieldValue("version");
			}
		} catch (Exception e) {
			logger.error("DBSchemaManager.getCurrentVersion exception", e);
			result = "0";
		}
		return result;
	}
	
	synchronized public void upgrade() throws Exception {
		upgrade(getRequiredVersion(), getCurrentVersion());
	}
	
	synchronized protected void upgrade(String requiredVersion, String currentVersion) throws Exception {
		if(!requiredVersion.equalsIgnoreCase(currentVersion)) {
			String scripts = Config.getInstance().getProperty(VERSION_UPGRADE_FROM_CONFIG_KEY_PREFIX + currentVersion, "");
			if(!Util.nullOrEmptyString(scripts)) {
				String[] scriptList = scripts.split(",");
				for(String script : scriptList) {
					String[] s = script.split(" ");
					if(s.length == 2) {
						DBScriptManager.getInstance().executeImpl(s[0], s[1]);
					}
				}
			}
			// continue to upgrade until required version is reached
			upgrade(requiredVersion, getCurrentVersion());
		}
	}
		
	static final public String DB_SCHEMA_MANAGER_SERVER_ENABLE_KEY = "DBSchemaManager.Server.Enable";
	static final public String DB_SCHEMA_MANAGER_SERVER_URLS_KEY = "DBSchemaManager.Server.URLs";
	
	static final public String DB_SCHEMA_MANAGER_SERVER_HTTP_REQUEST_CLASS_KEY = "DBSchemaManager.Server.HttpRequest.Class";
	static final public String DB_SCHEMA_MANAGER_SERVER_THREAD_POOL_NAME = "DBSchemaService";
	
	static final public String DB_SCHEMA_MANAGER_SERVER_DIRECTORY_KEY = "DBSchemaManager.Server.Directory";
	static final public String DB_SCHEMA_MANAGER_SERVER_DIRECTORY_DEFAULT = "schema";
	
	protected BatchHttpRequest batchHttpRequest = new BatchHttpRequest(Config.getInstance().getProperty(DB_SCHEMA_MANAGER_SERVER_HTTP_REQUEST_CLASS_KEY, null),	DB_SCHEMA_MANAGER_SERVER_THREAD_POOL_NAME) {
		@Override
		protected void onResponse(ArrayList<HttpResponseDocument> result) {
			String directoryPath = Config.getInstance().getProperty(DB_SCHEMA_MANAGER_SERVER_DIRECTORY_KEY, DB_SCHEMA_MANAGER_SERVER_DIRECTORY_DEFAULT);
			if(save(directoryPath, result)) {
				try {
					upgrade();
				} catch (Exception e) {
					logger.error("DBSchemaManager.onResponse exception", e);
				}
			}
			FileUtil.deleteDir(directoryPath);
		}
	};
	
	public boolean isSchemaFromServerEnabled() {
		return Config.getInstance().getBooleanProperty(DB_SCHEMA_MANAGER_SERVER_ENABLE_KEY, false);
	}
	
	protected void requestSchemaFromServer() {
		batchHttpRequest.request(Config.getInstance().getProperty(DB_SCHEMA_MANAGER_SERVER_URLS_KEY, null));
	}
}