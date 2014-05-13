package com.perpetumobile.bit.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.Logger;

public class LocalConfigProperties extends ConfigProperties {
	static private Logger logger = new Logger(LocalConfigProperties.class);
	
	public LocalConfigProperties() {
	}
	
	public LocalConfigProperties(String fileName) throws ConfigPropertiesException {
		super(fileName);
	}

	@Override
	protected void throwConfigPropertiesException(String msg) {
		logger.error(msg);
	}
	
	@Override
	protected InputStream getInputStream(String fileName) 
	throws IOException {
		Context context  = DataSingleton.getInstance().getAppContext();
		File dir = context.getDir(Config.CONFIG_PROPERTIES_DIRECTORY_PATH, Context.MODE_PRIVATE);
		return new BufferedInputStream(new FileInputStream(new File(dir, fileName)));
	}
	
	static protected void store(Properties properties) throws IOException {
		Context context  = DataSingleton.getInstance().getAppContext();
		File dir = context.getDir(Config.CONFIG_PROPERTIES_DIRECTORY_PATH, Context.MODE_PRIVATE);
		properties.store(new FileOutputStream(new File(dir, Config.CONFIG_LOCAL_FILE), false), null);
	}
}
