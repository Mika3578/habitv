package com.dabi.habitv.framework.plugin.utils.update;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Resolves the runtime static repository base URL (never the local Maven deploy path).
 */
public final class UpdateRepositoryUrls {

	private UpdateRepositoryUrls() {
	}

	public static String getUpdateBaseUrl() {
		final String configured = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		if (configured != null && !configured.trim().isEmpty()) {
			return normalizeBaseUrl(configured.trim());
		}
		return normalizeBaseUrl(FrameworkConf.UPDATE_URL);
	}

	public static String normalizeBaseUrl(final String baseUrl) {
		if (baseUrl == null) {
			return null;
		}
		final String trimmed = baseUrl.trim();
		if (trimmed.endsWith("/")) {
			return trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	public static String buildRepositoryUrl(final String relativePath) {
		final String base = getUpdateBaseUrl();
		if (relativePath == null || relativePath.isEmpty()) {
			return base;
		}
		final String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
		return base + "/" + path;
	}
}
