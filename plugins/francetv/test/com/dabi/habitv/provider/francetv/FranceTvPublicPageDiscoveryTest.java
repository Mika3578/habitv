package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class FranceTvPublicPageDiscoveryTest {

	private static final String COLLECTION_URL = "https://www.france.tv/sport/tennis/roland-garros/";

	private static final String FRANCEINFO_PROGRAM_URL = "https://www.france.tv/franceinfo/l-info-s-eclaire/";

	@Test
	public void franceinfoProgramFixtureParsesEditionsWithReplayLinks() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(FRANCEINFO_PROGRAM_URL,
				readFixture("franceinfo-l-info-s-eclaire.html"));

		assertTrue(sectionLabels(result).contains("Les éditions"));
		assertTrue(result.getDiagnostics().getCreatedReplayItems() >= 10);

		boolean foundEpisode = false;
		for (final FranceTvDiscoverySection section : result.getSections()) {
			for (final FranceTvDiscoveryItem item : section.getItems()) {
				if (!item.isReplayCandidate()) {
					continue;
				}
				if (item.getItemUrl().contains("8472138-emission-du-vendredi-29-mai-2026")) {
					foundEpisode = true;
					assertTrue(item.getTitle().contains("29 mai 2026"));
				}
			}
		}
		assertTrue(foundEpisode);
	}

	@Test
	public void rolandGarrosFixtureParsesMainSections() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-collection.html"));

		assertTrue(result.getSections().size() >= 5);
		assertEquals("Roland-Garros", result.getPageTitle());

		final Set<String> labels = sectionLabels(result);
		assertTrue(labels.contains("Notre sélection"));
		assertTrue(labels.contains("En direct"));
		assertTrue(labels.contains("Les résumés des matchs"));
		assertTrue(labels.contains("Les plus beaux points du tournoi"));

		final FranceTvDiscoveryDiagnostics diagnostics = result.getDiagnostics();
		assertTrue(diagnostics.getHeadingsFound() >= 5);
		assertTrue(diagnostics.getLinksScanned() > 0);
		assertTrue(diagnostics.getCandidateItemLinks() > 0);
		assertTrue(diagnostics.getCreatedReplayItems() > 0);
		assertEquals(result.getSections().size(), diagnostics.getCreatedCategories());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void replayCandidatesHaveTitleAndUrl() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-collection.html"));

		boolean foundNamedReplay = false;
		for (final FranceTvDiscoverySection section : result.getSections()) {
			for (final FranceTvDiscoveryItem item : section.getItems()) {
				if (!item.isReplayCandidate()) {
					continue;
				}
				assertNotNull(item.getTitle());
				assertFalse(item.getTitle().trim().isEmpty());
				assertTrue(FranceTvUrls.isVideoReplayUrl(item.getItemUrl()));
				if (item.getItemUrl().contains("8533859-3e-tour-joao-fonseca-vs-novak-djokovic")) {
					foundNamedReplay = true;
					assertEquals("3e tour : Joao Fonseca vs Novak Djokovic", item.getTitle());
				}
			}
		}
		assertTrue(foundNamedReplay);
	}

	@Test
	public void durationAndDateExtractionWhenPresent() {
		assertEquals(Long.valueOf(720L), FranceTvPublicPageDiscovery.parseDurationSeconds("Durée 12 min"));
		assertEquals(Long.valueOf(5160L), FranceTvPublicPageDiscovery.parseDurationSeconds("1 h 26 min"));
		assertEquals(Long.valueOf(332L), FranceTvPublicPageDiscovery.parseDurationSeconds("05:32"));
		assertNotNull(FranceTvPublicPageDiscovery.parsePublicationDate("Diffusé le 22 mai 2026"));
		assertNotNull(FranceTvPublicPageDiscovery.parsePublicationDate("2026-05-22"));
		assertNotNull(FranceTvPublicPageDiscovery.parsePublicationDate("Diffusé le 29/05/2026"));
	}

	@Test
	public void nonVideoCollectionLinksAreMarkedSafely() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-collection.html"));

		boolean foundDirect = false;
		for (final FranceTvDiscoverySection section : result.getSections()) {
			for (final FranceTvDiscoveryItem item : section.getItems()) {
				if (item.getItemUrl().contains("/sport/direct.html")) {
					foundDirect = true;
					assertTrue(item.isCollectionOnly());
				}
			}
		}
		assertTrue(foundDirect);
		assertTrue(result.getDiagnostics().getRejectedLinks() > 0);
	}

	@Test
	public void emptyMarkupReturnsDiagnosticWithoutThrowing() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-empty.html"));

		assertTrue(result.getSections().isEmpty());
		assertEquals("no-section-headings", result.getDiagnostics().getRootCauseSummary());
		assertEquals(0, result.getDiagnostics().getCreatedReplayItems());
	}

	@Test
	public void rejectionCountersAreDeterministic() throws IOException {
		final String html = readFixture("roland-garros-collection.html");
		final FranceTvDiscoveryResult first = FranceTvPublicPageDiscovery.discover(COLLECTION_URL, html);
		final FranceTvDiscoveryResult second = FranceTvPublicPageDiscovery.discover(COLLECTION_URL, html);

		assertEquals(first.getDiagnostics().getLinksScanned(), second.getDiagnostics().getLinksScanned());
		assertEquals(first.getDiagnostics().getRejectedLinks(), second.getDiagnostics().getRejectedLinks());
		assertEquals(first.getDiagnostics().getCandidateItemLinks(),
				second.getDiagnostics().getCandidateItemLinks());
		assertEquals(first.getDiagnostics().getCreatedReplayItems(),
				second.getDiagnostics().getCreatedReplayItems());
	}

	@Test
	public void mapperBuildsSectionCategoriesAndEpisodes() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-collection.html"));
		final CategoryDTO collection = FranceTvDiscoveryMapper.toCollectionCategory(result, COLLECTION_URL);

		assertFalse(collection.isDownloadable());
		assertTrue(collection.getSubCategories().size() >= 5);

		CategoryDTO downloadableSection = null;
		for (final CategoryDTO section : collection.getSubCategories()) {
			if (section.isDownloadable()) {
				downloadableSection = section;
				break;
			}
		}
		assertNotNull(downloadableSection);

		final Set<EpisodeDTO> episodes = FranceTvDiscoveryMapper.toEpisodes(result, downloadableSection);
		assertFalse(episodes.isEmpty());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(FranceTvUrls.isVideoReplayUrl(episode.getId()));
		}
	}

	@Test
	public void diagnosticsLogLineContainsRequiredFields() throws IOException {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(COLLECTION_URL,
				readFixture("roland-garros-collection.html"));
		final String line = result.getDiagnostics().formatLogLine(COLLECTION_URL);

		assertTrue(line.contains("provider=France.tv"));
		assertTrue(line.contains("sourceUrl=" + COLLECTION_URL));
		assertTrue(line.contains("strategy=public-html-section-cards"));
		assertTrue(line.contains("headingsFound="));
		assertTrue(line.contains("linksScanned="));
		assertTrue(line.contains("candidateItemLinks="));
		assertTrue(line.contains("rejectedLinks="));
		assertTrue(line.contains("createdCategories="));
		assertTrue(line.contains("createdReplayItems="));
		assertTrue(line.contains("rootCause="));
	}

	private static Set<String> sectionLabels(final FranceTvDiscoveryResult result) {
		final Set<String> labels = new HashSet<>();
		for (final FranceTvDiscoverySection section : result.getSections()) {
			labels.add(section.getLabel());
		}
		return labels;
	}

	private static String readFixture(final String name) throws IOException {
		final String path = "test/resources/fixtures/francetv/" + name;
		assertTrue("missing fixture " + path, new File(path).exists());
		try (InputStream input = new FileInputStream(path)) {
			return readUtf8(input);
		}
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
}
