package com.dabi.habitv.provider.canalplus;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.framework.FrameworkConf;

final class CanalPlusEndpointAvailability {

	static final String ENDPOINT_UNAVAILABLE_MESSAGE =
			"Canal+ provider endpoint is no longer reachable or requires protected access.";

	static final String PROTECTED_ACCESS_DETAIL =
			"Hodor/canalplus.com APIs require protected access or an authenticated Canal+ session. "
					+ "Browser cookies are not enabled for this provider.";

	static final String CANAL_PLUS_UNAVAILABLE_LABEL =
			"Unavailable - Canal+ catalogue requires protected access or is no longer publicly reachable";

	static final String CSTAR_UNAVAILABLE_LABEL =
			"Unavailable - CStar endpoint requires protected access or is no longer publicly reachable";

	static final String UNAVAILABLE_CATEGORY_ID_SUFFIX = "#unavailable-protected-endpoint";

	private CanalPlusEndpointAvailability() {
	}

	static Set<CategoryDTO> buildUnavailablePlaceholderCategories(final String pluginName, final String label) {
		final CategoryDTO placeholder = new CategoryDTO(pluginName, label, unavailableCategoryId(pluginName),
				FrameworkConf.MP4);
		placeholder.setDownloadable(false);
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		categories.add(placeholder);
		return categories;
	}

	static boolean isUnavailablePlaceholder(final CategoryDTO category) {
		return category != null && category.getId() != null
				&& category.getId().endsWith(UNAVAILABLE_CATEGORY_ID_SUFFIX);
	}

	static String unavailableCategoryId(final String pluginName) {
		return pluginName + UNAVAILABLE_CATEGORY_ID_SUFFIX;
	}

	static boolean isUnavailable(final Throwable error) {
		Throwable current = error;
		while (current != null) {
			if (current instanceof UnknownHostException) {
				return true;
			}
			if (current instanceof IOException && isForbiddenResponse(current.getMessage())) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	static String buildCategoryUnavailableMessage(final String providerName) {
		return providerName + ": " + ENDPOINT_UNAVAILABLE_MESSAGE;
	}

	static String buildCategoryUnavailableMessage(final String providerName, final Throwable throwable) {
		return buildCategoryUnavailableMessage(providerName) + " " + PROTECTED_ACCESS_DETAIL + " Cause: "
				+ shortCauseMessage(throwable);
	}

	static String buildEpisodeUnavailableMessage(final String providerName, final CategoryDTO category,
			final Throwable throwable) {
		final String categoryLabel = category == null || category.getName() == null ? "category" : category.getName();
		return providerName + ": Cannot list episodes for \"" + categoryLabel + "\". "
				+ ENDPOINT_UNAVAILABLE_MESSAGE + " " + PROTECTED_ACCESS_DETAIL + " Cause: "
				+ shortCauseMessage(throwable);
	}

	static String shortCauseMessage(final Throwable throwable) {
		if (throwable == null) {
			return "unknown cause";
		}
		final Throwable deepestCause = findDeepestCause(throwable);
		final String message = deepestCause.getMessage();
		if (message == null || message.trim().isEmpty()) {
			return "unknown cause";
		}
		return deepestCause.getClass().getSimpleName() + ": " + normalizeCauseMessage(message);
	}

	private static Throwable findDeepestCause(final Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current;
	}

	private static String normalizeCauseMessage(final String message) {
		final String http403Prefix = "Server returned HTTP response code: 403 for URL: ";
		if (message.startsWith(http403Prefix)) {
			return "HTTP 403 for URL: " + message.substring(http403Prefix.length());
		}
		return message;
	}

	private static boolean isForbiddenResponse(final String message) {
		return message != null && message.contains("HTTP response code: 403");
	}
}
