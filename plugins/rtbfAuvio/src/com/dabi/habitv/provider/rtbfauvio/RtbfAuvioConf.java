package com.dabi.habitv.provider.rtbfauvio;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * RTBF Auvio catalogue constants. Download of RedBee/Widevine media is
 * intentionally unsupported.
 */
interface RtbfAuvioConf {

	String NAME = "rtbfAuvio";

	String HOME_URL = "https://auvio.rtbf.be";

	String BFF_BASE = "https://bff-service.rtbf.be/auvio/v1.23";

	String EXTENSION = FrameworkConf.MP4;

	/** Native TV brands: path slug suffix used by Auvio channel pages. */
	String[][] CHANNELS = {
			{ "la-une-1", "La Une" },
			{ "tipik-32", "Tipik" },
			{ "la-trois-3", "La Trois" },
	};

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String STRATEGY = "bff-catalog-redbee-drm-unsupported";
}
