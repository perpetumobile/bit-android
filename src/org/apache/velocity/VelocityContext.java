package org.apache.velocity;

/**
 * Velocity is not included in bit-android project.
 * Provide no-op interface for the rest of the library to work without it.
 * 
 * @author Zoran Dukic
 */
public interface VelocityContext {
	public Object get(String key);
}
