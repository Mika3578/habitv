package com.dabi.habitv.provider.playrts;

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
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class PlayRtsOfflineCatalogTest {

	@Test
	public void findCategoryExposesTvShowsUnderEmissions() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://www.rts.ch/play/v3/api/rts/production/shows",
				read("test/resources/fixtures/playRts/shows.json"));
		final PlayRtsPluginManager plugin = new PlayRtsPluginManager(new PlayRtsClient(
				new PlayRtsClient.ContentLoader() {
					@Override
					public String load(final String url) throws IOException {
						final String body = pages.get(url);
						if (body == null) {
							throw new IOException("unexpected " + url);
						}
						return body;
					}
				}));
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO emissions = categories.iterator().next();
		assertEquals(PlayRtsConf.EMISSIONS_LABEL, emissions.getName());
		assertFalse(emissions.isDownloadable());
		assertFalse(emissions.getSubCategories().isEmpty());
		boolean found = false;
		for (final CategoryDTO show : emissions.getSubCategories()) {
			assertTrue(show.getId().startsWith(PlayRtsConf.CATEGORY_SHOW_PREFIX));
			assertTrue(show.isDownloadable());
			if ("120 secondes".equals(show.getName())) {
				found = true;
				assertEquals(PlayRtsUrls.showCategoryId("5917099"), show.getId());
			}
		}
		assertTrue(found);
	}

	@Test
	public void findEpisodeUsesIlMediaList() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(
				"https://il.srgssr.ch/integrationlayer/2.0/rts/mediaList/video/latest/byShow/5917099?vector=portalplay&pageSize=20",
				read("test/resources/fixtures/playRts/media-list-by-show.json"));
		final PlayRtsPluginManager plugin = new PlayRtsPluginManager(new PlayRtsClient(
				new PlayRtsClient.ContentLoader() {
					@Override
					public String load(final String url) throws IOException {
						final String body = pages.get(url);
						if (body == null) {
							throw new IOException("unexpected " + url);
						}
						return body;
					}
				}));
		final CategoryDTO show = new CategoryDTO(PlayRtsConf.NAME, "120 secondes",
				PlayRtsUrls.showCategoryId("5917099"), PlayRtsConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertFalse(episodes.isEmpty());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(PlayRtsUrls.isPlayRtsVideoPageUrl(episode.getId()));
			assertTrue(episode.getId().contains("urn=urn:rts:video:"));
			assertEquals("120 secondes", episode.getMetadata().getSeriesTitle());
		}
	}

	@Test
	public void canDownloadAcceptsPlayRtsVideoUrls() {
		final PlayRtsPluginManager plugin = new PlayRtsPluginManager();
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload(
				"https://www.rts.ch/play/tv/-/video/demo?urn=urn:rts:video:21408e81-e5bf-3f7f-8e11-5c8c10b46f2b"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.rts.ch/play/tv"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.example.com/video"));
	}

	@Test
	public void urlHelpersBuildStablePlayPage() {
		final Map<String, Object> video = new HashMap<String, Object>();
		video.put("id", "21408e81-e5bf-3f7f-8e11-5c8c10b46f2b");
		video.put("urn", "urn:rts:video:21408e81-e5bf-3f7f-8e11-5c8c10b46f2b");
		video.put("title", "Les Suisses sont illettrés");
		final String url = PlayRtsUrls.videoPageUrl(video);
		assertTrue(PlayRtsUrls.isPlayRtsVideoPageUrl(url));
		assertTrue(url.contains("urn=urn:rts:video:21408e81-e5bf-3f7f-8e11-5c8c10b46f2b"));
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
