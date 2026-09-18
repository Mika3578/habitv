package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
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

public class Tf1PlusPluginManagerOfflineTest {

	@Test
	public void buildsCatalogAndEpisodesFromFixtures() throws Exception {
		final Tf1PlusPluginManager manager = new Tf1PlusPluginManager(fixtureClient());
		final Set<CategoryDTO> channels = manager.findCategory();
		assertEquals(5, channels.size());
		final CategoryDTO tf1 = find(channels, "TF1");
		final CategoryDTO genre = tf1.getSubCategories().iterator().next();
		final CategoryDTO program = findBySlug(genre);
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(1, episodes.size());
		assertEquals(PluginDownloaderInterface.DownloadableState.SPECIFIC,
				manager.canDownload(episodes.iterator().next().getId()));
		assertEquals(PluginDownloaderInterface.DownloadableState.IMPOSSIBLE,
				manager.canDownload("https://evil.com/www.tf1.fr/videos/x.html"));
	}

	@Test
	public void delegatesDownloadToYoutube() throws Exception {
		final Tf1PlusPluginManager manager = new Tf1PlusPluginManager(fixtureClient());
		final RecordingDownloader downloader = new RecordingDownloader();
		final Map<String, PluginDownloaderInterface> plugins = new HashMap<String, PluginDownloaderInterface>();
		plugins.put(FrameworkConf.YOUTUBE, downloader);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder(null, plugins,
				new HashMap<String, String>(), "/tmp", "/tmp", "/tmp", "/tmp");
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html", "out.mp4",
				Tf1PlusConf.EXTENSION);
		manager.download(param, holder);
		assertEquals(param.getDownloadInput(), downloader.lastInput);
	}

	@Test
	public void returnsEmptyEpisodesForChannelRoots() throws Exception {
		final Tf1PlusPluginManager manager = new Tf1PlusPluginManager(fixtureClient());
		final CategoryDTO channel = manager.findCategory().iterator().next();
		assertTrue(manager.findEpisode(channel).isEmpty());
	}

	private static Tf1PlusCatalogClient fixtureClient() throws IOException {
		final String programs = Tf1PlusFixtureSupport.readRaw("graphql-programs-tf1.json");
		final String videos = Tf1PlusFixtureSupport.readRaw("graphql-videos-replay.json");
		return new Tf1PlusCatalogClient(new Tf1PlusHttpClient.Transport() {
			@Override
			public String get(final String url) throws IOException {
				if (url.contains("id=" + Tf1PlusConf.QUERY_PROGRAMS)) {
					return programs;
				}
				if (url.contains("id=" + Tf1PlusConf.QUERY_VIDEOS)) {
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

	private static CategoryDTO findBySlug(final CategoryDTO genre) {
		for (final CategoryDTO program : genre.getSubCategories()) {
			if ("evening-magazine".equals(program.getParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG))) {
				return program;
			}
		}
		return genre.getSubCategories().iterator().next();
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
