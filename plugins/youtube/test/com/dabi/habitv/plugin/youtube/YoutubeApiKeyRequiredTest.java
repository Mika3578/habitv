package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;

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
		Assume.assumeTrue(YoutubeConf.resolveApiKey() == null);
		try {
			YoutubePluginManager.requireApiKey();
			fail("Expected TechnicalException when YouTube API key is missing");
		} catch (TechnicalException e) {
			String message = e.getMessage();
			assertNotNull(message);
			assertTrue(message.contains("HABITV_YOUTUBE_API_KEY"));
			assertTrue(message.contains("habitv.youtube.apiKey"));
			assertTrue(message.contains("habitv configuration"));
		}
	}

	@Test
	public void requireApiKeyAcceptsConfiguredKey() {
		System.setProperty(API_KEY_PROPERTY, "test-key");
		YoutubePluginManager.requireApiKey();
	}

	@Test
	public void findEpisodeFailsFastBeforeAnyHttpCallWhenApiKeyMissing() {
		Assume.assumeTrue(YoutubeConf.resolveApiKey() == null);
		try {
			new YoutubePluginManagerNoHttpCall().findEpisode(buildPlaylistCategory());
			fail("Expected TechnicalException when YouTube API key is missing");
		} catch (TechnicalException e) {
			assertTrue(e.getMessage().contains("HABITV_YOUTUBE_API_KEY"));
		}
	}

	private CategoryDTO buildPlaylistCategory() {
		CategoryDTO parent = new CategoryDTO(YoutubeConf.NAME, "Playlist", "playlist-parent", FrameworkConf.MP4);
		CategoryDTO child = new CategoryDTO(YoutubeConf.NAME, "Any playlist", "playlistId=test-playlist", FrameworkConf.MP4);
		parent.addSubCategory(child);
		return child;
	}

	private static class YoutubePluginManagerNoHttpCall extends YoutubePluginManager {
		@Override
		public InputStream getInputStreamFromUrl(String url) {
			throw new AssertionError("Unexpected HTTP call: " + url);
		}
	}
}
