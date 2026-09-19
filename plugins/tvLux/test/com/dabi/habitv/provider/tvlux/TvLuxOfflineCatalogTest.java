package com.dabi.habitv.provider.tvlux;

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

public class TvLuxOfflineCatalogTest {

	@Test
	public void findCategoryParsesReplayShowLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.replayIndexUrl(), read("test/resources/fixtures/tvlux/replay-index.html"));
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(3, categories.size());
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(TvLuxConf.CATEGORY_SHOW_PREFIX));
		}
	}

	@Test
	public void findEpisodeParsesShowEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.showPageUrl("jt"), read("test/resources/fixtures/tvlux/show-jt.html"));
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvLuxConf.NAME, "JT", TvLuxUrls.showCategoryId("jt"),
				TvLuxConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(3, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvLuxUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertNotNull(episode.getMetadata());
			assertEquals(TvLuxConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void resolveHlsFromFreecasterEmbed() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260",
				read("test/resources/fixtures/tvlux/episode.html"));
		pages.put(TvLuxUrls.freecasterEmbedUrl("a2c6c841-5ee4-4eec-91c7-0c9197809e42"),
				read("test/resources/fixtures/tvlux/embed.html"));
		final TvLuxClient client = new TvLuxClient(new TvLuxClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				final String body = pages.get(url);
				if (body == null) {
					throw new IOException("unexpected " + url);
				}
				return body;
			}
		});
		final String hls = client.resolveHlsUrl("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260");
		assertNotNull(hls);
		assertTrue(hls.contains(".m3u8"));
		assertTrue(hls.startsWith("https://tvlocales-vod-cmaf.freecaster.com/"));
	}

	@Test
	public void canDownloadAcceptsEpisodePathsOnly() {
		final TvLuxPluginManager plugin = new TvLuxPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.tvlux.be/replay/jt"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TvLuxDiagnostics diagnostics = new TvLuxDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertFalse(line.contains("token=leak"));
	}

	private static TvLuxPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new TvLuxPluginManager(new TvLuxClient(new TvLuxClient.ContentLoader() {
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
