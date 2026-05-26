package com.dabi.habitv.core.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.configuration.entities.Configuration;

public class XMLUserConfigTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void embedSubtitlesDefaultsToFalseForNewConfig() throws Exception {
		final XMLUserConfig userConfig = newConfigInstance();
		assertFalse(userConfig.getEmbedSubtitles());
	}

	private static XMLUserConfig newConfigInstance() throws Exception {
		final Method buildDefaultConfig = XMLUserConfig.class.getDeclaredMethod(
				"buildDefaultConfig");
		buildDefaultConfig.setAccessible(true);
		final Configuration configuration = (Configuration) buildDefaultConfig
				.invoke(null);
		final Constructor<XMLUserConfig> constructor = XMLUserConfig.class
				.getDeclaredConstructor(Configuration.class);
		constructor.setAccessible(true);
		return constructor.newInstance(configuration);
	}

	@Test
	@Ignore
	public void testInitConfig() throws IOException {
		File configFile = new File("config.xml");
		configFile.delete();
		Files.copy(new File("testOldConfig.xml").toPath(), configFile.toPath());
		UserConfig config = XMLUserConfig.initConfig();
		assertNotNull(config);
	}

}
