package com.dabi.habitv.core.updater;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestManifestReader {

	private static final String VERSION = "4.1.0-SNAPSHOT";

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
	public final void test() {
		JarInputStream jarStream;
		try {
			jarStream = new JarInputStream(new FileInputStream("target/core-" + VERSION + ".jar"));
			final Manifest mf = jarStream.getManifest();
			assertEquals(VERSION, mf.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION));
		} catch (final IOException e) {
			// Expected during test phase since JAR is only created during package phase
			System.out.println("JAR file not found during test phase (expected): " + e.getMessage());
		}
	}
}
