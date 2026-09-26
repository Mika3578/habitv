package com.dabi.habitv.provider.telequebec;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class TeleQuebecUrls {

	private static final Pattern CONTENU_LINK = Pattern.compile(
			"href=\"(/contenu/([a-z0-9][a-z0-9\\-]*))\"", Pattern.CASE_INSENSITIVE);

	private TeleQuebecUrls() {
	}

	static String showCategoryId(final String slug) {
		return TeleQuebecConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TeleQuebecConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TeleQuebecConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static boolean isTeleQuebecWatchUrl(final String url) {
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
			if (!"telequebec.tv".equals(lowerHost) && !"www.telequebec.tv".equals(lowerHost)) {
				return false;
			}
			final String path = uri.getPath();
			return path != null && path.toLowerCase(Locale.ROOT).contains("/regarder/");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static List<String> parseShowSlugs(final String homeHtml) {
		final Set<String> slugs = new LinkedHashSet<String>();
		if (StringUtils.isEmpty(homeHtml)) {
			return new ArrayList<String>();
		}
		final Matcher matcher = CONTENU_LINK.matcher(homeHtml);
		while (matcher.find()) {
			final String slug = matcher.group(2);
			if (isUsableShowSlug(slug)) {
				slugs.add(slug.toLowerCase(Locale.ROOT));
			}
		}
		return new ArrayList<String>(slugs);
	}

	static boolean isUsableShowSlug(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return false;
		}
		// Skip numeric CMS ids and nested season/episode paths.
		if (slug.matches("\\d+")) {
			return false;
		}
		if (slug.contains("/")) {
			return false;
		}
		return true;
	}

	static String watchUrl(final String slug, final int season, final int episode) {
		return TeleQuebecConf.HOME_URL + "/regarder/" + slug + "/" + season + "/" + episode;
	}
}
