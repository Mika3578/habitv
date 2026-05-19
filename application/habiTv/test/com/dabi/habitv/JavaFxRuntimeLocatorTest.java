package com.dabi.habitv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavaFxRuntimeLocatorTest {

	private String previousJavaHome;
	private String previousOverride;

	@Before
	public void setUp() {
		previousJavaHome = System.getProperty("java.home");
		previousOverride = System.getProperty(JavaFxRuntimeLocator.JFXRT_PATH_PROPERTY);
	}

	@After
	public void tearDown() {
		restoreProperty("java.home", previousJavaHome);
		restoreProperty(JavaFxRuntimeLocator.JFXRT_PATH_PROPERTY, previousOverride);
	}

	@Test
	public void locateReturnsOverrideWhenFileExists() throws IOException {
		final File jar = Files.createTempFile("jfxrt", ".jar").toFile();
		jar.deleteOnExit();
		System.setProperty(JavaFxRuntimeLocator.JFXRT_PATH_PROPERTY, jar.getAbsolutePath());
		assertEquals(jar, JavaFxRuntimeLocator.locate());
	}

	@Test
	public void locateChecksLibLayout() throws IOException {
		clearOverride();
		final File fakeJavaHome = Files.createTempDirectory("fake-lib").toFile();
		fakeJavaHome.deleteOnExit();
		final File jfxrt = new File(fakeJavaHome, "lib/jfxrt.jar");
		assertTrue(jfxrt.getParentFile().mkdirs());
		assertTrue(jfxrt.createNewFile());
		System.setProperty("java.home", fakeJavaHome.getAbsolutePath());
		assertEquals(jfxrt, JavaFxRuntimeLocator.locate());
	}

	@Test
	public void locateChecksJreExtLayout() throws IOException {
		clearOverride();
		final File fakeJavaHome = Files.createTempDirectory("fake-jre").toFile();
		fakeJavaHome.deleteOnExit();
		final File jfxrt = new File(fakeJavaHome, "jre/lib/ext/jfxrt.jar");
		assertTrue(jfxrt.getParentFile().mkdirs());
		assertTrue(jfxrt.createNewFile());
		System.setProperty("java.home", fakeJavaHome.getAbsolutePath());
		assertEquals(jfxrt, JavaFxRuntimeLocator.locate());
	}

	@Test
	public void locateChecksLibExtLayout() throws IOException {
		clearOverride();
		final File fakeJavaHome = Files.createTempDirectory("fake-lib-ext").toFile();
		fakeJavaHome.deleteOnExit();
		final File jfxrt = new File(fakeJavaHome, "lib/ext/jfxrt.jar");
		assertTrue(jfxrt.getParentFile().mkdirs());
		assertTrue(jfxrt.createNewFile());
		System.setProperty("java.home", fakeJavaHome.getAbsolutePath());
		assertEquals(jfxrt, JavaFxRuntimeLocator.locate());
	}

	@Test
	public void locateChecksParentJdkLibFallback() throws IOException {
		clearOverride();
		final File fakeJdkHome = Files.createTempDirectory("fake-jdk").toFile();
		fakeJdkHome.deleteOnExit();
		final File fakeJavaHome = new File(fakeJdkHome, "jre");
		assertTrue(fakeJavaHome.mkdirs());
		final File jfxrt = new File(fakeJdkHome, "lib/jfxrt.jar");
		assertTrue(jfxrt.getParentFile().mkdirs());
		assertTrue(jfxrt.createNewFile());
		System.setProperty("java.home", fakeJavaHome.getAbsolutePath());
		assertEquals(jfxrt, JavaFxRuntimeLocator.locate());
	}

	@Test
	public void locateReturnsNullWhenMissing() throws IOException {
		clearOverride();
		final File fakeJavaHome = Files.createTempDirectory("no-javafx").toFile();
		fakeJavaHome.deleteOnExit();
		System.setProperty("java.home", fakeJavaHome.getAbsolutePath());
		assertNull(JavaFxRuntimeLocator.locate());
		assertNotNull(JavaFxRuntimeLocator.candidatePathsForDiagnostics());
	}

	private static void clearOverride() {
		System.clearProperty(JavaFxRuntimeLocator.JFXRT_PATH_PROPERTY);
	}

	private static void restoreProperty(final String key, final String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}
}
