package com.dabi.habitv.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.core.config.XMLUserConfig;
import com.dabi.habitv.framework.FrameworkConf;

/**
 * Utility class for directory operations.
 * This class provides static methods for common directory operations.
 */
public final class DirUtils {

	/** Default directory separator */
	public static final String SEPARATOR = File.separator;

	/** Default path separator */
	public static final String PATH_SEPARATOR = File.pathSeparator;

	/** Default temporary directory */
	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	/** Default user home directory */
	public static final String USER_HOME = System.getProperty("user.home");

	/** Default user directory */
	public static final String USER_DIR = System.getProperty("user.dir");

	/** Default line separator */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** Default file separator */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/** Default path separator */
	public static final String PATH_SEPARATOR_SYSTEM = System.getProperty("path.separator");

	/** Application user home directory */
	public static final String APP_USER_HOME_DIR = FrameworkConf.USER_HOME + "/" + "habitv";

	/** Local path for the application */
	public static final String LOCAL_PATH = getLocalPath();

	/** User configuration file path */
	public static final String USER_CONF_PATH = APP_USER_HOME_DIR + "/" + HabitTvConf.CONF_FILE;

	/** Local configuration file path */
	public static final String LOCAL_CONF_PATH = LOCAL_PATH + "/" + HabitTvConf.CONF_FILE;

	/** User old configuration file path */
	public static final String USER_OLD_CONF_PATH = APP_USER_HOME_DIR + "/" + HabitTvConf.OLD_CONF_FILE;

	/** Local old configuration file path */
	public static final String LOCAL_OLD_CONF_PATH = LOCAL_PATH + "/" + HabitTvConf.OLD_CONF_FILE;

	/** Local grab configuration file path */
	public static final String LOCAL_GRABCONFIG_PATH = LOCAL_PATH + "/" + HabitTvConf.GRABCONFIG_XML_FILE;

	/** Flag indicating if running in local mode */
	public static final boolean IS_LOCAL_MODE = exists(LOCAL_CONF_PATH) || exists(LOCAL_OLD_CONF_PATH) 
			|| exists(LOCAL_GRABCONFIG_PATH);

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private DirUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a directory if it doesn't exist.
	 * 
	 * @param path the directory path to create
	 * @return true if the directory was created or already exists, false otherwise
	 */
	public static boolean createDirIfNotExist(final String path) {
		try {
			Path dirPath = Paths.get(path);
			if (!Files.exists(dirPath)) {
				Files.createDirectories(dirPath);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Checks if a directory exists.
	 * 
	 * @param path the directory path to check
	 * @return true if the directory exists, false otherwise
	 */
	public static boolean dirExists(final String path) {
		return Files.exists(Paths.get(path));
	}

	/**
	 * Checks if a file exists.
	 * 
	 * @param path the file path to check
	 * @return true if the file exists, false otherwise
	 */
	public static boolean fileExists(final String path) {
		return Files.exists(Paths.get(path));
	}

	/**
	 * Deletes a file if it exists.
	 * 
	 * @param path the file path to delete
	 * @return true if the file was deleted or didn't exist, false otherwise
	 */
	public static boolean deleteFileIfExist(final String path) {
		try {
			Path filePath = Paths.get(path);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Gets the parent directory of a file.
	 * 
	 * @param path the file path
	 * @return the parent directory path, or null if no parent exists
	 */
	public static String getParentDir(final String path) {
		Path filePath = Paths.get(path);
		Path parent = filePath.getParent();
		return parent != null ? parent.toString() : null;
	}

	/**
	 * Checks if a file exists at the given path.
	 * 
	 * @param path the file path to check
	 * @return true if the file exists, false otherwise
	 */
	private static boolean exists(final String path) {
		return (new File(path)).exists();
	}

	/**
	 * Gets the application directory based on local mode.
	 * 
	 * @return the application directory path
	 */
	public static String getAppDir() {
		return IS_LOCAL_MODE ? LOCAL_PATH : APP_USER_HOME_DIR;
	}

	/**
	 * Gets the local path for the application.
	 * 
	 * @return the local path
	 */
	private static String getLocalPath() {
		String absolutePath = new File(XMLUserConfig.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath()).getAbsolutePath();
		return absolutePath.endsWith("\\target\\classes") ? absolutePath.replace("\\target\\classes", "") 
				: absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
	}

	/**
	 * Gets the grab configuration file path.
	 * 
	 * @return the grab configuration file path
	 */
	public static String getGrabConfigPath() {
		return getAppDir() + "/" + HabitTvConf.GRABCONFIG_XML_FILE;
	}

	/**
	 * Gets the configuration file path.
	 * 
	 * @return the configuration file path
	 */
	public static String getConfFile() {
		String confPath = getAppDir() + "/" + HabitTvConf.CONF_FILE;
		System.out.println("[Habitv] Debug: Resolved configuration file path: " + confPath);
		System.out.println("[Habitv] Debug: App directory: " + getAppDir());
		System.out.println("[Habitv] Debug: Is local mode: " + IS_LOCAL_MODE);
		return confPath;
	}

	/**
	 * Gets the old configuration file path.
	 * 
	 * @return the old configuration file path
	 */
	public static String getOldConfFile() {
		String oldConfPath = getAppDir() + "/" + HabitTvConf.OLD_CONF_FILE;
		System.out.println("[Habitv] Debug: Resolved old configuration file path: " + oldConfPath);
		return oldConfPath;
	}

	/**
	 * Gets the log file path.
	 * 
	 * @return the log file path
	 */
	public static String getLogFile() {
		return getAppDir() + "/" + HabitTvConf.LOG_FILE;
	}

	/**
	 * Gets the log file path with forward slashes.
	 * 
	 * @return the log file path with forward slashes
	 */
	public static String getLogFileSlash() {
		return getLogFile().replace("\\", "/");
	}
}
