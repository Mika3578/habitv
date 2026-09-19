package com.dabi.habitv.provider.tv5plus;

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

public class Tv5PlusOfflineCatalogTest {

	@Test
	public void findCategoryKeepsAvailableVideoShowsAndMovies() throws IOException {
		final Tv5PlusPluginManager plugin = newRecordingPlugin(catalogFixtures());
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(2, categories.size());
		boolean sawSeries = false;
		boolean sawMovie = false;
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(Tv5PlusConf.CATEGORY_SHOW_PREFIX));
			if (category.getId().endsWith("watatatow")) {
				sawSeries = true;
			}
			if (category.getId().endsWith("babysitting")) {
				sawMovie = true;
			}
			assertFalse(category.getId().contains("gone"));
			assertFalse(category.getId().contains("pod"));
		}
		assertTrue(sawSeries);
		assertTrue(sawMovie);
	}

	@Test
	public void findEpisodeListsAvailablePlayableStripEpisodes() throws IOException {
		final Map<String, String> fixtures = catalogFixtures();
		fixtures.put("productByRootProductSlug(rootProductSlug: \\\"watatatow\\\")",
				read("test/resources/fixtures/tv5plus/root-watatatow.json"));
		fixtures.put("productPage(rootProductSlug: \\\"watatatow\\\")",
				read("test/resources/fixtures/tv5plus/page-watatatow.json"));
		fixtures.put("seasonNumber: 12",
				read("test/resources/fixtures/tv5plus/page-watatatow-s12.json"));
		final Tv5PlusPluginManager plugin = newRecordingPlugin(fixtures);
		final CategoryDTO show = new CategoryDTO(Tv5PlusConf.NAME, "Watatatow",
				Tv5PlusUrls.showCategoryId("watatatow"), Tv5PlusConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(episode.getId().startsWith("https://www.tv5unis.ca/videos/watatatow/"));
			assertNotNull(episode.getMetadata());
			assertEquals("Watatatow", episode.getMetadata().getSeriesTitle());
			assertEquals(Tv5PlusConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void findEpisodeMovieUsesCanonicalYtdlpUrl() throws IOException {
		final Map<String, String> fixtures = new HashMap<String, String>();
		fixtures.put("rootProductSlug: \\\"babysitting\\\"",
				read("test/resources/fixtures/tv5plus/root-babysitting.json"));
		final Tv5PlusPluginManager plugin = newRecordingPlugin(fixtures);
		final CategoryDTO show = new CategoryDTO(Tv5PlusConf.NAME, "Babysitting",
				Tv5PlusUrls.showCategoryId("babysitting"), Tv5PlusConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(1, episodes.size());
		final EpisodeDTO movie = episodes.iterator().next();
		assertEquals("https://www.tv5unis.ca/videos/babysitting", movie.getId());
		assertNotNull(movie.getMetadata());
		assertEquals(null, movie.getMetadata().getSeriesTitle());
	}

	@Test
	public void canDownloadRewritesTv5plusHostAndRejectsBadUrls() {
		final Tv5PlusPluginManager plugin = new Tv5PlusPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.tv5plus.ca/videos/watatatow/saisons/12/episodes/1"));
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.tv5unis.ca/videos/babysitting"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.tv5plus.ca/watatatow"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@www.tv5unis.ca/videos/x/saisons/1/episodes/1"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final Tv5PlusDiagnostics diagnostics = new Tv5PlusDiagnostics("download");
		diagnostics.setSourceUrl("https://www.tv5unis.ca/videos/x/saisons/1/episodes/1?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://www.tv5unis.ca/videos/x/saisons/1/episodes/1"));
		assertFalse(line.contains("token=leak"));
	}

	private static Map<String, String> catalogFixtures() throws IOException {
		final Map<String, String> fixtures = new HashMap<String, String>();
		fixtures.put("featuredProductSets", read("test/resources/fixtures/tv5plus/featured-sets.json"));
		fixtures.put("slug: \\\"categorie-fiction-serie\\\"",
				read("test/resources/fixtures/tv5plus/set-fiction.json"));
		fixtures.put("slug: \\\"categorie-film\\\"",
				read("test/resources/fixtures/tv5plus/set-film.json"));
		return fixtures;
	}

	private static Tv5PlusPluginManager newRecordingPlugin(final Map<String, String> fixtures) {
		return new Tv5PlusPluginManager(new Tv5PlusClient(new Tv5PlusClient.GraphqlTransport() {
			@Override
			public String post(final String body) throws IOException {
				String bestKey = null;
				for (final String key : fixtures.keySet()) {
					if (body.contains(key) && (bestKey == null || key.length() > bestKey.length())) {
						bestKey = key;
					}
				}
				if (bestKey == null) {
					throw new IOException("unexpected graphql " + body);
				}
				return fixtures.get(bestKey);
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
