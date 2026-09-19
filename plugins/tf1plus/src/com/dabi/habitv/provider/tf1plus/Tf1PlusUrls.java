package com.dabi.habitv.provider.tf1plus;

import java.util.Map;

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

	static boolean isTf1PlusPageUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		final String lower = url.toLowerCase();
		return lower.contains("tf1.fr/") && !lower.contains("novo19");
	}

	static String programName(final Map<String, Object> program) {
		return firstNonEmpty(asString(program.get("name")), asString(program.get("slug")));
	}

	static String programSlug(final Map<String, Object> program) {
		return asString(program.get("slug"));
	}

	static String videoTitle(final Map<String, Object> video) {
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
		final String url = asString(video.get("url"));
		if (StringUtils.isNotEmpty(url)) {
			return url;
		}
		return null;
	}

	static String videoDescription(final Map<String, Object> video) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> decoration = (Map<String, Object>) video.get("decoration");
		if (decoration == null) {
			return null;
		}
		return asString(decoration.get("description"));
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
