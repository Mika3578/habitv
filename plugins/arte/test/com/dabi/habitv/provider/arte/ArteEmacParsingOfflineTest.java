package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Offline test covering {@link ArtePluginManager} category browsing and EMAC
 * teaser parsing against captured JSON fixtures. Runs without network access so
 * the parsing contract can be verified in CI sandboxes where arte.tv is
 * unreachable.
 *
 * <p>Completeness expectations (supported languages, catalogue areas) are
 * asserted against literal values, never against {@link ArteConf} constants,
 * so a regression to a partial catalogue fails loudly.
 */
public class ArteEmacParsingOfflineTest {

	private static final String ZONE_ALPHA_ID = "zone-alpha";

	private static final String PAGE_FIXTURE = "test/resources/fixtures/arte/emac-page-multizone.json";
	private static final String ALPHA_PAGE2_FIXTURE = "test/resources/fixtures/arte/emac-zone-alpha-page2.json";
	private static final String BETA_PAGE2_FIXTURE = "test/resources/fixtures/arte/emac-zone-beta-page2.json";
	private static final String CONCERT_FIXTURE = "test/resources/fixtures/arte/emac-page-concert.json";
	private static final String ES_FIXTURE = "test/resources/fixtures/arte/emac-page-es.json";

	private static final String PAGE_URL = ArteConf.EMAC_API_BASE + "/fr/web/pages/DOR/?authorizedCountry=FR";
	private static final String ALPHA_PAGE2_URL = ArteConf.EMAC_API_BASE
			+ "/fr/web/zones/listing_ALPHA_main/content?page=2&pageId=DOR&authorizedCountry=FR";
	private static final String BETA_PAGE2_URL = "https://api.arte.tv/api/emac/v4/fr/web/zones/aced3934-9828-4d5d-9fbb-bf848fd6cb24/content?authorizedCountry=FR&page=2";

	@Test
	public void emacApiBaseUsesPublicApiHostNotRetiredRproxy() {
		assertEquals("https://api.arte.tv/api/emac/v4", ArteConf.EMAC_API_BASE);
		assertFalse(ArteConf.EMAC_API_BASE.contains("/rproxy/"));
	}

	@Test
	public void findEpisodeMergesIndependentListingsWithOrderAndDedup() throws IOException {
		final RecordingArtePlugin plugin = multizonePlugin(true, true);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);

		final List<String> ids = episodeIds(episodes);
		assertEquals(
				Arrays.asList("https://www.arte.tv/fr/videos/119999-000-A/test-documentary-one/",
						"https://www.arte.tv/fr/videos/119999-001-A/test-documentary-two/",
						"https://www.arte.tv/fr/videos/119999-200-A/alpha-page-two/",
						"https://www.arte.tv/fr/videos/119999-002-A/beta-one/",
						"https://www.arte.tv/fr/videos/119999-100-A/beta-two/",
						"https://www.arte.tv/fr/videos/119999-201-A/beta-page-two/",
						"https://www.arte.tv/fr/videos/119999-300-A/no-code-zone/"),
				ids);
		assertEquals("duplicate URL collapses into a single episode", new HashSet<>(ids).size(), ids.size());

		final List<String> names = episodeNames(episodes);
		assertTrue("relative URL resolved against HOME_URL", names.contains("Test Documentary One"));
		assertTrue("teaser without title falls back to subtitle", names.contains("Beta Subtitle Only"));
		assertFalse("non-episode URL filtered out", names.contains("Not An Episode (programmes path)"));

