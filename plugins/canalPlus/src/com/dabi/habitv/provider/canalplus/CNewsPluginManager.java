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
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class CNewsPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	private static final String CANALPLUS_HOST = "https://www.canalplus.com";

	@Override
	public String getName() {
		return CNewsConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final Set<String> seenTitles = new HashSet<>();

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(categoryUrl(category.getId()), CNewsConf.ENCODING));

		final Elements links = doc.select("a[href*='/cnews/']");
		for (final Element link : links) {
			final String title = extractTitle(link);
			if (title.isEmpty() || !seenTitles.add(title)) {
				continue;
			}
			final String href = absoluteUrl(link.attr("href"));
			episodes.add(new EpisodeDTO(category, title, href));
		}

		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(CNewsConf.HOME_URL, CNewsConf.ENCODING));

		final Elements navLinks = doc.select("nav a[href*='/cnews/']");
		final Set<String> seenIds = new HashSet<>();
		for (final Element aElement : navLinks) {
			final String href = aElement.attr("href");
			final String name = aElement.text().trim();
			if (name.isEmpty() || !seenIds.add(href)) {
				continue;
			}
			final CategoryDTO categoryDTO = new CategoryDTO(CNewsConf.NAME, name, absoluteUrl(href), CNewsConf.EXTENSION);
			categoryDTO.setDownloadable(true);
			categoryDTO.addSubCategories(findSubCategories(href));
			categories.add(categoryDTO);
		}

		return categories;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		return CanalUtils.doDownload(downloadParam, downloaders, this, CNewsConf.VIDEO_INFO_URL, getName().toLowerCase());
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput == null) {
			return DownloadableState.IMPOSSIBLE;
		}
		if (downloadInput.contains("vid=")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private Collection<CategoryDTO> findSubCategories(final String catUrl) {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(categoryUrl(catUrl), CNewsConf.ENCODING));
		final Elements links = doc.select(".sub-category a, .program-list a");
		final Set<String> seenIds = new HashSet<>();
		for (final Element link : links) {
			final String href = link.attr("href");
			final String name = link.text().trim();
			if (name.isEmpty() || !seenIds.add(href)) {
				continue;
			}
			final CategoryDTO categoryDTO = new CategoryDTO(CNewsConf.NAME, name, absoluteUrl(href), CNewsConf.EXTENSION);
			categoryDTO.setDownloadable(true);
			categories.add(categoryDTO);
		}
		return categories;
	}

	private static String categoryUrl(final String categoryId) {
		return DownloadUtils.isHttpUrl(categoryId) ? categoryId : absoluteUrl(categoryId);
	}

	private static String absoluteUrl(final String href) {
		if (href == null || href.isEmpty()) {
			return CNewsConf.HOME_URL;
		}
		if (DownloadUtils.isHttpUrl(href)) {
			return href;
		}
		return CANALPLUS_HOST + (href.startsWith("/") ? href : "/" + href);
	}

	private static String extractTitle(final Element link) {
		final String linkText = link.text().trim();
		if (!linkText.isEmpty()) {
			return linkText;
		}
		final String title = link.attr("title").trim();
		if (!title.isEmpty()) {
			return title;
		}
		final String aria = link.attr("aria-label").trim();
		return aria.isEmpty() ? "" : aria;
	}

}
