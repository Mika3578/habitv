package com.dabi.habitv.provider.tvcom;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TVCom (Brabant wallon, Belgium) public replay via Freecaster HLS. No
 * account, DRM, or geo bypass. Sister Freecaster sites (TV Lux, Télé MB) are
 * out of scope for this module.
 */
interface TvComConf {

	String NAME = "tvCom";

	String HOME_URL = "https://www.tvcom.be";

	String EMISSIONS_PATH = "/emissions";

	String FRECAST_EMBED_PREFIX = "https://tvlocales-player.freecaster.com/embed/";

	/** Exact Freecaster VOD HLS host used by TVCom embeds (no subdomain substring match). */
	String FRECASTER_HLS_HOST = "tvlocales-vod-cmaf.freecaster.com";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "TVCom";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "tvcom:show:";
}
