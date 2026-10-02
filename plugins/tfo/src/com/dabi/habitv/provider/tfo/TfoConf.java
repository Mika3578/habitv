package com.dabi.habitv.provider.tfo;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * TFO public on-demand constants. Playback is frequently Canada-geo-limited;
 * Habitv does not implement geo bypass, DRM, or credential handling.
 */
interface TfoConf {

	String NAME = "tfo";

	String HOME_URL = "https://www.tfo.org";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "TFO";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_CATALOG_PREFIX = "tfo:catalog:";

	String CATEGORY_SHOW_PREFIX = "tfo:show:";

	/** Public catalogue pages used for discovery (not live / paywalled grids). */
	String[][] CATALOGS = new String[][] {
			{ "series", "Séries" },
			{ "documentaires", "Documentaires" },
			{ "films", "Films" },
			{ "animations", "Animations" },
	};
}
