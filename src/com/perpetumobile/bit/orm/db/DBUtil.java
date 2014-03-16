package com.perpetumobile.bit.orm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 * 
 */
public class DBUtil {
	
	static final String SQL_STRING_NULL = "null";
	/**	
	 * Replaces all occurances of single quotes to double quotes
	 * and back slash to double back slash. 
	 * eg. "this is alex's book" becomes "this is alex''s book"
	 *
	 * @param sql string to be fixed
	 */
	public static String encodeSQLString(String sql) {
		if (sql == null) {
			return SQL_STRING_NULL;
		}

		sql = Util.replaceAll(sql, "\\", "\\\\");
		sql = Util.replaceAll(sql, "'", "''");

		StringBuffer buf = new StringBuffer();
		buf.append('\'');
		buf.append(sql);
		buf.append('\'');
		return buf.toString();
	}
	
	public static String encodeSQLString(String sql, int limit) {
		if(sql.length() > limit) {
			return encodeSQLString(sql.substring(0, limit));
		}		
		return encodeSQLString(sql);
	}

	public static void close(ResultSet rs) {
		if (rs != null) {	
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
	}
	
	static public String getMD5(DBConnection dbConnection, String str) 
	throws SQLException {
		StringBuffer buf = new StringBuffer("SELECT MD5(");
		buf.append(encodeSQLString(str));
		buf.append(")");
		String strSQL = buf.toString();
		
		String result = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConnection.getConnection().createStatement();
			rs = stmt.executeQuery(strSQL);
			if(rs.next()) {
				result = rs.getString(1);
			}
		} finally {
			close(rs);
			close(stmt);
		}		
		return result;
	}
}
