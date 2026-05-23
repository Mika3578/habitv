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

	private static boolean isForbiddenResponse(final String message) {
		return message != null && message.contains("HTTP response code: 403");
	}
}
