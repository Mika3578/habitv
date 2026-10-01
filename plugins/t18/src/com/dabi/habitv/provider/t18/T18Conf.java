package com.dabi.habitv.provider.t18;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * T18 public replay constants. Catalogue discovery is public HTML; media is
 * embedded as private Dailymotion content that normal yt-dlp extraction cannot
 * access. No Dailymotion auth-token reconstruction is implemented.
 */
interface T18Conf {

	String NAME = "t18";

	String HOME_URL = "https://t18.fr";

	String REPLAY_URL = HOME_URL + "/replay";

	String EXTENSION = FrameworkConf.MP4;

	String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String STRATEGY = "html-catalog-private-dailymotion";
}
