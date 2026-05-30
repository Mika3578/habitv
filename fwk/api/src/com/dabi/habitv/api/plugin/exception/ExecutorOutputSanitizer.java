package com.dabi.habitv.api.plugin.exception;

import java.util.regex.Pattern;

/**
 * Redacts sensitive fragments from external downloader output before logging.
 */
public final class ExecutorOutputSanitizer {

	private static final int OUTPUT_SNIPPET_MAX_LENGTH = 300;

	private static final Pattern SENSITIVE_QUERY_PARAM = Pattern
			.compile("(?i)([?&](key|apiKey|access_token|token|oauth_token|authorization)=)([^&\\s\"']*)");

	private static final Pattern EMBEDDED_GOOGLE_API_KEY = Pattern.compile("AIza[0-9A-Za-z_-]+");

	private static final Pattern COOKIES_FLAG = Pattern.compile("(?i)(--cookies(?:-from-browser)?\\s+)(\\S+)");

	private static final Pattern BROWSER_PROFILE_FLAG = Pattern
			.compile("(?i)(--(?:cookies-from-browser|profile-directory)\\s+)(\\S+)");

	private ExecutorOutputSanitizer() {
	}

	public static String sanitizeCommand(final String command) {
		if (command == null || command.isEmpty()) {
			return "";
		}
		return redactSecretsInText(command);
	}

	public static String sanitizeOutput(final String output) {
		if (output == null || output.isEmpty()) {
			return "";
		}
		return buildOutputSnippet(redactSecretsInText(output));
	}

	public static String redactSecretsInText(final String text) {
		if (text == null) {
			return "";
		}
		String sanitized = SENSITIVE_QUERY_PARAM.matcher(text).replaceAll("$1***");
		sanitized = COOKIES_FLAG.matcher(sanitized).replaceAll("$1***");
		sanitized = BROWSER_PROFILE_FLAG.matcher(sanitized).replaceAll("$1***");
		return EMBEDDED_GOOGLE_API_KEY.matcher(sanitized).replaceAll("AIza***");
	}

	private static String buildOutputSnippet(final String sanitizedOutput) {
		final String compact = sanitizedOutput.replace('\r', ' ').replace('\n', ' ').trim();
		if (compact.isEmpty()) {
			return "";
		}
		if (compact.length() <= OUTPUT_SNIPPET_MAX_LENGTH) {
			return compact;
		}
		return compact.substring(0, OUTPUT_SNIPPET_MAX_LENGTH) + "...";
	}
}
