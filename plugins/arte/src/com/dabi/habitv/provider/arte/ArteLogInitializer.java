package com.dabi.habitv.provider.arte;

import org.apache.log4j.PropertyConfigurator;

/**
 * Initializes log4j for the Arte plugin.
 */
public class ArteLogInitializer {

    private static boolean initialized = false;
    private static final Object initLock = new Object();

    /**
     * Initializes log4j for the Arte plugin.
     * This method is thread-safe and idempotent.
     */
    public static void initialize() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    try {
                        // Try to load log4j.properties from the classpath
                        PropertyConfigurator.configure(
                            ArteLogInitializer.class.getClassLoader().getResource("log4j.properties")
                        );
                        System.out.println("Arte plugin log4j initialized successfully");
                        initialized = true;
                    } catch (Exception e) {
                        System.err.println("Failed to initialize log4j for Arte plugin: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Static initializer to ensure log4j is initialized when the class is loaded
    static {
        initialize();
    }
}