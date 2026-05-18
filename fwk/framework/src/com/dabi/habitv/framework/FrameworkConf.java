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

	String UPDATE_ENABLED_PROPERTY = "habitv.update.enabled";

	String UPDATE_URL_PROPERTY = "habitv.update.url";

	/** Overrides configuration.xml updateConfig/autoriseSnapshot when set to true or false. */
	String UPDATE_AUTORISE_SNAPSHOT_PROPERTY = "habitv.update.autoriseSnapshot";

	/** Target static repository base when updates are explicitly enabled. */
	String UPDATE_URL = "https://mika3578.github.io/habitv-repo/repository/";

	/** Update manifest file at the repository root (pipe-delimited entries). */
	String UPDATE_MANIFEST_FILE = "habitv-update-manifest.properties";

	/** Plugin list file at the repository root (one artifact id per line). */
	String PLUGINS_LIST_FILE = "plugins.txt";

	/** External tools layout: repository/tools/{tool-name}/{version}/{file} */
	String TOOLS_REPOSITORY_PREFIX = "tools";

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
