package com.perpetumobile.bit.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 * @author Zoran Dukic
 */
final public class Logger {
	private java.util.logging.Logger logger = null;
	
	public Logger(String name) {
		logger = java.util.logging.Logger.getLogger(name);
	}
	
	public Logger(Class<?> clazz) {
		logger = java.util.logging.Logger.getLogger(clazz.getName());
	}
	
	private String createMessage(String message, Throwable t) {
		StringWriter str = new StringWriter();
		str.write(message);
		str.write('\n');
		t.printStackTrace(new PrintWriter(str));
		return str.toString();
	}
	
	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}
	
	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}
	
	/*
	 * Log a message with the FATAL Level. 
	 */
	public void fatal(String message) { 
		logger.severe(message);
	}
	
	/*
	 * Log a message with the FATAL level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void fatal(String message, Throwable t) {		
		logger.severe(createMessage(message, t));
	}
	
	/*
	 * Log a message with the ERROR Level. 
	 */
	public void error(String message) { 
		logger.warning(message);
	}
	
	/*
	 * Log a message with the ERROR level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void error(String message, Throwable t) {
		logger.warning(createMessage(message, t));
	}
	
	/*
	 * Log a message with the INFO Level. 
	 */
	public void info(String message) { 
		logger.info(message);
	}
	
	/*
	 * Log a message with the INFO level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void info(String message, Throwable t) {
		logger.info(createMessage(message, t));
	}
	
	/*
	 * Log a message with the DEBUG Level. 
	 */
	public void debug(String message) { 
		logger.fine(message);
	}
	
	/*
	 * Log a message with the DEBUG level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void debug(String message, Throwable t) {
		logger.fine(createMessage(message, t));
	}
}
