package com.dabi.habitv.provider.tvlux;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TV Lux (Belgium) public replay via Freecaster HLS. No account, DRM, or geo
 * bypass. Sister Freecaster sites (TVCom, Télé MB) are out of scope for this
 * module.
 */
interface TvLuxConf {

	String NAME = "tvLux";

	String HOME_URL = "https://www.tvlux.be";

	String REPLAY_PATH = "/replay";

	String FRECAST_EMBED_PREFIX = "https://tvlocales-player.freecaster.com/embed/";

	/** Exact Freecaster VOD HLS host used by TV Lux embeds (no subdomain substring match). */
	String FRECASTER_HLS_HOST = "tvlocales-vod-cmaf.freecaster.com";

	/** Freecaster tenant path prefix for TV Lux assets on the shared VOD host. */
	String FRECASTER_TENANT_PATH = "/tvlux/";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "TV Lux";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "tvlux:show:";
}
