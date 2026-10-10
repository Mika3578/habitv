package com.dabi.habitv.provider.tvaplus;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TVA+ public catch-up constants. Playback is Canada-geo-limited via Brightcove.
 * Only PUBLIC permission groups are exposed. Subscription/illico+ paths are
 * excluded. No geo bypass, DRM unlock, or credential handling.
 */
interface TvaPlusConf {

	String NAME = "tvaPlus";

	String HOME_URL = "https://www.tvaplus.ca";

	String TVA_CHANNEL_PATH = "/tva";

	String RECENT_PATH = "/ajoutes-recemment";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "TVA";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "tvaplus:show:";

	String CATEGORY_RECENT = "tvaplus:recent";

	String RECENT_CATEGORY_NAME = "Ajoutés récemment (TVA)";

	/** Cap season pages fetched per show to keep discovery bounded. */
	int MAX_SEASONS_PER_SHOW = 20;
}
