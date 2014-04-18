package com.perpetumobile.bit.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	protected void throwConfigPropertiesException(String msg) throws ConfigPropertiesException {
		logger.error(msg);
	}
	
	protected InputStream getInputStream(String fileName) 
	throws IOException {
		Context context  = DataSingleton.getInstance().getAppContext();
		File dir = context.getDir(Config.CONFIG_PROPERTIES_DIRECTORY_PATH, Context.MODE_PRIVATE);
		return new BufferedInputStream(new FileInputStream(new File(dir, fileName)));
	}
}
