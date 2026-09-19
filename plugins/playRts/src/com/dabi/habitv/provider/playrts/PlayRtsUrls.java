package com.dabi.habitv.provider.playrts;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Category id helpers, play page URL builders, and field extraction for Play RTS.
 */
final class PlayRtsUrls {

	private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

	private PlayRtsUrls() {
	}

	static String showCategoryId(final String showId) {
		return PlayRtsConf.CATEGORY_SHOW_PREFIX + showId;
	}

	static String showIdFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(PlayRtsConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String showId = categoryId.substring(PlayRtsConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(showId) ? null : showId;
	}

	static boolean isShowCategory(final String categoryId) {
		return showIdFromCategoryId(categoryId) != null;
	}

	/**
	 * Accepts http(s) Play RTS video page URLs on {@code rts.ch} with a video URN or id.
	 */
	static boolean isPlayRtsVideoPageUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
				return false;
			}
			final String host = uri.getHost();
			if (host == null) {
				return false;
			}
			final String lowerHost = host.toLowerCase(Locale.ROOT);
			if (!"rts.ch".equals(lowerHost) && !"www.rts.ch".equals(lowerHost)) {
				return false;
			}
			final String path = uri.getPath();
			if (path == null || !path.toLowerCase(Locale.ROOT).contains("/play/tv/")) {
				return false;
			}
			if (!path.toLowerCase(Locale.ROOT).contains("/video/")) {
				return false;
			}
			final String query = uri.getQuery();
			if (StringUtils.isEmpty(query)) {
				return false;
			}
			final String lowerQuery = query.toLowerCase(Locale.ROOT);
			return lowerQuery.contains("urn=urn:rts:video:") || lowerQuery.contains("id=");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String showTitle(final Map<String, Object> show) {
		if (show == null) {
			return "";
		}
		return firstNonEmpty(asString(show.get("title")), asString(show.get("id")));
	}

	static String showId(final Map<String, Object> show) {
		if (show == null) {
			return null;
		}
		return asString(show.get("id"));
	}

	static String videoTitle(final Map<String, Object> video) {
		if (video == null) {
			return "";
		}
		return firstNonEmpty(asString(video.get("title")), asString(video.get("id")));
	}

	static String videoUrn(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		final String urn = asString(video.get("urn"));
		if (StringUtils.isNotEmpty(urn) && urn.startsWith("urn:rts:video:")) {
			return urn;
		}
		final String id = asString(video.get("id"));
		if (StringUtils.isNotEmpty(id)) {
			return "urn:rts:video:" + id;
		}
		return null;
	}

	static String videoId(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		final String id = asString(video.get("id"));
		if (StringUtils.isNotEmpty(id)) {
			return id;
		}
		final String urn = videoUrn(video);
		if (urn == null) {
			return null;
		}
		final int sep = urn.lastIndexOf(':');
		return sep >= 0 ? urn.substring(sep + 1) : null;
	}

	static String videoDescription(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		return asString(video.get("description"));
	}

	static Date videoPublicationDate(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		return parseIso8601(asString(video.get("date")));
	}

	/**
	 * Builds a public Play RTS URL accepted by yt-dlp's SRGSSRPlay extractor.
	 */
	static String videoPageUrl(final Map<String, Object> video) {
		final String urn = videoUrn(video);
		if (StringUtils.isEmpty(urn)) {
			return null;
		}
		final String slug = slugify(videoTitle(video));
		final String pathSlug = StringUtils.isEmpty(slug) ? "episode" : slug;
		return "https://www.rts.ch/play/tv/-/video/" + pathSlug + "?urn=" + urn;
	}

	static String slugify(final String title) {
		if (StringUtils.isEmpty(title)) {
			return "";
		}
		String normalized = title.toLowerCase(Locale.ROOT);
		normalized = normalized.replace('à', 'a').replace('â', 'a').replace('ä', 'a')
				.replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('ë', 'e')
				.replace('î', 'i').replace('ï', 'i')
				.replace('ô', 'o').replace('ö', 'o')
				.replace('ù', 'u').replace('û', 'u').replace('ü', 'u')
				.replace('ç', 'c');
		normalized = NON_SLUG.matcher(normalized).replaceAll("-");
		while (normalized.startsWith("-")) {
			normalized = normalized.substring(1);
		}
		while (normalized.endsWith("-")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.length() > 80) {
			normalized = normalized.substring(0, 80);
			while (normalized.endsWith("-")) {
				normalized = normalized.substring(0, normalized.length() - 1);
			}
		}
		return normalized;
	}

	static Date parseIso8601(final String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		final String[] patterns = new String[] {
				"yyyy-MM-dd'T'HH:mm:ssX",
				"yyyy-MM-dd'T'HH:mm:ss.SSSX",
				"yyyy-MM-dd'T'HH:mm:ss'Z'",
				"yyyy-MM-dd",
		};
		for (final String pattern : patterns) {
			try {
				final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
				return format.parse(value);
			} catch (final ParseException ignored) {
				// try next pattern
			}
		}
		return null;
	}

	private static String asString(final Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private static String firstNonEmpty(final String first, final String second) {
		if (StringUtils.isNotEmpty(first)) {
			return first;
		}
		if (StringUtils.isNotEmpty(second)) {
			return second;
		}
		return "";
	}
}
