package com.dabi.habitv.provider.sixplay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

public class SixPlayPluginManager extends BasePluginWithProxy implements PluginProviderInterface { // NO_UCD

	@Override
	public String getName() {
		return SixPlayConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();

		Document doc = Jsoup.parse(getUrlContent(category.getId()));
		for (Element aEp : doc.select(".tvshow-bloc__content ul li a")) {
			String href = getFullUrl(aEp.attr("href"));
			Elements titles = aEp.select(".tile__name");
			if (!titles.isEmpty()) {
				final String name = titles.first().text();
				if (!StringUtils.isEmpty(name)) {
					final String episodeUrl = getFullUrl(href);
					final EpisodeDTO episode = new EpisodeDTO(category, name, episodeUrl);
					// 6play program categories map to show/program; tile__name is episode.
					final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
					if (category.getName() != null && !category.getName().trim().isEmpty()) {
						metadata.setSeriesTitle(category.getName().trim());
					}
					metadata.setEpisodeTitle(name.trim());
					metadata.setSourceUrl(episodeUrl);
					if (category.getFatherCategory() != null
							&& StringUtils.isNotEmpty(category.getFatherCategory().getName())) {
						metadata.setChannel(category.getFatherCategory().getName().trim());
					}
					episode.setMetadata(metadata);
					episodes.add(episode);
				}
			}
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final Document doc = Jsoup.parse(getUrlContent(SixPlayConf.HOME_URL));

		for (final Element mainCatA : doc.select(".folders__list a")) {
			String href = mainCatA.attr("href");
			if (href != null && href.length() > 5) {
				String channel = mainCatA.text();
				CategoryDTO channelCat = new CategoryDTO(SixPlayConf.NAME, channel, getFullUrl(href), SixPlayConf.EXTENSION);
				channelCat.addSubCategories(findCategoryByMainCat(getFullUrl(href)));
				categories.add(channelCat);
			}
		}

		return categories;
	}

	private Collection<CategoryDTO> findCategoryByMainCat(String mainCatUrl) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(mainCatUrl));
		for (Element aCat : doc.select(".mosaic-programs a")) {
			Elements title = aCat.select(".tile__title");
			if (title.size() > 0) {
				String name = title.first().text();
				String href = aCat.attr("href");
				CategoryDTO catCat = new CategoryDTO(SixPlayConf.NAME, name, getFullUrl(href), SixPlayConf.EXTENSION);
				catCat.setDownloadable(true);
				categories.add(catCat);
			}
		}
		return categories;
	}

	private String getFullUrl(String url) {
		return url.startsWith("/") ? (SixPlayConf.HOME_URL + url) : url;
	}

}
