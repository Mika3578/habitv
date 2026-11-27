package com.dabi.habitv.provider.lequipe;

import java.io.IOException;
import java.net.UnknownHostException;

final class LEquipeEndpointAvailability {

	static final String ENDPOINT_UNAVAILABLE_MESSAGE =
			"L'Équipe video catalogue is unreachable or blocked (HTTP 403 / host unavailable).";

	private LEquipeEndpointAvailability() {
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

	static String buildCategoryUnavailableMessage(final String providerName, final Throwable throwable) {
		return "provider=" + providerName + " operation=catalogue rootCause=endpoint-unavailable cookiesEnabled=false"
				+ " note=" + ENDPOINT_UNAVAILABLE_MESSAGE + " cause=" + shortCauseMessage(throwable);
	}

	private static String shortCauseMessage(final Throwable throwable) {
		if (throwable == null) {
			return "unknown";
		}
		Throwable current = throwable;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		final String message = current.getMessage();
		if (message == null || message.trim().isEmpty()) {
			return current.getClass().getSimpleName();
		}
		return current.getClass().getSimpleName() + ": " + message;
	}

	private static boolean isForbiddenResponse(final String message) {
		return message != null && message.contains("HTTP response code: 403");
	}
}
