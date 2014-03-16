package com.perpetumobile.bit.config;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.perpetumobile.bit.android.DataSingleton;
import com.perpetumobile.bit.util.Logger;

public class AssetConfigProperties extends ConfigProperties {
	static private Logger logger = new Logger(AssetConfigProperties.class);
	
	public AssetConfigProperties() {
	}
	
	public AssetConfigProperties(String fileName) throws ConfigPropertiesException {
		super(fileName);
	}

	protected void throwConfigPropertiesException(String msg) throws ConfigPropertiesException {
		logger.error(msg);
	}
	
	protected InputStream getInputStream(String fileName) 
	throws IOException {
		AssetManager am  = DataSingleton.getInstance().getAssetManager();
		return am.open("properties/"+fileName);
	}
}
