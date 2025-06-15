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
	public static final String BASE_URL = "https://www.youtube.com";
	
	// Default max results for search
	public static final int DEFAULT_MAX_RESULTS = 10;
}
