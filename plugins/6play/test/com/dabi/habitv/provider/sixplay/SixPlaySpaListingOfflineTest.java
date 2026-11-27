package com.dabi.habitv.provider.sixplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
 * Offline integration-style tests for {@link SixPlayPluginManager} with stubbed HTTP.
 */
public class SixPlaySpaListingOfflineTest {

	private static final String SITEMAP_FIXTURE = "test/resources/fixtures/6play/sitemap-service-snippet.xml";
	private static final String W9_FOLDER_FIXTURE = "test/resources/fixtures/6play/w9-series-folder-min.html";
	private static final String PROGRAM_FIXTURE = "test/resources/fixtures/6play/smallville-program-min.html";

	@Test
	public void homeUrlUsesHttpsPublicHost() {
		assertEquals("https://www.6play.fr/", SixPlayConf.HOME_URL);
		assertTrue(SixPlayConf.HOME_URL.startsWith("https://"));
	}

	@Test
	public void findCategoryBuildsW9ProgramsFromSitemapAndFolderFixture() throws IOException {
		final String sitemap = readUtf8(SITEMAP_FIXTURE);
		final String folderHtml = readUtf8(W9_FOLDER_FIXTURE);
		final SixPlayPluginManager plugin = new SixPlayPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				if (SixPlayConf.SITEMAP_SERVICE_URL.equals(url)) {
					return sitemap;
				}
				if (url.contains("/w9/series-w9-f_18")) {
					return folderHtml;
				}
				return "";
			}
		};

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertFalse(categories.isEmpty());
		CategoryDTO w9 = null;
		for (final CategoryDTO channel : categories) {
			if ("W9".equals(channel.getName())) {
				w9 = channel;
				break;
			}
		}
		assertTrue("W9 channel must be present", w9 != null);
		assertTrue("W9 must list programs from folder fixture",
				w9.getSubCategories() != null && w9.getSubCategories().size() >= 2);
	}

	@Test
	public void findEpisodeReturnsEpisodesFromProgramFixture() throws IOException {
		final String programHtml = readUtf8(PROGRAM_FIXTURE);
		final SixPlayPluginManager plugin = new SixPlayPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return programHtml;
			}
		};
		final CategoryDTO category = new CategoryDTO(SixPlayConf.NAME, "Smallville",
				"https://www.6play.fr/smallville-p_28305", SixPlayConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(2, episodes.size());
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
