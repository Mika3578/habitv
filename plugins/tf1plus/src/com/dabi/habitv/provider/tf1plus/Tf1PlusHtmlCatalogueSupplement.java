package com.dabi.habitv.provider.tf1plus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * HTML replay-page supplement when GraphQL programme discovery returns no entries for a hub.
 */
final class Tf1PlusHtmlCatalogueSupplement {

	interface UrlContentProvider {
		String getUrlContent(String url);
	}

	private final UrlContentProvider urlContentProvider;

	Tf1PlusHtmlCatalogueSupplement(final UrlContentProvider urlContentProvider) {
		this.urlContentProvider = urlContentProvider;
	}

	List<Tf1PlusCatalogueEntry> discoverProgrammes(final Tf1PlusHubDescriptor hub) {
		final List<Tf1PlusCatalogueEntry> entries = new ArrayList<Tf1PlusCatalogueEntry>();
		if (hub == null || urlContentProvider == null) {
			return entries;
		}
		final String content = urlContentProvider.getUrlContent(hub.getReplayUrl());
		if (StringUtils.isEmpty(content)) {
			return entries;
		}
		final Document doc = Jsoup.parse(content, hub.getReplayUrl());
		final Elements anchors = doc.select("a[href]");
		final Set<String> seenUrls = new HashSet<String>();
		for (final Element anchor : anchors) {
			final String url = normalizeProgramUrl(anchor.absUrl("href"));
			if (StringUtils.isEmpty(url) || !isProgramUrlForChannel(url, hub.getUrlSlug())
					|| !seenUrls.add(url)) {
				continue;
			}
			final String label = extractProgramLabel(anchor);
			if (StringUtils.isEmpty(label)) {
				continue;
			}
			final String slug = extractProgramSlug(url, hub.getUrlSlug());
			if (Tf1PlusCatalogueClient.isExcludedProgrammeSlug(slug)) {
				continue;
			}
			entries.add(new Tf1PlusCatalogueEntry("", slug, label, hub.getHubId(), hub.getUrlSlug(), url,
					java.util.Collections.<String>emptyList(), java.util.Collections.<String>emptyList(), ""));
		}
		return entries;
	}

	private static boolean isProgramUrlForChannel(final String url, final String channelSlug) {
		if (StringUtils.isEmpty(url) || StringUtils.isEmpty(channelSlug)) {
			return false;
		}
		final String prefix = Tf1PlusConf.HOME_URL + "/" + channelSlug + "/";
		if (!url.startsWith(prefix)) {
			return false;
		}
		final String remainder = url.substring(prefix.length());
		return remainder.indexOf('/') < 0 && remainder.indexOf('?') < 0 && !remainder.isEmpty();
	}

	private static String extractProgramSlug(final String url, final String channelSlug) {
		final String prefix = Tf1PlusConf.HOME_URL + "/" + channelSlug + "/";
		if (!url.startsWith(prefix)) {
			return "";
		}
		return url.substring(prefix.length());
	}

	private static String normalizeProgramUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return "";
		}
		int hashIndex = url.indexOf('#');
		String normalized = hashIndex >= 0 ? url.substring(0, hashIndex) : url;
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	private static String extractProgramLabel(final Element anchor) {
		String title = anchor.select("h1, h2, h3, h4, [class*=title], [data-testid*=title]").text();
		if (StringUtils.isEmpty(title)) {
			title = anchor.attr("title");
		}
		if (StringUtils.isEmpty(title)) {
			title = anchor.attr("aria-label");
		}
		if (StringUtils.isEmpty(title)) {
			title = anchor.ownText();
		}
		return normalizeLabel(title);
	}

	private static String normalizeLabel(final String raw) {
		if (raw == null) {
			return "";
		}
		return raw.replaceAll("\\s+", " ").trim();
	}

}
