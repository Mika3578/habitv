package com.dabi.habitv.provider.telemb;

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

public class TeleMbOfflineCatalogTest {

	@Test
	public void findCategoryParsesEmissionShowLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TeleMbUrls.emissionsIndexUrl(), read("test/resources/fixtures/telemb/emissions-index.html"));
		final TeleMbPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(5, categories.size());
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(TeleMbConf.CATEGORY_SHOW_PREFIX));
		}
	}

	@Test
	public void findEpisodeParsesShowEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TeleMbUrls.showPageUrl("les-infos"), read("test/resources/fixtures/telemb/show-les-infos.html"));
		final TeleMbPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TeleMbConf.NAME, "Les infos", TeleMbUrls.showCategoryId("les-infos"),
				TeleMbConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(4, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TeleMbUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertNotNull(episode.getMetadata());
			assertEquals(TeleMbConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void resolveHlsFromFreecasterEmbed() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201",
				read("test/resources/fixtures/telemb/episode.html"));
		pages.put(TeleMbUrls.freecasterEmbedUrl("a2c66395-5dca-4317-b2ed-0e35f79e7568"),
				read("test/resources/fixtures/telemb/embed.html"));
		final TeleMbClient client = new TeleMbClient(new TeleMbClient.ContentLoader() {
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
				"https://www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201");
		assertNotNull(hls);
		assertTrue(hls.contains(".m3u8"));
		assertTrue(hls.startsWith("https://tvlocales-vod-cmaf.freecaster.com/"));
	}

	@Test
	public void canDownloadAcceptsShowAndCategoryEpisodePaths() {
		final TeleMbPluginManager plugin = new TeleMbPluginManager();
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201"));
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.telemb.be/replay/sports/basket-r2-colfontaine-tient-sa-premiere-victoire/41279"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.telemb.be/emission/les-infos"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(
				"https://user:pass@www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TeleMbDiagnostics diagnostics = new TeleMbDiagnostics("download");
		diagnostics.setSourceUrl(
				"https://www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains(
				"sourceUrl=https://www.telemb.be/replay/emission/les-infos/les-infos-du-samedi-19-septembre-2026/41201"));
		assertFalse(line.contains("token=leak"));
	}

	private static TeleMbPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new TeleMbPluginManager(new TeleMbClient(new TeleMbClient.ContentLoader() {
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
