package com.dabi.habitv.provider.tf1plus;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TF1+ public catalogue constants. Download uses yt-dlp on public page URLs.
 * Account login, Gigya tokens, and Widevine paths are intentionally unsupported.
 */
interface Tf1PlusConf {

	String NAME = "tf1plus";

	String HOME_URL = "https://www.tf1.fr";

	String GRAPHQL_URL = HOME_URL + "/graphql/web";

	String EXTENSION = FrameworkConf.MP4;

	/**
	 * Native TF1+ channel slugs. NOVO19 is excluded (separate Habitv module).
	 */
	String[][] CHANNELS = {
			{ "tf1", "TF1" },
			{ "tmc", "TMC" },
			{ "tfx", "TFX" },
			{ "tf1-series-films", "TF1 Séries Films" },
			{ "lci", "LCI" },
	};

	/** Persisted GraphQL query id: program listing by channel. */
	String QUERY_PROGRAMS = "483ce0f";

	/** Persisted GraphQL query id: videos for a program slug. */
	String QUERY_VIDEOS = "a6f9cf0e";

	String VIDEO_TYPE_REPLAY = "REPLAY";

	int PROGRAM_PAGE_LIMIT = 100;

	int VIDEO_PAGE_LIMIT = 40;

	int MAX_VIDEO_PAGES = 5;

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_CHANNEL_PREFIX = "tf1plus:channel:";

	String CATEGORY_PROGRAM_PREFIX = "tf1plus:program:";
}
