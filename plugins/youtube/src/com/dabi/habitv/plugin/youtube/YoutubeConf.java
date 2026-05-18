package com.dabi.habitv.plugin.youtube;

public final class YoutubeConf {
	private static final String API_KEY_PROPERTY = "habitv.youtube.apiKey";
	private static final String API_KEY_ENV = "HABITV_YOUTUBE_API_KEY";

	private YoutubeConf() {

	}

	public static final String NAME = "youtube";
	public static final String NAME_MP3 = "youtube-mp3";
	public static final String ENCODING = "UTF-8";
	public static final String DUMP_CMD = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\"  --write-sub --write-auto-sub --no-check-certificate";
	public static final String DUMP_CMD_MP3 = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\"  --extract-audio --audio-format mp3 --no-check-certificate";

	public static final long MAX_HUNG_TIME = 300000L;
	public static final String DEFAULT_WINDOWS_EXE = "youtube-dl.exe";
	public static final String DEFAULT_LINUX_BIN_PATH = "youtube-dl";
	public static final String BASE_URL = "https://www.youtube.com";

	public static String resolveApiKey() {
		String value = normalizeApiKey(System.getProperty(API_KEY_PROPERTY));
		if (value != null) {
			return value;
		}
		return normalizeApiKey(System.getenv(API_KEY_ENV));
	}

	static String normalizeApiKey(String candidate) {
		if (candidate == null) {
			return null;
		}
		String trimmed = candidate.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
