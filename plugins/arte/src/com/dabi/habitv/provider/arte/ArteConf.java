package com.dabi.habitv.provider.arte;

import com.dabi.habitv.framework.FrameworkConf;

interface ArteConf {

	String NAME = "arte";

	String CAT_PAGE = "https://www.arte.tv/fr/";

	String ID_EMISSION_TOKEN = "#ID_EMISSION#";

	String RSS_CATEGORY_URL = "http://videos.arte.tv/fr/do_delegate/videos/programmes/" + ID_EMISSION_TOKEN + ",view,rss.xml";

	String ID_EPISODE_TOKEN = "#ID_EPISODE#";

	String RTMPDUMP_CMD = "-r \"#VIDEO_URL#\" -c 1935 -m 10 -o \"#FILE_DEST#\"";

	String HOME_URL = "https://www.arte.tv/fr/";

	String EXTENSION = FrameworkConf.MP4;

}
