package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class YoutubeApiKeyRequiredTest {

	private static final String API_KEY_PROPERTY = "habitv.youtube.apiKey";
	private String previousProperty;

	@Before
	public void saveProperty() {
		previousProperty = System.getProperty(API_KEY_PROPERTY);
		System.clearProperty(API_KEY_PROPERTY);
	}

	@After
	public void restoreProperty() {
		if (previousProperty == null) {
			System.clearProperty(API_KEY_PROPERTY);
		} else {
			System.setProperty(API_KEY_PROPERTY, previousProperty);
		}
	}

	@Test
	public void requireApiKeyFailsFastWithGuidanceWhenMissing() {
		if (YoutubeConf.resolveApiKey() != null) {
			return;
		}
		try {
			YoutubePluginManager.requireApiKey();
			fail("Expected TechnicalException when YouTube API key is missing");
		} catch (TechnicalException e) {
			String message = e.getMessage();
			assertNotNull(message);
			assertTrue(message.contains("HABITV_YOUTUBE_API_KEY"));
			assertTrue(message.contains("habitv.youtube.apiKey"));
		}
	}

	@Test
	public void requireApiKeyAcceptsConfiguredKey() {
		System.setProperty(API_KEY_PROPERTY, "test-key");
		YoutubePluginManager.requireApiKey();
	}
}
