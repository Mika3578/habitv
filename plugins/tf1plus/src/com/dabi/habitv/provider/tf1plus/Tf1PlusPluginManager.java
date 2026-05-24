package com.dabi.habitv.provider.tf1plus;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
				StringBuilder label = new StringBuilder(title);
				appendMetadata(label, description);
				appendMetadata(label, date);
				appendMetadata(label, duration);
				appendMetadata(label, thumbnail);
				if (StringUtils.isNotEmpty(url)) {
					episodes.add(new EpisodeDTO(category, label.toString(), url));
				}
			}
		} catch (RuntimeException e) {
			LOG.warn("TF1+ replay parsing warning for " + category.getId() + ": " + e.getMessage());
		}
		return episodes;
	}

	private void appendMetadata(StringBuilder label, String value) {
		if (StringUtils.isNotEmpty(value)) {
			label.append(" | ").append(value.trim());
		}
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
