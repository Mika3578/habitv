package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FranceTvPublicHubCatalogTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void discoversRolandGarrosFromSportHubFixture() throws IOException {
		final Map<String, Object> body = readFixture("channel-sport.json");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("sport",
				body);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("sport",
				result.getItems());

		assertEquals(1, children.size());
		assertEquals("Roland-Garros", children.get(0).getLabel());
		assertEquals("sport_tennis_roland-garros", children.get(0).getTaxonomySlug());
		assertTrue(result.getDiagnostics().getTaxonomyCandidates() >= 1);
	}

	@Test
	public void discoversFranceinfoProgramFromHubFixture() throws IOException {
		final Map<String, Object> body = readFixture("channel-franceinfo.json");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog
				.discoverHubItems("franceinfo", body);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("franceinfo",
				result.getItems());

		assertEquals(1, children.size());
		assertEquals("L'info s'éclaire", children.get(0).getLabel());
		assertEquals("franceinfo_l-info-s-eclaire", children.get(0).getTaxonomySlug());
	}

	@Test
	public void discoversInaProgramFromHubFixture() throws IOException {
		final Map<String, Object> body = readFixture("channel-ina.json");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("ina",
				body);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("ina",
				result.getItems());

		assertEquals(1, children.size());
		assertEquals("L'INA éclaire l'actu", children.get(0).getLabel());
		assertEquals("ina_l-ina-eclaire-l-actu", children.get(0).getTaxonomySlug());
	}

	@Test
	public void integraleItemsWithoutTaxonomySlugAreIgnored() throws IOException {
		final Map<String, Object> body = readFixture("channel-ina.json");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("ina",
				body);

		assertEquals(1, result.getItems().size());
	}

	@Test
	public void emptyResponseIsNonBlocking() {
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("arte",
				null);

		assertTrue(result.getItems().isEmpty());
		assertEquals("empty-response", result.getDiagnostics().getRootCauseSummary());
	}

	@Test
	public void fallbackSeedUsedWhenCandidatesMissing() {
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("sport",
				java.util.Collections.<FranceTvHubItem>emptyList());

		assertEquals(1, children.size());
		assertEquals("sport_tennis_roland-garros", children.get(0).getTaxonomySlug());
	}

	@Test
	public void configuredSeedIsListedBeforeOtherApiChildren() {
		final FranceTvHubItem roland = new FranceTvHubItem("Roland-Garros", "sport", "sport_tennis_roland-garros",
				"https://www.france.tv/sport/tennis/roland-garros/", FranceTvHubItem.ItemType.EVENT);
		final FranceTvHubItem other = new FranceTvHubItem("Other sport", "sport", "sport_other-event",
				"https://www.france.tv/sport/other-event/", FranceTvHubItem.ItemType.PROGRAM);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("sport",
				Arrays.asList(other, roland));

		assertEquals(2, children.size());
		assertEquals("Roland-Garros", children.get(0).getLabel());
		assertEquals("Other sport", children.get(1).getLabel());
	}

	@Test
	public void duplicateTaxonomySlugsAreNotDuplicatedInSelection() throws IOException {
		final Map<String, Object> body = readFixture("channel-sport.json");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("sport",
				body);
		final List<FranceTvHubItem> doubled = new java.util.ArrayList<FranceTvHubItem>(result.getItems());
		doubled.addAll(result.getItems());
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("sport", doubled);

		assertEquals(1, children.size());
	}

	@Test
	public void urlCompleteItemBecomesChildCategory() {
		final Map<String, Object> item = new java.util.HashMap<String, Object>();
		item.put("type", "event");
		item.put("label", "Tour de France");
		item.put("url_complete", "sport_cyclisme_tour-de-france");
		final FranceTvHubItem hubItem = FranceTvPublicHubCatalog.discoverHubItems("ina",
				singleItemHubBody(item)).getItems().get(0);

		assertEquals("sport_cyclisme_tour-de-france", hubItem.getTaxonomySlug());
		assertEquals(1, FranceTvPublicHubCatalog.selectNavigableChildren("ina",
				java.util.Collections.singletonList(hubItem)).size());
	}

	@Test
	public void collectionPathItemBecomesChildCategory() {
		final Map<String, Object> item = new java.util.HashMap<String, Object>();
		item.put("type", "collection");
		item.put("label", "Collection sport");
		item.put("collection_path", "collection_sport_highlights");
		final FranceTvHubItem hubItem = FranceTvPublicHubCatalog.discoverHubItems("ina",
				singleItemHubBody(item)).getItems().get(0);

		assertEquals("sport_highlights", hubItem.getTaxonomySlug());
	}

	@Test
	public void itemsWithoutTaxonomyCandidateAreSkippedWithDiagnostics() {
		final Map<String, Object> integrale = new java.util.HashMap<String, Object>();
		integrale.put("type", "integrale");
		integrale.put("title", "Replay sans slug");
		final FranceTvPublicHubCatalog.HubDiscoveryResult result = FranceTvPublicHubCatalog.discoverHubItems("ina",
				singleItemHubBody(integrale));

		assertTrue(result.getItems().isEmpty());
		assertEquals(FranceTvPublicHubCatalog.ROOT_CAUSE_NO_USABLE_TAXONOMY,
				result.getDiagnostics().getRootCauseSummary());
	}

	@Test
	public void apiChildrenAfterConfiguredSeedsAreSortedByLabel() {
		final FranceTvHubItem roland = new FranceTvHubItem("Roland-Garros", "sport", "sport_tennis_roland-garros",
				"https://www.france.tv/sport/tennis/roland-garros/", FranceTvHubItem.ItemType.EVENT);
		final FranceTvHubItem zebra = new FranceTvHubItem("Zebra event", "sport", "sport_zebra",
				"https://www.france.tv/sport/zebra/", FranceTvHubItem.ItemType.PROGRAM);
		final FranceTvHubItem alpha = new FranceTvHubItem("Alpha event", "sport", "sport_alpha",
				"https://www.france.tv/sport/alpha/", FranceTvHubItem.ItemType.PROGRAM);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren("sport",
				Arrays.asList(zebra, alpha, roland));

		assertEquals(3, children.size());
		assertEquals("Roland-Garros", children.get(0).getLabel());
		assertEquals("Alpha event", children.get(1).getLabel());
		assertEquals("Zebra event", children.get(2).getLabel());
	}

	private static Map<String, Object> singleItemHubBody(final Map<String, Object> item) {
		final Map<String, Object> collection = new java.util.HashMap<String, Object>();
		collection.put("items", java.util.Collections.singletonList(item));
		final Map<String, Object> body = new java.util.HashMap<String, Object>();
		body.put("collections", java.util.Collections.singletonList(collection));
		return body;
	}

	@Test
	public void taxonomySlugFromItemPrefersProgramPath() {
		final Map<String, Object> item = new java.util.HashMap<String, Object>();
		item.put("program_path", "france-2_journal");
		item.put("url_complete", "other_slug");

		assertEquals("france-2_journal", FranceTvPublicHubCatalog.taxonomySlugFromItem(item));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> readFixture(final String name) throws IOException {
		final String path = "test/resources/fixtures/francetv/api/" + name;
		assertTrue(new File(path).exists());
		try (InputStream input = new FileInputStream(path)) {
			final byte[] buffer = new byte[8192];
			int read;
			final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return MAPPER.readValue(output.toString("UTF-8"), Map.class);
		}
	}

}
