package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class Tf1PlusCatalogueRefreshTest {

	@Test
	public void shouldPaginateProgrammePagesUntilShortPage() throws IOException {
		final PagingTf1PlusPluginManager plugin = new PagingTf1PlusPluginManager();
		final Tf1PlusCatalogueClient client = new Tf1PlusCatalogueClient(plugin);
		final Tf1PlusHubDescriptor hub = Tf1PlusHubDescriptor.flat("tmc", "TMC", "tmc", Tf1PlusConf.TMC_REPLAY_URL);
		final Tf1PlusCataloguePageResult page0 = client.fetchProgramPage(hub, 0, 2);
		assertEquals(2, page0.getEntries().size());
		assertFalse(page0.isLastPage());
		final Tf1PlusCataloguePageResult page1 = client.fetchProgramPage(hub, 2, 2);
		assertEquals(1, page1.getEntries().size());
		assertTrue(page1.isLastPage());
	}

	@Test
	public void shouldDetectSuspectedApiCapWhenProbePageIsEmpty() throws IOException {
		final CapProbeTf1PlusPluginManager plugin = new CapProbeTf1PlusPluginManager();
		final Tf1PlusCatalogueClient client = new Tf1PlusCatalogueClient(plugin);
		final Tf1PlusHubDescriptor hub = Tf1PlusHubDescriptor.flat("tmc", "TMC", "tmc", Tf1PlusConf.TMC_REPLAY_URL);
		final Tf1PlusCatalogueRefreshResult result = new Tf1PlusCatalogueRefreshResult();
		client.fetchAllProgrammesForHub(hub, result);
		assertTrue(result.entriesView().size() >= 1);
		assertTrue(result.formatLogLine().contains("suspectedApiCapReached=true"));
	}

	@Test
	public void shouldUseFreshCacheWithoutLiveRefresh() throws IOException {
		final Path cacheFile = Files.createTempFile("tf1plus-cache-fresh-", ".json");
		final Tf1PlusCatalogueCache cache = new Tf1PlusCatalogueCache(cacheFile);
		final List<Tf1PlusCatalogueEntry> entries = Arrays.asList(entry("tmc", "cached-show", "Cached show"));
		cache.save(entries);

		final FailingTf1PlusPluginManager plugin = new FailingTf1PlusPluginManager();
		final Tf1PlusCatalogueService service = new Tf1PlusCatalogueService(
				new Tf1PlusCatalogueClient(plugin), cache, new Tf1PlusTreeBuilder(), null);
		final Set<CategoryDTO> tree = service.buildCategoryTree();
		final CategoryDTO tmc = findByName(tree, "TMC");
		assertNotNull(tmc);
		assertEquals(new HashSet<String>(Arrays.asList("Cached show")), programmeNames(tmc));
	}

	@Test
	public void shouldUseStaleCacheWhenLiveRefreshFails() throws IOException {
		final Path cacheFile = Files.createTempFile("tf1plus-cache-stale-", ".json");
		final long staleTimestamp = System.currentTimeMillis() - (13L * 60L * 60L * 1000L);
		final String staleJson = "{\"schemaVersion\":1,\"cachedAtMillis\":" + staleTimestamp
				+ ",\"entries\":[{\"programmeId\":\"\",\"slug\":\"cached-doc\",\"title\":\"Cached doc\","
				+ "\"hubId\":\"arte\",\"urlSlug\":\"arte\",\"publicUrl\":\"https://www.tf1.fr/arte/cached-doc\","
				+ "\"editorialCategoryTypes\":[],\"rights\":[],\"thumbnailUrl\":\"\"}]}";
		Files.write(cacheFile, staleJson.getBytes(StandardCharsets.UTF_8));

		final FailingTf1PlusPluginManager plugin = new FailingTf1PlusPluginManager();
		final Tf1PlusCatalogueService service = new Tf1PlusCatalogueService(
				new Tf1PlusCatalogueClient(plugin), new Tf1PlusCatalogueCache(cacheFile), new Tf1PlusTreeBuilder(), null);
		System.setProperty(Tf1PlusConf.PROPERTY_CATALOGUE_CACHE_TTL_HOURS, "6");
		try {
			final Set<CategoryDTO> tree = service.buildCategoryTree();
			final CategoryDTO arte = findByName(tree, "ARTE");
			assertNotNull(arte);
			assertEquals(new HashSet<String>(Arrays.asList("Cached doc")), programmeNames(arte));
		} finally {
			System.clearProperty(Tf1PlusConf.PROPERTY_CATALOGUE_CACHE_TTL_HOURS);
		}
	}

	@Test
	public void shouldReturnHubShellsWhenNoCacheAndLiveRefreshFails() throws IOException {
		final Path cacheFile = Files.createTempFile("tf1plus-cache-miss-", ".json");
		Files.delete(cacheFile);
		final Tf1PlusCatalogueService service = new Tf1PlusCatalogueService(
				new Tf1PlusCatalogueClient(new FailingTf1PlusPluginManager()),
				new Tf1PlusCatalogueCache(cacheFile), new Tf1PlusTreeBuilder(), null);
		final Set<CategoryDTO> tree = service.buildCategoryTree();
		assertEquals(7, tree.size());
		for (final CategoryDTO hub : tree) {
			assertTrue(hub.getSubCategories() == null || hub.getSubCategories().isEmpty());
		}
	}

	@Test
	public void shouldInvalidateCacheOnSchemaMismatch() throws IOException {
		final Path cacheFile = Files.createTempFile("tf1plus-cache-schema-", ".json");
		Files.write(cacheFile, "{\"schemaVersion\":0,\"cachedAtMillis\":1,\"entries\":[]}".getBytes(StandardCharsets.UTF_8));
		final Tf1PlusCatalogueCache.Snapshot snapshot = new Tf1PlusCatalogueCache(cacheFile).load();
		assertFalse(snapshot.isValid());
	}

	@Test
	public void shouldBuildTf1GroupedTreeWithFilmsAndUnknownRubricFallback() {
		final Tf1PlusTreeBuilder builder = new Tf1PlusTreeBuilder();
		final List<Tf1PlusCatalogueEntry> entries = Arrays.asList(
				entryWithRubric("tf1", "series-a", "Series A", "MAIN_SERIES_AND_FICTIONS"),
				entryWithRubric("tf1", "movie-a", "Movie A", "MAIN_MOVIES"),
				entryWithRubric("tf1", "unknown-a", "Unknown A", "MAIN_UNKNOWN_TYPE"));
		final Set<CategoryDTO> tree = builder.buildTree(entries);
		final CategoryDTO tf1 = findByName(tree, "TF1");
		assertNotNull(tf1);
		assertNotNull(findByName(tf1.getSubCategories(), "Séries"));
		assertNotNull(findByName(tf1.getSubCategories(), "Films"));
		assertNotNull(findByName(tf1.getSubCategories(), Tf1PlusEditorialRubricRegistry.FALLBACK_LABEL));
	}

	@Test
	public void shouldAvoidDuplicateProgrammeNodesAcrossTree() {
		final Tf1PlusTreeBuilder builder = new Tf1PlusTreeBuilder();
		final List<Tf1PlusCatalogueEntry> entries = Arrays.asList(
				entry("tmc", "duplicate-show", "Duplicate show"),
				entry("tmc", "duplicate-show", "Duplicate show"));
		final CategoryDTO tmc = findByName(builder.buildTree(entries), "TMC");
		assertEquals(1, programmeNames(tmc).size());
	}

	@Test
	public void shouldEmitSanitizedCatalogueDiagnostics() throws IOException {
		final Tf1PlusCatalogueRefreshResult result = new Tf1PlusCatalogueRefreshResult();
		result.setRefreshSucceeded(true);
		result.addProgrammesFetched(2);
		result.incrementProgrammesAdded();
		result.markFinished();
		final String line = result.formatLogLine();
		assertTrue(line.contains("provider=TF1+"));
		assertTrue(line.contains("programmesFetched=2"));
		assertFalse(line.toLowerCase().contains("password"));
		assertFalse(line.toLowerCase().contains("token="));
	}

	private static Tf1PlusCatalogueEntry entry(final String hubId, final String slug, final String title) {
		return new Tf1PlusCatalogueEntry("", slug, title, hubId, hubId,
				Tf1PlusCatalogueClient.buildProgramUrl(hubId, slug), Collections.<String>emptyList(),
				Collections.<String>emptyList(), "");
	}

	private static Tf1PlusCatalogueEntry entryWithRubric(final String hubId, final String slug, final String title,
			final String rubricType) {
		return new Tf1PlusCatalogueEntry("", slug, title, hubId, hubId,
				Tf1PlusCatalogueClient.buildProgramUrl(hubId, slug), Arrays.asList(rubricType),
				Collections.<String>emptyList(), "");
	}

	private static CategoryDTO findByName(final Set<CategoryDTO> categories, final String name) {
		for (final CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}

	private static Set<String> programmeNames(final CategoryDTO parent) {
		final Set<String> names = new HashSet<String>();
		if (parent == null || parent.getSubCategories() == null) {
			return names;
		}
		for (final CategoryDTO subCategory : parent.getSubCategories()) {
			if (subCategory.isDownloadable()) {
				names.add(subCategory.getName());
			} else {
				names.addAll(programmeNames(subCategory));
			}
		}
		return names;
	}

	private static final class PagingTf1PlusPluginManager extends Tf1PlusPluginManager {
		@Override
		public InputStream getInputStreamFromUrl(final String url) {
			try {
				final int offset = extractOffset(url);
				final int limit = extractLimit(url);
				final String json;
				if (offset == 0 && limit == 2) {
					json = "{\"data\":{\"programs\":{\"items\":["
							+ "{\"name\":\"Page one A\",\"slug\":\"page-one-a\",\"categories\":[]},"
							+ "{\"name\":\"Page one B\",\"slug\":\"page-one-b\",\"categories\":[]}"
							+ "]}}}";
				} else if (offset == 2 && limit == 2) {
					json = "{\"data\":{\"programs\":{\"items\":["
							+ "{\"name\":\"Page two A\",\"slug\":\"page-two-a\",\"categories\":[]}"
							+ "]}}}";
				} else {
					json = "{\"data\":{\"programs\":{\"items\":[]}}}";
				}
				return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private static final class CapProbeTf1PlusPluginManager extends Tf1PlusPluginManager {
		@Override
		public InputStream getInputStreamFromUrl(final String url) {
			try {
				final int offset = extractOffset(url);
				final int limit = extractLimit(url);
				if (offset == 0 && limit == Tf1PlusConf.CATALOGUE_PAGE_SIZE) {
					final StringBuilder items = new StringBuilder();
					for (int index = 0; index < Tf1PlusConf.CATALOGUE_PAGE_SIZE; index++) {
						if (index > 0) {
							items.append(',');
						}
						items.append("{\"name\":\"Cap ").append(index).append("\",\"slug\":\"cap-")
								.append(index).append("\",\"categories\":[]}");
					}
					final String json = "{\"data\":{\"programs\":{\"items\":[" + items + "]}}}";
					return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
				}
				return new ByteArrayInputStream("{\"data\":{\"programs\":{\"items\":[]}}}".getBytes(StandardCharsets.UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private static final class FailingTf1PlusPluginManager extends Tf1PlusPluginManager {
		@Override
		public InputStream getInputStreamFromUrl(final String url) {
			throw new IllegalStateException("offline fixture failure");
		}
	}

	private static int extractOffset(final String url) throws UnsupportedEncodingException {
		return extractIntField(url, "offset");
	}

	private static int extractLimit(final String url) throws UnsupportedEncodingException {
		return extractIntField(url, "limit");
	}

	private static int extractIntField(final String url, final String fieldName) throws UnsupportedEncodingException {
		final int variablesIndex = url.indexOf("variables=");
		if (variablesIndex < 0) {
			return 0;
		}
		final String encoded = url.substring(variablesIndex + "variables=".length());
		final String decoded = URLDecoder.decode(encoded, "UTF-8");
		final String marker = "\"" + fieldName + "\":";
		final int start = decoded.indexOf(marker);
		if (start < 0) {
			return 0;
		}
		int valueStart = start + marker.length();
		while (valueStart < decoded.length() && decoded.charAt(valueStart) == ' ') {
			valueStart++;
		}
		int valueEnd = valueStart;
		while (valueEnd < decoded.length() && Character.isDigit(decoded.charAt(valueEnd))) {
			valueEnd++;
		}
		if (valueEnd == valueStart) {
			return 0;
		}
		return Integer.parseInt(decoded.substring(valueStart, valueEnd));
	}

}
