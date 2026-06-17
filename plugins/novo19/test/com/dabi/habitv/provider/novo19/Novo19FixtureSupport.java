package com.dabi.habitv.provider.novo19;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

final class Novo19FixtureSupport {

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

	static Novo19CatalogClient clientWithFixtures() {
		final Map<String, String> responses = new HashMap<>();
		try {
			responses.put(Novo19UrlBuilder.bffConfigUrl(), readFixture("bff-config.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("categories"), readFixture("bff-page-categories.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/f6a789ee-7e88-49c4-89ff-d4ef6ab044ce/tiles"),
					readFixture("bff-tiles-catalogue-carousel.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-series/tiles"),
					readFixture("bff-tiles-series.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-films/tiles"),
					readFixture("bff-tiles-films.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-info-banner/tiles"),
					readFixture("bff-tiles-info-banner.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-documentaries/tiles"),
					readFixture("bff-tiles-documentaries.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-talk-banner/tiles"),
					readFixture("bff-tiles-talk-banner.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-podcasts/tiles"),
					readFixture("bff-tiles-podcasts.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/fbi-portes-disparus"),
					readFixture("bff-page-series-details.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/un-plan-d-enfer"),
					readFixture("bff-page-film-details.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/inferno"),
					readFixture("bff-page-inferno-film.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/le-royaume-des-contes"),
					readFixture("bff-page-podcast-royaume.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/vos-objets-valent-de-l-or"),
					readFixture("bff-page-vos-objets.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/on-a-de-l-info"),
					readFixture("bff-page-info-collection.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/on-a-de-l-info-le-mag"),
					readFixture("bff-page-info-mag.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/on-a-du-nouveau"),
					readFixture("bff-page-on-a-du-nouveau.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/standalone-documentary"),
					readFixture("bff-page-standalone-documentary.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/elysee-les-secrets-d-un-palais"),
					readFixture("bff-page-elysee.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/cuisinons-l-histoire"),
					readFixture("bff-page-cuisinons-l-histoire.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/bucheron-un-metier-a-hauts-risques"),
					readFixture("bff-page-bucheron.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("details/aux-commandes-des-geants-des-mers"),
					readFixture("bff-page-immersion-multi.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("documentaires-et-magazines"),
					readFixture("bff-page-documentaires-section.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-catalog-master/tiles"),
					readFixture("bff-tiles-doc-catalog-master.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-histoire/tiles"),
					readFixture("bff-tiles-histoire-rail.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-immersion/tiles"),
					readFixture("bff-tiles-immersion-rail.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-societe/tiles"),
					readFixture("bff-tiles-societe-rail.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/documentaires-et-magazines/sections/rail-patrimoine/tiles"),
					readFixture("bff-tiles-patrimoine-rail.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/cuisinons-l-histoire/sections/cuisinons-episodes/tiles"),
					readFixture("bff-tiles-cuisinons-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-serie,bucheron-un-metier-a-hauts-risques/sections/bucheron-saison-3/tiles"),
					readFixture("bff-tiles-bucheron-saison-3.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-serie,bucheron-un-metier-a-hauts-risques/sections/bucheron-saison-2/tiles"),
					readFixture("bff-tiles-bucheron-saison-2.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/on-a-de-l-info/sections/info-episodes/tiles"),
					readFixture("bff-tiles-info-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/on-a-de-l-info-le-mag/sections/info-mag-episodes/tiles"),
					readFixture("bff-tiles-info-mag-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/on-a-du-nouveau/sections/talk-episodes/tiles"),
					readFixture("bff-tiles-talk-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,le-royaume-des-contes/sections/episodes/tiles"),
					readFixture("bff-tiles-podcast-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/vos-objets-valent-de-l-or/sections/episodes/tiles"),
					readFixture("bff-tiles-vos-objets-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/details/on-a-de-l-info/info-episodes"),
					readFixture("bff-pagination-more.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/details/on-a-du-nouveau/talk-episodes"),
					readFixture("bff-pagination-talk-more.json"));
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		return new Novo19CatalogClient(new MapContentLoader(responses));
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
