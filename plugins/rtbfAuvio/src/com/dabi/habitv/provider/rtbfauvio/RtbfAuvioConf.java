package com.dabi.habitv.provider.rtbfauvio;

import com.dabi.habitv.framework.FrameworkConf;

interface RtbfAuvioConf {

	String NAME = "rtbfAuvio";

	String HOME_URL = "https://auvio.rtbf.be";

	String PUBLIC_HOST = "auvio.rtbf.be";

	String BFF_BASE = "https://bff-service.rtbf.be/auvio/v1.23";

	String EXPOSURE_BASE_URL = "https://exposure.api.redbee.live/v2/customer/RTBF/businessunit/Auvio";

	String EXPOSURE_HOST = "exposure.api.redbee.live";

	String ANONYMOUS_AUTH_PATH = "/auth/anonymous";

	String ACCOUNT_AUTH_PATH = "/auth/gigyaLogin";

	String PLAY_PATH_PREFIX = "/entitlement/";

	String PLAY_PATH_SUFFIX = "/play";

	String ASSET_ID_SUFFIX = "_6BA97Bb";

	String PARAMETER_ASSET_ID = "assetId";

	String EXTENSION = FrameworkConf.MP4;

	String[][] CHANNELS = {
			{ "la-une-1", "La Une" },
			{ "tipik-32", "Tipik" },
			{ "la-trois-3", "La Trois" },
	};

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable or not supported. See logs for details.";

	String STRATEGY = "bff-catalog-playback";

	int BFF_GET_MAX_ATTEMPTS = 3;

	long BFF_GET_RETRY_DELAY_MS = 250L;

	String ENV_ID_TOKEN = "RTBF_AUVIO_ID_TOKEN";

	String PROPERTY_ID_TOKEN = "habitv.rtbfAuvio.idToken";

}
