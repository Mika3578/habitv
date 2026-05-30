package com.dabi.habitv.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class YoutubeApiKeyConfigTest {

	@Test
	public void sanitizePlainConfigValueTrimsWhitespace() {
		assertEquals(ConfigTestValues.YOUTUBE_API_KEY_PLAIN,
				YoutubeApiKeyConfig.sanitizePlainConfigValue("  " + ConfigTestValues.YOUTUBE_API_KEY_PLAIN + "  "));
	}

	@Test
	public void sanitizePlainConfigValueStripsSurroundingQuotes() {
		assertEquals(ConfigTestValues.YOUTUBE_API_KEY_PLAIN,
				YoutubeApiKeyConfig.sanitizePlainConfigValue("\"" + ConfigTestValues.YOUTUBE_API_KEY_PLAIN + "\""));
	}

	@Test
	public void sanitizePlainConfigValueReturnsNullForBlank() {
		assertNull(YoutubeApiKeyConfig.sanitizePlainConfigValue("   "));
	}
}
