package com.dabi.habitv.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Tf1PlusPremiumReplaySystemPropertyTest {

	@Before
	public void setUp() {
		Tf1PlusPremiumReplaySystemProperty.resetStateForTests();
	}

	@After
	public void tearDown() {
		Tf1PlusPremiumReplaySystemProperty.resetStateForTests();
	}

	@Test
	public void shouldApplySettingsFromConfigurationXml() {
		final Tf1PlusPremiumReplaySettings settings = new Tf1PlusPremiumReplaySettings("user@example.com",
				"secret", "C:/Tools/tf1plus/device.bin", "C:/Tools/N_m3u8DL-RE.exe", null, null, "py -3");
		Tf1PlusPremiumReplaySystemProperty.applyFromUserConfig(settings);
		assertEquals("user@example.com", System.getProperty(Tf1PlusPremiumReplaySystemProperty.PROPERTY_EMAIL));
		assertEquals("C:/Tools/tf1plus/device.bin",
				System.getProperty(Tf1PlusPremiumReplaySystemProperty.PROPERTY_DEVICE_PATH));
		assertEquals("py -3", System.getProperty(Tf1PlusPremiumReplaySystemProperty.PROPERTY_PYTHON));
	}

	@Test
	public void shouldClearPropertiesAppliedFromUserConfig() {
		Tf1PlusPremiumReplaySystemProperty.applyFromUserConfig(new Tf1PlusPremiumReplaySettings(
				"user@example.com", "secret", "C:/Tools/tf1plus/device.bin", "C:/Tools/N_m3u8DL-RE.exe", null, null, null));
		Tf1PlusPremiumReplaySystemProperty.applyFromUserConfig(new Tf1PlusPremiumReplaySettings(null, null, null,
				null, null, null, null));
		assertNull(System.getProperty(Tf1PlusPremiumReplaySystemProperty.PROPERTY_EMAIL));
	}
}
