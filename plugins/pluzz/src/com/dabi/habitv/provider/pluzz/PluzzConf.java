package com.dabi.habitv.provider.pluzz;

import com.dabi.habitv.framework.FrameworkConf;

public interface PluzzConf {

	String NAME = "pluzz";

	String EXTENSION = FrameworkConf.MP4;

	String MAIN_URL = "http://pluzz.webservices.francetelevisions.fr/pluzz/liste/type/replay/nb/10000/chaine";

	long MAX_CACHE_ARCHIVE_TIME_MS = 60000L;

	String BASE_URL = "https://www.france.tv";

	String WS_JSON = "http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=%s&catalogue=Pluzz&callback=webserviceCallback_%s";

}
