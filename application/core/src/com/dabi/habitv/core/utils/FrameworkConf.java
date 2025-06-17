package com.dabi.habitv.core.utils;

/**
 * Local version of FrameworkConf to fix compilation issues.
 * This interface defines constants used throughout the application.
 */
public interface FrameworkConf {
    /**
     * User home directory with backslashes replaced by forward slashes.
     */
    String USER_HOME = System.getProperty("user.home").replace("\\", "/");
    
    /**
     * Version property name.
     */
    String VERSION = "version";
}