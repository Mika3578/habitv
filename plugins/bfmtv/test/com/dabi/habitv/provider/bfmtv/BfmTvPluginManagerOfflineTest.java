package com.dabi.habitv.provider.bfmtv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;

public class BfmTvPluginManagerOfflineTest {

	@Test
	public void buildsCatalogAndEpisodesFromFixtures() throws Exception {
		final BfmTvPluginManager manager = new BfmTvPluginManager(fixtureClient());
		final Set<CategoryDTO> channels = manager.findCategory();
		assertEquals(2, channels.size());
		final CategoryDTO bfmtv = find(channels, "BFMTV");
		final CategoryDTO business = find(channels, "BFM Business");
		assertNotNull(bfmtv);
		assertNotNull(business);
		assertFalse(bfmtv.getId().equals(business.getId()));
		final CategoryDTO program = find(bfmtv.getSubCategories(), "Morning News");
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(1, episodes.size());
		assertEquals(PluginDownloaderInterface.DownloadableState.SPECIFIC,
				manager.canDownload(episodes.iterator().next().getId()));
		assertEquals(PluginDownloaderInterface.DownloadableState.IMPOSSIBLE,
				manager.canDownload("https://evil.com/www.bfmtv.com/replay-emissions/x.html"));
	}

	@Test
	public void delegatesDownloadToYoutube() throws Exception {
		final BfmTvPluginManager manager = new BfmTvPluginManager(fixtureClient());
		final RecordingDownloader downloader = new RecordingDownloader();
		final Map<String, PluginDownloaderInterface> plugins = new HashMap<String, PluginDownloaderInterface>();
		plugins.put(FrameworkConf.YOUTUBE, downloader);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder(null, plugins,
				new HashMap<String, String>(), "/tmp", "/tmp", "/tmp", "/tmp");
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.bfmtv.com/replay-emissions/morning-news/video-morning-news-friday-18-september-2026_VN-202609180001.html",
				"out.mp4", BfmTvConf.EXTENSION);
		manager.download(param, holder);
		assertEquals(param.getDownloadInput(), downloader.lastInput);
	}

	@Test
	public void returnsEmptyEpisodesForChannelRoots() throws Exception {
		final BfmTvPluginManager manager = new BfmTvPluginManager(fixtureClient());
		assertTrue(manager.findEpisode(find(manager.findCategory(), "BFMTV")).isEmpty());
	}

	private static BfmTvCatalogClient fixtureClient() throws IOException {
		final String token = BfmTvFixtureSupport.readRaw("session-token.json");
		final String replay = BfmTvFixtureSupport.readRaw("replay-page.json");
		final String videos = BfmTvFixtureSupport.readRaw("videos-page.json");
		return new BfmTvCatalogClient(new BfmTvHttpClient.Transport() {
			@Override
			public String get(final String url) throws IOException {
				if (url.endsWith("-applications/") || url.endsWith("-applications")) {
					return token;
				}
				if (url.contains("getPage?pagename=replay")) {
					return replay;
				}
				if (url.contains("getVideosList")) {
					return videos;
				}
				throw new IOException("unexpected-url");
			}
		});
	}

	private static CategoryDTO find(final Iterable<CategoryDTO> categories, final String name) {
		for (final CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
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
