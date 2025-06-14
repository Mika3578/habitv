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
					episodes.add(new EpisodeDTO(category, name, getFullUrl(href)));
				}
			}
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final Document doc = Jsoup.parse(getUrlContent(SixPlayConf.HOME_URL));

		// Try multiple selectors for main categories
		Elements mainCatElements = doc.select(".folders__list a");
		if (mainCatElements.isEmpty()) {
			// Try alternative selectors
			mainCatElements = doc.select("nav a, .navigation a, .menu a, .categories a");
		}
		if (mainCatElements.isEmpty()) {
			// Try even more generic selectors
			mainCatElements = doc.select("a[href*='/']");
		}

		for (final Element mainCatA : mainCatElements) {
			String href = mainCatA.attr("href");
			if (href != null && href.length() > 5 && !href.startsWith("#") && !href.startsWith("javascript:")) {
				String channel = mainCatA.text().trim();
				if (!channel.isEmpty() && channel.length() > 1) {
					CategoryDTO channelCat = new CategoryDTO(SixPlayConf.NAME, channel, getFullUrl(href), SixPlayConf.EXTENSION);
					channelCat.addSubCategories(findCategoryByMainCat(getFullUrl(href)));
					categories.add(channelCat);
				}
			}
		}

		return categories;
	}

	private Collection<CategoryDTO> findCategoryByMainCat(String mainCatUrl) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(mainCatUrl));
		
		// Try multiple selectors for sub-categories
		Elements aCatElements = doc.select(".mosaic-programs a");
		if (aCatElements.isEmpty()) {
			// Try alternative selectors
			aCatElements = doc.select(".programs a, .shows a, .content a, .videos a");
		}
		if (aCatElements.isEmpty()) {
			// Try even more generic selectors
			aCatElements = doc.select("a[href*='/']");
		}
		
		for (Element aCat : aCatElements) {
			Elements title = aCat.select(".tile__title");
			if (title.isEmpty()) {
				// Try alternative title selectors
				title = aCat.select("h1, h2, h3, h4, .title, .name");
			}
			if (title.size() > 0) {
				String name = title.first().text().trim();
				if (!name.isEmpty()) {
					String href = aCat.attr("href");
					if (href != null && !href.isEmpty()) {
						CategoryDTO catCat = new CategoryDTO(SixPlayConf.NAME, name, getFullUrl(href), SixPlayConf.EXTENSION);
						catCat.setDownloadable(true);
						categories.add(catCat);
					}
				}
			}
		}
		return categories;
	}

	private String getFullUrl(String url) {
		return url.startsWith("/") ? (SixPlayConf.HOME_URL + url) : url;
	}

}
