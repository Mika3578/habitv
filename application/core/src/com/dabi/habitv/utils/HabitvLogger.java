package com.dabi.habitv.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

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
    private static final String LOG_DIR = "log";
    
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
            // Create log directory if it doesn't exist
            createLogDirectory();
            
            // Load configuration
            Properties config = loadConfiguration();
            
            // Configure log4j
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(config);
            
            // Log successful initialization
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.info("habiTv logging system initialized successfully");
            
        } catch (Exception e) {
            // Fallback to basic console logging if configuration fails
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
        
        // Try to load the unified configuration first
        InputStream configStream = HabitvLogger.class.getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE);
        
        if (configStream == null) {
            // Fallback to standard log4j.properties
            configStream = HabitvLogger.class.getClassLoader()
                    .getResourceAsStream(FALLBACK_CONFIG_FILE);
        }
        
        if (configStream != null) {
            config.load(configStream);
            configStream.close();
            
            // Update log file path to use absolute path
            String logFile = config.getProperty("log4j.appender.file.File");
            if (logFile != null && logFile.startsWith("log/")) {
                String absoluteLogPath = new File(LOG_DIR, "habitv.log").getAbsolutePath();
                config.setProperty("log4j.appender.file.File", absoluteLogPath);
            }
        } else {
            // Create default configuration if no config file found
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
        config.setProperty("log4j.appender.console.layout.ConversionPattern", 
                "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5p] [%c{1}] %m%n");
        
        // File appender
        config.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
        config.setProperty("log4j.appender.file.File", new File(LOG_DIR, "habitv.log").getAbsolutePath());
        config.setProperty("log4j.appender.file.MaxFileSize", "10MB");
        config.setProperty("log4j.appender.file.MaxBackupIndex", "10");
        config.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
        config.setProperty("log4j.appender.file.layout.ConversionPattern", 
                "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5p] [%c{1}] %m%n");
        
        return config;
    }
    
    /**
     * Creates the log directory if it doesn't exist.
     */
    private static void createLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                System.out.println("Created log directory: " + logDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create log directory: " + logDir.getAbsolutePath());
            }
        }
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
    
    /**
     * Reloads the logging configuration from the configuration file.
     */
    public static void reloadConfiguration() {
        synchronized (initLock) {
            initialized = false;
            initialize();
        }
    }
} 