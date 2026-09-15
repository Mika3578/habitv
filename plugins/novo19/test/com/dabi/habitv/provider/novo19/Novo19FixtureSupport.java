package com.dabi.habitv.provider.novo19;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Novo19FixtureSupport {

	static final String CATALOGUE_TILES = "bff-section-catalogue-tiles.json";

	static final String DOCUMENTARY = "bff-section-documentary.json";

	static final String EPISODE_RAILS = "bff-section-episode-rails.json";

	static final String DETAIL_PAGES = "bff-section-detail-pages.json";

	static final String PAGINATION_TILES = "bff-section-pagination-tiles.json";

	static final String REDBEE = "redbee-scenarios.json";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final Map<String, Map<String, String>> SECTION_CACHE = new HashMap<String, Map<String, String>>();

	private Novo19FixtureSupport() {
	}

	static String readFixture(final String name) throws IOException {
		final String path = "test/resources/fixtures/novo19/" + name;
		final File file = new File(path);
		if (!file.exists()) {
			throw new IOException("missing fixture: " + path);
		}
		try (InputStream input = new FileInputStream(file)) {
			return readUtf8(input);
		}
	}

	static String readSection(final String file, final String sectionKey) throws IOException {
		final Map<String, String> sections = loadSectionCache(file);
		final String payload = sections.get(sectionKey);
		if (payload == null) {
			throw new IOException("missing section " + sectionKey + " in " + file);
		}
		return payload;
	}

	static String detailPage(final String sectionKey) throws IOException {
		return readSection(DETAIL_PAGES, sectionKey);
	}

	static String catalogueTiles(final String sectionKey) throws IOException {
		return readSection(CATALOGUE_TILES, sectionKey);
	}

	static String episodeRails(final String sectionKey) throws IOException {
		return readSection(EPISODE_RAILS, sectionKey);
	}

	static String paginationTiles(final String sectionKey) throws IOException {
		return readSection(PAGINATION_TILES, sectionKey);
	}

	static String redbeeScenario(final String sectionKey) throws IOException {
		return readSection(REDBEE, sectionKey);
	}

	static Novo19CatalogClient clientWithFixtures() {
		final Map<String, String> responses = new HashMap<String, String>();
		try {
			put(responses, Novo19UrlBuilder.bffConfigUrl(), readFixture("bff-config.json"));
			put(responses, Novo19UrlBuilder.bffPageByPath("categories"),
					readSection(DETAIL_PAGES, "catalogueRoot"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/f6a789ee-7e88-49c4-89ff-d4ef6ab044ce/tiles"),
					readSection(CATALOGUE_TILES, "carousel"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-series/tiles"),
					readSection(CATALOGUE_TILES, "series"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-films/tiles"),
					readSection(CATALOGUE_TILES, "films"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-info-banner/tiles"),
					readSection(CATALOGUE_TILES, "infoBanner"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-documentaries/tiles"),
					readSection(CATALOGUE_TILES, "documentaries"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-talk-banner/tiles"),
					readSection(CATALOGUE_TILES, "talkBanner"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-podcasts/tiles"),
					readSection(CATALOGUE_TILES, "podcasts"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/series-alpha"),
					readSection(DETAIL_PAGES, "seriesAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/film-alpha"),
					readSection(DETAIL_PAGES, "filmAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/film-beta"),
					readSection(DETAIL_PAGES, "filmBeta"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/podcast-alpha"),
					readSection(DETAIL_PAGES, "podcastAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/collection-alpha"),
					readSection(DETAIL_PAGES, "collectionAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/collection-alpha-extended"),
					readSection(DETAIL_PAGES, "collectionExtended"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/programme-beta"),
					readSection(DETAIL_PAGES, "programmeBeta"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/talk-programme-alpha"),
					readSection(DETAIL_PAGES, "talkProgrammeAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/standalone-documentary"),
					readSection(DETAIL_PAGES, "standaloneDocumentary"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/film-gamma"),
					readSection(DETAIL_PAGES, "filmGamma"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/programme-alpha"),
					readSection(DETAIL_PAGES, "programmeAlpha"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/series-beta"),
					readSection(DETAIL_PAGES, "seriesBeta"));
			put(responses, Novo19UrlBuilder.bffPageByPath("details/theme-alpha-programme"),
					readSection(DETAIL_PAGES, "themeAlphaProgramme"));
			put(responses, Novo19UrlBuilder.bffPageByPath("documentaires-et-magazines"),
					readSection(DOCUMENTARY, "sectionPage"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-catalog-master/tiles"),
					readSection(DOCUMENTARY, "catalogMaster"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-histoire/tiles"),
					readSection(DOCUMENTARY, "histoire"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-immersion/tiles"),
					readSection(DOCUMENTARY, "immersion"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-societe/tiles"),
					readSection(DOCUMENTARY, "societe"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-patrimoine/tiles"),
					readSection(DOCUMENTARY, "patrimoine"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/programme-alpha/sections/programme-alpha-episodes/tiles"),
					readSection(EPISODE_RAILS, "programmeAlpha"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-serie,series-beta/sections/series-beta-season-3/tiles"),
					readSection(EPISODE_RAILS, "seriesBetaSeason3"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-serie,series-beta/sections/series-beta-season-2/tiles"),
					readSection(EPISODE_RAILS, "seriesBetaSeason2"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/collection-alpha/sections/collection-alpha-episodes/tiles"),
					readSection(EPISODE_RAILS, "collectionAlpha"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/programme-beta/sections/programme-beta-episodes/tiles"),
					readSection(EPISODE_RAILS, "programmeBeta"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/talk-programme-alpha/sections/talk-programme-alpha-episodes/tiles"),
					readSection(EPISODE_RAILS, "talkAlpha"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,podcast-alpha/sections/episodes/tiles"),
					readSection(EPISODE_RAILS, "podcastAlpha"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/collection-alpha-extended/sections/episodes/tiles"),
					readSection(EPISODE_RAILS, "collectionExtended"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/details/collection-alpha/collection-alpha-episodes"),
					readSection(PAGINATION_TILES, "collectionPage2"));
			put(responses, Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/details/talk-programme-alpha/talk-programme-alpha-episodes"),
					readSection(PAGINATION_TILES, "talkPage2"));
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		return new Novo19CatalogClient(new MapContentLoader(responses));
	}

	private static void put(final Map<String, String> responses, final String url, final String body) {
		responses.put(url, body);
	}

	private static Map<String, String> loadSectionCache(final String file) throws IOException {
		Map<String, String> cached = SECTION_CACHE.get(file);
		if (cached != null) {
			return cached;
		}
		final JsonNode root = MAPPER.readTree(readFixture(file));
		final JsonNode sections = root.get("sections");
		if (sections == null || !sections.isObject()) {
			throw new IOException("fixture has no sections object: " + file);
		}
		cached = new HashMap<String, String>();
		final java.util.Iterator<Map.Entry<String, JsonNode>> fields = sections.fields();
		while (fields.hasNext()) {
			final Map.Entry<String, JsonNode> entry = fields.next();
			cached.put(entry.getKey(), MAPPER.writeValueAsString(entry.getValue()));
		}
		SECTION_CACHE.put(file, cached);
		return cached;
	}

	private static String readUtf8(final InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}

	private static final class MapContentLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private MapContentLoader(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String load(final String url) throws IOException {
			final String body = responses.get(url);
			if (body == null) {
				throw new IOException("no fixture for " + url);
			}
			return body;
		}

	}

}
