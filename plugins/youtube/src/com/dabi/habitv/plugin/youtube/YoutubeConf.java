package com.dabi.habitv.plugin.youtube;

public final class YoutubeConf {

	private YoutubeConf() {

	}

	public static final String NAME = "youtube";
	public static final String NAME_MP3 = "youtube-mp3";
	public static final String ENCODING = "UTF-8";
	public static final String DUMP_CMD = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\"  --write-sub --write-auto-sub --no-check-certificate --format best[ext=mp4]/best";
	public static final String DUMP_CMD_MP3 = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\"  --extract-audio --audio-format mp3 --no-check-certificate";

	public static final long MAX_HUNG_TIME = 300000L;
	public static final String DEFAULT_WINDOWS_EXE = "yt-dlp.exe";
	public static final String DEFAULT_LINUX_BIN_PATH = "yt-dlp";
	public static final String API_KEY = "AIzaSyCg3WitBUQl5ifC2QygQaZUPOSRMKfSD5E";
	public static final String BASE_URL = "https://www.youtube.com";

}
