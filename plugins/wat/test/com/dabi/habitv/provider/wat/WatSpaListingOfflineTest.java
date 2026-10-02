package com.dabi.habitv.provider.wat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class WatSpaListingOfflineTest {

	private static final String SPA_FIXTURE = "test/resources/fixtures/wat/spa-programmes-empty.html";
	private static final String BASELINE = "test/resources/fixtures/wat/fixture-baseline.txt";

	@Test
	public void urlsUseHttps() {
		assertEquals("https://www.tf1.fr", WatConf.HOME_URL);
		assertEquals("https://www.tf1.fr/programmes-tv", WatConf.PROGRAMME_URL);
	}

	@Test
	public void fixtureBaselineDocumentsObsoleteBoundary() throws IOException {
		assertTrue(new File(BASELINE).exists());
		final String content = readUtf8(BASELINE);
		assertTrue(content.contains("provider=wat"));
		assertTrue(content.contains("parserBoundary=legacy-html-tf1-obsolete"));
		assertTrue(content.contains("network=disabled"));
	}

	@Test
	public void findCategoryReturnsEmptyForSpaProgrammesPage() throws IOException {
		final String html = readUtf8(SPA_FIXTURE);
		final WatPluginManager plugin = new WatPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return html;
			}
		};
		assertTrue(plugin.findCategory().isEmpty());
	}

	@Test
	public void findEpisodeReturnsEmptyWithoutLegacyMarkup() throws IOException {
		final String html = readUtf8(SPA_FIXTURE);
		final WatPluginManager plugin = new WatPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return html;
			}
		};
		final CategoryDTO category = new CategoryDTO(WatConf.NAME, "sample", "https://www.tf1.fr/sample",
				WatConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
	}

	private static String readUtf8(final String path) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
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
