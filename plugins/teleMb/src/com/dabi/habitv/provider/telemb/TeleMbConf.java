package com.dabi.habitv.provider.telemb;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Télé MB (Belgium) public replay via Freecaster HLS. No account, DRM, or geo
 * bypass. Sister Freecaster sites (TVCom, TV Lux) are out of scope for this
 * module.
 */
interface TeleMbConf {

	String NAME = "teleMb";

	String HOME_URL = "https://www.telemb.be";

	String EMISSIONS_PATH = "/emissions";

	String FRECAST_EMBED_PREFIX = "https://tvlocales-player.freecaster.com/embed/";

	/** Exact Freecaster VOD HLS host used by Télé MB embeds (no subdomain substring match). */
	String FRECASTER_HLS_HOST = "tvlocales-vod-cmaf.freecaster.com";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "Télé MB";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "telemb:show:";
}
