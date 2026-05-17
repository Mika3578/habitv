package com.dabi.habitv.plugin.youtube;

public final class YoutubeConf {

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

	public static final String API_KEY_SYSTEM_PROPERTY = "habitv.youtube.api.key";
	public static final String API_KEY_ENV_VAR = "HABITV_YOUTUBE_API_KEY";
	// Shared fallback shipped with habiTv. Likely rate-limited or revoked;
	// override via -D habitv.youtube.api.key=... or the HABITV_YOUTUBE_API_KEY env var.
	public static final String API_KEY = "AIzaSyCg3WitBUQl5ifC2QygQaZUPOSRMKfSD5E";
	public static final String BASE_URL = "https://www.youtube.com";

	public static String getApiKey() {
		String key = System.getProperty(API_KEY_SYSTEM_PROPERTY);
		if (key != null && !key.isEmpty()) {
			return key;
		}
		key = System.getenv(API_KEY_ENV_VAR);
		if (key != null && !key.isEmpty()) {
			return key;
		}
		return API_KEY;
	}

}
