package com.dabi.habitv.provider.novo19;

public final class Novo19Conf {

	public static final String NAME = "novo19";

	public static final String EXTENSION = "mp4";

	public static final String HOME_URL = "https://novo19.ouest-france.fr";

	public static final String BFF_BASE_URL = "https://novo19-bff.ouest-france.fr";

	public static final String CONFIG_PATH = "/api/1/public/config?family=web&width=1920&height=1080";

	public static final String SLUG_RESOLVER_PREFIX = "/api/1/public/frontends/web/pages/by-path/";


	public static final String PARAMETER_ASSET_ID = "assetId";

	public static final String PARAMETER_SEASON_INDEX = "seasonIndex";

	public static final String PARAMETER_CONTENT_KIND = "contentKind";

	public static final String CONTENT_KIND_COLLECTION = "collection";

	public static final String CONTENT_KIND_PROGRAM = "program";

	public static final String CONTENT_KIND_FILM = "film";

	public static final String CONTENT_KIND_SEASON = "season";

	public static final String DOWNLOAD_UNAVAILABLE_MESSAGE = "This replay is currently unavailable or not supported. See logs for details.";

	public static final int MAX_PAGINATION_PAGES = 25;

	private Novo19Conf() {
	}

}
