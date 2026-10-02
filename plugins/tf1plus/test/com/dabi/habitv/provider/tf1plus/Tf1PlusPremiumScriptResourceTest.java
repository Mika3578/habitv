package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

public class Tf1PlusPremiumScriptResourceTest {

	@Test
	public void bundledScriptIsOnPluginClassLoaderClasspath() {
		final InputStream resource = Tf1PlusPremiumDownloadExecutor.class.getClassLoader()
				.getResourceAsStream("scripts/tf1plus_premium_download.py");
		assertNotNull("expected scripts/tf1plus_premium_download.py in tf1plus test classpath", resource);
		try {
			resource.close();
		} catch (Exception e) {
			// ignore
		}
	}
}
