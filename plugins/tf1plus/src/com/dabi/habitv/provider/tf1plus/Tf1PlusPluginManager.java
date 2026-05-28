package com.dabi.habitv.provider.tf1plus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

public class Tf1PlusPluginManager extends BasePluginWithProxy implements PluginProviderInterface { // NO_UCD

	private static final Logger LOG = Logger.getLogger(Tf1PlusPluginManager.class);
	private static final Pattern DURATION_HHMMSS_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{2})$");
	private static final Pattern DURATION_TEXT_PATTERN = Pattern.compile("(\\d+)\\s*(h|mn|min|s)");
	private static final Pattern TITLE_DATE_PATTERN = Pattern.compile("\\bdu\\s+(?:[a-zéû]+\\s+)?(\\d{1,2})\\s+([a-zéû]+)\\s+(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
	private static final Set<String> EXCLUDED_PROGRAM_SLUGS = new HashSet<>(Arrays.asList("replay", "videos", "news", "direct", "programme-tv", "recherche", "compte", "mentions-legales", "conditions-generales", "abonnement"));
	private static final Set<String> EXCLUDED_EPISODE_LABELS = new HashSet<>(Arrays.asList("regarder", "voir plus", "se connecter", "mon compte", "s'abonner"));
	private static final Map<String, Integer> FRENCH_MONTHS = createFrenchMonths();

	private static final List<Channel> CHANNELS = Arrays.asList(new Channel("TF1", Tf1PlusConf.TF1_REPLAY_URL),
			new Channel("TMC", Tf1PlusConf.TMC_REPLAY_URL), new Channel("TFX", Tf1PlusConf.TFX_REPLAY_URL),
			new Channel("TF1 Séries Films", Tf1PlusConf.TF1_SERIES_FILMS_REPLAY_URL), new Channel("LCI", Tf1PlusConf.LCI_REPLAY_URL));

	@Override
	public String getName() {
		return Tf1PlusConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		Set<CategoryDTO> categories = new LinkedHashSet<>();
		for (Channel channel : CHANNELS) {
			CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, channel.label, channel.replayUrl, Tf1PlusConf.EXTENSION);
			category.setDownloadable(false);
			try {
				category.addSubCategories(findReplayEntries(channel));
			} catch (RuntimeException e) {
				LOG.warn("TF1+ parsing warning for " + channel.label + ": " + e.getMessage());
			}
			categories.add(category);
		}
		return categories;
	}

	private Set<CategoryDTO> findReplayEntries(Channel channel) {
		Set<CategoryDTO> subCategories = new LinkedHashSet<>();
		String content = getUrlContent(channel.replayUrl);
		if (StringUtils.isEmpty(content)) {
			LOG.warn("TF1+ channel unavailable: " + channel.label);
			return subCategories;
		}
		Document doc = Jsoup.parse(content, channel.replayUrl);
		Elements anchors = doc.select("a[href]");
		int scannedAnchors = 0;
		int candidateProgramLinks = 0;
		int rejectedLinks = 0;
		String channelSlug = extractChannelSlug(channel.replayUrl);
		Set<String> seenUrls = new HashSet<>();
		for (Element anchor : anchors) {
			scannedAnchors++;
			String url = normalizeProgramUrl(anchor.absUrl("href"));
			if (StringUtils.isEmpty(url)) {
				rejectedLinks++;
				continue;
			}
			candidateProgramLinks++;
			if (!isProgramUrlForChannel(url, channelSlug) || seenUrls.contains(url)) {
				rejectedLinks++;
				continue;
			}
			String label = extractProgramLabel(anchor);
			if (StringUtils.isNotEmpty(label)) {
				seenUrls.add(url);
				CategoryDTO showCategory = new CategoryDTO(Tf1PlusConf.NAME, label, url, Tf1PlusConf.EXTENSION);
				showCategory.setDownloadable(true);
				subCategories.add(showCategory);
			} else {
				rejectedLinks++;
			}
		}
		LOG.debug("TF1+ channel " + channel.label + " scanned anchors=" + scannedAnchors + ", candidates=" + candidateProgramLinks + ", rejected=" + rejectedLinks + ", programs=" + subCategories.size());
		if (subCategories.isEmpty()) {
			LOG.debug("TF1+ no program categories found for channel url: " + channel.replayUrl);
		}
		return subCategories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(CategoryDTO category) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		try {
			String content = getUrlContent(category.getId());
			if (StringUtils.isEmpty(content)) {
				LOG.warn("TF1+ empty replay payload for: " + category.getId());
				return episodes;
			}
			Document doc = Jsoup.parse(content, category.getId());
			Elements anchors = doc.select("a[href*=/videos/]");
			for (Element anchor : anchors) {
				String url = normalizeEpisodeUrl(anchor.absUrl("href"));
				if (StringUtils.isEmpty(url)) {
					continue;
				}
				Element entry = findEpisodeContainer(anchor);
				String title = extractEpisodeLabel(anchor);
				if (StringUtils.isEmpty(title)) {
					title = extractEpisodeLabel(entry);
				}
				if (!isValidEpisodeLabel(title)) {
					continue;
				}
				String date = extractEpisodeDateValue(entry);
				String duration = extractEpisodeDurationValue(entry);
				EpisodeDTO episode = new EpisodeDTO(category, title, url);
				Date episodeDate = parseEpisodeDate(date);
				if (episodeDate == null) {
					episodeDate = parseEpisodeDateFromTitle(title);
				}
				if (episodeDate != null) {
					episode.setEpisodeDate(episodeDate);
				}
				Long durationSeconds = parseDurationSeconds(duration);
				if (durationSeconds != null) {
					episode.setDurationSeconds(durationSeconds);
				}
				episodes.add(episode);
			}
			LOG.debug("TF1+ parsed episodes for " + category.getId() + ": " + episodes.size());
			if (episodes.isEmpty()) {
				Elements entries = doc.select("article, li, div[class*=card]");
				for (Element entry : entries) {
					Element link = entry.selectFirst("a[href*=/videos/]");
					String url = link == null ? "" : normalizeEpisodeUrl(link.absUrl("href"));
					String title = extractEpisodeLabel(entry);
					if (StringUtils.isEmpty(url) || !isValidEpisodeLabel(title)) {
						continue;
					}
					EpisodeDTO episode = new EpisodeDTO(category, title, url);
					Date episodeDate = parseEpisodeDate(extractEpisodeDateValue(entry));
					if (episodeDate == null) {
						episodeDate = parseEpisodeDateFromTitle(title);
					}
					if (episodeDate != null) {
						episode.setEpisodeDate(episodeDate);
					}
					Long durationSeconds = parseDurationSeconds(extractEpisodeDurationValue(entry));
					if (durationSeconds != null) {
						episode.setDurationSeconds(durationSeconds);
					}
					episodes.add(episode);
				}
			}
		} catch (RuntimeException e) {
			LOG.warn("TF1+ replay parsing warning for " + category.getId() + ": " + e.getMessage());
		}
		return episodes;
	}

	private String extractChannelSlug(String channelUrl) {
		if (StringUtils.isEmpty(channelUrl)) {
			return "";
		}
		String normalized = channelUrl.toLowerCase(Locale.ROOT);
		if (normalized.startsWith(Tf1PlusConf.HOME_URL)) {
			normalized = normalized.substring(Tf1PlusConf.HOME_URL.length());
		}
		String[] parts = normalized.split("/");
		for (String part : parts) {
			if (StringUtils.isNotEmpty(part)) {
				return part;
			}
		}
		return "";
	}

	private String normalizeProgramUrl(String href) {
		if (StringUtils.isEmpty(href) || !href.startsWith(Tf1PlusConf.HOME_URL)) {
			return "";
		}
		String clean = href.split("\\?")[0].split("#")[0];
		return clean.endsWith("/") ? clean.substring(0, clean.length() - 1) : clean;
	}

	private boolean isProgramUrlForChannel(String url, String channelSlug) {
		if (StringUtils.isEmpty(url) || StringUtils.isEmpty(channelSlug) || !url.startsWith(Tf1PlusConf.HOME_URL + "/" + channelSlug + "/")) {
			return false;
		}
		String path = url.substring((Tf1PlusConf.HOME_URL + "/" + channelSlug + "/").length());
		if (StringUtils.isEmpty(path) || path.contains("/")) {
			return false;
		}
		String slug = path.toLowerCase(Locale.ROOT);
		return !EXCLUDED_PROGRAM_SLUGS.contains(slug);
	}

	private String extractProgramLabel(Element anchor) {
		String label = extractEpisodeLabel(anchor);
		if (StringUtils.isEmpty(label)) {
			label = anchor.attr("title");
		}
		if (StringUtils.isEmpty(label)) {
			label = anchor.attr("aria-label");
		}
		if (StringUtils.isEmpty(label)) {
			Element img = anchor.selectFirst("img[alt], img[title]");
			if (img != null) {
				label = StringUtils.isNotEmpty(img.attr("alt")) ? img.attr("alt") : img.attr("title");
			}
		}
		return normalizeLabel(label);
	}

	private String normalizeLabel(String value) {
		if (StringUtils.isEmpty(value)) {
			return "";
		}
		String normalized = value.replaceAll("\\s+", " ").trim();
		return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
	}

	private String normalizeEpisodeUrl(String href) {
		if (StringUtils.isEmpty(href) || !href.startsWith(Tf1PlusConf.HOME_URL) || !href.contains("/videos/")) {
			return "";
		}
		String clean = href.split("\\?")[0].split("#")[0];
		String lower = clean.toLowerCase(Locale.ROOT);
		if (lower.contains("/compte/") || lower.contains("/recherche") || lower.endsWith("/videos")) {
			return "";
		}
		return clean.endsWith("/") ? clean.substring(0, clean.length() - 1) : clean;
	}

	private Element findEpisodeContainer(Element anchor) {
		Element container = anchor.closest("article, li, div[class*=card]");
		return container == null ? anchor : container;
	}

	private String extractEpisodeDateValue(Element entry) {
		String date = entry.select("time[datetime]").attr("datetime");
		if (StringUtils.isNotEmpty(date)) {
			return date;
		}
		return entry.select("[data-date], [datetime]").attr("data-date");
	}

	private String extractEpisodeDurationValue(Element entry) {
		String duration = entry.select("[class*=duration], time[aria-label*=dur]").text();
		if (StringUtils.isNotEmpty(duration)) {
			return duration;
		}
		return entry.select("[data-duration]").attr("data-duration");
	}

	private boolean isValidEpisodeLabel(String title) {
		if (StringUtils.isEmpty(title)) {
			return false;
		}
		String normalized = title.toLowerCase(Locale.ROOT).trim();
		return !EXCLUDED_EPISODE_LABELS.contains(normalized);
	}

	private Date parseEpisodeDate(String rawDate) {
		if (StringUtils.isEmpty(rawDate)) {
			return null;
		}
		String normalized = rawDate.trim();
		if (normalized.length() >= 10) {
			normalized = normalized.substring(0, 10);
		}
		String[] parts = normalized.split("-");
		if (parts.length != 3) {
			return null;
		}
		try {
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);
			int day = Integer.parseInt(parts[2]);
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.setLenient(false);
			calendar.set(java.util.Calendar.YEAR, year);
			calendar.set(java.util.Calendar.MONTH, month - 1);
			calendar.set(java.util.Calendar.DAY_OF_MONTH, day);
			calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
			calendar.set(java.util.Calendar.MINUTE, 0);
			calendar.set(java.util.Calendar.SECOND, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			return calendar.getTime();
		} catch (RuntimeException e) {
			LOG.debug("TF1+ unable to parse episode date: " + rawDate);
			return null;
		}
	}

	private Date parseEpisodeDateFromTitle(String title) {
		if (StringUtils.isEmpty(title)) {
			return null;
		}
		Matcher matcher = TITLE_DATE_PATTERN.matcher(title.toLowerCase(Locale.ROOT));
		if (!matcher.find()) {
			return null;
		}
		try {
			int day = Integer.parseInt(matcher.group(1));
			Integer month = FRENCH_MONTHS.get(matcher.group(2));
			int year = Integer.parseInt(matcher.group(3));
			if (month == null) {
				return null;
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setLenient(false);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month.intValue() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTime();
		} catch (RuntimeException e) {
			LOG.debug("TF1+ unable to parse date from title: " + title);
			return null;
		}
	}

	private Long parseDurationSeconds(String rawDuration) {
		if (StringUtils.isEmpty(rawDuration)) {
			return null;
		}
		String normalized = rawDuration.trim().toLowerCase();
		Matcher hhmmssMatcher = DURATION_HHMMSS_PATTERN.matcher(normalized);
		if (hhmmssMatcher.matches()) {
			long hours = Long.parseLong(hhmmssMatcher.group(1));
			long minutes = Long.parseLong(hhmmssMatcher.group(2));
			long seconds = Long.parseLong(hhmmssMatcher.group(3));
			return Long.valueOf(hours * 3600L + minutes * 60L + seconds);
		}
		Matcher textMatcher = DURATION_TEXT_PATTERN.matcher(normalized);
		long total = 0L;
		boolean found = false;
		while (textMatcher.find()) {
			found = true;
			long value = Long.parseLong(textMatcher.group(1));
			String unit = textMatcher.group(2);
			if ("h".equals(unit)) {
				total += value * 3600L;
			} else if ("mn".equals(unit) || "min".equals(unit)) {
				total += value * 60L;
			} else if ("s".equals(unit)) {
				total += value;
			}
		}
		return found ? Long.valueOf(total) : null;
	}

	private String extractEpisodeLabel(Element node) {
		String title = node.select("h1, h2, h3, h4, [class*=title], [data-testid*=title]").text();
		if (StringUtils.isEmpty(title)) {
			title = node.attr("title");
		}
		if (StringUtils.isEmpty(title)) {
			title = node.attr("aria-label");
		}
		if (StringUtils.isEmpty(title)) {
			title = node.ownText();
		}
		return normalizeLabel(title);
	}

	private static class Channel {
		private final String label;
		private final String replayUrl;

		private Channel(String label, String replayUrl) {
			this.label = label;
			this.replayUrl = replayUrl;
		}
	}

	private static Map<String, Integer> createFrenchMonths() {
		Map<String, Integer> months = new HashMap<>();
		months.put("janvier", Integer.valueOf(1));
		months.put("fevrier", Integer.valueOf(2));
		months.put("février", Integer.valueOf(2));
		months.put("mars", Integer.valueOf(3));
		months.put("avril", Integer.valueOf(4));
		months.put("mai", Integer.valueOf(5));
		months.put("juin", Integer.valueOf(6));
		months.put("juillet", Integer.valueOf(7));
		months.put("aout", Integer.valueOf(8));
		months.put("août", Integer.valueOf(8));
		months.put("septembre", Integer.valueOf(9));
		months.put("octobre", Integer.valueOf(10));
		months.put("novembre", Integer.valueOf(11));
		months.put("decembre", Integer.valueOf(12));
		months.put("décembre", Integer.valueOf(12));
		return months;
	}
}
