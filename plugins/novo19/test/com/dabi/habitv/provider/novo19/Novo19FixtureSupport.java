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
					"/api/1/public/frontends/web/pages/categories/sections/rail-catalogue/tiles"),
					readFixture("bff-tiles-catalogue.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-series/tiles"),
					readFixture("bff-tiles-series.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/categories/sections/rail-films/tiles"),
					readFixture("bff-tiles-films.json"));
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
			responses.put(Novo19UrlBuilder.bffPageByPath("details/on-a-de-l-info"),
					readFixture("bff-page-info-collection.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/details/on-a-de-l-info/sections/info-episodes/tiles"),
					readFixture("bff-tiles-info-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,le-royaume-des-contes/sections/episodes/tiles"),
					readFixture("bff-tiles-podcast-episodes.json"));
			responses.put(Novo19UrlBuilder.bffAbsolutePath(
					"/api/1/public/frontends/web/pages/BFF%7Casset-details,inferno/sections/reco/tiles"),
					readFixture("bff-tiles-inferno-reco.json"));
			responses.put(Novo19UrlBuilder.bffPageByPath("voir-plus/rail/details/on-a-de-l-info/info-episodes"),
					readFixture("bff-pagination-more.json"));
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
