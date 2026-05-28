package com.dabi.habitv.provider.tf1plus;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
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
		Elements anchors = doc.select("a[href*=/replay/], a[data-testid*=replay], article a[href]");
		for (Element anchor : anchors) {
			String url = fullUrl(anchor.absUrl("href"));
			String label = extractEpisodeLabel(anchor);
			if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(label)) {
				CategoryDTO showCategory = new CategoryDTO(Tf1PlusConf.NAME, label, url, Tf1PlusConf.EXTENSION);
				showCategory.setDownloadable(true);
				subCategories.add(showCategory);
			}
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
			Elements entries = doc.select("article, li, div[class*=card], a[href*=/videos/], a[href*=/replay/]");
			for (Element entry : entries) {
				String url = fullUrl(entry.absUrl("href"));
				if (StringUtils.isEmpty(url)) {
					Element link = entry.selectFirst("a[href]");
					url = link == null ? "" : fullUrl(link.absUrl("href"));
				}
				String title = extractEpisodeLabel(entry);
				if (StringUtils.isEmpty(title)) {
					continue;
				}
				String thumbnail = entry.select("img[src]").attr("abs:src");
				String description = entry.select("p, [class*=description], [class*=summary]").text();
				String date = entry.select("time").attr("datetime");
				String duration = entry.select("[class*=duration], time[aria-label*=dur]").text();
				if (StringUtils.isNotEmpty(url)) {
					EpisodeDTO episode = new EpisodeDTO(category, title, url);
					Date episodeDate = parseEpisodeDate(date);
					if (episodeDate != null) {
						episode.setEpisodeDate(episodeDate);
					}
					Long durationSeconds = parseDurationSeconds(duration);
					if (durationSeconds != null) {
						episode.setDurationSeconds(durationSeconds);
					}
					if (StringUtils.isNotEmpty(description) || StringUtils.isNotEmpty(thumbnail)) {
						LOG.debug("TF1+ metadata ignored for filename generation, url=" + url);
					}
					episodes.add(episode);
				}
			}
		} catch (RuntimeException e) {
			LOG.warn("TF1+ replay parsing warning for " + category.getId() + ": " + e.getMessage());
		}
		return episodes;
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
		String title = node.select("h1, h2, h3, [class*=title], [data-testid*=title]").text();
		if (StringUtils.isEmpty(title)) {
			title = node.ownText();
		}
		return title == null ? "" : title.trim();
	}

	private String fullUrl(String url) {
		if (StringUtils.isEmpty(url)) {
			return "";
		}
		return url.startsWith("/") ? Tf1PlusConf.HOME_URL + url : url;
	}

	private static class Channel {
		private final String label;
		private final String replayUrl;

		private Channel(String label, String replayUrl) {
			this.label = label;
			this.replayUrl = replayUrl;
		}
	}
}
