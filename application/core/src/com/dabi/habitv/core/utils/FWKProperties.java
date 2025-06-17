package com.dabi.habitv.core.utils;

/**
 * Local version of FWKProperties to fix compilation issues.
 * This class provides utility methods for accessing framework properties.
 */
public class FWKProperties {
    
    private FWKProperties() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the version of the application.
     * 
     * @return the version string
     */
    public static String getVersion() {
        final String version = System.getProperty("habitv.version");
        return version == null ? "4.1.0-SNAPSHOT" : version;
    }
}