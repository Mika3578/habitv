package com.dabi.habitv.provider.bfmtv;

import com.dabi.habitv.framework.FrameworkConf;

final class BfmTvConf {

	static final String NAME = "bfmtv";

	static final String EXTENSION = FrameworkConf.MP4;

	static final String HOME_URL = "https://www.bfmtv.com";

	static final String PUBLIC_HOST = "www.bfmtv.com";

	static final String PUBLIC_HOST_BARE = "bfmtv.com";

	static final String API_BASE_URL = "https://api.nextradiotv.com";

	static final String USER_AGENT = "Mozilla/5.0 (compatible; Habitv/bfmtv)";

	static final String[] CHANNEL_IDS = { "bfmtv", "bfmbusiness" };

	static final String PARAMETER_KIND = "kind";

	static final String PARAMETER_CHANNEL = "channel";

	static final String PARAMETER_CATEGORY = "categoryId";

	static final String KIND_CHANNEL = "channel";

	static final String KIND_PROGRAM = "program";

	static final int VIDEO_PAGE_SIZE = 20;

	static final int MAX_VIDEO_PAGES = 10;

	static final String DOWNLOAD_UNAVAILABLE_MESSAGE = "This replay is currently unavailable or not supported. See logs for details.";

	private BfmTvConf() {
	}

	static String channelLabel(final String channelId) {
		if ("bfmtv".equals(channelId)) {
			return "BFMTV";
		}
		if ("bfmbusiness".equals(channelId)) {
			return "BFM Business";
		}
		return channelId;
	}

	static String channelPageUrl(final String channelId) {
		return HOME_URL + "/replay/" + channelId;
	}

}
