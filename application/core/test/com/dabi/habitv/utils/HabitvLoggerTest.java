package com.dabi.habitv.utils;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test class for HabitvLogger functionality.
 */
public class HabitvLoggerTest {

    @Test
    public void testLoggerInitialization() {
        // Test that the logger can be initialized
        Logger logger = HabitvLogger.getLogger(HabitvLoggerTest.class);
        logger.info("Test log message from HabitvLoggerTest");
        logger.debug("Debug message from HabitvLoggerTest");
        logger.warn("Warning message from HabitvLoggerTest");
        logger.error("Error message from HabitvLoggerTest");
    }

    @Test
    public void testLoggerByName() {
        // Test logger by name
        Logger logger = HabitvLogger.getLogger("TestLogger");
        logger.info("Test log message by name");
    }

    @Test
    public void testRootLogger() {
        // Test root logger
        Logger rootLogger = HabitvLogger.getRootLogger();
        rootLogger.info("Test message from root logger");
    }

    @Test
    public void testMultipleInitializations() {
        // Test that multiple initializations don't cause issues
        HabitvLogger.initialize();
        HabitvLogger.initialize();
        HabitvLogger.initialize();
        
        Logger logger = HabitvLogger.getLogger(HabitvLoggerTest.class);
        logger.info("Multiple initialization test passed");
    }
} 