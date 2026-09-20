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
	public void findCategoryParsesBareTvluxHostLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.replayIndexUrl(),
				"<!DOCTYPE html><html><body>"
						+ "<a href=\"https://tvlux.be/replay/jt\">JT</a>"
						+ "<a href=\"/Replay/cine-lux\">Ciné mixed path</a>"
						+ "<a href=\"/replay/l-hebdo/\">Hebdo trailing slash</a>"
						+ "<a href=\"/replay/Page_2\">Pagination</a>"
						+ "<a href=\"/replay/bad%2Fslug\">Bad</a>"
						+ "<a href=\"/replay/JT\">JT upper</a>"
						+ "</body></html>");
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(3, categories.size());
		final Map<String, String> namesById = new HashMap<String, String>();
		for (final CategoryDTO category : categories) {
			namesById.put(category.getId(), category.getName());
		}
		assertEquals("JT", namesById.get(TvLuxUrls.showCategoryId("jt")));
		assertEquals("Ciné mixed path", namesById.get(TvLuxUrls.showCategoryId("cine-lux")));
		assertEquals("Hebdo trailing slash", namesById.get(TvLuxUrls.showCategoryId("l-hebdo")));
		assertFalse(namesById.containsKey(TvLuxUrls.showCategoryId("page_2")));
	}

	@Test
	public void showSlugFromCategoryIdRejectsUnsafeValues() {
		assertEquals("jt", TvLuxUrls.showSlugFromCategoryId(TvLuxUrls.showCategoryId("JT")));
		assertEquals(null, TvLuxUrls.showSlugFromCategoryId(TvLuxUrls.showCategoryId("bad%2Fslug")));
		assertEquals(null, TvLuxUrls.showSlugFromCategoryId(TvLuxUrls.showCategoryId("a?b")));
		assertEquals(null, TvLuxUrls.showSlugFromCategoryId(TvLuxConf.CATEGORY_SHOW_PREFIX + "a/b"));
	}

	@Test
	public void findCategoryParsesReplayShowLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.replayIndexUrl(), read("test/resources/fixtures/tvlux/replay-index.html"));
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(3, categories.size());
		final Map<String, String> namesById = new HashMap<String, String>();
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(TvLuxConf.CATEGORY_SHOW_PREFIX));
			namesById.put(category.getId(), category.getName());
		}
		assertEquals("JT", namesById.get(TvLuxUrls.showCategoryId("jt")));
		assertEquals("L'hebdo", namesById.get(TvLuxUrls.showCategoryId("l-hebdo")));
		assertEquals("Ciné Lux", namesById.get(TvLuxUrls.showCategoryId("cine-lux")));
	}

	@Test
	public void findEpisodeParsesShowEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.showPageUrl("jt"),
				"<html><body>"
						+ "<a href=\"/replay/jt/jt-du-18-09-2026_52260/\"></a>"
						+ "<a href=\"/replay/jt/jt-du-17-09-2026_52250\"></a>"
						+ "<a href=\"/replay/JT/jt-du-16-09-2026_52240\"></a>"
						+ "<a href=\"/replay/other/ignored_1\"></a>"
						+ "</body></html>");
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvLuxConf.NAME, "JT", TvLuxUrls.showCategoryId("jt"),
				TvLuxConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(3, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvLuxUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertFalse("Episode".equals(episode.getName()));
			assertFalse(episode.getId().contains("/replay/other/"));
			final String name = episode.getName().toLowerCase();
			assertTrue(name.contains("jt du 18") || name.contains("jt du 17") || name.contains("jt du 16"));
		}
	}

	@Test
	public void findEpisodeParsesFixtureShowEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvLuxUrls.showPageUrl("jt"), read("test/resources/fixtures/tvlux/show-jt.html"));
		final TvLuxPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvLuxConf.NAME, "JT", TvLuxUrls.showCategoryId("jt"),
				TvLuxConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		boolean sawAnchorTitle = false;
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvLuxUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertTrue(episode.getId().contains("/replay/jt/"));
			assertFalse(episode.getId().contains("/replay/other/"));
			assertNotNull(episode.getMetadata());
			assertEquals(TvLuxConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			if ("JT 18".equals(episode.getName()) || "JT 17".equals(episode.getName())) {
				sawAnchorTitle = true;
			}
		}
		assertTrue(sawAnchorTitle);
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
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.tvlux.be/replay/jt"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be/replay/jt/page_2"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be/replay/jt/Page_2"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be/replay/jt/title%3Ftoken=x_1"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be/replay/jt/title%2Fextra_1"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be:8443/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tvlux.be:80/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("http://www.tvlux.be:443/replay/jt/jt-du-18-09-2026_52260"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void sanitizeHlsUrlRequiresHttpsFreecasterM3u8() {
		assertEquals(null, TvLuxUrls.sanitizeHlsUrl("http://tvlocales-vod-cmaf.freecaster.com/x.m3u8"));
		assertEquals(null, TvLuxUrls.sanitizeHlsUrl("https://evil.example.com/x.m3u8"));
		assertEquals(null, TvLuxUrls.sanitizeHlsUrl("https://evil-vod.freecaster.com/x.m3u8"));
		assertEquals(null, TvLuxUrls.sanitizeHlsUrl("https://tvlocales-vod-cmaf.freecaster.com/x.mp4"));
		assertEquals(null, TvLuxUrls.sanitizeHlsUrl(
				"https://tvlocales-vod-cmaf.freecaster.com:8443/tvlux/id/file.m3u8"));
		assertEquals("https://tvlocales-vod-cmaf.freecaster.com/tvlux/id/file.m3u8",
				TvLuxUrls.sanitizeHlsUrl(
						"https://tvlocales-vod-cmaf.freecaster.com/tvlux/id/file.m3u8?token=secret"));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TvLuxDiagnostics diagnostics = new TvLuxDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertFalse(line.contains("token=leak"));
	}

	@Test
	public void diagnosticsRejectEncodedDelimitersAndNewlines() {
		final TvLuxDiagnostics diagnostics = new TvLuxDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260%3Ftoken=secret");
		String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tvlux.be/replay/jt/jt-du-18-09-2026_52260"));
		assertFalse(line.contains("token=secret"));
		assertFalse(line.contains("%3F"));
		assertFalse(line.contains("%3f"));

		diagnostics.setSourceUrl("https://user:password with space@www.tvlux.be/replay/x");
		line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=invalid-url"));
		assertFalse(line.contains("password"));

		diagnostics.setShowSlug("jt\ninjected=1");
		line = diagnostics.formatLogLine();
		assertTrue(line.contains("showSlug=jt"));
		assertFalse(line.contains("injected"));
		assertFalse(line.contains("\n"));
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