		for (final EpisodeDTO episode : episodes) {
			assertNotNull("canonical metadata attached", episode.getMetadata());
			assertNull("Arte thematic category must not become seriesTitle",
					episode.getMetadata().getSeriesTitle());
			assertEquals("fr", episode.getMetadata().getContentLanguage());
			assertNotNull(episode.getMetadata().getEpisodeTitle());
			assertEquals(episode.getId(), episode.getMetadata().getSourceUrl());
			assertNotNull("originating listing preserved in description",
					episode.getMetadata().getDescription());
		}
		assertTrue("subtitle kept alongside listing title",
				descriptions(episodes).contains("Alpha Picks — Episode 2"));
	}

	@Test
	public void findEpisodeSupportsSingleListingScope() throws IOException {
		final RecordingArtePlugin plugin = multizonePlugin(true, true);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Alpha Picks",
				ArteCategoryId.forZone("fr", "DOR", ZONE_ALPHA_ID), ArteConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);

		assertEquals(Arrays.asList("https://www.arte.tv/fr/videos/119999-000-A/test-documentary-one/",
				"https://www.arte.tv/fr/videos/119999-001-A/test-documentary-two/",
				"https://www.arte.tv/fr/videos/119999-200-A/alpha-page-two/"), episodeIds(episodes));
	}

	@Test
	public void findEpisodeReturnsEmptyForUnknownListing() throws IOException {
		final RecordingArtePlugin plugin = multizonePlugin(true, true);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "x", "fr:DOR:no_such_zone", ArteConf.EXTENSION);

		assertTrue(plugin.findEpisode(category).isEmpty());
	}

	@Test
	public void findEpisodeKeepsOtherListingsWhenOnePaginationFails() throws IOException {
		// Beta page 2 is missing: that listing keeps its first page while the
		// Alpha listing still paginates through the legacy pageId URL.
		final RecordingArtePlugin plugin = multizonePlugin(true, false);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		final List<String> ids = episodeIds(plugin.findEpisode(category));

		assertEquals(6, ids.size());
		assertTrue(ids.contains("https://www.arte.tv/fr/videos/119999-200-A/alpha-page-two/"));
		assertTrue(ids.contains("https://www.arte.tv/fr/videos/119999-100-A/beta-two/"));
		assertFalse(ids.contains("https://www.arte.tv/fr/videos/119999-201-A/beta-page-two/"));
	}

	@Test
	public void findEpisodeLoadsCollectionZones() throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		urlToContent.put(ArteConf.EMAC_API_BASE + "/fr/web/collections/RC-028069/?authorizedCountry=FR",
				readFixture("test/resources/fixtures/arte/emac-collection-rc.json"));
		final RecordingArtePlugin plugin = new RecordingArtePlugin(urlToContent);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Collection",
				ArteCategoryId.forCollection("fr", "RC-028069"), ArteConf.EXTENSION);

		final List<String> ids = episodeIds(plugin.findEpisode(category));
		assertEquals(Arrays.asList("https://www.arte.tv/fr/videos/122704-001-A/l-empire-lvmh-1-2/",
				"https://www.arte.tv/fr/videos/122704-002-A/l-empire-lvmh-2-2/"), ids);
	}

	@Test
	public void findEpisodeParsesConcertThroughCommonMechanism() throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		urlToContent.put(ArteConf.EMAC_API_BASE + "/fr/web/pages/ARTE_CONCERT/?authorizedCountry=FR",
				readFixture(CONCERT_FIXTURE));

		final RecordingArtePlugin plugin = new RecordingArtePlugin(urlToContent);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Concert", "fr:ARTE_CONCERT", ArteConf.EXTENSION);

		final List<String> ids = episodeIds(plugin.findEpisode(category));

		assertEquals(Arrays.asList("https://www.arte.tv/fr/videos/123976-000-A/gomorra-manifeste-antimafia/"), ids);
	}

	@Test
	public void findEpisodeParsesLocalisedCatalogue() throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		urlToContent.put(ArteConf.EMAC_API_BASE + "/es/web/pages/DEC/?authorizedCountry=FR",
				readFixture(ES_FIXTURE));

		final RecordingArtePlugin plugin = new RecordingArtePlugin(urlToContent);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Viajes", "es:DEC", ArteConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);

		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("https://www.arte.tv/es/videos/119999-400-A/viaje-uno/", episode.getId());
		assertEquals("Viaje Uno", episode.getName());
		assertEquals("es", episode.getMetadata().getContentLanguage());
	}

	@Test
	public void findEpisodeStillParsesLegacyValueWrappedFixtures() throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		urlToContent.put(PAGE_URL,
				"{\"value\":{\"zones\":[{\"code\":\"listing_LEGACY_main\",\"content\":{\"data\":[{\"url\":\"/fr/videos/119999-900-A/legacy/\",\"title\":\"Legacy Wrapped\"}],\"pagination\":{\"pages\":2}}}]}}");
		urlToContent.put(
				ArteConf.EMAC_API_BASE
						+ "/fr/web/zones/listing_LEGACY_main/content?page=2&pageId=DOR&authorizedCountry=FR",
				"{\"value\":{\"data\":[{\"url\":\"/fr/videos/119999-901-A/legacy-page-two/\",\"title\":\"Legacy Value Data\"}]}}");

		final RecordingArtePlugin plugin = new RecordingArtePlugin(urlToContent);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);

		final List<String> ids = episodeIds(episodes);
		assertEquals(2, episodes.size());
		assertTrue(ids.contains("https://www.arte.tv/fr/videos/119999-900-A/legacy/"));
		assertTrue(ids.contains("https://www.arte.tv/fr/videos/119999-901-A/legacy-page-two/"));
		assertTrue("legacy value.zones title must still be extracted",
				episodeNames(episodes).contains("Legacy Wrapped"));
		assertTrue("legacy value.data title must still be extracted",
				episodeNames(episodes).contains("Legacy Value Data"));
	}

	@Test
	public void findEpisodeRejectsInvalidCategoryIdentifiers() {
		final ArtePluginManager plugin = new ArtePluginManager();
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr:", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr:DOR:", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr:DOR:a:b", ArteConf.EXTENSION))
				.isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", ":DOR", ArteConf.EXTENSION)).isEmpty());
	}

	@Test
	public void findEpisodeReturnsEmptyOnUpstreamFailure() {
		final RecordingArtePlugin plugin = new RecordingArtePlugin(new HashMap<String, String>());
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		assertTrue("network failures must surface as an empty set, not a runtime exception",
				plugin.findEpisode(category).isEmpty());
	}

	private static RecordingArtePlugin multizonePlugin(final boolean withAlphaPage2, final boolean withBetaPage2)
			throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		urlToContent.put(PAGE_URL, readFixture(PAGE_FIXTURE));
		if (withAlphaPage2) {
			urlToContent.put(ALPHA_PAGE2_URL, readFixture(ALPHA_PAGE2_FIXTURE));
		}
		if (withBetaPage2) {
			urlToContent.put(BETA_PAGE2_URL, readFixture(BETA_PAGE2_FIXTURE));
		}
		return new RecordingArtePlugin(urlToContent);
	}

	private static List<String> episodeIds(final Set<EpisodeDTO> episodes) {
		final List<String> ids = new ArrayList<>();
		for (final EpisodeDTO episode : episodes) {
			ids.add(episode.getId());
		}
		return ids;
	}

	private static List<String> episodeNames(final Set<EpisodeDTO> episodes) {
		final List<String> names = new ArrayList<>();
		for (final EpisodeDTO episode : episodes) {
			names.add(episode.getName());
		}
		return names;
	}

	private static List<String> descriptions(final Set<EpisodeDTO> episodes) {
		final List<String> descriptions = new ArrayList<>();
		for (final EpisodeDTO episode : episodes) {
			descriptions.add(episode.getMetadata().getDescription());
		}
		return descriptions;
	}

	private static String readFixture(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toString("UTF-8");
		}
	}

	private static final class RecordingArtePlugin extends ArtePluginManager {

		RecordingArtePlugin(final Map<String, String> urlToContent) {
			super(new ArteCatalogDiscovery(recordingTransport(urlToContent)), recordingTransport(urlToContent));
		}

		private static ArteCatalogDiscovery.ArteEmacTransport recordingTransport(
				final Map<String, String> urlToContent) {
			return url -> {
				final String content = urlToContent.get(url);
				if (content == null) {
					throw new TechnicalException("unexpected URL fetched: " + url);
				}
				return content;
			};
		}
	}
}
