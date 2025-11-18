package com.dabi.habitv.framework;

public interface FrameworkConf {

	String DOWNLOAD_INPUT = "#VIDEO_URL#";

	String DOWNLOAD_DESTINATION = "#FILE_DEST#";

	String EXTENSION = "#EXTENSION#";

	String PARAMETER_ARGS = "ARGUMENTS";

	String DOWNLOADER_PARAM = "downloader";

	long TIME_BETWEEN_LOG = 2000L;

	long HUNG_PROCESS_TIME = 180000L;

	Integer TIME_OUT_MS = 30000;

	// GitHub Pages URL for plugin distribution (free, public, no authentication required)
	// Base URL: https://mika3578.github.io/habitv/plugins
	String UPDATE_URL = "https://mika3578.github.io/habitv/plugins";

	String GROUP_ID = "com.dabi.habitv";

	String VERSION = "version";

	String RTMDUMP = "rtmpdump";

	String CURL = "curl";

	String MP4 = "mp4";

	String FFMPEG = "ffmpeg";

	String M3U8 = "m3u8";
	
	String M4U8 = "m4u8";

	String UTF8 = "UTF-8";

	String DEFAULT_DOWNLOADER = CURL;

	String ERROR_FILE = "dlError.index";

	String USER_HOME = System.getProperty("user.home").replace("\\", "/");

	String ADOBEHDS = "adobeHDS";

	String YOUTUBE = "youtube";
}
