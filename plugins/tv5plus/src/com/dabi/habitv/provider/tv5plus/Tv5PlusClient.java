package com.dabi.habitv.provider.tv5plus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * TV5+ GraphQL client (api.tv5unis.ca). Offline tests inject JSON via
 * {@link GraphqlTransport}.
 */
final class Tv5PlusClient {

	interface GraphqlTransport {
		String post(String body) throws IOException;
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final GraphqlTransport transport;

	Tv5PlusClient(final GraphqlTransport transport) {
		this.transport = transport;
	}

	static String postGraphql(final String body, final Proxy proxy) throws IOException {
		final HttpURLConnection connection;
		if (proxy != null) {
			connection = (HttpURLConnection) new URL(Tv5PlusConf.GRAPHQL_URL).openConnection(proxy);
		} else {
			connection = (HttpURLConnection) new URL(Tv5PlusConf.GRAPHQL_URL).openConnection();
		}
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(60000);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "HabiTV-tv5plus/1.0");
		connection.setRequestProperty("apollographql-client-name", "tv5plus-web");
		final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
		try (OutputStream output = connection.getOutputStream()) {
			output.write(bytes);
		}
		final int status = connection.getResponseCode();
		try (InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
			if (stream == null) {
				throw new IOException("graphql-http-" + status);
			}
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = stream.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			if (status >= 400) {
				throw new IOException("graphql-http-" + status);
			}
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	List<ShowRef> loadShows() throws IOException {
		final JsonNode sets = execute("{ featuredProductSets { id slug title } }").path("featuredProductSets");
		final Map<String, ShowRef> bySlug = new LinkedHashMap<String, ShowRef>();
		int setCount = 0;
		if (sets.isArray()) {
			for (final JsonNode set : sets) {
				if (setCount >= Tv5PlusConf.MAX_PRODUCT_SETS) {
					break;
				}
				setCount++;
				final String setSlug = text(set, "slug");
				if (StringUtils.isEmpty(setSlug)) {
					continue;
				}
				final JsonNode items = execute(
						"{ productSetBySlug(slug: \"" + escape(setSlug) + "\") { items { product { id title slug "
								+ "availabilityStatus mediaType productType } } } }")
						.path("productSetBySlug").path("items");
				if (!items.isArray()) {
					continue;
				}
				for (final JsonNode item : items) {
					if (bySlug.size() >= Tv5PlusConf.MAX_SHOWS) {
						return new ArrayList<ShowRef>(bySlug.values());
					}
					final JsonNode product = item.path("product");
					if (!isAvailableVideo(product)) {
						continue;
					}
					final String type = text(product, "productType");
					if (!Tv5PlusConf.TYPE_COLLECTION.equals(type) && !Tv5PlusConf.TYPE_MOVIE.equals(type)) {
						continue;
					}
					final String slug = text(product, "slug");
					final String title = text(product, "title");
					if (StringUtils.isEmpty(slug) || StringUtils.isEmpty(title) || bySlug.containsKey(slug)) {
						continue;
					}
					bySlug.put(slug, new ShowRef(title, slug, type));
				}
			}
		}
		return new ArrayList<ShowRef>(bySlug.values());
	}

	List<EpisodeRef> loadShowEpisodes(final String slug) throws IOException {
		final Map<String, EpisodeRef> byUrl = new LinkedHashMap<String, EpisodeRef>();
		final JsonNode root = execute("{ productByRootProductSlug(rootProductSlug: \"" + escape(slug)
				+ "\") { id title slug productType availabilityStatus mediaType "
				+ "videoCanonicalUrl seasons { seasonNumber availabilityStatus } } }")
				.path("productByRootProductSlug");
		if (root.isMissingNode() || root.isNull() || !isAvailableVideo(root)) {
			return new ArrayList<EpisodeRef>();
		}
		final String productType = text(root, "productType");
		if (Tv5PlusConf.TYPE_MOVIE.equals(productType)) {
			final String watchUrl = resolveWatchUrl(text(root, "videoCanonicalUrl"), slug, productType, null, null);
			if (watchUrl != null) {
				byUrl.put(watchUrl, new EpisodeRef(text(root, "title"), watchUrl, null, null));
			}
			return new ArrayList<EpisodeRef>(byUrl.values());
		}

		addPlayableProducts(byUrl, slug, null);
		final JsonNode seasons = root.path("seasons");
		int seasonCount = 0;
		if (seasons.isArray()) {
			for (final JsonNode season : seasons) {
				if (seasonCount >= Tv5PlusConf.MAX_SEASONS_PER_SHOW) {
					break;
				}
				if (!Tv5PlusConf.AVAILABILITY_AVAILABLE.equals(text(season, "availabilityStatus"))) {
					continue;
				}
				final Integer seasonNumber = asInt(season.get("seasonNumber"));
				if (seasonNumber == null) {
					continue;
				}
				seasonCount++;
				addPlayableProducts(byUrl, slug, seasonNumber);
			}
		}
		return new ArrayList<EpisodeRef>(byUrl.values());
	}

	private void addPlayableProducts(final Map<String, EpisodeRef> byUrl, final String slug,
			final Integer seasonNumber) throws IOException {
		final StringBuilder query = new StringBuilder();
		query.append("{ productPage(rootProductSlug: \"").append(escape(slug)).append('"');
		if (seasonNumber != null) {
			query.append(", seasonNumber: ").append(seasonNumber.intValue());
		}
		query.append(") { ... on ArtisanPage { blocks { ... on ArtisanBlocksProductPlayableProductsStrip { "
				+ "blockConfiguration { products { id title episodeNumber seasonNumber availabilityStatus "
				+ "mediaType productType videoCanonicalUrl } } } } } } }");
		final JsonNode blocks = execute(query.toString()).path("productPage").path("blocks");
		if (!blocks.isArray()) {
			return;
		}
		for (final JsonNode block : blocks) {
			final JsonNode products = block.path("blockConfiguration").path("products");
			if (!products.isArray()) {
				continue;
			}
			for (final JsonNode product : products) {
				if (!isAvailableVideo(product)) {
					continue;
				}
				final String type = text(product, "productType");
				if (type == null || (!Tv5PlusConf.TYPE_EPISODE.equals(type) && !Tv5PlusConf.TYPE_MOVIE.equals(type))) {
					continue;
				}
				final Integer season = asInt(product.get("seasonNumber"));
				final Integer episode = asInt(product.get("episodeNumber"));
				final String title = firstNonEmpty(text(product, "title"), buildFallbackTitle(season, episode));
				final String watchUrl = resolveWatchUrl(text(product, "videoCanonicalUrl"), slug, type, season,
						episode);
				if (watchUrl != null && !byUrl.containsKey(watchUrl)) {
					byUrl.put(watchUrl, new EpisodeRef(title, watchUrl, season, episode));
				}
			}
		}
	}

	/**
	 * Prefer a sanitized canonical URL. Otherwise synthesize an episode URL only
	 * when season+episode numbers exist, or a movie URL only for MOVIE products.
	 * Never fall an EPISODE back to the series/movie root URL — including when the
	 * API supplies a root path as videoCanonicalUrl.
	 */
	private static String resolveWatchUrl(final String canonicalUrl, final String slug, final String productType,
			final Integer season, final Integer episode) {
		final String sanitizedCanonical = Tv5PlusUrls.sanitizeEpisodeUrl(canonicalUrl);
		if (sanitizedCanonical != null) {
			if (!Tv5PlusConf.TYPE_EPISODE.equals(productType)
					|| Tv5PlusUrls.isEpisodeWatchUrl(sanitizedCanonical)) {
				return sanitizedCanonical;
			}
			// EPISODE with a movie/series-root canonical: ignore and try synthesis below.
		}
		if (season != null && episode != null) {
			return Tv5PlusUrls.episodeWatchUrl(slug, season.intValue(), episode.intValue());
		}
		if (Tv5PlusConf.TYPE_MOVIE.equals(productType)) {
			return Tv5PlusUrls.movieWatchUrl(slug);
		}
		return null;
	}

	private JsonNode execute(final String query) throws IOException {
		final ObjectNode body = MAPPER.createObjectNode();
		body.put("query", query);
		final String response = transport.post(MAPPER.writeValueAsString(body));
		final JsonNode root = MAPPER.readTree(response);
		if (root.has("errors") && root.path("errors").isArray() && root.path("errors").size() > 0) {
			throw new IOException("graphql-error:" + root.path("errors").get(0).path("message").asText("unknown"));
		}
		return root.path("data");
	}

	private static boolean isAvailableVideo(final JsonNode product) {
		return Tv5PlusConf.AVAILABILITY_AVAILABLE.equals(text(product, "availabilityStatus"))
				&& Tv5PlusConf.MEDIA_VIDEO.equals(text(product, "mediaType"));
	}

	private static String escape(final String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"")
				.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}

