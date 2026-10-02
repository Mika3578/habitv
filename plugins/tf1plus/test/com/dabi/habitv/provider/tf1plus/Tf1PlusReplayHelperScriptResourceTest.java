package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

public class Tf1PlusReplayHelperScriptResourceTest {

	@Test
	public void bundledScriptIsOnPluginClassLoaderClasspath() {
		final InputStream resource = Tf1PlusReplayHelperExecutor.class.getClassLoader()
				.getResourceAsStream("scripts/tf1plus_helper.py");
		assertNotNull("expected scripts/tf1plus_helper.py in tf1plus test classpath", resource);
		try {
			resource.close();
		} catch (Exception e) {
			// ignore
		}
	}
}
