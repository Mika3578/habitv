package com.dabi.habitv.provider.francetv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parses public France.tv collection pages (rubrique headings + card links) without
 * JavaScript execution.
 */
final class FranceTvPublicPageDiscovery {

	private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*min", Pattern.CASE_INSENSITIVE);

	private static final Pattern HOURS_MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*h\\s*(\\d+)\\s*min",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern CLOCK_PATTERN = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");

	private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");

	private static final Pattern FRENCH_DATE_PATTERN = Pattern
			.compile("\\b(\\d{1,2})\\s+(janv|févr|fevr|mars|avr|mai|juin|juil|août|aout|sept|oct|nov|déc|dec)\\.?\\s+(20\\d{2})\\b",
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern SLASH_DATE_PATTERN = Pattern.compile("\\b(\\d{2})/(\\d{2})/(20\\d{2})\\b");

	private static final Set<String> IGNORED_SPAN_TEXT = new HashSet<String>(Arrays.asList("Direct", "Chaine sport",
			"Passer cette liste", "France TV"));

	private FranceTvPublicPageDiscovery() {
	}

	static FranceTvDiscoveryResult discover(final String sourceUrl, final String html) {
		final FranceTvDiscoveryResult result = new FranceTvDiscoveryResult(sourceUrl);
		final FranceTvDiscoveryDiagnostics diagnostics = result.getDiagnostics();
		if (StringUtils.isEmpty(html)) {
			diagnostics.setRootCauseSummary("empty-page");
			return result;
		}
		try {
			final Document document = Jsoup.parse(html, sourceUrl);
			final String pageTitle = pageTitle(document);
			final FranceTvDiscoveryResult populated = new FranceTvDiscoveryResult(sourceUrl, pageTitle);
			final FranceTvDiscoveryDiagnostics activeDiagnostics = populated.getDiagnostics();
			final Elements sectionBlocks = document.select("div[data-testid^=test-]:has(h2)");
			activeDiagnostics.setHeadingsFound(sectionBlocks.size());
			if (sectionBlocks.isEmpty()) {
				activeDiagnostics.setRootCauseSummary("no-section-headings");
				return populated;
			}
			// Replay URLs are deduplicated per source page; the first section keeps the item.
			final Set<String> seenReplayUrls = new HashSet<String>();
			for (final Element sectionBlock : sectionBlocks) {
				final Element heading = sectionBlock.selectFirst("h2");
				final String label = heading == null ? "" : heading.text().trim();
				if (StringUtils.isEmpty(label)) {
					continue;
				}
				final String slug = sectionSlug(sectionBlock, label);
				final FranceTvDiscoverySection section = new FranceTvDiscoverySection(label, slug, sourceUrl);
				parseSectionCards(sectionBlock, section, activeDiagnostics, seenReplayUrls);
				if (!section.getItems().isEmpty()) {
					populated.addSection(section);
				}
			}
			activeDiagnostics.setCreatedCategories(populated.getSections().size());
			if (populated.getSections().isEmpty()) {
				activeDiagnostics.setRootCauseSummary("no-collection-content");
			}
			return populated;
		} catch (RuntimeException e) {
			diagnostics.setRootCauseSummary("parse-failed");
			return result;
		}
	}

	private static String pageTitle(final Document document) {
		final Element h1 = document.selectFirst("h1");
		if (h1 != null && StringUtils.isNotEmpty(h1.text())) {
			return h1.text().trim();
		}
		final String title = document.title();
		if (StringUtils.isNotEmpty(title)) {
			final int dash = title.indexOf(" - ");
			return dash > 0 ? title.substring(0, dash).trim() : title.trim();
		}
		return null;
	}

	private static String sectionSlug(final Element sectionBlock, final String labelFallback) {
		final String testId = sectionBlock.attr("data-testid");
		if (StringUtils.isNotEmpty(testId) && testId.startsWith("test-")) {
			return testId.substring("test-".length());
		}
		final String id = sectionBlock.id();
		if (StringUtils.isNotEmpty(id)) {
			return id;
		}
		return FranceTvUrls.slugifyPathSegment(labelFallback);
	}

	private static void parseSectionCards(final Element sectionBlock, final FranceTvDiscoverySection section,
			final FranceTvDiscoveryDiagnostics diagnostics, final Set<String> seenReplayUrls) {
		final Elements cards = sectionBlock.select("a[data-card-link=true][href]");
		for (final Element card : cards) {
			diagnostics.incrementLinksScanned();
			final String href = card.attr("href");
			final String absoluteUrl = FranceTvUrls.absoluteFranceTvUrl(href);
			if (StringUtils.isEmpty(absoluteUrl)) {
				diagnostics.incrementRejectedLinks();
				continue;
			}
			final String rejection = classifyRejection(absoluteUrl);
			final String title = cardTitle(card);
			if (StringUtils.isEmpty(title)) {
				diagnostics.incrementRejectedLinks();
				continue;
			}
			if (rejection != null) {
				diagnostics.incrementRejectedLinks();
				section.addItem(new FranceTvDiscoveryItem(title, absoluteUrl, parseDurationSeconds(card.text()),
						parsePublicationDate(card.text()), true, section.getSlug()));
				continue;
			}
			if (!FranceTvUrls.isVideoReplayUrl(absoluteUrl)) {
				diagnostics.incrementRejectedLinks();
				section.addItem(new FranceTvDiscoveryItem(title, absoluteUrl, parseDurationSeconds(card.text()),
						parsePublicationDate(card.text()), true, section.getSlug()));
				continue;
			}
			diagnostics.incrementCandidateItemLinks();
			if (!seenReplayUrls.add(absoluteUrl)) {
				diagnostics.incrementRejectedLinks();
				continue;
			}
			diagnostics.incrementCreatedReplayItems();
			section.addItem(new FranceTvDiscoveryItem(title, absoluteUrl, parseDurationSeconds(card.text()),
					parsePublicationDate(card.text()), false, section.getSlug()));
		}
	}

	static String classifyRejection(final String absoluteUrl) {
		if (!FranceTvUrls.isFranceTvHost(absoluteUrl)) {
			return "external-domain";
		}
		if (FranceTvUrls.isDirectStreamUrl(absoluteUrl)) {
			return "direct-stream";
		}
		return null;
	}

	static String cardTitle(final Element card) {
		String best = "";
		for (final Element span : card.select("span")) {
			final String text = span.ownText();
			if (StringUtils.isEmpty(text)) {
				continue;
			}
			final String trimmed = text.trim();
			if (IGNORED_SPAN_TEXT.contains(trimmed)) {
				continue;
			}
			if (trimmed.startsWith("Progression en cours")) {
				continue;
			}
			if (trimmed.length() > best.length()) {
				best = trimmed;
			}
		}
		if (StringUtils.isEmpty(best)) {
			final String aria = card.attr("aria-label");
			if (StringUtils.isNotEmpty(aria)) {
				best = aria.trim();
			}
		}
		return best;
	}

	static Long parseDurationSeconds(final String cardText) {
		if (StringUtils.isEmpty(cardText)) {
			return null;
		}
		final Matcher hoursMinutes = HOURS_MINUTES_PATTERN.matcher(cardText);
		if (hoursMinutes.find()) {
			final long hours = Long.parseLong(hoursMinutes.group(1));
			final long minutes = Long.parseLong(hoursMinutes.group(2));
			return Long.valueOf(hours * 3600L + minutes * 60L);
		}
		final Matcher minutes = MINUTES_PATTERN.matcher(cardText);
		if (minutes.find()) {
			return Long.valueOf(Long.parseLong(minutes.group(1)) * 60L);
		}
		final Matcher clock = CLOCK_PATTERN.matcher(cardText);
		if (clock.find()) {
			final int hoursOrMinutes = Integer.parseInt(clock.group(1));
			final int secondsPart = Integer.parseInt(clock.group(2));
			if (hoursOrMinutes < 24 && cardText.contains(" h ")) {
				return Long.valueOf(hoursOrMinutes * 3600L + secondsPart * 60L);
			}
			return Long.valueOf(hoursOrMinutes * 60L + secondsPart);
		}
		return null;
	}

	static Date parsePublicationDate(final String cardText) {
		if (StringUtils.isEmpty(cardText)) {
			return null;
		}
		final Matcher iso = ISO_DATE_PATTERN.matcher(cardText);
		if (iso.find()) {
			return parseIsoDate(iso.group(1));
		}
		final Matcher french = FRENCH_DATE_PATTERN.matcher(cardText);
		if (french.find()) {
			return parseFrenchDate(french.group(1), french.group(2), french.group(3));
		}
		final Matcher slashDate = SLASH_DATE_PATTERN.matcher(cardText);
		if (slashDate.find()) {
			try {
				return new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(
						slashDate.group(1) + "/" + slashDate.group(2) + "/" + slashDate.group(3));
			} catch (ParseException e) {
				return null;
			}
		}
		return null;
	}

	private static Date parseIsoDate(final String isoDate) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(isoDate);
		} catch (ParseException e) {
			return null;
		}
	}

	private static Date parseFrenchDate(final String day, final String monthToken, final String year) {
		final String month = frenchMonthNumber(monthToken);
		if (month == null) {
			return null;
		}
		try {
			return new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse(day + "-" + month + "-" + year);
		} catch (ParseException e) {
			return null;
		}
	}

	private static String frenchMonthNumber(final String monthToken) {
		final String normalized = monthToken.toLowerCase(Locale.ROOT);
		if (normalized.startsWith("janv")) {
			return "01";
		}
		if (normalized.startsWith("fevr") || normalized.startsWith("févr")) {
			return "02";
		}
		if (normalized.startsWith("mars")) {
			return "03";
		}
		if (normalized.startsWith("avr")) {
			return "04";
		}
		if (normalized.startsWith("mai")) {
			return "05";
		}
		if (normalized.startsWith("juin")) {
			return "06";
		}
		if (normalized.startsWith("juil")) {
			return "07";
		}
		if (normalized.startsWith("ao") || normalized.startsWith("aou")) {
			return "08";
		}
		if (normalized.startsWith("sept")) {
			return "09";
		}
		if (normalized.startsWith("oct")) {
			return "10";
		}
		if (normalized.startsWith("nov")) {
			return "11";
		}
		if (normalized.startsWith("dec") || normalized.startsWith("déc")) {
			return "12";
		}
		return null;
	}

}
