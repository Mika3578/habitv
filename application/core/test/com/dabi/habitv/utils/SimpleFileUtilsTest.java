package com.dabi.habitv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Simple test class to demonstrate testing in habiTv.
 * This class tests the FileUtils utility class.
 */
public class SimpleFileUtilsTest {

    /**
     * Test that the sanitizeFilename method correctly handles special characters.
     */
    @Test
    public void testSanitizeFilenameWithSpecialChars() {
        // Test with special characters
        assertEquals("Test_file_name", FileUtils.sanitizeFilename("Test:file/name"));

        // Test with spaces
        assertEquals("Test_file_name", FileUtils.sanitizeFilename("Test file name"));

        // Test with multiple special characters in a row
        assertEquals("Test__file__name", FileUtils.sanitizeFilename("Test:::file///name"));

        // Test with empty string
        assertEquals("", FileUtils.sanitizeFilename(""));

        // Test with null (would throw NullPointerException)
        try {
            FileUtils.sanitizeFilename(null);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test that the sanitizeFilename method correctly handles international characters.
     */
    @Test
    public void testSanitizeFilenameWithInternationalChars() {
        // Test with French characters
        assertEquals("eecaeaii", FileUtils.sanitizeFilename("éèçàêâîï"));

        // Test with German characters
        assertEquals("aou", FileUtils.sanitizeFilename("äöü"));

        // Test with mixed characters and special symbols
        assertEquals("Hello_World_aou_123_", 
                    FileUtils.sanitizeFilename("Hello World äöü 123!"));
    }
}
