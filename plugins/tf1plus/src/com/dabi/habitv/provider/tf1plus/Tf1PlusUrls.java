package com.dabi.habitv.provider.tf1plus;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

/**
 * Category id helpers and field extraction for TF1+ catalogue nodes.
 */
final class Tf1PlusUrls {

	private Tf1PlusUrls() {
	}

	static String channelCategoryId(final String channelSlug) {
		return Tf1PlusConf.CATEGORY_CHANNEL_PREFIX + channelSlug;
	}

	static String programCategoryId(final String channelSlug, final String programSlug) {
		return Tf1PlusConf.CATEGORY_PROGRAM_PREFIX + channelSlug + ":" + programSlug;
	}

	static String channelSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId)) {
			return null;
		}
		if (categoryId.startsWith(Tf1PlusConf.CATEGORY_CHANNEL_PREFIX)) {
			return categoryId.substring(Tf1PlusConf.CATEGORY_CHANNEL_PREFIX.length());
		}
		if (categoryId.startsWith(Tf1PlusConf.CATEGORY_PROGRAM_PREFIX)) {
			final String rest = categoryId.substring(Tf1PlusConf.CATEGORY_PROGRAM_PREFIX.length());
			final int sep = rest.indexOf(':');
			if (sep > 0) {
				return rest.substring(0, sep);
			}
		}
		return null;
	}

	static String programSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(Tf1PlusConf.CATEGORY_PROGRAM_PREFIX)) {
			return null;
		}
		final String rest = categoryId.substring(Tf1PlusConf.CATEGORY_PROGRAM_PREFIX.length());
		final int sep = rest.indexOf(':');
		if (sep < 0 || sep == rest.length() - 1) {
			return null;
		}
		return rest.substring(sep + 1);
	}

	static boolean isProgramCategory(final String categoryId) {
		return programSlugFromCategoryId(categoryId) != null;
	}

	/**
	 * Accepts only http(s) URLs whose host is {@code tf1.fr} or {@code www.tf1.fr}.
	 */
	static boolean isTf1PlusPageUrl(final String url) {
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
			if (!"tf1.fr".equals(lowerHost) && !"www.tf1.fr".equals(lowerHost)) {
				return false;
			}
			final String path = uri.getPath();
			if (path == null || path.toLowerCase(Locale.ROOT).contains("/novo19")) {
				return false;
			}
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isTf1PlusVideoPageUrl(final String url) {
		if (!isTf1PlusPageUrl(url)) {
			return false;
		}
		try {
			final String path = URI.create(url.trim()).getPath();
			return path != null && path.contains("/videos/");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String programName(final Map<String, Object> program) {
		if (program == null) {
			return "";
		}
		return firstNonEmpty(asString(program.get("name")), asString(program.get("slug")));
	}

	static String programSlug(final Map<String, Object> program) {
		if (program == null) {
			return null;
		}
		return asString(program.get("slug"));
	}

	static String videoTitle(final Map<String, Object> video) {
		if (video == null) {
			return "";
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> decoration = (Map<String, Object>) video.get("decoration");
		if (decoration != null) {
			final String label = asString(decoration.get("label"));
			if (StringUtils.isNotEmpty(label)) {
				return label;
			}
		}
		return firstNonEmpty(asString(video.get("title")), asString(video.get("slug")));
	}

	static String videoUrl(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		final String url = asString(video.get("url"));
		if (StringUtils.isNotEmpty(url)) {
			return url;
		}
		return null;
	}

	static String videoDescription(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> decoration = (Map<String, Object>) video.get("decoration");
		if (decoration == null) {
			return null;
		}
		return asString(decoration.get("description"));
	}

	/**
	 * GraphQL {@code date} is catalogue publication / mise en ligne, not a broadcast
	 * air date.
	 */
	static Date videoPublicationDate(final Map<String, Object> video) {
		if (video == null) {
			return null;
		}
		return parseIso8601(asString(video.get("date")));
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
