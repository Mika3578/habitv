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

	String ENV_TF1_EMAIL = "TF1_EMAIL";

	String ENV_TF1_PASSWORD = "TF1_PASSWORD";

	String ENV_DEVICE_PATH = "TF1_DEVICE_PATH";

	/** Legacy env alias; prefer {@link #ENV_DEVICE_PATH}. */
	String ENV_WVD_PATH = "WVD_PATH";

	String ENV_TF1_PYTHON = "TF1_PYTHON";

	String ENV_N_M3U8DL_RE = "N_M3U8DL_RE";

	String ENV_MEDIAFLOW_URL = "MEDIAFLOW_URL";

	String ENV_MEDIAFLOW_PASSWORD = "MEDIAFLOW_PASSWORD";

	/** Set from {@code configuration.xml} via Habitv core at startup. */
	String PROPERTY_EMAIL = "habitv.tf1plus.email";

	String PROPERTY_PASSWORD = "habitv.tf1plus.password";

	String PROPERTY_DEVICE_PATH = "habitv.tf1plus.devicePath";

	/** Legacy property alias; prefer {@link #PROPERTY_DEVICE_PATH}. */
	String PROPERTY_WVD_PATH = "habitv.tf1plus.wvdPath";

	String PROPERTY_N_M3U8DL_RE = "habitv.tf1plus.nM3u8dlRe";

	String PROPERTY_MEDIAFLOW_URL = "habitv.tf1plus.mediaflowUrl";

	String PROPERTY_MEDIAFLOW_PASSWORD = "habitv.tf1plus.mediaflowPassword";

	String PROPERTY_PYTHON = "habitv.tf1plus.python";

	/** Tags under {@code configuration.xml} / {@code downloadConfig}/{@code downloaders}. */
	String CONFIG_TAG_EMAIL = "tf1plusEmail";

	String CONFIG_TAG_PASSWORD = "tf1plusPassword";

	String CONFIG_TAG_DEVICE_PATH = "tf1plusDevicePath";

	/** Legacy configuration.xml tag; prefer {@link #CONFIG_TAG_DEVICE_PATH}. */
	String CONFIG_TAG_WVD_PATH = "tf1plusWvdPath";

	String CONFIG_TAG_N_M3U8DL_RE = "tf1plusNM3u8dlRe";

	String CONFIG_TAG_MEDIAFLOW_URL = "tf1plusMediaflowUrl";

	String CONFIG_TAG_MEDIAFLOW_PASSWORD = "tf1plusMediaflowPassword";

	String CONFIG_TAG_PYTHON = "tf1plusPython";

	String USER_MESSAGE_UNAVAILABLE = "TF1+ replay is unavailable for this selection.";

	String USER_MESSAGE_CATALOGUE_CACHED = "The TF1+ catalogue is temporarily unavailable. Habitv is showing the last cached catalogue.";

	String USER_MESSAGE_CATALOGUE_UNAVAILABLE = "The TF1+ catalogue is currently unavailable. See logs for details.";

	String USER_MESSAGE_PREMIUM_REPLAY = "This TF1+ replay requires premium-replay configuration on this machine.";

	String USER_MESSAGE_TF1_CREDENTIALS_REQUIRED = "Merci de saisir votre e-mail et votre mot de passe TF1+ dans l'onglet Configuration.";

	String USER_MESSAGE_TF1_DEVICE_PATH_REQUIRED = "Merci de renseigner le chemin du fichier device local TF1+ dans l'onglet Configuration.";

	String USER_MESSAGE_TF1_DOWNLOAD_BACKEND_REQUIRED = "Configurez N_m3u8DL-RE (onglet Configuration) ou MediaFlow dans configuration.xml pour le replay TF1+.";

	String USER_MESSAGE_PREMIUM_REPLAY_NOT_CONFIGURED = "Le replay TF1+ n'est pas entièrement configuré sur cette machine. Complétez l'onglet Configuration ou configuration.xml.";



}
