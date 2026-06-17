package com.dabi.habitv.provider.novo19;

public final class Novo19Conf {

	public static final String NAME = "novo19";

	public static final String EXTENSION = "mp4";

	public static final String HOME_URL = "https://novo19.ouest-france.fr";

	public static final String BFF_BASE_URL = "https://novo19-bff.ouest-france.fr";

	public static final String CONFIG_PATH = "/api/1/public/config?family=web&width=1920&height=1080";

	public static final String SLUG_RESOLVER_PREFIX = "/api/1/public/frontends/web/pages/by-path/";

	public static final String REDBEE_BASE_URL = "https://exposure.api.redbee.live/v2/customer/OuestFrance/businessunit/novoplus";

	public static final String REDBEE_ANONYMOUS_AUTH_PATH = "/auth/anonymous";

	public static final String REDBEE_PLAY_PATH_PREFIX = "/entitlement/";

	public static final String REDBEE_PLAY_PATH_SUFFIX = "/play";

	public static final String LIVE_ASSET_ID = "novo19_565BFFb";

	public static final String PARAMETER_ASSET_ID = "assetId";

	public static final String PARAMETER_SEASON_INDEX = "seasonIndex";

	public static final String PARAMETER_CONTENT_KIND = "contentKind";

	public static final String CONTENT_KIND_COLLECTION = "collection";

	public static final String CONTENT_KIND_PROGRAM = "program";

	public static final String CONTENT_KIND_FILM = "film";

	public static final String CONTENT_KIND_SEASON = "season";

	public static final String CONTENT_KIND_PODCAST = "podcast";

	public static final String EDITORIAL_INFO = "Info";

	public static final String EDITORIAL_TALK = "Talk";

	public static final String PARAMETER_EDITORIAL_BUCKET = "editorialBucket";

	public static final String PARAMETER_AUDIO_CONTENT = "audioContent";

	public static final String PARAMETER_DESCRIPTION = "description";

	public static final String PARAMETER_CANONICAL_PROGRAM_ID = "canonicalProgramId";

	public static final String PARAMETER_SEASON_RAIL_SRC = "seasonRailSrc";

	public static final String SECTION_DOCUMENTARIES = "Nos documentaires et magazines";

	public static final String THEME_UNCLASSIFIED = "Sans thématique";

	public static final String DOCUMENTARIES_PUBLIC_PATH = "documentaires-et-magazines";

	public static final String DOWNLOAD_UNAVAILABLE_MESSAGE = "This replay is currently unavailable or not supported. See logs for details.";

	/**
	 * yt-dlp audio-only flags for podcast episodes (delegated to the youtube downloader).
	 */
	public static final String PODCAST_YT_DLP_ARGS = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\" --extract-audio --audio-format mp3 --no-check-certificate";

	public static final int MAX_PAGINATION_PAGES = 25;

	private Novo19Conf() {
	}

}
