package com.dabi.habitv.provider.francetv;

import com.dabi.habitv.framework.FrameworkConf;

interface FranceTvConf {

	String NAME = "francetv";

	String HOME_URL = "https://www.france.tv";

	String API_MOBILE_URL = "https://api-mobile.yatta.francetv.fr";

	String API_PLATFORM = "apps";

	String[] CHANNEL_SLUGS = { "france-2", "france-3", "france-4", "france-5", "la1ere" };

	String EXTENSION = FrameworkConf.MP4;

}
