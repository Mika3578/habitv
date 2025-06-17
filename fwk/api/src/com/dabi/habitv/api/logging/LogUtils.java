package com.dabi.habitv.api.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Utility class for logging operations.
 * This class provides static methods for common logging operations.
 * 
 * @deprecated Use {@link HabitvLogger} for new code. This class is maintained for backward compatibility.
 */
@Deprecated
public final class LogUtils {

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private LogUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Gets a logger for the specified class.
	 * 
	 * @param clazz the class to get a logger for
	 * @return the logger instance
	 */
	public static Logger getLogger(final Class<?> clazz) {
		return HabitvLogger.getLogger(clazz);
	}

	/**
	 * Gets a logger for the specified name.
	 * 
	 * @param name the name to get a logger for
	 * @return the logger instance
	 */
	public static Logger getLogger(final String name) {
		return HabitvLogger.getLogger(name);
	}

	/**
	 * Gets the root logger.
	 * 
	 * @return the root logger instance
	 */
	public static Logger getRootLogger() {
		return HabitvLogger.getRootLogger();
	}

	/**
	 * Updates the log4j configuration with the log file path.
	 * 
	 * @deprecated Use {@link HabitvLogger#updateConfiguration(Properties)} instead.
	 */
	@Deprecated
	public static void updateLog4jConfiguration() {
		Properties props = new Properties();
		try {
			InputStream configStream = ClassLoader.getSystemClassLoader()
					.getResourceAsStream("log4j.properties");
			if (configStream != null) {
				props.load(configStream);
				configStream.close();
			}
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		// Set default log file path if not using DirUtils
		if (!props.containsKey("log4j.appender.file.File")) {
			props.setProperty("log4j.appender.file.File", 
					System.getProperty("user.home") + "/habitv/habiTv.log");
		}
		
		HabitvLogger.updateConfiguration(props);
	}
}