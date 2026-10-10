package com.dabi.habitv.provider.clubic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Offline coverage for the current Clubic /video page that embeds public
 * YouTube Shorts in a Next.js payload instead of legacy .listingChaine markup.
 */
public class ClubicYoutubeShortsOfflineTest {

	private static final String SPA_SHORTS_FIXTURE = "test/resources/fixtures/clubic/spa-shorts.html";
	private static final String SPA_EMPTY_FIXTURE = "test/resources/fixtures/clubic/spa-empty.html";

	@Test
	public void homeUrlUsesHttpsPublicHost() {
		assertEquals("https://www.clubic.com/video", ClubicConf.HOME_VIDEO_URL);
		assertTrue(ClubicConf.HOME_URL.startsWith("https://"));
	}

	@Test
	public void findCategoryReturnsShortsCategoryForCurrentSpaPage() throws IOException {
		final String html = readUtf8(SPA_SHORTS_FIXTURE);
		final ClubicPluginManager plugin = new ClubicPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				return html;
			}
		};

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO category = categories.iterator().next();
		assertEquals(ClubicConf.CATEGORY_SHORTS, category.getName());
		assertEquals(ClubicConf.HOME_VIDEO_URL, category.getId());
		assertTrue(category.isDownloadable());
	}

	@Test
	public void findEpisodeParsesYoutubeShortUrlsFromFlightPayload() throws IOException {
		final String html = readUtf8(SPA_SHORTS_FIXTURE);
		final ClubicPluginManager plugin = new ClubicPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				return html;
			}
		};
		final CategoryDTO category = new CategoryDTO(ClubicConf.NAME, ClubicConf.CATEGORY_SHORTS,
				ClubicConf.HOME_VIDEO_URL, ClubicConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(3, episodes.size());
		final EpisodeDTO first = new ArrayList<EpisodeDTO>(episodes).get(0);
		assertTrue(first.getId().startsWith(ClubicConf.YOUTUBE_SHORTS_URL_PREFIX));
		assertTrue(first.getName().length() > 5);
	}

	@Test
	public void findCategoryReturnsEmptyWhenNoLegacyOrShortsMarkup() throws IOException {
		final String html = readUtf8(SPA_EMPTY_FIXTURE);
		final ClubicPluginManager plugin = new ClubicPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				return html;
			}
		};
		assertTrue(plugin.findCategory().isEmpty());
	}

	private static String readUtf8(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return output.toString("UTF-8");
		}
	}
}
