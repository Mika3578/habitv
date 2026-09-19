package com.dabi.habitv.provider.tvcom;

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

public class TvComOfflineCatalogTest {

	@Test
	public void findCategoryParsesEmissionShowLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvComUrls.emissionsIndexUrl(), read("test/resources/fixtures/tvcom/emissions-index.html"));
		final TvComPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(5, categories.size());
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(TvComConf.CATEGORY_SHOW_PREFIX));
		}
	}

	@Test
	public void findEpisodeParsesShowEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvComUrls.showPageUrl("coin-lecture"),
				read("test/resources/fixtures/tvcom/show-coin-lecture.html"));
		final TvComPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvComConf.NAME, "Coin Lecture",
				TvComUrls.showCategoryId("coin-lecture"), TvComConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(4, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvComUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertNotNull(episode.getMetadata());
			assertEquals(TvComConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			assertFalse(episode.getName().isEmpty());
		}
	}

	@Test
	public void resolveHlsFromFreecasterEmbed() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524",
				read("test/resources/fixtures/tvcom/episode.html"));
		pages.put(TvComUrls.freecasterEmbedUrl("a2981fdf-9ec4-4d17-b423-c1a6807049fc"),
				read("test/resources/fixtures/tvcom/embed.html"));
		final TvComClient client = new TvComClient(new TvComClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				final String body = pages.get(url);
				if (body == null) {
					throw new IOException("unexpected " + url);
				}
				return body;
			}
		});
		final String hls = client.resolveHlsUrl(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524");
		assertNotNull(hls);
		assertTrue(hls.contains(".m3u8"));
		assertTrue(hls.startsWith("https://tvlocales-vod-cmaf.freecaster.com/"));
	}

	@Test
	public void canDownloadAcceptsEpisodePathsOnly() {
		final TvComPluginManager plugin = new TvComPluginManager();
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524"));
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://tvcom.be/replay/emissions/emission-speciale-confreries/58494"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.tvcom.be/emission/coin-lecture"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(
				"https://user:pass@www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TvComDiagnostics diagnostics = new TvComDiagnostics("download");
		diagnostics.setSourceUrl(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains(
				"sourceUrl=https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524"));
		assertFalse(line.contains("token=leak"));
	}

	private static TvComPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new TvComPluginManager(new TvComClient(new TvComClient.ContentLoader() {
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
