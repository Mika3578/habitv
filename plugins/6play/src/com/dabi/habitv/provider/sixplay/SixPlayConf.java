package com.dabi.habitv.provider.sixplay;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dabi.habitv.framework.FrameworkConf;

interface SixPlayConf {

	String NAME = "6play";

	String EXTENSION = FrameworkConf.MP4;

	/** Public replay catalogue (M6+). */
	String HOME_URL = "https://www.6play.fr/";

	/** Service sitemap listing channel genre folders (paths map to {@link #HOME_URL}). */
	String SITEMAP_SERVICE_URL = "https://www.m6.fr/sitemap-service.xml";

	Map<String, String> CHANNEL_SLUG_TO_LABEL = Collections.unmodifiableMap(new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("m6", "M6");
			put("w9", "W9");
			put("6ter", "6ter");
			put("gulli", "Gulli");
			put("pp", "Paris Première");
			put("teva", "Téva");
		}
	});

}
