package com.dabi.habitv.provider.tfo;

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

public class TfoOfflineCatalogTest {

	@Test
	public void findCategoryBuildsCatalogRootsAndShows() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tfo.org/series", read("test/resources/fixtures/tfo/series.html"));
		pages.put("https://www.tfo.org/documentaires", "<html></html>");
		pages.put("https://www.tfo.org/films", read("test/resources/fixtures/tfo/films.html"));
		pages.put("https://www.tfo.org/animations", "<html></html>");
		final TfoPluginManager plugin = newRecordingPlugin(pages);

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(4, categories.size());
		CategoryDTO series = null;
		CategoryDTO films = null;
		for (final CategoryDTO root : categories) {
			assertFalse(root.isDownloadable());
			if ("Séries".equals(root.getName())) {
				series = root;
			}
			if ("Films".equals(root.getName())) {
				films = root;
			}
		}
		assertNotNull(series);
		assertNotNull(films);
		assertEquals(3, series.getSubCategories().size());
		assertEquals(2, films.getSubCategories().size());
		for (final CategoryDTO show : series.getSubCategories()) {
			assertTrue(show.isDownloadable());
			assertTrue(show.getId().startsWith(TfoConf.CATEGORY_SHOW_PREFIX + "/serie/"));
		}
		for (final CategoryDTO film : films.getSubCategories()) {
			assertTrue(film.getId().startsWith(TfoConf.CATEGORY_SHOW_PREFIX + "/film/"));
		}
	}

	@Test
	public void findEpisodeParsesSerieProductsIntoWatchUrls() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.tfo.org/serie/capitaine-tonus/003263406",
				read("test/resources/fixtures/tfo/serie-capitaine.html"));
		final TfoPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(TfoConf.NAME, "Capitaine Tonus",
				TfoUrls.showCategoryId("/serie/capitaine-tonus/003263406"), TfoConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(episode.getId().contains("/regarder/"));
			assertTrue(TfoUrls.isTfoWatchUrl(episode.getId()));
			assertNotNull(episode.getMetadata());
			assertEquals("Capitaine Tonus", episode.getMetadata().getSeriesTitle());
			assertEquals(TfoConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
			assertNotNull(episode.getMetadata().getEpisodeNumber());
		}
	}

	@Test
	public void findEpisodeMapsFilmCategoryToSingleWatchUrl() {
		final TfoPluginManager plugin = newRecordingPlugin(new HashMap<String, String>());
		final CategoryDTO film = new CategoryDTO(TfoConf.NAME, "La promesse verte",
				TfoUrls.showCategoryId("/film/la-promesse-verte/GP257334"), TfoConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(film);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("https://www.tfo.org/regarder/la-promesse-verte/GP257334", episode.getId());
	}

	@Test
	public void canDownloadAcceptsWatchPagesOnly() {
		final TfoPluginManager plugin = new TfoPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.tfo.org/regarder/foo/GP259853"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tfo.org/serie/foo/003263406"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://attacker.example/tfo.org/regarder/foo/GP1"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@www.tfo.org/regarder/foo/GP1"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void watchPageExposesJwplayerPlaylist() throws IOException {
		final String html = read("test/resources/fixtures/tfo/regarder.html");
		final TfoHtml.WatchMedia media = TfoHtml.extractWatchMedia(html);
		assertNotNull(media);
		assertEquals("n1l0ZNmY", media.videoId);
		assertTrue(media.playlistUrl.contains("cdn.jwplayer.com/manifests/"));
		assertTrue(media.playlistUrl.endsWith(".m3u8"));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final TfoDiagnostics diagnostics = new TfoDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tfo.org/regarder/foo/GP1?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tfo.org/regarder/foo/GP1"));
		assertFalse(line.contains("token=leak"));
		assertFalse(line.contains("?"));
		assertFalse(line.contains("#"));
	}

	private static TfoPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new TfoPluginManager(new TfoClient(new TfoClient.ContentLoader() {
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
