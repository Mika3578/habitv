package com.dabi.habitv.framework.plugin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for OSUtils
 */
public class OSUtilsTest {

    @Test
    public void testIsWindows() {
        // This test will pass or fail depending on the OS it's run on
        // Since we're running on Windows, we expect isWindows() to return true
        boolean isWindows = OSUtils.isWindows();
        System.out.println("[DEBUG_LOG] Running on Windows: " + isWindows);
        
        // Get the actual OS name for verification
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("[DEBUG_LOG] OS name: " + osName);
        
        // The expected result depends on the actual OS
        boolean expectedResult = osName.contains("win");
        
        assertEquals("isWindows() should return " + expectedResult + " on " + osName, 
                     expectedResult, isWindows);
    }
}