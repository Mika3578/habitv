package com.dabi.habitv.provider.icitoutv;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * ICI TOU.TV public/free catalogue constants. Playback is Canada-geo-limited and
 * may require a free Member session via Habitv optional cookies. Premium EXTRA
 * content is excluded. No geo bypass, DRM unlock, or credential handling.
 */
interface IciToutTvConf {

	String NAME = "iciToutTv";

	String HOME_URL = "https://ici.tou.tv";

	String FREE_COLLECTION_PATH = "/collection/gratuit";

	String EXTENSION = FrameworkConf.MP4;

	String CHANNEL_LABEL = "ICI TOU.TV";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "icitoutv:show:";
}
