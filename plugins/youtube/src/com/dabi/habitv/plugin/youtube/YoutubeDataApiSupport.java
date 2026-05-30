package com.dabi.habitv.plugin.youtube;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * YouTube Data API v3 URL building, credential checks, and safe diagnostics.
 */
public final class YoutubeDataApiSupport {

	static final String API_KEY_PARAM = "key";

	static final String DATA_API_HOST = "www.googleapis.com/youtube/v3/";

	static final String MISSING_API_KEY_MESSAGE = "YouTube Data API key is not configured.";

	static final String FORBIDDEN_SUMMARY = "YouTube Data API request was rejected (HTTP 403). "
			+ "Likely causes: missing or invalid API key, YouTube Data API v3 not enabled for the project, "
			+ "API quota exceeded, or unauthorized request.";

	static final String BAD_REQUEST_SUMMARY = "YouTube Data API request was rejected (HTTP 400). "
			+ "Likely causes: malformed API key value (use the key only, not a file path), invalid query parameters, "
			+ "or an unsupported request.";

	static final String INVALID_API_KEY_MESSAGE = "YouTube Data API key is invalid. "
			+ "Enter only the API key value in Configuration (not a file path).";

	/** YouTube Data API search.list lower bound for {@code publishedAfter}. */
	static final String MIN_PUBLISHED_AFTER_RFC3339 = "1970-01-01T00:00:00Z";

	static final String SEARCH_EMPTY_MESSAGE = "YouTube search returned no items.";

	static final String VIDEOS_EMPTY_MESSAGE = "YouTube mostPopular chart returned no items.";

	static final String CHART_MOST_POPULAR = "mostPopular";

	private static final Pattern SENSITIVE_QUERY_PARAM = Pattern
			.compile("(?i)([?&](key|apiKey|access_token|token|oauth_token|authorization)=)([^&]*)");

	private static final Pattern EMBEDDED_GOOGLE_API_KEY = Pattern.compile("AIza[0-9A-Za-z_-]+");

	private YoutubeDataApiSupport() {
	}

	static boolean isDataApiUrl(final String url) {
		return url != null && url.contains(DATA_API_HOST);
	}

	static boolean isSearchApiUrl(final String url) {
		return url != null && url.contains(DATA_API_HOST + "search");
	}

	static boolean isVideosApiUrl(final String url) {
		return url != null && url.contains(DATA_API_HOST + "videos");
	}

	/**
	 * Resolves {@code publishedAfter} for search.list, or returns {@code null} to omit the
	 * parameter (all-time window) when {@code days} is missing, invalid, or would fall before
	 * {@link #MIN_PUBLISHED_AFTER_RFC3339}.
	 */
	static String resolvePublishedAfterForSearch(final String daysParam, final DateFormat dateFormat,
			final Date referenceNow) {
		if (daysParam == null || daysParam.trim().isEmpty()) {
			return null;
		}
		final int days;
		try {
			days = Integer.parseInt(daysParam.trim());
		} catch (NumberFormatException e) {
			return null;
		}
		if (days <= 0) {
			return null;
		}
		final Date publishedAfterDate = DateUtils.addDays(referenceNow, -days);
		if (publishedAfterDate.before(minPublishedAfterUtc())) {
			return null;
		}
		return dateFormat.format(publishedAfterDate);
	}

	static Date minPublishedAfterUtc() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	static Date parsePublishedAfterRfc3339(final DateFormat dateFormat, final String value) throws ParseException {
		return dateFormat.parse(value);
	}

	static boolean urlHasApiKey(final String url) {
		return url != null && url.matches(".*[?&]" + API_KEY_PARAM + "=[^&]+.*");
	}

	static String appendApiKeyParam(final String url, final String apiKey) {
		final String normalized = YoutubeConf.normalizeApiKey(apiKey);
		if (normalized == null) {
			return url;
		}
		try {
			return url + "&" + API_KEY_PARAM + "=" + URLEncoder.encode(normalized, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		}
	}

	static String redactUrl(final String url) {
		if (url == null) {
			return "";
		}
		return SENSITIVE_QUERY_PARAM.matcher(url).replaceAll("$1***");
	}

	static String redactSecretsInText(final String text) {
		if (text == null) {
			return "";
		}
		return EMBEDDED_GOOGLE_API_KEY.matcher(redactUrl(text)).replaceAll("AIza***");
	}

	static boolean isRecoverableApiError(final Throwable throwable) {
		return summarizeRecoverableApiError(throwable) != null;
	}

	static String summarizeRecoverableApiError(final Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			final String message = current.getMessage();
			if (message != null) {
				if (message.contains("HTTP response code: 400") || message.contains("HTTP 400")) {
					return BAD_REQUEST_SUMMARY;
				}
				if (message.contains("HTTP response code: 403") || message.contains("HTTP 403")) {
					return FORBIDDEN_SUMMARY;
				}
			}
			current = current.getCause();
		}
		return null;
	}

	static String buildSafeApiFailureMessage(final String url) {
		return buildSafeApiFailureMessage(null, url);
	}

	static String buildSafeApiFailureMessage(final CategoryDTO category, final String url) {
		final String categoryName = category == null ? "n/a" : category.getName();
		final String endpoint;
		if (isSearchApiUrl(url)) {
			endpoint = "search";
		} else if (isVideosApiUrl(url)) {
			endpoint = "videos";
		} else {
			endpoint = "data";
		}
		return "provider=YouTube, category=" + categoryName + ", endpoint=" + endpoint + ", url="
				+ redactUrl(url);
	}

	static TechnicalException newNonRecoverableApiFailure(final CategoryDTO category, final String url) {
		return new TechnicalException(buildSafeApiFailureMessage(category, url));
	}
}
