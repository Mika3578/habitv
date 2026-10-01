package com.dabi.habitv.provider.lemanbleu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class LemanBleuOfflineCatalogTest {

	@Test
	public void findCategoryParsesProgramTitles() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(LemanBleuHtml.programsUrl(),
				read("test/resources/fixtures/lemanBleu/programs.html"));
		final LemanBleuPluginManager plugin = new LemanBleuPluginManager(loader(pages));
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertFalse(categories.isEmpty());
		boolean foundJournal = false;
		for (final CategoryDTO category : categories) {
			assertTrue(category.getId().startsWith(LemanBleuConf.CATEGORY_SHOW_PREFIX));
			assertTrue(category.isDownloadable());
			if ("Le Journal".equals(category.getName())) {
				foundJournal = true;
				assertEquals(LemanBleuHtml.showCategoryId("56242"), category.getId());
			}
		}
		assertTrue(foundJournal);
	}

	@Test
	public void findEpisodeParsesArchiveCards() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(LemanBleuHtml.archiveUrl("82070"),
				read("test/resources/fixtures/lemanBleu/archive.html"));
		final LemanBleuPluginManager plugin = new LemanBleuPluginManager(loader(pages));
		final CategoryDTO show = new CategoryDTO(LemanBleuConf.NAME, "Genève Grandeur Nature",
				LemanBleuHtml.showCategoryId("82070"), LemanBleuConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertFalse(episodes.isEmpty());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(LemanBleuHtml.isLemanBleuEpisodeUrl(episode.getId()));
			assertTrue(episode.getId().contains("videos.lemanbleu.ch"));
			assertEquals("Genève Grandeur Nature", episode.getMetadata().getSeriesTitle());
			assertEquals(LemanBleuConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void extractBestMp4PrefersHd() throws IOException {
		final String html = read("test/resources/fixtures/lemanBleu/episode.html");
		final String mp4 = LemanBleuHtml.extractBestMp4Url(html);
		assertTrue(mp4.endsWith("1jijk03unjwuw.mp4"));
		assertTrue(mp4.startsWith("https://play.vod2.infomaniak.com/"));
	}

	@Test
	public void downloadResolvesMp4ThenDelegatesToCurl() throws Exception {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://videos.lemanbleu.ch/fr/Emissions/546353-Le-Journal.html",
				read("test/resources/fixtures/lemanBleu/episode.html"));
		final LemanBleuPluginManager plugin = new LemanBleuPluginManager(loader(pages));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://videos.lemanbleu.ch/fr/Emissions/546353-Le-Journal.html",
				"/tmp/lemanbleu-test.mp4", LemanBleuConf.EXTENSION);
		final RecordingCurlDownloader curl = new RecordingCurlDownloader();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.CURL, curl);
		final DownloaderPluginHolder holders = new DownloaderPluginHolder("cmd", map,
				new HashMap<String, String>(), "out", "index", "bin", "plugins");
		plugin.download(param, holders);
		assertTrue(curl.lastInput.startsWith("https://play.vod2.infomaniak.com/"));
		assertTrue(curl.lastInput.endsWith(".mp4"));
	}

	@Test
	public void downloadFailsClosedWhenMp4Missing() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://videos.lemanbleu.ch/fr/Emissions/1-x.html",
				"<html><body><h1>No video</h1></body></html>");
		final LemanBleuPluginManager plugin = new LemanBleuPluginManager(loader(pages));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://videos.lemanbleu.ch/fr/Emissions/1-x.html", "/tmp/x.mp4",
				LemanBleuConf.EXTENSION);
		try {
			plugin.download(param, null);
			throw new AssertionError("expected failure");
		} catch (final DownloadFailedException e) {
			assertEquals(LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void canDownloadAcceptsEpisodeUrls() {
		final LemanBleuPluginManager plugin = new LemanBleuPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://videos.lemanbleu.ch/fr/Emissions/546353-Le-Journal.html"));
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.lemanbleu.ch/fr/Emissions/546353-Le-Journal.html"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://videos.lemanbleu.ch/fr/"));
	}

	private static LemanBleuPluginManager.ContentLoader loader(final Map<String, String> pages) {
		return new LemanBleuPluginManager.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				final String body = pages.get(url);
				if (body == null) {
					throw new IOException("unexpected " + url);
				}
				return body;
			}
		};
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

	private static final class RecordingCurlDownloader implements PluginDownloaderInterface {
		private String lastInput;

		@Override
		public String getName() {
			return FrameworkConf.CURL;
		}

		@Override
		public DownloadableState canDownload(final String downloadInput) {
			return DownloadableState.POSSIBLE;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam,
				final DownloaderPluginHolder downloaders) {
			lastInput = downloadParam.getDownloadInput();
			return ProcessHolder.EMPTY_PROCESS_HOLDER;
		}
	}
}
