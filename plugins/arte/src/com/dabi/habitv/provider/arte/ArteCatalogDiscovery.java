package com.dabi.habitv.provider.arte;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Discovers Arte languages and catalogue page codes from public EMAC HOME
 * payloads (web + tv) and genre hubs. Uses {@link ArteConf#FALLBACK_PAGE_CODES}
 * only when a known area is absent from dynamic discovery.
 */
final class ArteCatalogDiscovery {

	private static final Set<String> PAGE_CODE_BLOCKLIST = new LinkedHashSet<>();

	static {
		PAGE_CODE_BLOCKLIST.add("HOME");
		PAGE_CODE_BLOCKLIST.add("home");
		PAGE_CODE_BLOCKLIST.add("MY_FAVORITES");
		PAGE_CODE_BLOCKLIST.add("MY_RESUME");
		PAGE_CODE_BLOCKLIST.add("SEARCH");
	}

	private final ArteEmacTransport transport;

	/** Page roots fetched during the latest {@link #discoverPages(String)} call. */
	private final Map<String, JsonNode> discoveredPageRootsByUrl = new LinkedHashMap<>();

	ArteCatalogDiscovery(final ArteEmacTransport transport) {
		this.transport = transport;
	}

	List<ArteLanguage> discoverLanguages() {
		final Map<String, String> languages = new LinkedHashMap<>();
		try {
			final String homeUrl = buildHomeUrl(ArteConf.DISCOVERY_REFERENCE_LANGUAGE);
			final JsonNode home = ArteEmacJson.parseTree(transport.get(homeUrl), homeUrl);
			collectLanguagesFromHome(home, languages);
		} catch (final RuntimeException e) {
			languages.put(ArteConf.DISCOVERY_REFERENCE_LANGUAGE, "Français");
		}
		if (!languages.containsKey(ArteConf.DISCOVERY_REFERENCE_LANGUAGE)) {
			languages.put(ArteConf.DISCOVERY_REFERENCE_LANGUAGE, "Français");
		}
		appendRomanianIfAvailable(languages);
		final List<ArteLanguage> result = new ArrayList<>();
		for (final Map.Entry<String, String> entry : languages.entrySet()) {
			result.add(new ArteLanguage(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	List<ArtePageRef> discoverPages(final String languageCode) {
		discoveredPageRootsByUrl.clear();
		final Set<String> codes = new LinkedHashSet<>();
		collectFromHome(languageCode, "web", codes);
		collectFromHome(languageCode, "tv", codes);
		for (final String fallback : ArteConf.FALLBACK_PAGE_CODES) {
			codes.add(fallback);
		}
		final List<ArtePageRef> pages = new ArrayList<>();
		for (final String code : codes) {
			if (!isCataloguePageCode(code)) {
				continue;
			}
			final String pageUrl = buildPageUrl(languageCode, code);
			try {
				final JsonNode pageRoot = ArteEmacJson.parseTree(transport.get(pageUrl), pageUrl);
				discoveredPageRootsByUrl.put(pageUrl, pageRoot);
				pages.add(new ArtePageRef(code, ArteEmacJson.pageTitle(pageRoot), pageUrl));
			} catch (final RuntimeException e) {
				// Missing page for this language (e.g. ro/DOR) must not break discovery.
			}
		}
		return pages;
	}

	/**
	 * Returns the page JSON already loaded while verifying a catalogue page
	 * during {@link #discoverPages(String)}, avoiding a second EMAC fetch when
	 * building the category tree for the same language.
	 */
	JsonNode getDiscoveredPageRoot(final String pageUrl) {
		return discoveredPageRootsByUrl.get(pageUrl);
	}

	private static void collectLanguagesFromHome(final JsonNode home, final Map<String, String> languages) {
		for (final JsonNode alt : home.path("alternativeLanguages")) {
			final String code = alt.path("code").asText(null);
			if (StringUtils.isNotEmpty(code)) {
				languages.put(code, alt.path("label").asText(code));
			}
		}
	}

	private void collectFromHome(final String languageCode, final String support, final Set<String> codes) {
		final String homeUrl = ArteConf.EMAC_API_BASE + "/" + languageCode + "/" + support + "/pages/HOME/?authorizedCountry="
				+ ArteConf.AUTHORIZED_COUNTRY;
		try {
			final JsonNode home = ArteEmacJson.parseTree(transport.get(homeUrl), homeUrl);
			ArteEmacJson.collectEmacPageCodes(home, codes);
			collectGenreHubPages(languageCode, support, home, codes);
		} catch (final RuntimeException e) {
			// Non-blocking: try the other HOME variant.
		}
	}

	private void collectGenreHubPages(final String languageCode, final String support, final JsonNode home,
			final Set<String> codes) {
		for (final JsonNode zone : ArteEmacJson.emacZonesNode(home)) {
			if (!"genres_HOME".equals(zone.path("code").asText(null))) {
				continue;
			}
			collectGenreItems(languageCode, zone.path("content").path("data"), codes);
			final String zoneId = zone.path("id").asText(null);
			if (StringUtils.isNotEmpty(zoneId)) {
				final String zoneUrl = ArteConf.EMAC_API_BASE + "/" + languageCode + "/" + support + "/zones/" + zoneId
						+ "/content?authorizedCountry=" + ArteConf.AUTHORIZED_COUNTRY + "&page=1";
				try {
					final JsonNode zoneContent = ArteEmacJson.parseTree(transport.get(zoneUrl), zoneUrl);
					collectGenreItems(languageCode, ArteEmacJson.emacDataNode(zoneContent), codes);
					followGenreZonePagination(languageCode, support, zoneId, zoneContent, codes);
				} catch (final RuntimeException e) {
					// Genre zone optional.
				}
			}
		}
	}

	private void collectGenreItems(final String languageCode, final JsonNode data, final Set<String> codes) {
		if (!data.isArray()) {
			return;
		}
		for (final JsonNode item : data) {
			final String deeplink = item.path("deeplink").asText(null);
			if (StringUtils.isNotEmpty(deeplink)) {
				ArteEmacJson.collectEmacPageCodes(item, codes);
			} else {
				resolveHubPageCode(languageCode, item.path("url").asText(null), codes);
			}
		}
	}

	private void followGenreZonePagination(final String languageCode, final String support, final String zoneId,
			JsonNode zoneContent, final Set<String> codes) {
		String nextUrl = ArteEmacJson.zonePagination(zoneContent).path("links").path("next").asText(null);
		int fetched = 1;
		while (StringUtils.isNotEmpty(nextUrl) && ArteRequestUrls.isTrustedEmacApiUrl(nextUrl)
				&& fetched < ArteConf.MAX_PAGINATION_REQUESTS) {
			try {
				zoneContent = ArteEmacJson.parseTree(transport.get(nextUrl), nextUrl);
				collectGenreItems(languageCode, ArteEmacJson.emacDataNode(zoneContent), codes);
				fetched++;
				nextUrl = ArteEmacJson.zonePagination(zoneContent).path("links").path("next").asText(null);
			} catch (final RuntimeException e) {
				break;
			}
		}
	}

	private void resolveHubPageCode(final String languageCode, final String hubUrl, final Set<String> codes) {
		final String resolved = resolvePublicSiteUrl(hubUrl);
		if (!ArteRequestUrls.isTrustedPublicSiteUrl(resolved)) {
			return;
		}
		try {
			final String html = transport.get(resolved);
			ArteEmacJson.addPageCodesFromHtml(html, codes);
		} catch (final RuntimeException e) {
			// Hub HTML resolution is best-effort only.
		}
	}

	private void appendRomanianIfAvailable(final Map<String, String> languages) {
		if (languages.containsKey("ro")) {
			return;
		}
		final String roHome = buildHomeUrl("ro");
		try {
			ArteEmacJson.parseTree(transport.get(roHome), roHome);
			languages.put("ro", "Română");
		} catch (final RuntimeException e) {
			// Romanian web edition not reachable from this network.
		}
	}

	private static boolean isCataloguePageCode(final String code) {
		if (StringUtils.isEmpty(code) || PAGE_CODE_BLOCKLIST.contains(code)) {
			return false;
		}
		if (code.startsWith("RC-") || code.startsWith("CATEGORY_") || code.startsWith("SUBCATEGORY_")) {
			return false;
		}
		if ("FAM".equals(code) || "ALL_VIDEOS".equals(code) || "all-videos".equals(code)) {
			return false;
		}
		return code.matches("[A-Z][A-Z0-9_]*");
	}

	static String buildHomeUrl(final String languageCode) {
		return ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/pages/HOME/?authorizedCountry="
				+ ArteConf.AUTHORIZED_COUNTRY;
	}

	static String resolvePublicSiteUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return url;
		}
		if (url.startsWith("/")) {
			return ArteConf.HOME_URL + url;
		}
		return ArteConf.HOME_URL + "/" + url;
	}

	static String buildPageUrl(final String languageCode, final String pageCode) {
		return ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/pages/" + pageCode + "/?authorizedCountry="
				+ ArteConf.AUTHORIZED_COUNTRY;
	}

	static final class ArteLanguage {
		private final String code;
		private final String label;

		ArteLanguage(final String code, final String label) {
			this.code = code;
			this.label = label;
		}

		String getCode() {
			return code;
		}

		String getLabel() {
			return label;
		}
	}

	static final class ArtePageRef {
		private final String code;
		private final String title;
		private final String sourceUrl;

		ArtePageRef(final String code, final String title, final String sourceUrl) {
			this.code = code;
			this.title = title;
			this.sourceUrl = sourceUrl;
		}

		String getCode() {
			return code;
		}

		String getTitle() {
			return title;
		}

		String getSourceUrl() {
			return sourceUrl;
		}
	}

	/**
	 * Indirection for network access (production plugin vs offline tests).
	 */
	interface ArteEmacTransport {
		String get(String url);
	}
}
