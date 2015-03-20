package com.perpetumobile.bit.orm.db;


import java.io.File;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.util.Logger;

/**
 * 
 * @author  Zoran Dukic
 */
public class DBConnection extends RecordConnection<Connection> {
	static private Logger logger = new Logger(DBConnection.class);
	
	//config file keys  
	final static public String DB_DRIVER_KEY = "Database.Driver";
	final static public String DB_RETRIES_KEY = "Database.Retries";
	final static public String DB_LOGIN_TIMEOUT_KEY = "Database.LoginTimeout";
	
	final static public String DB_URL_KEY = "Database.Url";
	final static public String DB_SCHEMA_KEY = "Database.Schema";
	final static public String DB_DATABASE_KEY = "Database.Database";
	
	final static public String DB_TRANSACTION_ENABLED_KEY = "Database.Transaction.Enabled";
	
	final static public String DB_SQL_LAST_INSERT_ID_KEY = "Database.SQL.LastInsertId";
	
	//default config values
	final static public int DB_RETRIES_DEFAULT = 0;
	final static public int DB_LOGIN_TIMEOUT_DEFAULT = 30;
	
	final static public String DB_SQL_LAST_INSERT_ID_DEFAULT = "SELECT LAST_INSERT_ROWID()";
	
	private String driver = null;
	private int maxRetries = 0;
	private int loginTimeout = 30;
	
	protected String url = null;
	protected String schema = null;
	protected boolean isTransactionEnabled = true;
	
	protected String lastInsertIdSQL = null;
	
	/**
	 * Creates a new instance of BaseDBConnection
	 */
	public DBConnection(String configName){
		super(configName);
		driver = Config.getInstance().getClassProperty(configName, DB_DRIVER_KEY, null);
		maxRetries = Config.getInstance().getIntClassProperty(configName, DB_RETRIES_KEY, DB_RETRIES_DEFAULT);
		loginTimeout = Config.getInstance().getIntClassProperty( configName, DB_LOGIN_TIMEOUT_KEY, DB_LOGIN_TIMEOUT_DEFAULT);
		url = Config.getInstance().getClassProperty(configName, DB_URL_KEY, "");
		schema = Config.getInstance().getClassProperty(configName, DB_SCHEMA_KEY, Config.getInstance().getClassProperty(configName, DB_DATABASE_KEY, ""));
		isTransactionEnabled = Config.getInstance().getBooleanClassProperty(configName, DB_TRANSACTION_ENABLED_KEY, true);
		lastInsertIdSQL = Config.getInstance().getClassProperty(configName, DB_SQL_LAST_INSERT_ID_KEY, DB_SQL_LAST_INSERT_ID_DEFAULT);
	}
	
	public void connect() {
		if(connection == null) {
			Context context = DataSingleton.getInstance().getAppContext();
			// make sure that database exists
			SQLiteDatabase db = context.openOrCreateDatabase(schema, Context.MODE_PRIVATE, null);
			if(db != null) { 
				// close native connection to avoid connection leak
				db.close();
				// now open jdbc connection
				File f = context.getDatabasePath(schema);
				connection = connect(url+f.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Initiates a connection to the database.
	 * @return	Connection object
	 */
	protected Connection connect(String connectURL) {
		Connection result = null;
		
		// load driver class
		try {
			Class.forName(driver);
		} catch(ClassNotFoundException e) {
			logger.error("JDBC Driver is missing: " + driver, e);
			return null;
		}
		
		// set the login timeout
		DriverManager.setLoginTimeout(loginTimeout);
		
		int retries = 0;
		while (result == null && retries < maxRetries) {
			try {
				// get JDBC connection
				result = DriverManager.getConnection(connectURL, user, password);
			} catch(SQLException e) {
				result = null;
				logger.error("Cannot open connection: " + connectURL, e);
			}
			retries++;
		}
		
		return result;
	}
	
	/**
	 * Frees up connection resources.
	 * You should call this method before your DB object goes out of scope.
	 * This method explicitly frees up resources rather than waiting for
	 * garbage collection to do so.
	 *
	 * @param con Connection object
	 */
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch(SQLException e) {
			logger.error("Cannot close database connection", e);
		}
		connection = null;
	}
	
	public boolean validate() throws Exception {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.executeQuery("SELECT 1");
		} finally {
			if (stmt!=null) stmt.close();
		}
		return true;
	}
	
	public String getUrl() {
		return url;
	}
	
	@Deprecated
	public String getDatabaseName(){
		return schema;
	}
	
	public String getSchema() throws SQLException {
		String result = schema;
		if(connection != null) {
			result = connection.getCatalog();
		} 
		return (result != null ? result : "");
	}
	
	public void setSchema(String schema) throws SQLException {
		this.schema = schema;
		if(connection != null) {
			connection.setCatalog(schema);
		}
	}

	public String getLastInsertIdSQL() {
		return lastInsertIdSQL;
	}
	
	public int startTransaction() throws SQLException {
		if(isTransactionEnabled) {
			Statement stmt = connection.createStatement();
			// setEscapeProcessing not implemented in sqldroid
			// stmt.setEscapeProcessing(false);
			return stmt.executeUpdate("START TRANSACTION");
		}
		return 0;
	}
	
	public int commit() throws SQLException {
		if(isTransactionEnabled) {
			Statement stmt = connection.createStatement();
			// setEscapeProcessing not implemented in sqldroid
			// stmt.setEscapeProcessing(false);
			return stmt.executeUpdate("COMMIT");
		}
		return 0;	
	}
	
	public int rollback() {
		if(isTransactionEnabled) {
			int result = -1;
			Statement stmt;
			try {
				stmt = connection.createStatement();
				// setEscapeProcessing not implemented in sqldroid
				// stmt.setEscapeProcessing(false);
				result = stmt.executeUpdate("ROLLBACK");
			} catch (SQLException e) {
				logger.error("Exception at DBConnection.rollback", e);
			}
			return result;
		}
		return 0;
	}
}
