package com.dabi.habitv.provider.francetv;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class FranceTvUrls {

	private static final Pattern VIDEO_REPLAY_PATH = Pattern.compile("/\\d+-[^/]+\\.html$");

	private static final String SECTION_FRAGMENT_PREFIX = "#section-";

	private FranceTvUrls() {
	}

	static String programPageUrl(final String programPath) {
		if (StringUtils.isEmpty(programPath)) {
			return null;
		}
		final int separator = programPath.indexOf('_');
		if (separator <= 0 || separator >= programPath.length() - 1) {
			return null;
		}
		final String channel = programPath.substring(0, separator);
		final String program = programPath.substring(separator + 1);
		return FranceTvConf.HOME_URL + "/" + channel + "/" + program + "/";
	}

	static String programPathFromCategoryUrl(final String categoryUrl) {
		if (StringUtils.isEmpty(categoryUrl) || !categoryUrl.startsWith(FranceTvConf.HOME_URL)) {
			return null;
		}
		final String path = pathAfterHome(stripSectionFragment(categoryUrl));
		if (path == null) {
			return null;
		}
		final String[] segments = nonEmptySegments(path);
		if (segments.length < 2) {
			return null;
		}
		if (segments.length == 2 && isKnownChannelSlug(segments[0])) {
			return segments[0] + "_" + segments[1];
		}
		if (FranceTvConf.isCuratedPublicRootHub(segments[0]) || isPublicHubSlug(segments[0])) {
			return taxonomySlugFromBrowseSegments(segments);
		}
		return null;
	}

	static String programPageUrlFromTaxonomySlug(final String taxonomySlug) {
		if (StringUtils.isEmpty(taxonomySlug)) {
			return null;
		}
		final String browsePath = browsePathFromTaxonomySlug(taxonomySlug);
		if (StringUtils.isEmpty(browsePath)) {
			return null;
		}
		return FranceTvConf.HOME_URL + "/" + browsePath + "/";
	}

	static String browsePathFromTaxonomySlug(final String taxonomySlug) {
		if (StringUtils.isEmpty(taxonomySlug) || taxonomySlug.indexOf('_') < 0) {
			return null;
		}
		for (final String channelSlug : FranceTvConf.CHANNEL_SLUGS) {
			final String prefix = channelSlug + "_";
			if (taxonomySlug.startsWith(prefix)) {
				return channelSlug + "/" + taxonomySlug.substring(prefix.length());
			}
		}
		return taxonomySlug.replace('_', '/');
	}

	private static String taxonomySlugFromBrowseSegments(final String[] segments) {
		if (segments == null || segments.length == 0) {
			return null;
		}
		final StringBuilder slug = new StringBuilder(segments[0]);
		for (int i = 1; i < segments.length; i++) {
			slug.append('_').append(segments[i]);
		}
		return slug.toString();
	}

	static String episodePageUrl(final Map<String, Object> item) {
		return episodePageUrl(item, programPathFromItem(item));
	}

	static String episodePageUrl(final Map<String, Object> item, final String taxonomyPath) {
		if (StringUtils.isEmpty(taxonomyPath)) {
			return null;
		}
		final String browsePath = browsePathFromTaxonomySlug(taxonomyPath);
		if (StringUtils.isEmpty(browsePath)) {
			return null;
		}
		final Object rawId = item.get("id");
		if (rawId == null) {
			return null;
		}
		final String videoId = String.valueOf(rawId);
		final String titleSlug = slugify(episodeTitle(item));

		final StringBuilder url = new StringBuilder(FranceTvConf.HOME_URL).append('/').append(browsePath).append('/');

		final Object rawSeason = item.get("season");
		if (rawSeason instanceof Number && ((Number) rawSeason).intValue() > 0) {
			final String programSegment = lastBrowseSegment(browsePath);
			if (StringUtils.isNotEmpty(programSegment)) {
				url.append(programSegment).append("-saison-").append(((Number) rawSeason).intValue()).append('/');
			}
		}

		url.append(videoId).append('-').append(titleSlug).append(".html");
		return url.toString();
	}

	private static String lastBrowseSegment(final String browsePath) {
		final int slash = browsePath.lastIndexOf('/');
		if (slash < 0) {
			return browsePath;
		}
		return browsePath.substring(slash + 1);
	}

	static boolean isReplayVideoType(final String type) {
		return "integrale".equals(type) || "unitaire".equals(type);
	}

	static String sectionPageUrl(final String channelSlug, final String urlComplete) {
		if (StringUtils.isEmpty(channelSlug)) {
			return null;
		}
		if (StringUtils.isEmpty(urlComplete)) {
			return FranceTvConf.HOME_URL + "/" + channelSlug + "/";
		}
		return FranceTvConf.HOME_URL + "/" + channelSlug + "/" + urlComplete + "/";
	}

	static boolean isPublicCollectionPageUrl(final String url) {
		if (StringUtils.isEmpty(url) || !url.startsWith(FranceTvConf.HOME_URL)) {
			return false;
		}
		final String normalized = stripSectionFragment(url);
		if (isVideoReplayUrl(normalized)) {
			return false;
		}
		final String path = pathAfterHome(normalized);
		if (path == null) {
			return false;
		}
		if (path.endsWith(".html")) {
			return false;
		}
		if (isApiProgramPageUrl(normalized)) {
			return false;
		}
		final String[] segments = nonEmptySegments(path);
		final int depth = segments.length;
		if (depth >= 3) {
			return true;
		}
		if (depth == 2 && !isKnownChannelSlug(segments[0])) {
			return true;
		}
		return depth == 1 && isPublicHubSlug(segments[0]);
	}

	static boolean isPublicHubSlug(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return false;
		}
		for (final String hubSlug : FranceTvConf.PUBLIC_HUB_SLUGS) {
			if (hubSlug.equals(slug)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * True for a curated public hub landing URL such as {@code https://www.france.tv/ina/}.
	 * These are navigation containers, not replay episode lists.
	 */
	static boolean isPublicHubContainerUrl(final String url) {
		if (StringUtils.isEmpty(url) || !url.startsWith(FranceTvConf.HOME_URL)) {
			return false;
		}
		final String path = pathAfterHome(stripSectionFragment(url));
		if (path == null) {
			return false;
		}
		final String[] segments = nonEmptySegments(path);
		return segments.length == 1 && FranceTvConf.isCuratedPublicRootHub(segments[0]);
	}

	private static boolean isApiProgramPageUrl(final String url) {
		final String path = pathAfterHome(url);
		if (path == null) {
			return false;
		}
		final String[] segments = nonEmptySegments(path);
		return segments.length == 2 && isKnownChannelSlug(segments[0]);
	}

	private static String pathAfterHome(final String url) {
		String path = url.substring(FranceTvConf.HOME_URL.length());
		if (!path.startsWith("/")) {
			return null;
		}
		final int fragment = path.indexOf('#');
		if (fragment >= 0) {
			path = path.substring(0, fragment);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	private static String[] nonEmptySegments(final String path) {
		final String[] raw = path.split("/");
		int count = 0;
		for (final String segment : raw) {
			if (StringUtils.isNotEmpty(segment)) {
				count++;
			}
		}
		final String[] segments = new String[count];
		int index = 0;
		for (final String segment : raw) {
			if (StringUtils.isNotEmpty(segment)) {
				segments[index++] = segment;
			}
		}
		return segments;
	}

	private static boolean isKnownChannelSlug(final String slug) {
		for (final String channelSlug : FranceTvConf.CHANNEL_SLUGS) {
			if (channelSlug.equals(slug)) {
				return true;
			}
		}
		return false;
	}

	static String sectionCategoryId(final String collectionUrl, final String sectionSlug) {
		final String base = stripSectionFragment(collectionUrl);
		if (StringUtils.isEmpty(base) || StringUtils.isEmpty(sectionSlug)) {
			return base;
		}
		return base + SECTION_FRAGMENT_PREFIX + sectionSlug;
	}

	static String sectionSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId)) {
			return null;
		}
		final int index = categoryId.indexOf(SECTION_FRAGMENT_PREFIX);
		if (index < 0) {
			return null;
		}
		return categoryId.substring(index + SECTION_FRAGMENT_PREFIX.length());
	}

	static String collectionUrlFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId)) {
			return null;
		}
		final int index = categoryId.indexOf(SECTION_FRAGMENT_PREFIX);
		if (index < 0) {
			return categoryId;
		}
		return categoryId.substring(0, index);
	}

	static String stripSectionFragment(final String categoryId) {
		final String collectionUrl = collectionUrlFromCategoryId(categoryId);
		if (collectionUrl == null) {
			return categoryId;
		}
		if (collectionUrl.endsWith("/")) {
			return collectionUrl;
		}
		return collectionUrl + "/";
	}

	static boolean isVideoReplayUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		try {
			final java.net.URI uri = new java.net.URI(url);
			if (!isFranceTvHost(url)) {
				return false;
			}
			final String path = uri.getPath();
			return path != null && VIDEO_REPLAY_PATH.matcher(path).find();
		} catch (Exception e) {
			return false;
		}
	}

	static boolean isFranceTvHost(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		return url.startsWith(FranceTvConf.HOME_URL + "/") || FranceTvConf.HOME_URL.equals(url);
	}

	static boolean isDirectStreamUrl(final String url) {
		return url != null && url.contains("/direct.html");
	}

	static String absoluteFranceTvUrl(final String href) {
		if (StringUtils.isEmpty(href)) {
			return null;
		}
		if (href.startsWith("http://") || href.startsWith("https://")) {
			return href;
		}
		if (href.startsWith("/")) {
			return FranceTvConf.HOME_URL + href;
		}
		return null;
	}

	static String slugifyPathSegment(final String text) {
		return slugify(text == null ? "" : text);
	}

	static String hubPageUrl(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return null;
		}
		return FranceTvConf.HOME_URL + "/" + slug + "/";
	}

	/**
	 * First path segment of a public browse URL (e.g. {@code sport} for Roland-Garros).
	 */
	static String parentHubSlugForPublicPage(final String pageUrl) {
		final String path = pathAfterHome(stripSectionFragment(pageUrl));
		if (path == null) {
			return null;
		}
		final String[] segments = nonEmptySegments(path);
		return segments.length == 0 ? null : segments[0];
	}

	static String collectionLabelFromUrl(final String collectionUrl) {
		final String path = collectionUrlFromCategoryId(collectionUrl);
		if (StringUtils.isEmpty(path)) {
			return "Collection";
		}
		String trimmed = path;
		if (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		final int slash = trimmed.lastIndexOf('/');
		final String segment = slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
		if (StringUtils.isEmpty(segment)) {
			return "Collection";
		}
		if (isPublicHubSlug(segment)) {
			return channelLabel(segment);
		}
		return segment.replace('-', ' ');
	}

	static String channelLabel(final String slug) {
		switch (slug) {
		case "france-2":
			return "France 2";
		case "france-3":
			return "France 3";
		case "france-4":
			return "France 4";
		case "france-5":
			return "France 5";
		case "la1ere":
			return "La 1ère";
		case "franceinfo":
			return "Franceinfo";
		case "arte":
			return "Arte";
		case "tv5-monde":
			return "TV5 Monde Plus";
		case "france-24":
			return "France 24";
		case "ina":
			return "INA";
		case "lcp":
			return "LCP";
		case "public-senat":
			return "Public Sénat";
		case "mieux":
			return "Mieux";
		case "sport":
			return "Sport";
		case "series-et-fictions":
			return "Séries & fictions";
		case "documentaires":
			return "Documentaires";
		case "films":
			return "Cinéma";
		case "societe":
			return "Société";
		case "info":
			return "Info";
		case "spectacles-et-culture":
			return "Spectacles et culture";
		case "jeux-et-divertissements":
			return "Jeux et divertissements";
		case "enfants":
			return "Enfants";
		case "podcasts":
			return "Podcasts";
		default:
			return slug;
		}
	}

	private static String programPathFromItem(final Map<String, Object> item) {
		final Object program = item.get("program");
		if (!(program instanceof Map)) {
			return null;
		}
		final Object programPath = ((Map<?, ?>) program).get("program_path");
		return programPath == null ? null : String.valueOf(programPath);
	}

	private static String episodeTitle(final Map<String, Object> item) {
		final Object title = item.get("title");
		if (title != null && StringUtils.isNotEmpty(String.valueOf(title))) {
			return String.valueOf(title);
		}
		final Object episodeTitle = item.get("episode_title");
		return episodeTitle == null ? "episode" : String.valueOf(episodeTitle);
	}

	private static String slugify(final String text) {
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		normalized = normalized.toLowerCase(Locale.ROOT);
		normalized = normalized.replaceAll("[^a-z0-9]+", "-");
		normalized = normalized.replaceAll("^-+|-+$", "");
		if (StringUtils.isEmpty(normalized)) {
			return "episode";
		}
		return normalized;
	}

}
