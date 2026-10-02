package com.dabi.habitv.provider.tf1plus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FixtureTf1PlusPluginManager extends Tf1PlusPluginManager {

	private static final String FIXTURE_ROOT = "test/resources/fixtures/tf1plus/";

	private java.nio.file.Path catalogueCachePath;

	@Override
	protected Tf1PlusCatalogueService createCatalogueService() {
		if (catalogueCachePath == null) {
			try {
				catalogueCachePath = java.nio.file.Files
						.createTempFile("tf1plus-catalogue-test-", ".json");
				catalogueCachePath.toFile().deleteOnExit();
			} catch (java.io.IOException e) {
				throw new IllegalStateException("unable to create tf1plus catalogue test cache", e);
			}
		}
		return new Tf1PlusCatalogueService(new Tf1PlusCatalogueClient(new Tf1PlusGraphqlClient(this)),
				new Tf1PlusCatalogueCache(catalogueCachePath), new Tf1PlusTreeBuilder(),
				new Tf1PlusHtmlCatalogueSupplement(new Tf1PlusHtmlCatalogueSupplement.UrlContentProvider() {
					@Override
					public String getUrlContent(final String url) {
						return FixtureTf1PlusPluginManager.this.getUrlContent(url);
					}
				}));
	}

	@Override
	protected boolean isPremiumDownloadEnabled() {
		return false;
	}

	@Override
	public InputStream getInputStreamFromUrl(final String url) {
		try {
			if (url != null && url.contains("/graphql/web")) {
				final String fixture = resolveGraphqlFixture(url);
				if (fixture != null) {
					return new ByteArrayInputStream(Files.readAllBytes(Paths.get(fixture)));
				}
				return new ByteArrayInputStream("{\"data\":{}}".getBytes(StandardCharsets.UTF_8));
			}
			return super.getInputStreamFromUrl(url);
		} catch (IOException e) {
			throw new IllegalStateException("unable to read tf1plus graphql fixture for " + url, e);
		}
	}

	@Override
	protected String getUrlContent(final String url) {
		try {
			if (url.contains("/replay")) {
				return new String(Files.readAllBytes(Paths.get(FIXTURE_ROOT + "tf1-channel-replay.html")),
						StandardCharsets.UTF_8);
			}
			if (url.contains("/tf1/demain-nous-appartient")) {
				return new String(Files.readAllBytes(Paths.get(FIXTURE_ROOT + "tf1-program-page.html")),
						StandardCharsets.UTF_8);
			}
			if (url.contains("/tf1/miraculous")) {
				return new String(Files.readAllBytes(Paths.get(FIXTURE_ROOT + "tf1-program-miraculous-page.html")),
						StandardCharsets.UTF_8);
			}
			return "";
		} catch (IOException e) {
			throw new IllegalStateException("unable to read tf1plus html fixture", e);
		}
	}

	private String resolveGraphqlFixture(final String url) throws UnsupportedEncodingException {
		if (url.contains("id=" + Tf1PlusConf.GRAPHQL_VIDEO_BY_SLUG)) {
			return FIXTURE_ROOT + "graphql-video-by-slug-premium-and-basic.json";
		}
		if (!url.contains("id=" + Tf1PlusConf.GRAPHQL_PROGRAMS_BY_CHANNEL)
				&& !url.contains("id=" + Tf1PlusConf.GRAPHQL_VIDEOS_BY_PROGRAM)) {
			return null;
		}
		if (url.contains("id=" + Tf1PlusConf.GRAPHQL_PROGRAMS_BY_CHANNEL)) {
			final String channel = extractJsonField(url, "channel");
			if ("tf1".equals(channel)) {
				return FIXTURE_ROOT + "graphql-programs-tf1.json";
			}
			if ("tmc".equals(channel)) {
				return FIXTURE_ROOT + "graphql-programs-tmc.json";
			}
			if ("arte".equals(channel)) {
				return FIXTURE_ROOT + "graphql-programs-arte.json";
			}
			return FIXTURE_ROOT + "graphql-programs-empty.json";
		}
		final String programSlug = extractJsonField(url, "programSlug");
		if ("demain-nous-appartient".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-demain.json";
		}
		if ("miraculous".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-miraculous.json";
		}
		if ("automoto".equals(programSlug) || "auto-moto".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-premium-only.json";
		}
		if ("fixture-legacy-premium-replay".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-legacy-premium-only.json";
		}
		if ("fixture-basic-max-replay".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-basic-max.json";
		}
		if ("fixture-premium-basic-replay".equals(programSlug)) {
			return FIXTURE_ROOT + "graphql-videos-premium-and-basic.json";
		}
		return FIXTURE_ROOT + "graphql-programs-empty.json";
	}

	private String extractJsonField(final String url, final String fieldName) throws UnsupportedEncodingException {
		final int variablesIndex = url.indexOf("variables=");
		if (variablesIndex < 0) {
			return "";
		}
		final String encoded = url.substring(variablesIndex + "variables=".length());
		final String decoded = URLDecoder.decode(encoded, "UTF-8");
		final String marker = "\"" + fieldName + "\":\"";
		final int start = decoded.indexOf(marker);
		if (start < 0) {
			return "";
		}
		final int valueStart = start + marker.length();
		final int valueEnd = decoded.indexOf('"', valueStart);
		if (valueEnd < 0) {
			return "";
		}
		return decoded.substring(valueStart, valueEnd);
	}
}
