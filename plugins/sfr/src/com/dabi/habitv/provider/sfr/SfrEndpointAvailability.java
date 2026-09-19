package com.dabi.habitv.provider.sfr;

import java.net.UnknownHostException;

final class SfrEndpointAvailability {

	static final String ENDPOINT_UNAVAILABLE_MESSAGE =
			"SFR Sport host sport.sfr.fr is no longer resolvable; provider catalogue is obsolete.";

	private SfrEndpointAvailability() {
	}

	static boolean isUnavailable(final Throwable error) {
		Throwable current = error;
		while (current != null) {
			if (current instanceof UnknownHostException) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	static String buildCategoryUnavailableMessage(final String providerName, final Throwable throwable) {
		return "provider=" + providerName + " operation=catalogue rootCause=host-unresolvable cookiesEnabled=false"
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
}
