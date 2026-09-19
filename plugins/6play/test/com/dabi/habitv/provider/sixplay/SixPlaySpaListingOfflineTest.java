package com.dabi.habitv.provider.sixplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Offline coverage for the current SPA homepage shape that no longer includes
 * the legacy catalogue selectors used by {@link SixPlayPluginManager}.
 */
public class SixPlaySpaListingOfflineTest {

	private static final String SPA_HOME_FIXTURE = "test/resources/fixtures/6play/spa-home-empty.html";

	@Test
	public void homeUrlUsesHttpsPublicHost() {
		assertEquals("https://www.6play.fr/", SixPlayConf.HOME_URL);
		assertTrue(SixPlayConf.HOME_URL.startsWith("https://"));
	}

	@Test
	public void findCategoryReturnsEmptyForSpaHomeWithoutLegacySelectors() throws IOException {
		final String html = readUtf8(SPA_HOME_FIXTURE);
		final SixPlayPluginManager plugin = new SixPlayPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return html;
			}
		};

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertTrue("SPA homepage without legacy markup must not invent categories", categories.isEmpty());
	}

	@Test
	public void findEpisodeReturnsEmptyWhenLegacyEpisodeMarkupMissing() throws IOException {
		final String html = readUtf8(SPA_HOME_FIXTURE);
		final SixPlayPluginManager plugin = new SixPlayPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return html;
			}
		};
		final CategoryDTO category = new CategoryDTO(SixPlayConf.NAME, "sample",
				"https://www.6play.fr/sample-program", SixPlayConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
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
