package com.dabi.habitv.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class YoutubeApiKeySystemPropertyTest {

	private static final String EXTERNAL_KEY = "external-youtube-api-key-from-jvm";

	@Before
	public void setUp() {
		YoutubeApiKeySystemProperty.resetStateForTests();
		System.clearProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME);
	}

	@After
	public void tearDown() {
		YoutubeApiKeySystemProperty.resetStateForTests();
		System.clearProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME);
	}

	@Test
	public void applyFromUserConfigSetsSanitizedXmlKey() {
		YoutubeApiKeySystemProperty.applyFromUserConfig("  " + ConfigTestValues.YOUTUBE_API_KEY_PLAIN + "  ");
		assertEquals(ConfigTestValues.YOUTUBE_API_KEY_PLAIN,
				System.getProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME));
		assertTrue(YoutubeApiKeySystemProperty.isAppliedFromUserConfigForTests());
	}

	@Test
	public void blankXmlConfigPreservesExternalJvmProperty() {
		System.setProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME, EXTERNAL_KEY);
		YoutubeApiKeySystemProperty.applyFromUserConfig("   ");
		assertEquals(EXTERNAL_KEY, System.getProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME));
		assertFalse(YoutubeApiKeySystemProperty.isAppliedFromUserConfigForTests());
	}

	@Test
	public void blankXmlConfigClearsOnlyConfigAppliedProperty() {
		YoutubeApiKeySystemProperty.applyFromUserConfig(ConfigTestValues.YOUTUBE_API_KEY_PLAIN);
		YoutubeApiKeySystemProperty.applyFromUserConfig(null);
		assertNull(System.getProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME));
		assertFalse(YoutubeApiKeySystemProperty.isAppliedFromUserConfigForTests());
	}

	@Test
	public void xmlConfigOverridesExternalPropertyThenBlankConfigClears() {
		System.setProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME, EXTERNAL_KEY);
		YoutubeApiKeySystemProperty.applyFromUserConfig(ConfigTestValues.YOUTUBE_API_KEY_PLAIN);
		assertEquals(ConfigTestValues.YOUTUBE_API_KEY_PLAIN,
				System.getProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME));
		YoutubeApiKeySystemProperty.applyFromUserConfig("");
		assertNull(System.getProperty(YoutubeApiKeySystemProperty.PROPERTY_NAME));
	}
}
