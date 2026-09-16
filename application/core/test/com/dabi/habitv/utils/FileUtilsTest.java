package com.dabi.habitv.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSanitizeFilename() {
		assertEquals("eecaeaii_", FileUtils.sanitizeFilename("éèçàêâîï$%"));
	}

	@Test
	public final void sanitizePathSegmentPreservesAccentsAndRemovesIllegalChars() {
		assertEquals("éèçàêâîï$%", FileUtils.sanitizePathSegment("éèçàêâîï$%"));
		assertEquals("C dans l'air", FileUtils.sanitizePathSegment("C dans l'air?"));
		assertEquals("Émission spéciale", FileUtils.sanitizePathSegment("Émission : spéciale"));
		assertEquals("", FileUtils.sanitizePathSegment("???"));
	}
}
