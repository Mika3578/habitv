package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CanalPlusProtectedEndpointTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void canDownloadRequiresExactApprovedHost() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager();
		assertEquals(DownloadableState.IMPOSSIBLE, manager.canDownload(
				"https://evil.example/?next=https://www.canalplus.com/decouverte/h/31338503_50017"));
		assertEquals(DownloadableState.SPECIFIC, manager.canDownload(
				"https://www.canalplus.com/decouverte/h/31338503_50017"));
		assertEquals(DownloadableState.SPECIFIC, manager.canDownload(
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json"));
	}

	@Test
	public void unavailablePlaceholderIsVisibleAndNotDownloadable() {
		final Set<CategoryDTO> categories = CanalPlusEndpointAvailability.buildUnavailablePlaceholderCategories(
				CanalPlusConf.NAME, CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL);
		assertEquals(1, categories.size());
		final CategoryDTO placeholder = categories.iterator().next();
		assertEquals(CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL, placeholder.getName());
		assertFalse(placeholder.isDownloadable());
		assertTrue(CanalPlusEndpointAvailability.isUnavailablePlaceholder(placeholder));
	}

	@Test
	public void canalPlusCategoryDiscoveryReturnsUnavailablePlaceholderWhenLegacyHostIsUnavailable() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new java.net.UnknownHostException("service.mycanal.fr"));
			}
		};

		final Set<CategoryDTO> categories = manager.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO placeholder = categories.iterator().next();
		assertEquals(CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL, placeholder.getName());
		assertFalse(placeholder.isDownloadable());
		assertFalse(containsHubSeedLabels(placeholder));
	}

	@Test
	public void cStarCategoryDiscoveryReturnsUnavailablePlaceholderWhenEndpointIsForbidden() {
		final CStarPluginManager manager = new CStarPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: https://www.canalplus.com/chaines/cstar"));
			}
		};

		final Set<CategoryDTO> categories = manager.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO placeholder = categories.iterator().next();
		assertEquals(CanalPlusEndpointAvailability.CSTAR_UNAVAILABLE_LABEL, placeholder.getName());
		assertFalse(placeholder.isDownloadable());
	}

	@Test
	public void findEpisodeSkipsUnavailablePlaceholderWithoutNetworkCall() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new IOException("unexpected network call to " + url));
			}
		};
		final CategoryDTO placeholder = CanalPlusEndpointAvailability.buildUnavailablePlaceholderCategories(
				CanalPlusConf.NAME, CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL).iterator().next();
		assertTrue(manager.findEpisode(placeholder).isEmpty());
	}

	@Test
	public void findEpisodeFromHodorLandingFixtureUsesModernEpisodeUrls() throws IOException {
		final String landingJson = readFixture("hodor-landing-decouverte.json");
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new ByteArrayInputStream(landingJson.getBytes(StandardCharsets.UTF_8));
			}
		};

		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Decouverte",
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/decouverte.json?detailType=landingPage&objectType=brand",
				"mp4");
		category.setDownloadable(true);
		final Set<EpisodeDTO> episodes = manager.findEpisode(category);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().contains("31338503_50017"));
		assertTrue(episode.getName().contains("Les 10 hôtels"));
		assertNotNull(episode.getMetadata());
		assertEquals("31338503_50017", episode.getMetadata().getProviderEpisodeId());
		assertNull(episode.getMetadata().getSeriesTitle());
		assertEquals(
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/b63a43e7548cb1a6e7c7319084f48af8/okapi/31338503_50017.json?detailType=detailPage&objectType=unit",
				episode.getMetadata().getSourceUrl());
	}

	@Test
	public void catalogMetadataKeepsOriginalUrlPageWhenLegacyMediaUrlIsResolved() throws IOException {
		final String listingUrl = "http://service.mycanal.fr/page/listing.json";
		final String catalogUrl = "http://service.mycanal.fr/page/abc/123.json";
		final String categoryJson = "{\"strates\":[{\"type\":\"contentGrid\",\"contents\":[{"
				+ "\"title\":\"Legacy Show\",\"subtitle\":\"Ep 1\","
				+ "\"onClick\":{\"URLPage\":\"" + catalogUrl + "\"}}]}]}";
		final String mediaJson = "{\"detail\":{\"informations\":{\"VoD\":{\"videoURL\":\"https://cdn.example/master.m3u8\"}}}}";
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				if (catalogUrl.equals(url)) {
					return new ByteArrayInputStream(mediaJson.getBytes(StandardCharsets.UTF_8));
				}
				return new ByteArrayInputStream(categoryJson.getBytes(StandardCharsets.UTF_8));
			}
		};

		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Legacy", listingUrl, "mp4");
		final EpisodeDTO episode = manager.findEpisode(category).iterator().next();
		assertEquals("https://cdn.example/master.m3u8", episode.getId());
		assertEquals(catalogUrl, episode.getMetadata().getSourceUrl());
	}

	@Test
	public void findCategoryUsesModernHomeCatalogWithoutLegacyAuthenticate() throws IOException {
		final String homeHtml = readFixture("page-home-react-query.html");
		final String homeLanding = readFixture("hodor-home-landing.json");
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				if (CanalPlusConf.URL_HOME.equals(url)) {
					throw new AssertionError("legacy authenticate endpoint must not be used when modern catalog succeeds");
				}
				if (CanalPlusModernConf.PAGE_BASE_URL.equals(url)) {
					return new ByteArrayInputStream(homeHtml.getBytes(StandardCharsets.UTF_8));
				}
				if (url.contains("okapi/home.json")) {
					return new ByteArrayInputStream(homeLanding.getBytes(StandardCharsets.UTF_8));
				}
				if (url.contains("31338503_50017") || url.contains("objectType=unit")) {
					throw new AssertionError("unit detail pages must not be fetched during category discovery: " + url);
				}
				throw new TechnicalException("unexpected url " + url);
			}
		};

		final Set<CategoryDTO> categories = manager.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO discovery = categories.iterator().next();
		assertEquals("Découverte", discovery.getName());
		assertTrue(discovery.isDownloadable());
		assertTrue(discovery.getId().contains("okapi/decouverte.json"));
		assertFalse("Les 10 hôtels les plus incroyables de France".equals(discovery.getName()));
	}

	@Test
	public void findCategoryThenFindEpisodeUsesModernCatalogFixtures() throws IOException {
		final String homeHtml = readFixture("page-home-react-query.html");
		final String homeLanding = readFixture("hodor-home-landing.json");
		final String discoveryLanding = readFixture("hodor-landing-decouverte.json");
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				if (CanalPlusModernConf.PAGE_BASE_URL.equals(url)) {
					return new ByteArrayInputStream(homeHtml.getBytes(StandardCharsets.UTF_8));
				}
				if (url.contains("okapi/home.json")) {
					return new ByteArrayInputStream(homeLanding.getBytes(StandardCharsets.UTF_8));
				}
				if (url.contains("okapi/decouverte.json")) {
					return new ByteArrayInputStream(discoveryLanding.getBytes(StandardCharsets.UTF_8));
				}
				if (url.contains("31338503_50017") || url.contains("detailType=detailPage")) {
					throw new AssertionError("unit detail pages must not be fetched during category discovery: " + url);
				}
				throw new TechnicalException("unexpected url " + url);
			}
		};

		final CategoryDTO discovery = manager.findCategory().iterator().next();
		final Set<EpisodeDTO> episodes = manager.findEpisode(discovery);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getId().contains("31338503_50017"));
	}

	@Test
	public void findEpisodeMaterializesUnitDetailPayload() throws IOException {
		final String unitJson = readFixture("hodor-detail-unit.json");
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new ByteArrayInputStream(unitJson.getBytes(StandardCharsets.UTF_8));
			}
		};
		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Unit",
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json?detailType=detailPage&objectType=unit",
				"mp4");
		category.setDownloadable(true);
		final Set<EpisodeDTO> episodes = manager.findEpisode(category);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("31338503_50017", episode.getMetadata().getProviderEpisodeId());
		assertTrue(episode.getName().contains("Les 10 hôtels"));
		assertNull(episode.getMetadata().getSeriesTitle());
		assertEquals("Journaliste, animatrice télé, Caroline Ithurbide parcourt l'Hexagone.",
				episode.getMetadata().getDescription());
	}

	@Test
	public void downloadModernStreamFailsWithDrmMessageWithoutPlaysetFetch() throws IOException {
		final String hodorDetail = readFixture("hodor-detail-unit.json");
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				if (url.contains("playset") || url.contains("secure-gen-hapi")) {
					throw new AssertionError("playset must not be fetched before DRM fail");
				}
				if (url.contains("hodor.canalplus.pro")) {
					return new ByteArrayInputStream(hodorDetail.getBytes(StandardCharsets.UTF_8));
				}
				throw new TechnicalException("unexpected url " + url);
			}
		};

		try {
			manager.download(new DownloadParamDTO(
					"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json?detailType=detailPage&objectType=unit",
					"out.mp4", "mp4"), null);
			fail("expected DRM-protected download failure");
		} catch (DownloadFailedException e) {
			assertTrue(e.getMessage().contains("DRM-protected"));
			assertFalse(e.getMessage().toLowerCase().contains("playset"));
		}
	}

	@Test
	public void downloadModernPageFetchFailureNormalizesTechnicalException() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: " + url));
			}
		};

		try {
			manager.download(new DownloadParamDTO(
					"https://www.canalplus.com/decouverte/les-10-hotels/h/31338503_50017",
					"out.mp4", "mp4"), null);
			fail("expected page-fetch download failure");
		} catch (DownloadFailedException e) {
			assertTrue(e.getMessage().contains("page fetch failed"));
			assertTrue(e.getCause() instanceof TechnicalException);
		}
	}

	@Test
	public void downloadModernHodorForbiddenFailureKeepsProtectedAccessDiagnostic() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: " + url));
			}
		};

		try {
			manager.download(new DownloadParamDTO(
					"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json?detailType=detailPage&objectType=unit",
					"out.mp4", "mp4"), null);
			fail("expected catalog-fetch download failure");
		} catch (DownloadFailedException e) {
			assertTrue(e.getMessage().contains(CanalPlusEndpointAvailability.PROTECTED_ACCESS_DETAIL));
			assertFalse(e.getMessage().toLowerCase().contains("pass token"));
		}
	}

	@Test
	public void findEpisodeSkipsRowsWithBlankTitleAndSubtitle() {
		final String listingJson = "{\"strates\":[{\"type\":\"contentGrid\",\"contents\":[{"
				+ "\"title\":null,\"subtitle\":null,"
				+ "\"onClick\":{\"URLPage\":\"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/1.json\","
				+ "\"displayTemplate\":\"detailPage\"}},{"
				+ "\"title\":\"\",\"subtitle\":\"\","
				+ "\"onClick\":{\"URLPage\":\"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/2.json\","
				+ "\"displayTemplate\":\"detailPage\"}},{"
				+ "\"title\":null,\"subtitle\":\"Subtitle only\","
				+ "\"onClick\":{\"URLPage\":\"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json\","
				+ "\"displayTemplate\":\"detailPage\"}}]}]}";
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new ByteArrayInputStream(listingJson.getBytes(StandardCharsets.UTF_8));
			}
		};
		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Decouverte",
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/decouverte.json", "mp4");
		final Set<EpisodeDTO> episodes = manager.findEpisode(category);
		assertEquals(1, episodes.size());
		assertEquals("Subtitle only", episodes.iterator().next().getName());
	}

	@Test
	public void findEpisodeSkipsContentRowWithMissingContents() {
		final String listingJson = "{\"strates\":[{\"type\":\"contentRow\"},{\"type\":\"contentGrid\",\"contents\":[{"
				+ "\"title\":\"Unit\",\"subtitle\":\"\","
				+ "\"onClick\":{\"URLPage\":\"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json\","
				+ "\"displayTemplate\":\"detailPage\"}}]}]}";
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new ByteArrayInputStream(listingJson.getBytes(StandardCharsets.UTF_8));
			}
		};
		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Decouverte",
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/decouverte.json", "mp4");
		final Set<EpisodeDTO> episodes = manager.findEpisode(category);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getId().contains("31338503_50017"));
	}

	@Test
	public void findEpisodeReturnsEmptyWhenHodorIsForbidden() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: " + url));
			}
		};
		final CategoryDTO category = new CategoryDTO(CanalPlusConf.NAME, "Decouverte",
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/decouverte.json?detailType=landingPage&objectType=brand",
				"mp4");
		category.setDownloadable(true);
		assertTrue(manager.findEpisode(category).isEmpty());
	}

	@Test
	public void protectedAccessDiagnosticsDoNotMentionPassToken() {
		final TechnicalException error = new TechnicalException(new IOException(
				"Server returned HTTP response code: 403 for URL: https://hodor.canalplus.pro/test"));
		final String categoryMessage = CanalPlusEndpointAvailability.buildCategoryUnavailableMessage("canalPlus", error);
		final String episodeMessage = CanalPlusEndpointAvailability.buildEpisodeUnavailableMessage("canalPlus",
				new CategoryDTO("canalPlus", "Decouverte", "id", "mp4"), error);
		final String drmMessage = CanalPlusModernStreamSupport.buildDrmNotSupportedMessage();
		assertFalse(categoryMessage.toLowerCase().contains("pass token"));
		assertFalse(episodeMessage.toLowerCase().contains("pass token"));
		assertFalse(drmMessage.toLowerCase().contains("pass token"));
		assertTrue(categoryMessage.contains(CanalPlusEndpointAvailability.PROTECTED_ACCESS_DETAIL));
	}

	@Test
	public void extractStratesReadsRootAndNestedLandingPayloads() throws IOException {
		@SuppressWarnings("unchecked")
		final Map<String, Object> landing = MAPPER.readValue(readFixture("hodor-landing-decouverte.json"), Map.class);
		final List<Object> strates = CanalPlusHodorParser.extractStrates(landing);
		assertNotNull(strates);
		assertEquals(2, strates.size());
		assertTrue(CanalPlusHodorParser.hasUnitEpisodeContents(landing));
		@SuppressWarnings("unchecked")
		final Map<String, Object> firstStrate = (Map<String, Object>) strates.get(0);
		@SuppressWarnings("unchecked")
		final List<Object> firstContents = (List<Object>) firstStrate.get("contents");
		@SuppressWarnings("unchecked")
		final Map<String, Object> firstItem = (Map<String, Object>) firstContents.get(0);
		assertTrue(CanalPlusHodorParser.isUnitDetailItem(firstItem));

		@SuppressWarnings("unchecked")
		final Map<String, Object> nested = MAPPER.readValue(
				"{\"currentPage\":{\"strates\":[{\"type\":\"contentGrid\"}]}}", Map.class);
		assertEquals(1, CanalPlusHodorParser.extractStrates(nested).size());
	}

	private static boolean containsHubSeedLabels(final CategoryDTO root) {
		for (final CategoryDTO sub : root.getSubCategories()) {
			final String name = sub.getName();
			if ("Découverte".equals(name) || "Séries".equals(name) || "Cinéma".equals(name) || "Sport".equals(name)) {
				return true;
			}
		}
		return false;
	}

	private String readFixture(final String name) throws IOException {
		final String path = "test/resources/fixtures/canalplus/" + name;
		assertTrue("missing fixture " + path, new File(path).exists());
		try (InputStream input = new FileInputStream(path)) {
			final byte[] buffer = new byte[4096];
			final StringBuilder builder = new StringBuilder();
			int read;
			while ((read = input.read(buffer)) != -1) {
				builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
			}
			return builder.toString();
		}
	}

}
