package com.dabi.habitv.core.utils;

/**
 * Local version of OSUtils to fix compilation issues.
 * This class provides utility methods for operating system detection.
 */
public class OSUtils {
    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Checks if the current operating system is Windows.
     * 
     * @return true if the operating system is Windows, false otherwise
     */
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
}