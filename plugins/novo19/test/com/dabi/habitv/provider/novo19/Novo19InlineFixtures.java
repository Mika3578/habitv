package com.dabi.habitv.provider.novo19;

/**
 * Small synthetic BFF/RedBee payloads kept inline to avoid one-off JSON files.
 */
final class Novo19InlineFixtures {

	static final String MALFORMED_JSON = "{not-json";

	static final String UNAVAILABLE_ENVELOPE = "{\"httpCode\":404,\"message\":\"NOT_FOUND\"}";

	static final String EMPTY_CATEGORIES_PAGE = "{\"page\":{\"type\":\"RAILS\",\"id\":\"categories\",\"title\":\"Catégories\",\"rails\":[]},\"version\":\"0.9.1\"}";

	static final String NESTED_DATA_TILES = "{\"data\":{\"tiles\":[{\"id\":\"asset-episode-alpha\",\"type\":\"VOD\","
			+ "\"title\":\"Episode Alpha\",\"href\":\"/player/programme-alpha-episode-alpha\",\"duration\":1800}],"
			+ "\"more\":{\"href\":\"/voir-plus/rail/details/programme-alpha/episodes\"}}}";

	static final String PLAYER_COLLECTION_EPISODE_PAGE = "{\"page\":{\"type\":\"PLAYER\",\"id\":\"collection-alpha-episode-alpha\","
			+ "\"title\":\"Episode Alpha\",\"content\":{\"id\":\"asset-collection-alpha-episode-alpha\",\"type\":\"EPISODE\","
			+ "\"title\":\"Episode Alpha\",\"href\":\"/player/collection-alpha-episode-alpha\",\"source\":{"
			+ "\"provider\":\"redbee\",\"type\":\"EPISODE\",\"id\":\"asset-collection-alpha-episode-alpha\"}}},\"version\":\"0.9.1\"}";

	static final String PLAYER_SERIES_EPISODE_NO_HREF_PAGE = "{\"page\":{\"type\":\"PLAYER\",\"id\":\"series-beta-episode-beta\","
			+ "\"title\":\"Episode Beta\",\"content\":{\"id\":\"asset-series-beta-episode-beta\",\"type\":\"EPISODE\","
			+ "\"title\":\"Episode Beta\",\"source\":{\"provider\":\"redbee\",\"type\":\"EPISODE\","
			+ "\"id\":\"asset-series-beta-episode-beta\"}}},\"version\":\"0.9.1\"}";

	static final String PLAYER_PODCAST_EPISODE_PAGE = "{\"page\":{\"type\":\"PLAYER\",\"id\":\"podcast-alpha-episode-alpha\","
			+ "\"title\":\"Episode Alpha\",\"content\":{\"id\":\"asset-podcast-alpha-episode-alpha\",\"type\":\"PODCAST\","
			+ "\"title\":\"Episode Alpha\",\"href\":\"/player/podcast-alpha-episode-alpha\",\"source\":{"
			+ "\"id\":\"asset-podcast-alpha-episode-alpha\"}}},\"version\":\"0.9.1\"}";

	static final String FILM_BETA_PLAYBACK_INFOS_PAGE = "{\"page\":{\"type\":\"DETAILS\",\"id\":\"film-beta\",\"title\":\"Film Beta\","
			+ "\"content\":{\"id\":\"asset-film-beta\",\"type\":\"VOD\",\"title\":\"Film Beta\",\"duration\":7005,"
			+ "\"source\":{\"id\":\"asset-film-beta\"}},\"playbackInfos\":[{\"type\":\"COMPLETE\",\"assetId\":\"asset-film-beta\","
			+ "\"player\":\"/player/film-beta\"},{\"type\":\"TRAILER\",\"assetId\":\"asset-film-beta-trailer\","
			+ "\"player\":\"/player/film-beta-trailer\"}],\"rails\":[{\"id\":\"reco\",\"title\":\"Recommendations\","
			+ "\"src\":\"/api/1/public/frontends/web/pages/BFF%7Casset-details,film-beta/sections/reco/tiles\"}]},\"version\":\"0.9.1\"}";

	static final String HISTOIRE_PAGINATION_PAGE_1 = "{\"tiles\":[{\"id\":\"asset-theme-gamma\",\"type\":\"SERIE\","
			+ "\"title\":\"Theme Gamma Programme\",\"href\":\"/details/theme-gamma-programme\",\"source\":{"
			+ "\"id\":\"asset-theme-gamma\"}}],\"more\":{\"href\":\"/voir-plus/rail/documentaires-et-magazines/histoire\"},\"version\":\"0.9.1\"}";

	static final String VOIR_PLUS_MOSAIC_PAGE = "{\"page\":{\"type\":\"MOSAIC\",\"id\":\"see-more/histoire\","
			+ "\"title\":\"Histoire\",\"src\":\"/api/1/public/frontends/web/pages/BFF%7Crail-see-more,documentaires-et-magazines,histoire/sections/mosaic/tiles\"},\"version\":\"0.9.1\"}";

	static final String VOIR_PLUS_MOSAIC_TILES = "{\"tiles\":[{\"id\":\"asset-theme-beta\",\"type\":\"SERIE\","
			+ "\"title\":\"Theme Beta Programme\",\"href\":\"/details/theme-beta-programme\",\"source\":{"
			+ "\"id\":\"asset-theme-beta\"}}],\"version\":\"0.9.1\"}";

	private Novo19InlineFixtures() {
	}

}
