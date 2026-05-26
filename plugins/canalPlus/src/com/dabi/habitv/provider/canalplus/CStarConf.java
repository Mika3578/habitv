package com.dabi.habitv.provider.canalplus;

import com.dabi.habitv.framework.FrameworkConf;

interface CStarConf {

	String NAME = "cstar";

	String EXTENSION = FrameworkConf.MP4;

	String CATALOG_URL = "https://www.canalplus.com/chaines/cstar/index.php/api/applicationv2/flux/replays/theme/%s";

	String PROGRAM_URL = "https://www.canalplus.com/chaines/cstar/pid5316-cstar-musique.html?cat=%s";

	String PROGRAM_API_URL = "https://www.canalplus.com/chaines/cstar/index.php/api/applicationv2/flux/programme/id/%s";

	int ROOT_CATEGORY_SIZE = 9;

	String HOME_URL = "https://www.canalplus.com/chaines/cstar";

	String VIDEO_INFO_URL = "http://service.canal-plus.com/video/rest/getVideosLiees/cstar/";

	String ENCODING = "ISO-8859-1";

}
