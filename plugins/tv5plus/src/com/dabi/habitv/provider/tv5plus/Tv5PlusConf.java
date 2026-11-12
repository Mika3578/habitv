package com.dabi.habitv.provider.tv5plus;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TV5+ Canada (ex-TV5Unis) public catch-up constants. Playback is Canada-geo
 * limited. Only GraphQL availabilityStatus=AVAILABLE video products are
 * exposed. Live-auth / subscription paths are excluded.
 */
interface Tv5PlusConf {

	String NAME = "tv5plus";

	String HOME_URL = "https://www.tv5plus.ca";

	/** Legacy host still accepted by yt-dlp's tv5unis extractors. */
	String YTDLP_HOST_URL = "https://www.tv5unis.ca";

	String GRAPHQL_URL = "https://api.tv5unis.ca/graphql";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "TV5+";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "tv5plus:show:";

	String AVAILABILITY_AVAILABLE = "AVAILABLE";

	String MEDIA_VIDEO = "VIDEO";

	String TYPE_COLLECTION = "COLLECTION";

	String TYPE_MOVIE = "MOVIE";

	String TYPE_EPISODE = "EPISODE";

	/** Cap featured product sets walked during catalogue discovery. */
	int MAX_PRODUCT_SETS = 20;

	/** Cap shows collected across all featured sets. */
	int MAX_SHOWS = 200;

	/** Cap AVAILABLE seasons fetched per collection. */
	int MAX_SEASONS_PER_SHOW = 15;
}
