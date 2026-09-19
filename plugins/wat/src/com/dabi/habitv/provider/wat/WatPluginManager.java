package com.dabi.habitv.provider.wat;

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

public class WatPluginManager extends BasePluginWithProxy implements PluginProviderInterface { // NO_UCD

	@Override
	public String getName() {
		return WatConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();

		Document doc = Jsoup.parse(getUrlContent(category.getId()));
		for (Element aEp : doc.select("section.list_videos div.content ul li div.description a")) {
			String url = getFullUrl(aEp.attr("href"));
			final String name = aEp.select("div.text p.title").text();
			if (!StringUtils.isEmpty(name)) {
				episodes.add(new EpisodeDTO(category, name, getFullUrl(url)));
			}
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final Document doc = Jsoup.parse(getUrlContent(WatConf.PROGRAMME_URL));

		final Elements aChannels = doc.select("ul.mobile_channel li.mobile_channel_item a");

		for (final Element aChannel : aChannels) {
			String href = aChannel.attr("href");
			if (href != null && href.length() > 5) {
				String channel = aChannel.text();
				CategoryDTO channelCat = new CategoryDTO(WatConf.NAME, channel, href, WatConf.EXTENSION);
				channelCat.addSubCategories(findCategoryByChannel(href));
				categories.add(channelCat);
			}
		}

		if (categories.isEmpty()) {
			getLog().warn("provider=wat operation=catalogue sourceUrl=" + WatConf.PROGRAMME_URL
					+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
					+ " note=tf1-html-no-longer-matches-legacy-wat-scraper");
		}

		return categories;
	}

	private Collection<CategoryDTO> findCategoryByChannel(String href) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(href));
		final Elements aCats = doc.select("section#filter_section ul.filters_2 .link a");
		for (Element aCat : aCats) {
			CategoryDTO catCat = new CategoryDTO(WatConf.NAME, aCat.text(), aCat.attr("data-target"), WatConf.EXTENSION);
			catCat.addSubCategories(findCategoryByCat(doc, catCat.getId()));
			categories.add(catCat);
		}
		return categories;
	}

	private Collection<CategoryDTO> findCategoryByCat(Document doc, String id) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		final Elements aShows;
		if ("all".equals(id)) {
			aShows = doc.select("div.content li div.description a");
		} else {
			aShows = doc.select("div.content li[data-type=" + id + "] div.description a");
		}

		for (Element aShow : aShows) {
			CategoryDTO catCat = new CategoryDTO(WatConf.NAME, aShow.child(0).child(0).text(), getFullUrl(aShow.attr("href")), WatConf.EXTENSION);
			catCat.setDownloadable(true);
			categories.add(catCat);
		}
		return categories;
	}

	private String getFullUrl(String url) {
		return url.startsWith("/") ? (WatConf.HOME_URL + url) : url;
	}

}
