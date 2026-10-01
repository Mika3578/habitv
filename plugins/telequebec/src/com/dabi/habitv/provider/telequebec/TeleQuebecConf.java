package com.dabi.habitv.provider.telequebec;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Télé-Québec public catalogue constants. Playback is frequently Canada-only;
 * Habitv does not implement geo bypass, DRM, or login.
 */
interface TeleQuebecConf {

	String NAME = "telequebec";

	String HOME_URL = "https://www.telequebec.tv";

	String GRAPHQL_URL = "https://api.pc-cms.tele.quebec/graphql";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "Télé-Québec";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "telequebec:show:";

	int MAX_SEASONS = 20;

	int MAX_EPISODES_PER_SEASON = 40;
}