	private static String text(final JsonNode node, final String field) {
		final JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		final String text = value.asText();
		return StringUtils.isEmpty(text) ? null : text.trim();
	}

	private static Integer asInt(final JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return null;
		}
		if (node.isNumber()) {
			return Integer.valueOf(node.asInt());
		}
		try {
			return Integer.valueOf(Integer.parseInt(node.asText().trim()));
		} catch (final Exception e) {
			return null;
		}
	}

	private static String firstNonEmpty(final String... values) {
		for (final String value : values) {
			if (StringUtils.isNotEmpty(value)) {
				return value;
			}
		}
		return "";
	}

	private static String buildFallbackTitle(final Integer season, final Integer episode) {
		if (season != null && episode != null) {
			return "S" + season + "E" + episode;
		}
		return "Episode";
	}

	static final class ShowRef {
		final String title;
		final String slug;
		final String productType;

		ShowRef(final String title, final String slug, final String productType) {
			this.title = title;
			this.slug = slug;
			this.productType = productType;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String watchUrl;
		final Integer seasonNumber;
		final Integer episodeNumber;

		EpisodeRef(final String title, final String watchUrl, final Integer seasonNumber,
				final Integer episodeNumber) {
			this.title = title;
			this.watchUrl = watchUrl;
			this.seasonNumber = seasonNumber;
			this.episodeNumber = episodeNumber;
		}
	}

}
