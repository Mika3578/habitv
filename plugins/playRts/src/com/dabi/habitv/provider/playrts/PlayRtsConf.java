package com.dabi.habitv.provider.playrts;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Play RTS public catalogue constants. Download uses yt-dlp on public play page
 * URLs. Subscription, login, DRM, and geo bypass are intentionally unsupported.
 */
interface PlayRtsConf {

	String NAME = "playRts";

	String HOME_URL = "https://www.rts.ch/play/tv";

	String API_BASE = "https://www.rts.ch/play/v3/api/rts/production";

	String IL_BASE = "https://il.srgssr.ch/integrationlayer/2.0";

	String EXTENSION = FrameworkConf.MP4;

	/** Native Play RTS TV brands (livestream catalogue). Replay is show-based. */
	String[] TV_BRANDS = { "RTS 1", "RTS 2" };

	String SHOWS_PATH = "/shows";

	String MEDIA_LIST_BY_SHOW_TEMPLATE = IL_BASE
			+ "/rts/mediaList/video/latest/byShow/%s?vector=portalplay&pageSize=20";

	int MAX_VIDEO_PAGES = 5;

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_EMISSIONS_ID = "playrts:emissions";

	String CATEGORY_SHOW_PREFIX = "playrts:show:";

	String EMISSIONS_LABEL = "Émissions";
}
