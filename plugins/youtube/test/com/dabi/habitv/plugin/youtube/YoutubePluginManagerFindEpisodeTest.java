package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
import com.google.common.collect.ImmutableMap;

public class YoutubePluginManagerFindEpisodeTest {

	private static final String API_KEY_PROPERTY = "habitv.youtube.apiKey";

	private static final String PROGRAMMES = "Programmes";

	@Before
	public void setUpApiKey() {
		System.setProperty(API_KEY_PROPERTY, YoutubeTestSecrets.googleApiKeyPlaceholder());
	}

	@After
	public void tearDownApiKey() {
		System.clearProperty(API_KEY_PROPERTY);
	}

	private static final String PLAYLIST_ITEMS_JSON = "{\"items\":[{\"snippet\":{\"title\":\"France24\",\"resourceId\":{\"videoId\":\"vid123\"}}}]}";

	private static final String SEARCH_JSON = "{\"items\":[{\"id\":{\"videoId\":\"topVid\"},\"snippet\":{\"title\":\"Top hit\"}}]}";

	private static final String MOST_POPULAR_JSON = "{\"items\":[{\"id\":\"topVid\",\"snippet\":{\"title\":\"Top hit\"}}]}";

	@Test
	public void findEpisodeOnTopTemplateReturnsEmptyWithoutNetwork() {
		final YoutubePluginManager manager = networkBlockingManager();
		final CategoryDTO topTemplate = findCategoryByName(manager.findCategory(), "Top");
		assertTrue(topTemplate.isTemplate());
		assertFalse(topTemplate.isDownloadable());
		assertTrue(manager.findEpisode(topTemplate).isEmpty());
	}

	@Test
	public void findEpisodeOnPlaylistTemplateReturnsEmptyWithoutNetwork() {
		final YoutubePluginManager manager = networkBlockingManager();
		final CategoryDTO playlistTemplate = findCategoryByName(manager.findCategory(), "Playlist");
		assertTrue(playlistTemplate.isTemplate());
		assertFalse(playlistTemplate.isDownloadable());
		assertTrue(manager.findEpisode(playlistTemplate).isEmpty());
	}

	@Test
	public void findEpisodeOnNonDownloadableCategoryReturnsEmptyWithoutNetwork() {
		final YoutubePluginManager manager = networkBlockingManager();
		final CategoryDTO topFather = new CategoryDTO(YoutubeConf.NAME, "Top", "top-template", FrameworkConf.MP4);
		final CategoryDTO category = new CategoryDTO(YoutubeConf.NAME, "blocked", "maxResults=10", FrameworkConf.MP4);
		category.setDownloadable(false);
		topFather.addSubCategory(category);
		assertTrue(manager.findEpisode(category).isEmpty());
	}

