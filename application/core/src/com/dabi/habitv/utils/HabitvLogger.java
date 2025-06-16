package com.dabi.habitv.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Centralized logging manager for habiTv application.
 * Provides unified logging setup with thread-safety and minimal performance impact.
 * 
 * @author habiTv Team
 * @version 4.1.0
 */
public final class HabitvLogger {

    private static final String DEFAULT_CONFIG_FILE = "habitv-log.properties";
    private static final String FALLBACK_CONFIG_FILE = "log4j.properties";

    private static volatile boolean initialized = false;
    private static final Object initLock = new Object();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private HabitvLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Initializes the logging system with the unified configuration.
     * This method is thread-safe and idempotent.
     */
    public static void initialize() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    doInitialize();
                    initialized = true;
                }
            }
        }
    }

    /**
     * Performs the actual initialization of the logging system.
     */
    private static void doInitialize() {
        try {
            // Chargement de la configuration
            Properties config = loadConfiguration();

            // Configuration de log4j
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(config);

            // Log d'initialisation
            Logger rootLogger = Logger.getRootLogger();
            String logFilePath = config.getProperty("log4j.appender.file.File");
            rootLogger.info("habiTv logging system initialized successfully");
            rootLogger.info("Log file path = " + logFilePath);

        } catch (Exception e) {
            // Fallback console logging
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            setupFallbackLogging();
        }
    }

    /**
     * Loads the logging configuration from properties file.
     * 
     * @return Properties object containing logging configuration
     * @throws IOException if configuration file cannot be loaded
     */
    private static Properties loadConfiguration() throws IOException {
        Properties config = new Properties();

        // Essayer de charger la configuration unifiée
        InputStream configStream = HabitvLogger.class.getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE);

        if (configStream == null) {
            // Fallback sur log4j.properties
            configStream = HabitvLogger.class.getClassLoader()
                    .getResourceAsStream(FALLBACK_CONFIG_FILE);
        }

        if (configStream != null) {
            config.load(configStream);
            configStream.close();
        } else {
            // Créer une configuration par défaut si aucun fichier trouvé
            config = createDefaultConfiguration();
        }

        return config;
    }

    /**
     * Creates a default logging configuration.
     * 
     * @return Properties object with default configuration
     */
    private static Properties createDefaultConfiguration() {
        Properties config = new Properties();

        // Root logger
        config.setProperty("log4j.rootLogger", "INFO, console, file");

        // Console appender
        config.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
        config.setProperty("log4j.appender.console.ImmediateFlush", "true");
        config.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
        config.setProperty("log4j.appender.console.layout.ConversionPattern", "%m%n");

        // File appender
        config.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
        config.setProperty("log4j.appender.file.File", System.getProperty("user.home") + "/habitv/habiTv.log");
        config.setProperty("log4j.appender.file.MaxFileSize", "10000KB");
        config.setProperty("log4j.appender.file.MaxBackupIndex", "30");
        config.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
        config.setProperty("log4j.appender.file.layout.ConversionPattern", "%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] %m%n");

        return config;
    }

    /**
     * Sets up fallback logging when configuration fails.
     */
    private static void setupFallbackLogging() {
        Properties fallback = new Properties();
        fallback.setProperty("log4j.rootLogger", "INFO, console");
        fallback.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
        fallback.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
        fallback.setProperty("log4j.appender.console.layout.ConversionPattern", 
                "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5p] [%c{1}] %m%n");

        LogManager.resetConfiguration();
        PropertyConfigurator.configure(fallback);

        Logger.getRootLogger().warn("Using fallback logging configuration");
    }

    /**
     * Gets a logger for the specified class.
     * Ensures logging system is initialized before returning logger.
     * 
     * @param clazz the class to get a logger for
     * @return the logger instance
     */
    public static Logger getLogger(final Class<?> clazz) {
        initialize();
        return Logger.getLogger(clazz);
    }

    /**
     * Gets a logger for the specified name.
     * Ensures logging system is initialized before returning logger.
     * 
     * @param name the name to get a logger for
     * @return the logger instance
     */
    public static Logger getLogger(final String name) {
        initialize();
        return Logger.getLogger(name);
    }

    /**
     * Gets the root logger.
     * Ensures logging system is initialized before returning logger.
     * 
     * @return the root logger instance
     */
    public static Logger getRootLogger() {
        initialize();
        return Logger.getRootLogger();
    }

    /**
     * Updates the logging configuration at runtime.
     * 
     * @param properties the new configuration properties
     */
    public static void updateConfiguration(Properties properties) {
        initialize();
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(properties);
        getRootLogger().info("Logging configuration updated");
    }
}
