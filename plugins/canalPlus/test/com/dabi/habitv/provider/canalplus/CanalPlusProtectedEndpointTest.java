package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CanalPlusProtectedEndpointTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

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
