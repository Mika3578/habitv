package com.dabi.habitv.provider.tvaplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class TvaPlusOfflineCatalogTest {

	@Test
	public void findCategoryKeepsPublicTvaShowsAndSkipsOtherBrands() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvaPlusUrls.tvaChannelUrl(), read("test/resources/fixtures/tvaplus/tva-channel.html"));
		final TvaPlusPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(3, categories.size());
		boolean sawRecent = false;
		boolean sawJe = false;
		boolean sawBienvenue = false;
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			if (TvaPlusConf.CATEGORY_RECENT.equals(category.getId())) {
				sawRecent = true;
				continue;
			}
			assertTrue(category.getId().startsWith(TvaPlusConf.CATEGORY_SHOW_PREFIX));
			assertTrue(category.getId().contains("/tva/"));
			assertFalse(category.getId().contains("/zeste/"));
			assertFalse(category.getId().contains("premium-show"));
			if (category.getId().endsWith("/tva/j-e")) {
				sawJe = true;
			}
			if (category.getId().endsWith("/tva/bienvenue")) {
				sawBienvenue = true;
			}
		}
		assertTrue(sawRecent);
		assertTrue(sawJe);
		assertTrue(sawBienvenue);
	}

	@Test
	public void findEpisodeWalksSeasonsAndFetchesEmptyCarouselPage() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvaplus.ca/tva/j-e", read("test/resources/fixtures/tvaplus/show-je.html"));
		pages.put("https://www.tvaplus.ca/tva/j-e/saison-34", read("test/resources/fixtures/tvaplus/season-34.html"));
		pages.put("https://www.tvaplus.ca/tva/j-e/saison-33", read("test/resources/fixtures/tvaplus/season-33.html"));
		pages.put("https://www.tvaplus.ca/tva/j-e/saison-33/tous-les-episodes",
				read("test/resources/fixtures/tvaplus/season-33-episodes.html"));
		final TvaPlusPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvaPlusConf.NAME, "J.E",
				TvaPlusUrls.showCategoryId("/tva/j-e"), TvaPlusConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvaPlusUrls.isTvaPlusEpisodeUrl(episode.getId()));
			assertNotNull(episode.getMetadata());
			assertEquals(TvaPlusConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			assertFalse(episode.getId().contains("trailer"));
			assertFalse(episode.getName().toLowerCase().contains("premium"));
		}
	}

	@Test
	public void findEpisodeRecentKeepsPublicTvaOnly() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvaPlusUrls.recentUrl(), read("test/resources/fixtures/tvaplus/recent.html"));
		final TvaPlusPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO recent = new CategoryDTO(TvaPlusConf.NAME, TvaPlusConf.RECENT_CATEGORY_NAME,
				TvaPlusConf.CATEGORY_RECENT, TvaPlusConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(recent);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().startsWith("https://www.tvaplus.ca/tva/"));
	}

	@Test
	public void canDownloadAcceptsEpisodePathsOnly() {
		final TvaPlusPluginManager plugin = new TvaPlusPluginManager();
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.tvaplus.ca/tva/j-e/saison-34/episode-973-2018367086"));
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.tvaplus.ca/tva/j-e/saison-34/episode-973-2018367086?x=1#frag"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.tvaplus.ca/tva/j-e"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://attacker.example/www.tvaplus.ca/tva/j-e/saison-1/episode-1-1"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@www.tvaplus.ca/tva/j-e/saison-1/episode-1-1"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TvaPlusDiagnostics diagnostics = new TvaPlusDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tvaplus.ca/tva/j-e/saison-1/episode-1-1?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tvaplus.ca/tva/j-e/saison-1/episode-1-1"));
		assertFalse(line.contains("token=leak"));
	}

	private static TvaPlusPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new TvaPlusPluginManager(new TvaPlusClient(new TvaPlusClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				final String body = pages.get(url);
				if (body == null) {
					throw new IOException("unexpected " + url);
				}
				return body;
			}
		}));
	}

	private static String read(final String path) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toString("UTF-8");
		}
	}
}
