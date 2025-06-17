package com.dabi.habitv.utils;

import org.apache.log4j.Logger;

/**
 * Utility class for debug logging.
 * This class provides static methods for logging debug, info, warn, and error messages.
 * It replaces direct System.out/err calls with proper logging.
 */
public final class DebugLogger {

    private static final Logger LOG = HabitvLogger.getLogger(DebugLogger.class);

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private DebugLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Logs a debug message.
     * Replaces System.out.println calls for debug information.
     * 
     * @param message the message to log
     */
    public static void debug(String message) {
        LOG.debug(message);
    }

    /**
     * Logs an info message.
     * 
     * @param message the message to log
     */
    public static void info(String message) {
        LOG.info(message);
    }

    /**
     * Logs a warning message.
     * 
     * @param message the message to log
     */
    public static void warn(String message) {
        LOG.warn(message);
    }

    /**
     * Logs an error message.
     * Replaces System.err.println calls for error information.
     * 
     * @param message the message to log
     */
    public static void error(String message) {
        LOG.error(message);
    }

    /**
     * Logs an error message with an exception.
     * Replaces System.err.println calls for error information with stack traces.
     * 
     * @param message the message to log
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        LOG.error(message, throwable);
    }

    /**
     * Logs a debug message with a specific logger.
     * Useful when you want to log from a specific class.
     * 
     * @param logger the logger to use
     * @param message the message to log
     */
    public static void debug(Logger logger, String message) {
        logger.debug(message);
    }

    /**
     * Logs an info message with a specific logger.
     * 
     * @param logger the logger to use
     * @param message the message to log
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message with a specific logger.
     * 
     * @param logger the logger to use
     * @param message the message to log
     */
    public static void warn(Logger logger, String message) {
        logger.warn(message);
    }

    /**
     * Logs an error message with a specific logger.
     * 
     * @param logger the logger to use
     * @param message the message to log
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
    }

    /**
     * Logs an error message with an exception and a specific logger.
     * 
     * @param logger the logger to use
     * @param message the message to log
     * @param throwable the exception to log
     */
    public static void error(Logger logger, String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}