package com.dabi.habitv.provider.arte;

import com.dabi.habitv.framework.FrameworkConf;

interface ArteConf {

	String NAME = "arte";

	String HOME_URL = "https://www.arte.tv";

	/**
	 * Public EMAC catalogue API host. Arte retired the previous
	 * {@code www.arte.tv/api/rproxy/emac/v4} reverse-proxy path (HTTP 404).
	 */
	String EMAC_API_BASE = "https://api.arte.tv/api/emac/v4";

	/** Query param required by EMAC zone listing endpoints. */
	String AUTHORIZED_COUNTRY = "FR";

	/** Language used to bootstrap {@code alternativeLanguages} discovery. */
	String DISCOVERY_REFERENCE_LANGUAGE = "fr";

	/**
	 * Resilience fallback when EMAC HOME payloads omit a stable page deeplink
	 * (notably {@code DEC} and {@code ACT} on web HOME). Primary discovery is
	 * dynamic via {@link ArteCatalogDiscovery}.
	 */
	String[] FALLBACK_PAGE_CODES = { "DEC", "ACT" };

	/** Safety cap when following {@code pagination.links.next} chains. */
	int MAX_PAGINATION_REQUESTS = 100;

	String EXTENSION = FrameworkConf.MP4;
}
