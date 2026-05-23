package com.dabi.habitv.provider.canalplus;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class D8PluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	@Override
	public String getName() {
		return D8Conf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(getUrl(category), D8Conf.ENCODING));

		Elements select = doc.select(".list-programmes-emissions");

		if (!select.isEmpty()) {

			final Elements emission = select.get(0).children();
			for (final Element liElement : emission) {
				if (!liElement.children().isEmpty()) {
					final Element aLink = liElement.child(0);
					if (aLink.children().size() > 1 && aLink.child(1).children().size() > 0) {
						String id = aLink.attr("data-vid");
						final String title = aLink.child(1).child(0).text() + " - " + aLink.child(1).child(1).text() + "-" + id;
						final String url = aLink.attr(getAttrName(aLink));
						episodes.add(new EpisodeDTO(category, title, url));
					}
				}
			}
		}

		select = doc.select(".block-common ");
		if (!select.isEmpty()) {

			for (final Element block : select) {
				if (block.children().size() > 1) {
					final Elements emission = block.select("a");
					for (final Element aLink : emission) {
						if (aLink.children().size() > 2) {
							String id = aLink.attr("data-vid");
							final String title = aLink.child(1).text() + " - " + aLink.child(2).text() + id;
							final String url = aLink.attr(getAttrName(aLink));
							if (url != null) {
								episodes.add(new EpisodeDTO(category, title, url));
							}
						}
					}
				}
			}
		}
		return episodes;
	}

	private String getUrl(final CategoryDTO category) {
		return DownloadUtils.isHttpUrl(category.getId()) ? category.getId() : (D8Conf.HOME_URL + category.getId());
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		try {
			final Set<CategoryDTO> categories = new LinkedHashSet<>();
			final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(D8Conf.HOME_URL, D8Conf.ENCODING));
			final Elements select = doc.select("#nav").get(0).child(0).children();
			for (final Element liElement : select) {
				final Element aElement = liElement.child(0);
				final String url = aElement.attr("href");
				final String name = aElement.text();
				final CategoryDTO categoryDTO = new CategoryDTO(D8Conf.NAME, name, url, D8Conf.EXTENSION);
				categoryDTO.addSubCategories(findSubCategories(url));
				categories.add(categoryDTO);
			}
			return categories;
		} catch (RuntimeException e) {
			if (CanalPlusEndpointAvailability.isUnavailable(e)) {
				getLog().warn(CanalPlusEndpointAvailability.buildCategoryUnavailableMessage(getName(), e));
				return new LinkedHashSet<>();
			}
			throw e;
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		return CanalUtils.doDownload(downloadParam, downloaders, this, D8Conf.VIDEO_INFO_URL, getName().toLowerCase());
	}

	private static String getAttrName(final Element aLink) {
		String attr;
		if (aLink.hasAttr("href")) {
			attr = "href";
		} else {
			attr = "data-href";
		}
		return attr;
	}

	private Collection<CategoryDTO> findSubCategories(final String catUrl) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		final org.jsoup.nodes.Document doc = Jsoup.parse(getFullUrl(catUrl));
		final Elements tpGrid = doc.select(".tp-grid");
		if (!tpGrid.isEmpty()) {
			final Elements select = tpGrid.get(0).children();
			for (final Element divElement : select) {
				for (final Element subDivElement : divElement.children()) {
					if (subDivElement.children().size() > 0) {
						final Element aElement = subDivElement.child(0);
						final String url = aElement.attr("href");
						final String name = aElement.child(1).text();
						final CategoryDTO categoryDTO = new CategoryDTO(D8Conf.NAME, name, url, D8Conf.EXTENSION);
						categoryDTO.setDownloadable(true);
						categories.add(categoryDTO);
					}
				}
			}
		}
		return categories;
	}

	private String getFullUrl(final String catUrl) {
		return DownloadUtils.isHttpUrl(catUrl) ? catUrl : getUrlContent(D8Conf.HOME_URL + catUrl, D8Conf.ENCODING);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.startsWith(D8Conf.HOME_URL) ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
