package com.dabi.habitv.provider.tf1plus;

import com.dabi.habitv.framework.FrameworkConf;

interface Tf1PlusConf {

	String NAME = "tf1plus";

	String DISPLAY_NAME = "TF1+";

	String EXTENSION = FrameworkConf.MP4;

	String HOME_URL = "https://www.tf1.fr";

	String GRAPHQL_URL = HOME_URL + "/graphql/web";

	String GRAPHQL_PROGRAMS_BY_CHANNEL = "483ce0f";

	String GRAPHQL_VIDEOS_BY_PROGRAM = "a6f9cf0e";

	String GRAPHQL_VIDEO_BY_SLUG = "9b80783950b85247541dd1d851f9cc7fa36574af015621f853ab111a679ce26f";

	String TF1_REPLAY_URL = "https://www.tf1.fr/tf1/replay";

	String TMC_REPLAY_URL = "https://www.tf1.fr/tmc/replay";

	String TFX_REPLAY_URL = "https://www.tf1.fr/tfx/replay";

	String TF1_SERIES_FILMS_REPLAY_URL = "https://www.tf1.fr/tf1-series-films/replay";

	String LCI_REPLAY_URL = "https://www.tf1.fr/lci/replay";

	String ARTE_REPLAY_URL = "https://www.tf1.fr/arte/replay";

	String PUBLIC_SENAT_REPLAY_URL = "https://www.tf1.fr/public-senat/replay";

	int CATALOGUE_PAGE_SIZE = 500;

	int CATALOGUE_SAFETY_PROGRAMME_LIMIT = 10000;

	int CATALOGUE_CACHE_SCHEMA_VERSION = 1;

	long CATALOGUE_DEFAULT_CACHE_TTL_MS = 6L * 60L * 60L * 1000L;

	String PROPERTY_CATALOGUE_CACHE_TTL_HOURS = "habitv.tf1plus.catalogueCacheTtlHours";

	String PROPERTY_CATALOGUE_CACHE_PATH = "habitv.tf1plus.catalogueCachePath";

	java.util.Set<String> EXCLUDED_PROGRAM_SLUGS = new java.util.HashSet<String>(java.util.Arrays.asList("replay",
			"videos", "news", "direct", "programme-tv", "recherche", "compte", "mentions-legales",
			"conditions-generales", "abonnement"));

	String RIGHT_BASIC = "BASIC";

	/** Legacy GraphQL entitlement; live catalogue mostly uses {@link #RIGHT_MAX}. */
	String RIGHT_PREMIUM = "PREMIUM";

	/** TF1+ MAX subscription entitlement (live API). */
	String RIGHT_MAX = "MAX";

	String USER_MESSAGE_UNAVAILABLE = "TF1+ replay is unavailable for this selection.";

	String USER_MESSAGE_CATALOGUE_CACHED = "The TF1+ catalogue is temporarily unavailable. Habitv is showing the last cached catalogue.";

	String USER_MESSAGE_CATALOGUE_UNAVAILABLE = "The TF1+ catalogue is currently unavailable. See logs for details.";

	String USER_MESSAGE_PROTECTED_CONTENT = "Content appears protected. Habitv cannot decrypt or bypass protection.";

}
