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

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;

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
	public void parseFrenchBroadcastDateFromTitle() {
		final java.util.Date parsed = TeleMbHtml.parseFrenchBroadcastDate("Les Infos du samedi 19 septembre 2026");
		assertNotNull(parsed);
		final java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
		cal.setTime(parsed);
		assertEquals(2026, cal.get(java.util.Calendar.YEAR));
		assertEquals(java.util.Calendar.SEPTEMBER, cal.get(java.util.Calendar.MONTH));
		assertEquals(19, cal.get(java.util.Calendar.DAY_OF_MONTH));
		assertEquals(null, TeleMbHtml.parseFrenchBroadcastDate("Basket reportage"));
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
		boolean sawAirDate = false;
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TeleMbUrls.sanitizeEpisodeUrl(episode.getId()) != null);
			assertNotNull(episode.getMetadata());
			assertEquals(TeleMbConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			assertNotNull("title missing date for name=[" + episode.getName() + "] id=" + episode.getId(),
					TeleMbHtml.parseFrenchBroadcastDate(episode.getName()));
			if (episode.getMetadata().getAirDate() != null) {
				sawAirDate = true;
				assertEquals(episode.getMetadata().getAirDate(), episode.getEpisodeDate());
			}
		}
		assertTrue("expected airDate on at least one episode; names=" + episodes, sawAirDate);
	}

	@Test
	public void resolveCategoryReplayHlsAndDownload() throws Exception {
		final String categoryUrl = "https://www.telemb.be/replay/sports/basket-r2-colfontaine/41279";
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(categoryUrl, read("test/resources/fixtures/telemb/episode-category.html"));
		pages.put(TeleMbUrls.freecasterEmbedUrl("a2c94991-a92a-4760-9371-fff42179f24b"),
				read("test/resources/fixtures/telemb/embed-category.html"));
		final TeleMbPluginManager plugin = newRecordingPlugin(pages);
		final RecordingDownloader downloader = new RecordingDownloader();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.FFMPEG, downloader);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder("cmd", map, new HashMap<String, String>(),
				".", ".", ".", ".");
		plugin.download(new DownloadParamDTO(categoryUrl + "?token=x", "out.mp4", TeleMbConf.EXTENSION), holder);
		assertNotNull(downloader.lastInput);
		assertTrue(downloader.lastInput.startsWith("https://tvlocales-vod-cmaf.freecaster.com/"));
		assertTrue(downloader.lastInput.contains(".m3u8"));
	}

	@Test
	public void sanitizeHlsRejectsNonFreecasterHosts() {
		assertEquals(null, TeleMbUrls.sanitizeHlsUrl("https://evil.example/x.m3u8"));
		assertEquals(null, TeleMbUrls.sanitizeHlsUrl("http://tvlocales-vod-cmaf.freecaster.com/x.m3u8"));
		assertNotNull(TeleMbUrls.sanitizeHlsUrl("https://tvlocales-vod-cmaf.freecaster.com/telemb/id/file.m3u8"));
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
		final java.io.File file = new java.io.File(path);
		assertTrue("missing fixture: " + path, file.isFile());
		try (InputStream input = new FileInputStream(file)) {
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
