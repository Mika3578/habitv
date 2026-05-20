package com.dabi.habitv.provider.arte;

import com.dabi.habitv.framework.FrameworkConf;

interface ArteConf {

	String NAME = "arte";

	String HOME_URL = "https://www.arte.tv";

	String EMAC_API_BASE = "https://www.arte.tv/api/rproxy/emac/v4";

	/** Query param required by EMAC zone listing endpoints. */
	String AUTHORIZED_COUNTRY = "FR";

	String[][] LANGUAGES = {
			{ "fr", "Français" },
			{ "de", "Deutsch" },
			{ "en", "English" },
	};

	/**
	 * Stable EMAC page codes used to discover replay categories.
	 */
	String[][] PAGE_CODES = {
			{ "DOR", "Documentaries" },
			{ "CIN", "Cinema" },
			{ "SER", "Series" },
			{ "ACT", "News & Society" },
			{ "CPO", "Culture & Pop" },
			{ "SCI", "Science" },
			{ "HIS", "History" },
	};

	String EXTENSION = FrameworkConf.MP4;
}
