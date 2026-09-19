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

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;

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
		boolean foundCoinLectureDate = false;
		boolean foundCoinLecturePaulColize = false;
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TvComUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertNotNull(episode.getMetadata());
			assertEquals(TvComConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			assertFalse(episode.getName().isEmpty());
			if ("Coin lecture : 18-09-26".equals(episode.getName())) {
				foundCoinLectureDate = true;
			}
			if ("Coin lecture : 28-08-26 Paul Colize".equals(episode.getName())) {
				foundCoinLecturePaulColize = true;
			}
		}
		assertTrue(foundCoinLectureDate);
		assertTrue(foundCoinLecturePaulColize);
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
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin%3Ftoken=x/58524"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(
				"https://www.tvcom.be:8443/replay/emission/coin-lecture/coin-lecture-18-09-26/58524"));
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

	@Test
	public void diagnosticsRejectEncodedDelimitersAndNewlines() {
		final TvComDiagnostics diagnostics = new TvComDiagnostics("download");
		diagnostics.setSourceUrl(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524%3Ftoken=secret");
		String line = diagnostics.formatLogLine();
		assertTrue(line.contains(
				"sourceUrl=https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524"));
		assertFalse(line.contains("token=secret"));
		assertFalse(line.contains("%3F"));
		assertFalse(line.contains("%3f"));

		diagnostics.setSourceUrl("https://user:password with space@www.tvcom.be/replay/x");
		line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=invalid-url"));
		assertFalse(line.contains("password"));
	}

	@Test
	public void findEpisodeStoresSanitizedUrlsWithoutQuery() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(TvComUrls.showPageUrl("coin-lecture"),
				"<html><body>"
						+ "<h2><span>Legacy</span></h2>"
						+ "<a href=\"/replay/emissions/emission-speciale-confreries/58494?token=secret\"></a>"
						+ "<h2><span>Current</span></h2>"
						+ "<a href=\"/replay/emission/coin-lecture/coin-lecture-18-09-26/58524?x=1#frag\"></a>"
						+ "</body></html>");
		final TvComPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvComConf.NAME, "Coin Lecture",
				TvComUrls.showCategoryId("coin-lecture"), TvComConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertFalse(episode.getId().contains("?"));
			assertFalse(episode.getId().contains("#"));
			assertFalse(episode.getId().contains("token"));
			assertTrue(episode.getId().startsWith("https://www.tvcom.be/replay/"));
			assertNotNull(episode.getMetadata());
			assertEquals(episode.getId(), episode.getMetadata().getSourceUrl());
			assertFalse(episode.getMetadata().getSourceUrl().contains("?"));
			assertFalse(episode.getMetadata().getSourceUrl().contains("#"));
			assertFalse(episode.getMetadata().getSourceUrl().contains("token"));
		}
	}

	@Test
	public void downloadDelegatesSanitizedHlsToFfmpeg() throws Exception {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524",
				read("test/resources/fixtures/tvcom/episode.html"));
		pages.put(TvComUrls.freecasterEmbedUrl("a2981fdf-9ec4-4d17-b423-c1a6807049fc"),
				read("test/resources/fixtures/tvcom/embed.html"));
		final TvComPluginManager plugin = newRecordingPlugin(pages);
		final RecordingDownloader downloader = new RecordingDownloader();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.FFMPEG, downloader);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder("cmd", map,
				new HashMap<String, String>(), ".", ".", ".", ".");
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524?token=x",
				"out.mp4", TvComConf.EXTENSION);
		plugin.download(param, holder);
		assertNotNull(downloader.lastInput);
		assertTrue(downloader.lastInput.startsWith("https://tvlocales-vod-cmaf.freecaster.com/"));
		assertTrue(downloader.lastInput.contains(".m3u8"));
	}

	@Test(expected = DownloadFailedException.class)
	public void downloadFailsWhenHlsMissing() throws Exception {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524",
				"<html><body>no player</body></html>");
		final TvComPluginManager plugin = newRecordingPlugin(pages);
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.FFMPEG, new RecordingDownloader());
		final DownloaderPluginHolder holder = new DownloaderPluginHolder("cmd", map,
				new HashMap<String, String>(), ".", ".", ".", ".");
		plugin.download(new DownloadParamDTO(
				"https://www.tvcom.be/replay/emission/coin-lecture/coin-lecture-18-09-26/58524", "out.mp4",
				TvComConf.EXTENSION), holder);
	}

	@Test
	public void sanitizeHlsRejectsNonFreecasterHosts() {
		assertEquals(null, TvComUrls.sanitizeHlsUrl("https://evil.example/x.m3u8"));
		assertEquals(null, TvComUrls.sanitizeHlsUrl("http://tvlocales-vod-cmaf.freecaster.com/x.m3u8"));
		assertEquals(null, TvComUrls.sanitizeHlsUrl("httpfoo://tvlocales-vod-cmaf.freecaster.com/x.m3u8"));
		assertEquals(null, TvComUrls.sanitizeHlsUrl("https://evil-vod.freecaster.com/x.m3u8"));
		assertEquals(null, TvComUrls.sanitizeHlsUrl(
				"https://tvlocales-vod-cmaf.freecaster.com:8443/tvcom/id/file.m3u8"));
		assertEquals(null, TvComUrls.sanitizeHlsUrl(
				"https://tvlocales-vod-cmaf.freecaster.com/tvcom/id/file%3Ftoken=x.m3u8"));
		assertEquals("https://tvlocales-vod-cmaf.freecaster.com/tvcom/id/file.m3u8",
				TvComUrls.sanitizeHlsUrl(
						"https://tvlocales-vod-cmaf.freecaster.com/tvcom/id/file.m3u8?token=x"));
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

	private static final class RecordingDownloader implements PluginDownloaderInterface {

		private String lastInput;

		@Override
		public String getName() {
			return FrameworkConf.FFMPEG;
		}

		@Override
		public DownloadableState canDownload(final String downloadInput) {
			return DownloadableState.SPECIFIC;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
				throws DownloadFailedException {
			lastInput = downloadParam.getDownloadInput();
			return new ProcessHolder() {
				@Override
				public void start() {
				}

				@Override
				public void stop() {
				}

				@Override
				public String getProgression() {
					return null;
				}
			};
		}
	}
}
