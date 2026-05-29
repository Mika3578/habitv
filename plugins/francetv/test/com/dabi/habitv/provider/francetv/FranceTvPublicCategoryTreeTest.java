package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.InputStream;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FranceTvPublicCategoryTreeTest {

	private static final String ROLAND_GARROS_URL = "https://www.france.tv/sport/tennis/roland-garros/";

	private static final String FRANCEINFO_PROGRAM_URL = "https://www.france.tv/franceinfo/l-info-s-eclaire/";

	private static final String ROLAND_TAXONOMY = "sport_tennis_roland-garros";

	private static final List<String> EXPECTED_ROOT_HUBS = Arrays.asList("Sport", "Franceinfo", "Arte",

			"TV5 Monde Plus", "France 24", "INA", "LCP", "Public Sénat", "Mieux");

	private static final List<String> GENERIC_RUBRIQUES = Arrays.asList("Séries & fictions", "Documentaires",

			"Cinéma", "Société", "Info", "Spectacles et culture", "Jeux et divertissements", "Enfants", "Podcasts");

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	@Test

	public void channelRootsRemainConfiguredSeparatelyFromPublicRoot() {

		assertEquals(5, FranceTvConf.CHANNEL_SLUGS.length);

		assertEquals(9, FranceTvConf.publicRootHubDisplayOrder().length);

		assertFalse(Arrays.asList(FranceTvConf.CHANNEL_SLUGS).contains("sport"));

	}

	@Test

	public void publicRootContainsOnlyCuratedHubRootsInDeterministicOrder() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertEquals(EXPECTED_ROOT_HUBS, childNames(publicRoot));

	}

	@Test

	public void publicHubOrderIsIndependentOfFixtureMapIterationOrder() throws IOException {

		final Map<String, Map<String, Object>> shuffled = new HashMap<String, Map<String, Object>>();

		shuffled.put("mieux", readApiFixture("channel-mieux.json"));

		shuffled.put("ina", readApiFixture("channel-ina.json"));

		shuffled.put("sport", readApiFixture("channel-sport.json"));

		shuffled.put("arte", readApiFixture("channel-arte.json"));

		shuffled.put("lcp", readApiFixture("channel-lcp.json"));

		shuffled.put("france-24", readApiFixture("channel-france-24.json"));

		shuffled.put("public-senat", readApiFixture("channel-public-senat.json"));

		shuffled.put("tv5-monde", readApiFixture("channel-tv5-monde.json"));

		shuffled.put("franceinfo", readApiFixture("channel-franceinfo.json"));

		final CategoryDTO publicRoot = new FranceTvPublicCategoryTreeBuilder(new StaticHubLoader(shuffled), null,

				null).buildPublicRootCategory();

		assertEquals(EXPECTED_ROOT_HUBS, childNames(publicRoot));

	}

	@Test

	public void publicHubOrderIsIndependentOfApiResponseCollectionOrder() throws IOException {

		final Map<String, Object> sportBody = readApiFixture("channel-sport.json");

		final List<Map<String, Object>> collections = FranceTvApiClient.castItemList(sportBody.get("collections"));

		Collections.reverse(collections);

		sportBody.put("collections", collections);

		final Map<String, Map<String, Object>> fixtures = loadAllHubFixtures();

		fixtures.put("sport", sportBody);

		final CategoryDTO sport = findChild(

				new FranceTvPublicCategoryTreeBuilder(new StaticHubLoader(fixtures), null, null).buildPublicRootCategory(),

				"Sport");

		assertNotNull(findChild(sport, "Roland-Garros"));

	}

	@Test

	public void publicRootDoesNotContainGenericRubriqueRoots() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final List<String> rootNames = childNames(publicRoot);

		for (final String rubrique : GENERIC_RUBRIQUES) {

			assertFalse("generic rubrique must not appear at public root: " + rubrique,

					rootNames.contains(rubrique));

		}

	}

	@Test

	public void publicRootDoesNotDirectlyContainRolandGarros() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertNull(findChild(publicRoot, "Roland-Garros"));

	}

	@Test

	public void publicRootDoesNotDirectlyContainFranceinfoProgram() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertNull(findChild(publicRoot, "L'info s'éclaire"));

	}

	@Test

	public void sportContainsRolandGarros() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final CategoryDTO sport = findChild(publicRoot, "Sport");

		final CategoryDTO rolandGarros = findChild(sport, "Roland-Garros");

		assertNotNull(rolandGarros);

		assertEquals(ROLAND_GARROS_URL, rolandGarros.getId());

		assertTrue(rolandGarros.isDownloadable());

	}

	@Test

	public void franceinfoContainsProgramPage() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final CategoryDTO franceinfo = findChild(publicRoot, "Franceinfo");

		final CategoryDTO program = findChild(franceinfo, "L'info s'éclaire");

		assertNotNull(program);

		assertEquals(FRANCEINFO_PROGRAM_URL, program.getId());

		assertTrue(program.isDownloadable());

	}

	@Test

	public void inaIsHubContainerWithApiChildNotEmptyReplayLeaf() throws IOException {

		final CategoryDTO ina = findChild(buildTreeWithApiFixtures(), "INA");

		assertNotNull(ina);

		assertFalse(ina.isDownloadable());

		assertFalse(ina.getSubCategories().isEmpty());

		assertNotNull(findChild(ina, "L'INA éclaire l'actu"));

	}

	@Test

	public void arteIsHubContainerWithApiChildNotEmptyReplayLeaf() throws IOException {

		final CategoryDTO arte = findChild(buildTreeWithApiFixtures(), "Arte");

		assertNotNull(arte);

		assertFalse(arte.isDownloadable());

		assertFalse(arte.getSubCategories().isEmpty());

		assertNotNull(findChild(arte, "Arte Journal"));

	}

	@Test

	public void lcpIsHubContainerWithApiChildNotEmptyReplayLeaf() throws IOException {

		final CategoryDTO lcp = findChild(buildTreeWithApiFixtures(), "LCP");

		assertNotNull(lcp);

		assertFalse(lcp.isDownloadable());

		assertFalse(lcp.getSubCategories().isEmpty());

	}

	@Test

	public void publicSenatIsHubContainerWithApiChildNotEmptyReplayLeaf() throws IOException {

		final CategoryDTO publicSenat = findChild(buildTreeWithApiFixtures(), "Public Sénat");

		assertNotNull(publicSenat);

		assertFalse(publicSenat.isDownloadable());

		assertFalse(publicSenat.getSubCategories().isEmpty());

	}

	@Test

	public void partnerHubsWithUsableApiCandidatesExposeChildCategories() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertFalse(findChild(publicRoot, "TV5 Monde Plus").getSubCategories().isEmpty());

		assertFalse(findChild(publicRoot, "France 24").getSubCategories().isEmpty());

		assertFalse(findChild(publicRoot, "Mieux").getSubCategories().isEmpty());

	}

	@Test

	public void partnerHubWithNoCandidatesRemainsVisibleContainer() throws IOException {

		final Map<String, Map<String, Object>> fixtures = loadAllHubFixtures();

		fixtures.put("arte", emptyHubFixture("Arte"));

		final CategoryDTO publicRoot = new FranceTvPublicCategoryTreeBuilder(new StaticHubLoader(fixtures), null,

				null).buildPublicRootCategory();

		final CategoryDTO arte = findChild(publicRoot, "Arte");

		assertNotNull(arte);

		assertFalse(arte.isDownloadable());

		assertTrue(arte.getSubCategories().isEmpty());

		assertEquals(EXPECTED_ROOT_HUBS, childNames(publicRoot));

	}

	@Test

	public void configuredSeedsDoNotDuplicateApiDiscoveredChildren() throws IOException {

		final CategoryDTO sport = findChild(buildTreeWithApiFixtures(), "Sport");

		assertEquals(1, sport.getSubCategories().size());

		assertNotNull(findChild(sport, "Roland-Garros"));

	}

	@Test
	public void apiHubItemsWithoutResolvablePageUrlAreSkipped() throws IOException {
		final Map<String, Object> body = new LinkedHashMap<String, Object>();
		final Map<String, Object> collection = new LinkedHashMap<String, Object>();
		final Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("type", "program");
		item.put("label", "En immersion");
		item.put("path", "noslugseparator");
		collection.put("items", Collections.singletonList(item));
		body.put("collections", Collections.singletonList(collection));

		final Map<String, Map<String, Object>> fixtures = Collections.singletonMap("sport", body);
		final CategoryDTO sport = findChild(new FranceTvPublicCategoryTreeBuilder(new StaticHubLoader(fixtures), null,
				null).buildPublicRootCategory(), "Sport");
		assertNotNull(sport);
		assertNull(findChild(sport, "En immersion"));

	}

	@Test

	public void hubContainerEpisodeLookupDoesNotUseHtmlFallback() {

		final FranceTvPluginManager manager = new FranceTvPluginManager();

		final CategoryDTO ina = new CategoryDTO(FranceTvConf.NAME, "INA", "https://www.france.tv/ina/",

				FranceTvConf.EXTENSION);

		ina.setDownloadable(false);

		assertTrue(manager.findEpisode(ina).isEmpty());

	}

	@Test

	public void rolandGarrosUsesTaxonomySlugFromApi() throws IOException {

		final CategoryDTO rolandGarros = findChild(findChild(buildTreeWithApiFixtures(), "Sport"), "Roland-Garros");

		assertEquals(ROLAND_TAXONOMY, FranceTvUrls.programPathFromCategoryUrl(rolandGarros.getId()));

	}

	@Test

	public void rolandGarrosEpisodesLoadFromTaxonomyFixture() throws IOException {

		final CategoryDTO rolandGarros = findChild(findChild(buildTreeWithApiFixtures(), "Sport"), "Roland-Garros");

		final Set<EpisodeDTO> episodes = loadEpisodesFromTaxonomyFixture(rolandGarros, ROLAND_TAXONOMY,

				"taxonomy-roland-garros-page0.json");

		assertFalse(episodes.isEmpty());

		for (final EpisodeDTO episode : episodes) {

			assertTrue(FranceTvUrls.isVideoReplayUrl(episode.getId()));

		}

	}

	@Test

	public void publicRootDoesNotContainMieuxEnDirectLabel() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertNull(findChild(publicRoot, "Mieux en direct"));

		for (final CategoryDTO child : publicRoot.getSubCategories()) {

			assertFalse(child.getName().contains("Mieux en direct"));

		}

	}

	@Test

	public void mieuxHubPointsToBrowseRootNotDirectStream() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final CategoryDTO mieux = findChild(publicRoot, "Mieux");

		assertNotNull(mieux);

		assertEquals("https://www.france.tv/mieux/", mieux.getId());

		assertFalse(mieux.getId().contains("/direct.html"));

	}

	@Test

	public void partnerHubLabelsUseShortCuratedNames() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		assertEquals("Arte", findChild(publicRoot, "Arte").getName());

		assertEquals("INA", findChild(publicRoot, "INA").getName());

		assertEquals("LCP", findChild(publicRoot, "LCP").getName());

		assertFalse(findChild(publicRoot, "Arte").getName().contains("Arté"));

		assertFalse(findChild(publicRoot, "INA").getName().contains("Institut"));

		assertFalse(findChild(publicRoot, "LCP").getName().contains("Assemblée"));

	}

	@Test

	public void partnerHubsDoNotContainSportSeedSections() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final CategoryDTO arte = findChild(publicRoot, "Arte");

		assertEquals("https://www.france.tv/arte/", arte.getId());

		assertFalse(childNames(arte).contains("Roland-Garros"));

	}

	@Test

	public void publicHubOrderParameterMatchesDisplayOrder() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		int index = 0;

		for (final CategoryDTO child : publicRoot.getSubCategories()) {

			assertEquals(String.valueOf(index),

					child.getParameter(FranceTvPublicCategoryTreeBuilder.PUBLIC_HUB_ORDER_PARAM));

			index++;

		}

	}

	@Test

	public void rolandGarrosHasNoHtmlSectionChildren() throws IOException {

		final CategoryDTO rolandGarros = findChild(findChild(buildTreeWithApiFixtures(), "Sport"), "Roland-Garros");

		assertTrue(rolandGarros.getSubCategories().isEmpty());

	}

	@Test

	public void directStreamUrlIsNotPublicBrowseRoot() {

		assertFalse(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/mieux/direct.html"));

	}

	@Test

	public void apiDiscoveryFailureIsNonBlocking() throws IOException {

		final FranceTvPublicCategoryTreeBuilder builder = new FranceTvPublicCategoryTreeBuilder(

				new FailingHubLoader(), null, null);

		final CategoryDTO publicRoot = builder.buildPublicRootCategory();

		assertEquals(EXPECTED_ROOT_HUBS, childNames(publicRoot));

		final CategoryDTO sport = findChild(publicRoot, "Sport");

		assertNotNull(findChild(sport, "Roland-Garros"));

		assertTrue(findChild(publicRoot, "Arte").getSubCategories().isEmpty());

	}

	@Test

	public void emptyApiResponseIsNonBlocking() throws IOException {

		final Map<String, Object> empty = readApiFixture("channel-empty.json");

		final FranceTvPublicCategoryTreeBuilder builder = new FranceTvPublicCategoryTreeBuilder(

				new StaticHubLoader(Collections.singletonMap("sport", empty)), null, null);

		final CategoryDTO sport = findChild(builder.buildPublicRootCategory(), "Sport");

		assertNotNull(findChild(sport, "Roland-Garros"));

	}

	@Test

	public void malformedApiResponseIsNonBlocking() throws IOException {

		final FranceTvPublicCategoryTreeBuilder builder = new FranceTvPublicCategoryTreeBuilder(

				new FranceTvPublicCategoryTreeBuilder.ChannelHubLoader() {

					@Override

					public Map<String, Object> loadChannelHub(final String hubSlug) throws IOException {

						throw new IOException("malformed-response");

					}

				}, null, null);

		final CategoryDTO publicRoot = builder.buildPublicRootCategory();

		assertEquals(EXPECTED_ROOT_HUBS, childNames(publicRoot));

	}

	@Test

	public void htmlDiscoveryDoesNotRunDuringPublicTreeBuild() throws IOException {

		final FranceTvPublicCategoryTreeBuilder builder = new FranceTvPublicCategoryTreeBuilder(

				new StaticHubLoader(loadAllHubFixtures()), null, null);

		final CategoryDTO rolandGarros = findChild(findChild(builder.buildPublicRootCategory(), "Sport"),

				"Roland-Garros");

		assertTrue(rolandGarros.getSubCategories().isEmpty());

	}

	@Test

	public void treeShapeDumpHelperIsStable() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final String shape = FranceTvPublicCategoryTreeBuilder.formatTreeShape(publicRoot, 3);

		assertTrue(shape.contains("Pages publiques (france.tv)"));

		assertTrue(shape.contains("Sport"));

		assertTrue(shape.contains("Roland-Garros"));

		assertTrue(shape.contains("INA"));

		assertFalse(shape.contains("Séries & fictions"));

	}

	@Test

	public void publicHubDebugReportExposesOrderAndChildren() throws IOException {

		final CategoryDTO publicRoot = buildTreeWithApiFixtures();

		final String report = FranceTvPublicCategoryTreeBuilder.formatPublicHubDebugReport(publicRoot);

		assertTrue(report.contains("hub=Sport"));

		assertTrue(report.contains("order=0"));

		assertTrue(report.contains("child=Roland-Garros"));

		assertTrue(report.contains("hub=INA"));

		assertTrue(report.contains("child=L'INA éclaire l'actu"));

		assertTrue(report.contains("downloadable=false"));

	}

	@Test

	public void hubDiagnosticsExposeRootCauseOnFailure() {

		final FranceTvPublicHubDiagnostics diagnostics = new FranceTvPublicHubDiagnostics("sport");

		diagnostics.setRootCauseSummary("io-error:IOException");

		assertTrue(diagnostics.formatLogLine("https://example.test").contains("strategy=public-api-channel-hub"));

		assertTrue(diagnostics.formatLogLine("https://example.test").contains("rootCause=io-error:IOException"));

		assertTrue(diagnostics.formatLogLine("https://example.test").contains("createdReplayItems="));

	}

	@Test

	public void taxonomyDiagnosticsExposeStrategy() {

		final FranceTvTaxonomyDiagnostics diagnostics = new FranceTvTaxonomyDiagnostics(ROLAND_TAXONOMY);

		diagnostics.setCreatedReplayItems(2);

		assertTrue(diagnostics.formatLogLine("https://example.test").contains("strategy=taxonomy-api"));

		assertTrue(diagnostics.formatLogLine("https://example.test").contains("taxonomyPath=" + ROLAND_TAXONOMY));

	}

	private static CategoryDTO buildTreeWithApiFixtures() throws IOException {

		return new FranceTvPublicCategoryTreeBuilder(new StaticHubLoader(loadAllHubFixtures()), null, null)

				.buildPublicRootCategory();

	}

	private static Map<String, Map<String, Object>> loadAllHubFixtures() throws IOException {

		final Map<String, Map<String, Object>> fixtures = new LinkedHashMap<String, Map<String, Object>>();

		fixtures.put("sport", readApiFixture("channel-sport.json"));

		fixtures.put("franceinfo", readApiFixture("channel-franceinfo.json"));

		fixtures.put("arte", readApiFixture("channel-arte.json"));

		fixtures.put("tv5-monde", readApiFixture("channel-tv5-monde.json"));

		fixtures.put("france-24", readApiFixture("channel-france-24.json"));

		fixtures.put("ina", readApiFixture("channel-ina.json"));

		fixtures.put("lcp", readApiFixture("channel-lcp.json"));

		fixtures.put("public-senat", readApiFixture("channel-public-senat.json"));

		fixtures.put("mieux", readApiFixture("channel-mieux.json"));

		return fixtures;

	}

	private static Map<String, Object> emptyHubFixture(final String label) {

		final Map<String, Object> body = new HashMap<String, Object>();

		body.put("label", label);

		body.put("collections", Collections.emptyList());

		return body;

	}

	private static Set<EpisodeDTO> loadEpisodesFromTaxonomyFixture(final CategoryDTO category,

			final String taxonomyPath, final String fixtureName) throws IOException {

		final Map<String, Object> body = readApiFixture(fixtureName);

		final Set<EpisodeDTO> episodes = new java.util.LinkedHashSet<EpisodeDTO>();

		for (final Map<String, Object> item : FranceTvApiClient.castItemList(body.get("items"))) {

			final Object type = item.get("type");

			if (type == null || !FranceTvUrls.isReplayVideoType(String.valueOf(type))) {

				continue;

			}

			final String pageUrl = FranceTvUrls.episodePageUrl(item, taxonomyPath);

			if (pageUrl == null) {

				continue;

			}

			final EpisodeDTO episode = new EpisodeDTO(category, String.valueOf(item.get("title")), pageUrl);

			FranceTvEpisodeMetadata.apply(item, episode);

			episodes.add(episode);

		}

		return episodes;

	}

	private static Map<String, Object> readApiFixture(final String name) throws IOException {

		final String path = "test/resources/fixtures/francetv/api/" + name;

		assertTrue("missing fixture " + path, new File(path).exists());

		try (InputStream input = new FileInputStream(path)) {

			return MAPPER.readValue(readUtf8(input), MAP_TYPE);

		}

	}

	private static CategoryDTO findChild(final CategoryDTO parent, final String name) {

		if (parent == null) {

			return null;

		}

		for (final CategoryDTO child : parent.getSubCategories()) {

			if (name.equals(child.getName())) {

				return child;

			}

		}

		return null;

	}

	private static List<String> childNames(final CategoryDTO parent) {

		final List<String> names = new ArrayList<String>();

		for (final CategoryDTO child : parent.getSubCategories()) {

			names.add(child.getName());

		}

		return names;

	}

	private static String readUtf8(final InputStream input) throws IOException {

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		final byte[] buffer = new byte[4096];

		int read;

		while ((read = input.read(buffer)) != -1) {

			output.write(buffer, 0, read);

		}

		return output.toString("UTF-8");

	}

	private static final class StaticHubLoader implements FranceTvPublicCategoryTreeBuilder.ChannelHubLoader {

		private final Map<String, Map<String, Object>> fixtures;

		StaticHubLoader(final Map<String, Map<String, Object>> fixtures) {

			this.fixtures = fixtures;

		}

		@Override

		public Map<String, Object> loadChannelHub(final String hubSlug) throws IOException {

			final Map<String, Object> fixture = fixtures.get(hubSlug);

			if (fixture == null) {

				return emptyHubFixture(hubSlug);

			}

			return fixture;

		}

	}

	private static final class FailingHubLoader implements FranceTvPublicCategoryTreeBuilder.ChannelHubLoader {

		@Override

		public Map<String, Object> loadChannelHub(final String hubSlug) throws IOException {

			throw new IOException("network-unavailable");

		}

	}

}
