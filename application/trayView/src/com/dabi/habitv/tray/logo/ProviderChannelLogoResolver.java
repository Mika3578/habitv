package com.dabi.habitv.tray.logo;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Maps provider / channel category nodes to optional classpath logo resources.
 * <p>
 * Assets live under {@code icons/providers/} and {@code icons/channels/} in the
 * trayView image classpath. Missing resources resolve to empty — callers must
 * keep text labels and never fail listing.
 */
public final class ProviderChannelLogoResolver {

	public static final int DISPLAY_SIZE_PX = 16;

	private static final Pattern FRANCE_TV_SLUG = Pattern
			.compile("(?:https?://)?(?:www\\.)?france\\.tv/([^/?#]+)/?", Pattern.CASE_INSENSITIVE);

	private final ClasspathResourceProbe probe;

	public ProviderChannelLogoResolver() {
		this(new SystemClasspathResourceProbe());
	}

	public ProviderChannelLogoResolver(final ClasspathResourceProbe probe) {
		this.probe = probe == null ? new SystemClasspathResourceProbe() : probe;
	}

	/**
	 * @return classpath path such as {@code icons/providers/francetv.png}, or empty
	 */
	public Optional<String> resolveClasspathResource(final CategoryDTO category) {
		if (category == null) {
			return Optional.empty();
		}
		final String plugin = normalizeId(category.getPlugin());
		if (plugin == null) {
			return Optional.empty();
		}
		if (isProviderRoot(category)) {
			return existing("icons/providers/" + plugin + ".png");
		}
		if (isChannelLevel(category)) {
			final String channelKey = channelKey(plugin, category);
			if (channelKey != null) {
				return existing("icons/channels/" + plugin + "/" + channelKey + ".png");
			}
		}
		return Optional.empty();
	}

	static boolean isProviderRoot(final CategoryDTO category) {
		return category.getFatherCategory() == null;
	}

	static boolean isChannelLevel(final CategoryDTO category) {
		final CategoryDTO father = category.getFatherCategory();
		return father != null && father.getFatherCategory() == null;
	}

	static String normalizeId(final String raw) {
		if (raw == null) {
			return null;
		}
		final String trimmed = raw.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	static String channelKey(final String plugin, final CategoryDTO category) {
		if (plugin == null) {
			return null;
		}
		if ("francetv".equalsIgnoreCase(plugin)) {
			return franceTvChannelKey(category);
		}
		if ("6play".equalsIgnoreCase(plugin)) {
			return sixPlayChannelKey(category);
		}
		if ("wat".equalsIgnoreCase(plugin)) {
			return watChannelKey(category);
		}
		return sanitizeKey(category.getName());
	}

	private static String franceTvChannelKey(final CategoryDTO category) {
		final String id = category.getId();
		if (id != null) {
			final Matcher matcher = FRANCE_TV_SLUG.matcher(id.trim());
			if (matcher.find()) {
				return sanitizeKey(matcher.group(1));
			}
		}
		return sanitizeKey(category.getName());
	}

	private static String sixPlayChannelKey(final CategoryDTO category) {
		final String fromName = sanitizeKey(category.getName());
		if (fromName == null) {
			return null;
		}
		if ("m6".equals(fromName) || "w9".equals(fromName) || "6ter".equals(fromName)) {
			return fromName;
		}
		return fromName;
	}

	private static String watChannelKey(final CategoryDTO category) {
		final String fromName = sanitizeKey(category.getName());
		if (fromName == null) {
			return null;
		}
		if ("tf1".equals(fromName) || "tfx".equals(fromName) || "tmc".equals(fromName)) {
			return fromName;
		}
		return fromName;
	}

	private Optional<String> existing(final String path) {
		return probe.exists(path) ? Optional.of(path) : Optional.empty();
	}

	static String sanitizeKey(final String raw) {
		if (raw == null) {
			return null;
		}
		final String trimmed = raw.trim().toLowerCase(Locale.ROOT);
		if (trimmed.isEmpty()) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(trimmed.length());
		for (int i = 0; i < trimmed.length(); i++) {
			final char c = trimmed.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_') {
				builder.append(c);
			} else if (c == ' ' || c == '.' || c == '/') {
				builder.append('-');
			}
		}
		String key = builder.toString();
		while (key.contains("--")) {
			key = key.replace("--", "-");
		}
		if (key.startsWith("-")) {
			key = key.substring(1);
		}
		if (key.endsWith("-")) {
			key = key.substring(0, key.length() - 1);
		}
		return key.isEmpty() ? null : key;
	}
}
