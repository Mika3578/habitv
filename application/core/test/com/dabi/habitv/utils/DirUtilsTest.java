package com.dabi.habitv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for DirUtils utility class.
 * Demonstrates how to test directory operations in habiTv.
 */
public class DirUtilsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String testDirPath;
    private String testFilePath;

    @Before
    public void setUp() throws IOException {
        testDirPath = tempFolder.getRoot().toPath().resolve("testDir").toString();
        testFilePath = tempFolder.getRoot().toPath().resolve("testFile.txt").toString();
    }

    @After
    public void tearDown() {
        // Clean up any created files or directories
        try {
            Files.deleteIfExists(Paths.get(testFilePath));
            Files.deleteIfExists(Paths.get(testDirPath));
        } catch (IOException e) {
            System.err.println("Error cleaning up test files: " + e.getMessage());
        }
    }

    /**
     * Test creating a directory that doesn't exist.
     */
    @Test
    public void testCreateDirIfNotExist() {
        assertTrue("Directory should be created successfully", DirUtils.createDirIfNotExist(testDirPath));
        assertTrue("Directory should exist after creation", Files.exists(Paths.get(testDirPath)));

        // Test creating a directory that already exists
        assertTrue("Should return true for existing directory", DirUtils.createDirIfNotExist(testDirPath));
    }

    /**
     * Test checking if a directory exists.
     */
    @Test
    public void testDirExists() {
        // Directory doesn't exist initially
        assertFalse("Directory should not exist initially", DirUtils.dirExists(testDirPath));

        // Create directory
        try {
            Files.createDirectory(Paths.get(testDirPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test directory", e);
        }

        // Directory should exist now
        assertTrue("Directory should exist after creation", DirUtils.dirExists(testDirPath));
    }

    /**
     * Test checking if a file exists.
     */
    @Test
    public void testFileExists() {
        // File doesn't exist initially
        assertFalse("File should not exist initially", DirUtils.fileExists(testFilePath));

        // Create file
        try {
            Files.createFile(Paths.get(testFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }

        // File should exist now
        assertTrue("File should exist after creation", DirUtils.fileExists(testFilePath));
    }

    /**
     * Test deleting a file.
     */
    @Test
    public void testDeleteFileIfExist() throws IOException {
        // Create file
        Files.createFile(Paths.get(testFilePath));

        // File should exist
        assertTrue("File should exist after creation", Files.exists(Paths.get(testFilePath)));

        // Delete file
        assertTrue("Should return true when deleting existing file", DirUtils.deleteFileIfExist(testFilePath));

        // File should not exist after deletion
        assertFalse("File should not exist after deletion", Files.exists(Paths.get(testFilePath)));

        // Deleting non-existent file should return true
        assertTrue("Should return true when file doesn't exist", DirUtils.deleteFileIfExist(testFilePath));
    }

    /**
     * Test getting parent directory.
     */
    @Test
    public void testGetParentDir() {
        String filePath = tempFolder.getRoot().toPath().resolve("subdir").resolve("file.txt").toString();
        String expectedParent = tempFolder.getRoot().toPath().resolve("subdir").toString();

        assertEquals("Parent directory should match expected value", expectedParent, DirUtils.getParentDir(filePath));
    }

    /**
     * Test constants in DirUtils.
     */
    @Test
    public void testConstants() {
        assertEquals("SEPARATOR should match File.separator", File.separator, DirUtils.SEPARATOR);
        assertEquals("PATH_SEPARATOR should match File.pathSeparator", File.pathSeparator, DirUtils.PATH_SEPARATOR);
        assertNotNull("TEMP_DIR should not be null", DirUtils.TEMP_DIR);
        assertNotNull("USER_HOME should not be null", DirUtils.USER_HOME);
        assertNotNull("USER_DIR should not be null", DirUtils.USER_DIR);
    }
}
