package com.dabi.habitv.provider.canalplus;

import java.util.Collection;
import java.util.HashSet;
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
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class CStarPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	@Override
	public String getName() {
		return CStarConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final Set<String> episodesNames = new HashSet<>();

		final String categoryId = category.getId();
		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(categoryUrl(categoryId), CStarConf.ENCODING));

		final Elements select = doc.select("a.loop-videos");
		for (final Element aVideoElement : select) {
			try {
				final String maintitle = aVideoElement.select("h4").first().text();
				final String subtitle = aVideoElement.select("p").first().text();
				final String title = maintitle + " - " + subtitle;
				String hrefRelative = aVideoElement.attr("href");
				if (!episodesNames.contains(title)) {
					episodes.add(new EpisodeDTO(category, title, CStarConf.HOME_URL + hrefRelative));
					episodesNames.add(title);
				}
			} catch (final IndexOutOfBoundsException e) {
				throw new TechnicalException(e);
			}
		}

		return episodes;
	}

	private String categoryUrl(final String categoryId) {
		return DownloadUtils.isHttpUrl(categoryId) ? categoryId : CStarConf.HOME_URL + categoryId;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		try {
			final Set<CategoryDTO> categories = new LinkedHashSet<>();
			final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(CStarConf.HOME_URL, CStarConf.ENCODING));
			final Elements select = doc.select(".main-menu").get(0).children();
			for (final Element liElement : select) {
				final Element aElement = liElement.child(0);
				final String url = aElement.attr("href");
				final String name = aElement.text();
				final CategoryDTO categoryDTO = new CategoryDTO(CStarConf.NAME, name, url, CStarConf.EXTENSION);
				categoryDTO.addSubCategories(findSubCategories(url));
				categories.add(categoryDTO);
			}
			return categories;
		} catch (RuntimeException e) {
			if (CanalPlusEndpointAvailability.isUnavailable(e)) {
				getLog().warn(CanalPlusEndpointAvailability.buildCategoryUnavailableMessage(getName()), e);
				return new LinkedHashSet<>();
			}
			throw e;
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		return CanalUtils.doDownload(downloadParam, downloaders, this, CStarConf.VIDEO_INFO_URL, getName().toLowerCase());
	}

	private Collection<CategoryDTO> findSubCategories(final String catUrl) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(categoryUrl(catUrl), CStarConf.ENCODING));
		final Elements select = doc.select(".block-videos li a");
		for (final Element link : select) {
			final String url = link.attr("href");
			final String name = link.select("h4").first().text();
			final CategoryDTO categoryDTO = new CategoryDTO(CStarConf.NAME, name, url, CStarConf.EXTENSION);
			categoryDTO.setDownloadable(true);
			categories.add(categoryDTO);
		}
		return categories;
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.startsWith(CStarConf.HOME_URL) ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
