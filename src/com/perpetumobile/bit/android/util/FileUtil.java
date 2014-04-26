package com.perpetumobile.bit.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import android.content.Context;
import android.content.res.AssetManager;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.Util;

/**
 * Misc file utility methods.
 */
final public class FileUtil {
	
	static final public String ASSET_DIRECTORY_PROTOCOL = "asset:"; 
	
	static private boolean isAssetFile(String filePath) {
		return filePath.startsWith(ASSET_DIRECTORY_PROTOCOL);
	}
	
	/**
	 * Return asset file input stream.
	 */
	static public InputStream getAssetFileInputStream(String filePath) 
	throws IOException {
		AssetManager am = DataSingleton.getInstance().getAssetManager();
		return am.open(filePath);
	}
	
	/**
	 * Return asset file reader.
	 */
	static public Reader getAssetFileReader(String filePath) 
	throws IOException {
		return new InputStreamReader(getAssetFileInputStream(filePath));
	}
	
	/**
	 * Read asset file.
	 */
	static public StringBuffer readAssetFile(String filePath) 
	throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader in = new BufferedReader(getAssetFileReader(filePath));
		char[] buf = new char[1024];
		while (in.read(buf) != -1) {
			result.append(buf);
		}
		in.close();
		return result;
	}
	
	static public String getDirectoryPath(String filePath) {
		String directoryPath = "";
		int index = filePath.lastIndexOf("/");
		if(index != -1) {
			directoryPath = filePath.substring(0, index);
		}
		return directoryPath;
	}
	
	static public String getFileName(String filePath) {
		String fileName = filePath;
		int index = filePath.lastIndexOf("/");
		if(index != -1) {
			fileName = filePath.substring(index+1);
		}
		return fileName;
	}
	
	/**
	 * Return directory. Returns null if directoryPath is prefixed with "asset:".
	 */
	static public File getDir(String directoryPath) {
		if(isAssetFile(directoryPath)) {
			return null;			
		}
		
		// getDir needs top level directory
		String topLevelPath = directoryPath;
		String lowLevelPath = "";
		int index = directoryPath.indexOf("/");
		if(index != -1) {
			topLevelPath = directoryPath.substring(0,index);
			lowLevelPath = directoryPath.substring(index+1);
		}
		
		Context context  = DataSingleton.getInstance().getAppContext();
		File tld = context.getDir(topLevelPath, Context.MODE_PRIVATE);
		File result = tld;
		if(!Util.nullOrEmptyString(lowLevelPath)) {
			result = new File(tld, lowLevelPath);
			result.mkdirs();	
		}
		return result;
	}
	
	/**
	 * Delete directory. Does nothing if directoryPath is prefixed with "asset:".
	 */
	static public void deleteDir(String directoryPath) {
		if(!isAssetFile(directoryPath)) {
			File dir = getDir(directoryPath);
			Util.deleteDirectory(dir);
		}
	}
	
	/**
	 * Return file. Returns null if filePath is prefixed with "asset:".
	 */
	static public File getFile(String filePath) {
		if(isAssetFile(filePath)) {
			return null;			
		}	
		return new File(getDir(getDirectoryPath(filePath)), getFileName(filePath));
	}
	
	/**
	 * Return file input stream. If file is in the Asset directory the filePath should be prefixed with "asset:".
	 */
	static public InputStream getFileInputStream(String filePath) 
	throws IOException {
		if(isAssetFile(filePath)) {
			return getAssetFileInputStream(filePath.substring(ASSET_DIRECTORY_PROTOCOL.length()));			
		} 
		return new FileInputStream(getFile(filePath));
	}
	
	/**
	 * Return file reader. If file is in the Asset directory the filePath should be prefixed with "asset:".
	 */
	static public Reader getFileReader(String filePath) 
	throws IOException {
		if(isAssetFile(filePath)) {
			return getAssetFileReader(filePath.substring(ASSET_DIRECTORY_PROTOCOL.length()));			
		}
		return new FileReader(getFile(filePath));
	}
	
	
	/**
	 * Read file. If file is in the Asset directory the filePath should be prefixed with "asset:".
	 */
	static public StringBuffer readFile(String filePath) 
	throws IOException {
		if(isAssetFile(filePath)) {
			return readAssetFile(filePath.substring(ASSET_DIRECTORY_PROTOCOL.length()));			
		}
		return Util.readFile(getFile(filePath));
	}

	/**
	 * Return file output stream. Throws IOException if filePath is prefixed with "asset:".
	 */
	static public OutputStream getFileOutputStream(String filePath, boolean append) 
	throws IOException {
		if(isAssetFile(filePath)) {
			throw new IOException("Cannot write to asset directory!");			
		}
		return new FileOutputStream(getFile(filePath), append);
	}
	
	/**
	 * Return file writer. Throws IOException if filePath is prefixed with "asset:".
	 */
	static public Writer getFileWriter(String filePath, boolean append) 
	throws IOException {
		if(isAssetFile(filePath)) {
			throw new IOException("Cannot write to asset directory!");			
		}
		return new FileWriter(getFile(filePath), append);
	}
	
	/**
	 * Save to file. Throws IOException if filePath is prefixed with "asset:".
	 */
	static public void saveToFile(String content, String filePath, boolean append) 
	throws IOException {
		if(isAssetFile(filePath)) {
			throw new IOException("Cannot write to asset directory!");			
		}
		Util.saveToFile(content, getFile(filePath), append);
	}	
}
