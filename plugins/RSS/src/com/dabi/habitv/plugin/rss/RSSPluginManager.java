package com.dabi.habitv.plugin.rss;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.StatusEnum;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RSSPluginManager extends BasePluginWithProxy implements PluginProviderInterface {

	private static final int MIN_TITLE_SIZE = 5;

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList;
		try {
			final SyndFeedInput input = new SyndFeedInput();
			final SyndFeed feed = input.build(new XmlReader(RetrieverUtils.getInputStreamFromUrl(category.getId(), 60000, getHttpProxy())));
			episodeList = convertFeedToEpisodeList(feed, category);
		} catch (IllegalArgumentException | FeedException | IOException e) {
			throw new TechnicalException(e);
		}
		return episodeList;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryList = new LinkedHashSet<>();
		categoryList.add(buildCategoryTemplate("Dailymotion",
		        "http://www.dailymotion.com/rss/user/§ID§/1!!Saisissez l'identifiant d'un utilisateur dailymotion"));
		categoryList.add(buildCategoryTemplate("RSS", "§ID§!!Saisissez l'URL d'un flux RSS"));
		categoryList.add(buildCategoryTemplate("Vimeo", "http://vimeo.com/§ID§/videos/rss!!Saisissez l'identifiant d'un utilisateur vimeo"));
		categoryList.add(buildCategoryTemplate("Youtube",
		        "https://www.youtube.com/feeds/videos.xml?user=§ID§!!Saisissez l'identifiant d'un utilisateur youtube"));
		return categoryList;
	}

	private CategoryDTO buildCategoryTemplate(String name, String id) {
		final CategoryDTO categoryDTO = new CategoryDTO(RSSConf.NAME, name, id, FrameworkConf.MP4);
		categoryDTO.setTemplate(true);
		categoryDTO.setDownloadable(false);
		categoryDTO.setState(StatusEnum.USER);
		return categoryDTO;
	}

	@Override
	public String getName() {
		return RSSConf.NAME;
	}

	private static Set<EpisodeDTO> convertFeedToEpisodeList(final SyndFeed feed, final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		final List<?> entries = feed.getEntries();
		if (!entries.isEmpty()) {
			for (final Object object : entries) {
				final SyndEntry entry = (SyndEntry) object;
				final List<?> enclosures = entry.getEnclosures();
				String url;
				if (!enclosures.isEmpty()) {
					url = ((SyndEnclosure) enclosures.get(0)).getUrl();
				} else {
					url = entry.getLink();
				}

				String safeTtitle = entry.getTitle().replaceAll("[^\\x00-\\x7F]", "").trim();
				if (safeTtitle.length() <= 5 && entry.getTitle().length() >= MIN_TITLE_SIZE) {
					safeTtitle = entry.getAuthor() + "-" + (new SimpleDateFormat("yyyyMMddHHmmss").format(entry.getPublishedDate()));
				}
				episodeList.add(new EpisodeDTO(category, safeTtitle, url));
			}
		}
		return episodeList;
	}

}
