package com.dabi.habitv.plugin.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YoutubeConf {
	private static final String API_KEY_PROPERTY = "habitv.youtube.apiKey";
	private static final String API_KEY_ENV = "HABITV_YOUTUBE_API_KEY";

	/** Google API keys for YouTube Data API v3 typically start with {@code AIza}. */
	private static final Pattern GOOGLE_API_KEY_TOKEN = Pattern.compile("AIza[0-9A-Za-z_-]+");

	private YoutubeConf() {

	}

	public static final String NAME = "youtube";
	public static final String NAME_MP3 = "youtube-mp3";
	public static final String ENCODING = "UTF-8";
	/**
	 * Default video download flags for yt-dlp (youtube-dl compatible subset).
	 * Placeholders {@link com.dabi.habitv.framework.FrameworkConf#DOWNLOAD_INPUT} and
	 * {@link com.dabi.habitv.framework.FrameworkConf#DOWNLOAD_DESTINATION} are resolved at runtime.
	 */
	public static final String DUMP_CMD = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\" -f \"bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b\" --merge-output-format mp4 --no-check-certificate";
	public static final String DUMP_CMD_EMBED_SUBS = " --embed-subs --sub-langs \"fr.*,fr,en.*,en,-live_chat\" --sub-format \"srt/vtt/best\"";
	/**
	 * Default MP3 extraction flags for yt-dlp ({@code --extract-audio} / {@code --audio-format}).
	 */
	public static final String DUMP_CMD_MP3 = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\"  --extract-audio --audio-format mp3 --no-check-certificate";

	public static final long MAX_HUNG_TIME = 300000L;
	public static final String DEFAULT_WINDOWS_EXE = "yt-dlp.exe";
	public static final String DEFAULT_LINUX_BIN_PATH = "yt-dlp";
	public static final String BASE_URL = "https://www.youtube.com";

	public static String resolveApiKey() {
		return normalizeApiKey(readRawApiKeyCandidate());
	}

	static String apiKeySkipReason() {
		final String raw = readRawApiKeyCandidate();
		if (raw == null || raw.trim().isEmpty()) {
			return YoutubeDataApiSupport.MISSING_API_KEY_MESSAGE;
		}
		if (normalizeApiKey(raw) == null) {
			return YoutubeDataApiSupport.INVALID_API_KEY_MESSAGE;
		}
		return YoutubeDataApiSupport.MISSING_API_KEY_MESSAGE;
	}

	static String readRawApiKeyCandidate() {
		final String propertyValue = System.getProperty(API_KEY_PROPERTY);
		if (propertyValue != null && !propertyValue.trim().isEmpty()) {
			return propertyValue;
		}
		return System.getenv(API_KEY_ENV);
	}

	static String normalizeApiKey(String candidate) {
		if (candidate == null) {
			return null;
		}
		String trimmed = candidate.trim();
		if (trimmed.length() >= 2
				&& ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
						|| (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
			trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
		}
		if (trimmed.isEmpty()) {
			return null;
		}
		final Matcher matcher = GOOGLE_API_KEY_TOKEN.matcher(trimmed);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
}
