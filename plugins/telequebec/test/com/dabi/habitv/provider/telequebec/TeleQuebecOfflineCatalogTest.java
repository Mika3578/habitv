package com.dabi.habitv.provider.telequebec;

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

public class TeleQuebecOfflineCatalogTest {

	@Test
	public void findCategoryParsesHomeShowSlugs() throws IOException {
		final String home = read("test/resources/fixtures/telequebec/home-links.html");
		final String collection = read("test/resources/fixtures/telequebec/collection.json");
		final TeleQuebecPluginManager plugin = new TeleQuebecPluginManager(new TeleQuebecClient(
				new TeleQuebecClient.ContentLoader() {
					@Override
					public String load(final String url) {
						return home;
					}
				},
				new TeleQuebecClient.GraphqlPoster() {
					@Override
					public String post(final String url, final String jsonBody) {
						return collection;
					}
				}));
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertFalse(categories.isEmpty());
		boolean found = false;
		for (final CategoryDTO category : categories) {
			assertTrue(category.getId().startsWith(TeleQuebecConf.CATEGORY_SHOW_PREFIX));
			if ("Pénélope partout".equals(category.getName())) {
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	public void findEpisodeListsSeasonEpisodes() throws IOException {
		final String collection = read("test/resources/fixtures/telequebec/collection.json");
		final Map<String, String> episodeByKey = new HashMap<String, String>();
		for (int e = 1; e <= 8; e++) {
			episodeByKey.put("e" + e, episodeJson(e, "Episode " + e));
		}
		final TeleQuebecPluginManager plugin = new TeleQuebecPluginManager(new TeleQuebecClient(
				new TeleQuebecClient.ContentLoader() {
					@Override
					public String load(final String url) {
						return "";
					}
				},
				new TeleQuebecClient.GraphqlPoster() {
					@Override
					public String post(final String url, final String jsonBody) {
						if (jsonBody.contains("seasons {")) {
							return collection;
						}
						for (int e = 1; e <= 8; e++) {
							if (jsonBody.contains("\"e\":" + e) || jsonBody.contains("\"e\": " + e)) {
								return episodeByKey.get("e" + e);
							}
						}
						return episodeByKey.get("e1");
					}
				}));
		final CategoryDTO show = new CategoryDTO(TeleQuebecConf.NAME, "Pénélope partout",
				TeleQuebecUrls.showCategoryId("penelope-partout"), TeleQuebecConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(8, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(TeleQuebecUrls.isTeleQuebecWatchUrl(episode.getId()));
			assertEquals(TeleQuebecConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void downloadFailsClosedWhenGeoUnavailable() throws IOException {
		final String episode = read("test/resources/fixtures/telequebec/episode.json");
		final TeleQuebecPluginManager plugin = new TeleQuebecPluginManager(new TeleQuebecClient(
				new TeleQuebecClient.ContentLoader() {
					@Override
					public String load(final String url) {
						return "";
					}
				},
				new TeleQuebecClient.GraphqlPoster() {
					@Override
					public String post(final String url, final String jsonBody) {
						return episode;
					}
				}));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.telequebec.tv/regarder/penelope-partout/1/1", "/tmp/x.mp4",
				TeleQuebecConf.EXTENSION);
		try {
			plugin.download(param, null);
			throw new AssertionError("expected failure");
		} catch (final DownloadFailedException e) {
			assertEquals(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void canDownloadAcceptsWatchUrls() {
		final TeleQuebecPluginManager plugin = new TeleQuebecPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.telequebec.tv/regarder/penelope-partout/1/1"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.telequebec.tv/contenu/x"));
	}

	@Test
	public void parseShowSlugsSkipsNumericIds() {
		final String html = "<a href=\"/contenu/110901\">x</a><a href=\"/contenu/penelope-partout\">y</a>";
		assertEquals(1, TeleQuebecUrls.parseShowSlugs(html).size());
		assertEquals("penelope-partout", TeleQuebecUrls.parseShowSlugs(html).get(0));
	}

	private static String episodeJson(final int episodeNumber, final String title) {
		return "{\"data\":{\"productByRootProductSlug\":{"
				+ "\"id\":\"" + episodeNumber + "\","
				+ "\"title\":\"" + title + "\","
				+ "\"episodeNumber\":" + episodeNumber + ","
				+ "\"seasonNumber\":1,"
				+ "\"availabilityStatus\":\"AVAILABLE\","
				+ "\"videoCanonicalUrl\":\"https://telequebec.tv/regarder/penelope-partout/1/" + episodeNumber + "\","
				+ "\"videoElement\":{\"__typename\":\"Video\",\"mediaId\":\"-1\",\"drmProtected\":false,\"encodings\":null}"
				+ "}}}";
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
