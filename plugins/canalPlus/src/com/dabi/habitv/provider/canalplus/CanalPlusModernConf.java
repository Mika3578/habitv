package com.dabi.habitv.provider.canalplus;

/**
 * Modern Canal+ web stack (canalplus.com, hodor, secure-gen-hapi, pass-api-v2).
 * Legacy {@link CanalPlusConf} endpoints remain for backward compatibility only.
 */
interface CanalPlusModernConf {

	String PAGE_HOST = "canalplus.com";

	String HODOR_HOST = "hodor.canalplus.pro";

	String PLAYSET_URL_TEMPLATE = "https://secure-gen-hapi.canal-plus.com/conso/playset/unit/%s";

	String VIEW_URL = "https://secure-gen-hapi.canal-plus.com/conso/view?include=medias";

	String ROUTE_MEUP_HOST = "routemeup.canalplus-bo.net";

	String DRM_TYPE_PLAYREADY_DOWNLOAD = "DRM_MKPC_PLAYREADY_DASH_DOWNLOAD";

	String DIST_TECHNOLOGY_DOWNLOAD = "DOWNLOAD";

	String DIST_MODE_CATCHUP = "catchup";

	String COM_MODE_CATCHUP_NOLIMIT = "CATCHUP_NOLIMIT";

	String PAGE_BASE_URL = "https://www.canalplus.com";

	/** Okapi hash used in hodor detail URLs for France (canalplus.com). */
	String HODOR_DETAIL_HASH = "b63a43e7548cb1a6e7c7319084f48af8";

}