	@Test
	public void findEpisodeOnPlaylistChildWithoutPlaylistIdSkipsNetwork() {
		final AtomicBoolean networkCalled = new AtomicBoolean(false);
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				networkCalled.set(true);
				fail("network must not be called when playlistId is missing");
				return null;
			}
		};
		final CategoryDTO father = new CategoryDTO(YoutubeConf.NAME, "Playlist", "playlist-template", FrameworkConf.MP4);
		final CategoryDTO child = new CategoryDTO(YoutubeConf.NAME, "Empty playlist", "maxResults=10", FrameworkConf.MP4);
		child.setDownloadable(true);
		father.addSubCategory(child);

		assertTrue(manager.findEpisode(child).isEmpty());
		assertFalse(networkCalled.get());
	}

	@Test
	public void findEpisodeOnNestedPlaylistHierarchyUsesPlaylistApiPath() {
		final AtomicReference<String> requestedUrl = new AtomicReference<>();
		final YoutubePluginManager manager = urlCapturingManager(PLAYLIST_ITEMS_JSON, requestedUrl);
		final CategoryDTO france24 = buildNestedPlaylistLeaf("France24 Live EN",
				ImmutableMap.of("playlistId", "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));

		final Set<EpisodeDTO> episodes = manager.findEpisode(france24);
		assertEquals(1, episodes.size());
		assertNotNull(requestedUrl.get());
		assertTrue(requestedUrl.get().contains("playlistItems"));
	}

	@Test
	public void findEpisodeOnNestedTopHierarchyUsesMostPopularVideosApiPath() {
		final AtomicReference<String> requestedUrl = new AtomicReference<>();
		final YoutubePluginManager manager = urlCapturingManager(MOST_POPULAR_JSON, requestedUrl);
		final CategoryDTO top50 = buildNestedTopLeaf("Top 50 All Times",
				ImmutableMap.of("maxResults", "50"));

		final Set<EpisodeDTO> episodes = manager.findEpisode(top50);
		assertEquals(1, episodes.size());
		assertNotNull(requestedUrl.get());
		assertTrue(requestedUrl.get().contains("/videos?"));
		assertTrue(requestedUrl.get().contains("chart=mostPopular"));
		assertFalse("all-time Top must not use search.list without q",
				requestedUrl.get().contains("/search?"));
		assertFalse(requestedUrl.get().contains("publishedAfter="));
		assertTrue(requestedUrl.get().contains("maxResults=50"));
	}

	@Test
	public void findEpisodeOnFrance24LiveReturnsEpisodeFromFixture() {
		final YoutubePluginManager manager = fixtureManager(PLAYLIST_ITEMS_JSON);
		final CategoryDTO france24 = buildNestedPlaylistLeaf("France24 Live EN",
				ImmutableMap.of("playlistId", "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));
		assertTrue(france24.isDownloadable());

		final Set<EpisodeDTO> episodes = manager.findEpisode(france24);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("France24", episode.getName());
		assertEquals(YoutubeConf.BASE_URL + "/watch?v=vid123", episode.getId());
	}

	@Test
	public void findEpisodeOnTop50AllTimesReturnsEpisodeFromFixture() {
		final YoutubePluginManager manager = fixtureManager(MOST_POPULAR_JSON);
		final CategoryDTO top50 = buildNestedTopLeaf("Top 50 All Times",
				ImmutableMap.of("maxResults", "50"));
		assertTrue(top50.isDownloadable());

		final Set<EpisodeDTO> episodes = manager.findEpisode(top50);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Top hit", episode.getName());
		assertEquals(YoutubeConf.BASE_URL + "/watch?v=topVid", episode.getId());
	}

	@Test
	public void findEpisodeTopUsesMostPopularWhenDaysWouldPrecede1970WithoutQuery() {
		final AtomicReference<String> requestedUrl = new AtomicReference<>();
		final YoutubePluginManager manager = urlCapturingManager(MOST_POPULAR_JSON, requestedUrl);
		final CategoryDTO top50 = buildNestedTopLeaf("Top 50 All Times",
				ImmutableMap.of("days", "36500", "maxResults", "50"));

		manager.findEpisode(top50);

		assertNotNull(requestedUrl.get());
		assertTrue(requestedUrl.get().contains("chart=mostPopular"));
		assertFalse(requestedUrl.get().contains("publishedAfter="));
	}

	@Test
	public void findEpisodeTopIncludesPublishedAfterForRecentWindow() {
		final AtomicReference<String> requestedUrl = new AtomicReference<>();
		final YoutubePluginManager manager = urlCapturingManager(SEARCH_JSON, requestedUrl);
		final CategoryDTO recentTop = buildNestedTopLeaf("Recent top",
				ImmutableMap.of("days", "30", "maxResults", "10"));

		manager.findEpisode(recentTop);

		assertNotNull(requestedUrl.get());
		assertTrue(requestedUrl.get().contains("publishedAfter="));
		assertFalse(requestedUrl.get().contains("publishedAfter=1926"));
	}

	@Test
	public void findEpisodeTopReturnsEmptyWhenMostPopularItemsMissing() {
		final YoutubePluginManager manager = fixtureManager("{\"items\":[]}");
		final CategoryDTO top50 = buildNestedTopLeaf("Top 50 All Times",
				ImmutableMap.of("maxResults", "50"));

		assertTrue(manager.findEpisode(top50).isEmpty());
	}

	private YoutubePluginManager networkBlockingManager() {
		return new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException("network must not be called");
			}
		};
	}

	private YoutubePluginManager fixtureManager(final String json) {
		return urlCapturingManager(json, null);
	}

	private YoutubePluginManager urlCapturingManager(final String json, final AtomicReference<String> requestedUrl) {
		return new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				assertNotNull(url);
				if (requestedUrl != null) {
					requestedUrl.set(url);
				}
				return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
			}
		};
	}

	private CategoryDTO buildNestedPlaylistLeaf(final String leafName, final ImmutableMap<String, String> params) {
		final CategoryDTO playlistRoot = findCategoryByName(new YoutubePluginManager().findCategory(), "Playlist");
		final CategoryDTO programmes = new CategoryDTO(YoutubeConf.NAME, PROGRAMMES, "programmes-playlist", FrameworkConf.MP4);
		programmes.setDownloadable(false);
		final CategoryDTO leaf = TemplateUtils.buildSampleCat(YoutubeConf.NAME, leafName, params);
		playlistRoot.addSubCategory(programmes);
		programmes.addSubCategory(leaf);
		return leaf;
	}

	private CategoryDTO buildNestedTopLeaf(final String leafName, final ImmutableMap<String, String> params) {
		final CategoryDTO topRoot = findCategoryByName(new YoutubePluginManager().findCategory(), "Top");
		final CategoryDTO programmes = new CategoryDTO(YoutubeConf.NAME, PROGRAMMES, "programmes-top", FrameworkConf.MP4);
		programmes.setDownloadable(false);
		final CategoryDTO leaf = TemplateUtils.buildSampleCat(YoutubeConf.NAME, leafName, params);
		topRoot.addSubCategory(programmes);
		programmes.addSubCategory(leaf);
		return leaf;
	}

	private CategoryDTO findCategoryByName(final Set<CategoryDTO> categories, final String name) {
		for (final CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		fail("category not found: " + name);
		return null;
	}
}
