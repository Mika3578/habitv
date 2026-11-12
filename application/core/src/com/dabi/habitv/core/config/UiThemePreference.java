package com.dabi.habitv.core.config;

import java.util.Locale;

/**
 * Normalizes persisted UI theme values. Light remains the default.
 */
public final class UiThemePreference {

	public static final String LIGHT = "light";

	public static final String DARK = "dark";

	public static final String DEFAULT = LIGHT;

	private UiThemePreference() {
	}

	public static String normalize(final String raw) {
		if (raw == null) {
			return DEFAULT;
		}
		final String trimmed = raw.trim().toLowerCase(Locale.ROOT);
		if (DARK.equals(trimmed)) {
			return DARK;
		}
		return LIGHT;
	}

	public static boolean isDark(final String raw) {
		return DARK.equals(normalize(raw));
	}
}
