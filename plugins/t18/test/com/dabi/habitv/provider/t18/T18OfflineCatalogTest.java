package com.dabi.habitv.provider.t18;

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

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;

public class T18OfflineCatalogTest {

	@Test
	public void findCategoryParsesProgramLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(T18Conf.HOME_URL, read("test/resources/fixtures/t18/home.html"));
		final T18PluginManager plugin = new T18PluginManager(urlLoader(pages));

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO root = categories.iterator().next();
		assertFalse(root.isDownloadable());
		assertEquals(2, root.getSubCategories().size());
		boolean foundCalvi = false;
		for (final CategoryDTO program : root.getSubCategories()) {
			assertTrue(program.getId().startsWith("https://t18.fr/prog/"));
			assertFalse(T18Html.isEpisodePath(program.getId().replace(T18Conf.HOME_URL, "")));
			if (program.getId().contains("chez-calvi")) {
				foundCalvi = true;
			}
		}
		assertTrue(foundCalvi);
	}

	@Test
	public void findEpisodeParsesProgramEpisodeLinks() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://t18.fr/prog/chez-calvi-33869", read("test/resources/fixtures/t18/program.html"));
		final T18PluginManager plugin = new T18PluginManager(urlLoader(pages));
		final CategoryDTO program = new CategoryDTO(T18Conf.NAME, "Chez Calvi",
				"https://t18.fr/prog/chez-calvi-33869", T18Conf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(program);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(episode.getId().contains("/prog/chez-calvi-33869/"));
			assertEquals("Chez Calvi", episode.getMetadata().getSeriesTitle());
		}
	}

	@Test
	public void canDownloadAcceptsProgUrlsOnly() {
		final T18PluginManager plugin = new T18PluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://t18.fr/prog/chez-calvi-33869/ep-1"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://t18.fr/direct"));
	}

	@Test
	public void downloadFailsClosedWithoutAuthReconstruction() {
		final T18PluginManager plugin = new T18PluginManager();
		final DownloadParamDTO param = new DownloadParamDTO("https://t18.fr/prog/chez-calvi-33869/ep-1",
				"/tmp/out.mp4", T18Conf.EXTENSION);
		try {
			plugin.download(param, null);
			throw new AssertionError("expected DownloadFailedException");
		} catch (final DownloadFailedException e) {
			assertEquals(T18Conf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	private static T18PluginManager.PageLoader urlLoader(final Map<String, String> pages) {
		return new T18PluginManager.PageLoader() {
			@Override
			public String load(final String url) {
				final String html = pages.get(url);
				if (html == null) {
					throw new IllegalStateException("unexpected url " + url);
				}
				return html;
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
}
