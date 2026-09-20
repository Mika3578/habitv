package com.dabi.habitv.provider.tvaplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
	public void findEpisodeFetchesCarouselWhenNestedOnlyNonPublic() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tvaplus.ca/tva/j-e",
				read("test/resources/fixtures/tvaplus/show-je-s50.html"));
		pages.put("https://www.tvaplus.ca/tva/j-e/saison-50",
				read("test/resources/fixtures/tvaplus/season-50-premium-nested.html"));
		pages.put("https://www.tvaplus.ca/tva/j-e/saison-50/tous-les-episodes",
				read("test/resources/fixtures/tvaplus/season-50-episodes.html"));
		final TvaPlusPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TvaPlusConf.NAME, "J.E",
				TvaPlusUrls.showCategoryId("/tva/j-e"), TvaPlusConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().contains("/saison-50/episode-2-2"));
		assertFalse(episode.getId().contains("episode-1-1"));
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
		assertNotNull(episode.getMetadata());
		assertEquals(null, episode.getMetadata().getSeriesTitle());
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
	public void findEpisodeRejectsUnsafeShowSlugFromCategoryId() {
		final TvaPlusPluginManager plugin = new TvaPlusPluginManager();
		assertTrue(plugin.findEpisode(new CategoryDTO(TvaPlusConf.NAME, "Evil",
				TvaPlusUrls.showCategoryId("https://evil.example/tva/j-e"), TvaPlusConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(TvaPlusConf.NAME, "Evil",
				TvaPlusUrls.showCategoryId("/tva/j-e?token=leak"), TvaPlusConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(TvaPlusConf.NAME, "Evil",
				TvaPlusUrls.showCategoryId("/tva/j-e#frag"), TvaPlusConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(TvaPlusConf.NAME, "Evil",
				TvaPlusUrls.showCategoryId("/tva/j-e/extra"), TvaPlusConf.EXTENSION)).isEmpty());
	}

	@Test
	public void pageUrlRejectsAbsoluteAndUnsafeRemoteSlugs() {
		assertEquals("https://www.tvaplus.ca/tva/j-e/saison-34/tous-les-episodes",
				TvaPlusUrls.pageUrl("/tva/j-e/saison-34/tous-les-episodes"));
		assertEquals(null, TvaPlusUrls.pageUrl("https://evil.example/tva/j-e"));
		assertEquals(null, TvaPlusUrls.pageUrl("http://www.tvaplus.ca/tva/j-e"));
		assertEquals(null, TvaPlusUrls.pageUrl("/tva/j-e?token=x"));
		assertEquals(null, TvaPlusUrls.pageUrl("/zeste/foo"));
		assertFalse(TvaPlusUrls.isSafeRelativeTvaPath("https://evil.example/tva/j-e"));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TvaPlusDiagnostics diagnostics = new TvaPlusDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tvaplus.ca/tva/j-e/saison-1/episode-1-1?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tvaplus.ca/tva/j-e/saison-1/episode-1-1"));
		assertFalse(line.contains("token=leak"));
	}

	@Test
	public void downloadDelegatesSanitizedUrlToYtdlp() throws Exception {
		final TvaPlusPluginManager plugin = new TvaPlusPluginManager();
		final RecordingDownloader downloader = new RecordingDownloader();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.YOUTUBE, downloader);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder("cmd", map,
				new HashMap<String, String>(), ".", ".", ".", ".");
		plugin.download(new DownloadParamDTO(
				"https://www.tvaplus.ca/tva/j-e/saison-34/episode-973-2018367086?token=x#frag",
				"out.mp4", TvaPlusConf.EXTENSION), holder);
		assertEquals("https://www.tvaplus.ca/tva/j-e/saison-34/episode-973-2018367086", downloader.lastInput);
	}

	@Test(expected = DownloadFailedException.class)
	public void downloadRejectsNullParam() throws Exception {
		final TvaPlusPluginManager plugin = new TvaPlusPluginManager();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.YOUTUBE, new RecordingDownloader());
		plugin.download(null, new DownloaderPluginHolder("cmd", map, new HashMap<String, String>(), ".", ".", ".", "."));
	}

	@Test(expected = DownloadFailedException.class)
	public void downloadRejectsUnsupportedUrl() throws Exception {
		final TvaPlusPluginManager plugin = new TvaPlusPluginManager();
		final Map<String, PluginDownloaderInterface> map = new HashMap<String, PluginDownloaderInterface>();
		map.put(FrameworkConf.YOUTUBE, new RecordingDownloader());
		plugin.download(new DownloadParamDTO("https://evil.example/tva/j-e/saison-1/episode-1-1", "out.mp4",
				TvaPlusConf.EXTENSION),
				new DownloaderPluginHolder("cmd", map, new HashMap<String, String>(), ".", ".", ".", "."));
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
		assertTrue("missing local fixture: " + path, new File(path).exists());
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
			return FrameworkConf.YOUTUBE;
		}

		@Override
		public DownloadableState canDownload(final String downloadInput) {
			return DownloadableState.SPECIFIC;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
				throws DownloadFailedException {
			lastInput = downloadParam.getDownloadInput();
			return ProcessHolder.EMPTY_PROCESS_HOLDER;
		}
	}
}
