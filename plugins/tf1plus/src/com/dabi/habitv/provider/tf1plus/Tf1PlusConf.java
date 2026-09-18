package com.dabi.habitv.provider.tf1plus;

import com.dabi.habitv.framework.FrameworkConf;

final class Tf1PlusConf {

	static final String NAME = "tf1plus";

	static final String EXTENSION = FrameworkConf.MP4;

	static final String HOME_URL = "https://www.tf1.fr";

	static final String PUBLIC_HOST = "www.tf1.fr";

	static final String PUBLIC_HOST_BARE = "tf1.fr";

	static final String GRAPHQL_URL = "https://www.tf1.fr/graphql/web";

	static final String USER_AGENT = "Mozilla/5.0 (compatible; Habitv/tf1plus)";

	static final String REFERER = "https://www.tf1.fr/programmes-tv";

	/**
	 * Persisted GraphQL query hashes used by the public TF1+ website. These are
	 * public client identifiers, not secrets.
	 */
	static final String QUERY_CATEGORIES = "909c68c0";

	static final String QUERY_PROGRAMS = "483ce0f";

	static final String QUERY_VIDEOS = "a6f9cf0e";

	static final String[] CHANNEL_SLUGS = { "tf1", "tmc", "tfx", "tf1-series-films", "lci" };

	static final String PARAMETER_KIND = "kind";

	static final String PARAMETER_CHANNEL = "channel";

	static final String PARAMETER_PROGRAM_SLUG = "programSlug";

	static final String KIND_CHANNEL = "channel";

	static final String KIND_GENRE = "genre";

	static final String KIND_PROGRAM = "program";

	static final String VIDEO_TYPE_REPLAY = "REPLAY";

	static final int PROGRAM_PAGE_SIZE = 100;

	static final int VIDEO_PAGE_SIZE = 20;

	static final int MAX_PROGRAM_PAGES = 10;

	static final int MAX_VIDEO_PAGES = 10;

	static final String DOWNLOAD_UNAVAILABLE_MESSAGE = "This replay is currently unavailable or not supported. See logs for details.";

	private Tf1PlusConf() {
	}

	static String channelLabel(final String slug) {
		if ("tf1".equals(slug)) {
			return "TF1";
		}
		if ("tmc".equals(slug)) {
			return "TMC";
		}
		if ("tfx".equals(slug)) {
			return "TFX";
		}
		if ("tf1-series-films".equals(slug)) {
			return "TF1 Series Films";
		}
		if ("lci".equals(slug)) {
			return "LCI";
		}
		return slug;
	}

}
