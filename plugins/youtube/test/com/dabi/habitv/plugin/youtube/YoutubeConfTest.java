package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class YoutubeConfTest {

	@Test
	public void normalizeApiKeyReturnsNullForNullInput() {
		assertNull(YoutubeConf.normalizeApiKey(null));
	}

	@Test
	public void normalizeApiKeyReturnsNullForBlankInput() {
		assertNull(YoutubeConf.normalizeApiKey("   "));
	}

	@Test
	public void normalizeApiKeyTrimsWhitespace() {
		assertEquals("api-key-value", YoutubeConf.normalizeApiKey("  api-key-value  "));
	}
}
