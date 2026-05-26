package com.dabi.habitv.provider.canalplus;

import java.io.IOException;
import java.net.UnknownHostException;

final class CanalPlusEndpointAvailability {

	static final String ENDPOINT_UNAVAILABLE_MESSAGE =
			"Canal+ provider endpoint is no longer reachable or requires protected access.";

	private CanalPlusEndpointAvailability() {
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
		return buildCategoryUnavailableMessage(providerName) + " Cause: " + shortCauseMessage(throwable);
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
