package com.dabi.habitv.provider.beinsport;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

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
import com.dabi.habitv.framework.plugin.utils.SoccerUtils;

public class BeinSportPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	@Override
	public String getName() {
		return BeinSportConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();

		for (int i = 1; i <= 3; i++) {
			final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(category.getId()));

			for (final Element aHref : doc.select("main article h3 a")) {
				String href = aHref.attr("data-url");
				String name = aHref.text();
				name = SoccerUtils.maskScore(name);
				episodeList.add(new EpisodeDTO(category, name, toUrl(href)));
			}
		}
		return episodeList;
	}

	private String toUrl(String href) {
		return (href.startsWith("http://") || href.startsWith("https://")) ? href : (BeinSportConf.HOME_URL + href);
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<>();
		CategoryDTO videoCategory = new CategoryDTO(BeinSportConf.VIDEOS_CATEGORY, BeinSportConf.VIDEOS_CATEGORY, BeinSportConf.VIDEOS_URL,
		        BeinSportConf.EXTENSION);
		videoCategory.setDownloadable(false);
		addSubCategories(videoCategory);
		categoryDTOs.add(videoCategory);
		return categoryDTOs;
	}

	private void addSubCategories(CategoryDTO category) {
		org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.parse(getInputStreamFromUrl(category.getId()), "UTF-8", category.getId());
		} catch (IOException e) {
			throw new TechnicalException(e);
		}

		addSubCategories(category, doc, "categories");
		addSubCategories(category, doc, "tags");
	}

	private void addSubCategories(CategoryDTO category, Element doc, String type) {
		for (final Element aMain : doc.select(".bein-selectBox option")) {
			final String href = aMain.attr("data-url");
			final String text = aMain.attr("value");
			if (href != null && href.length() > 5) {
				CategoryDTO mainCat = buildSubCategory(category, text, toUrl(href));
				// addSubSubCat(mainCat, doc);
				category.addSubCategory(mainCat);
			}
		}
	}

	private CategoryDTO buildSubCategory(CategoryDTO category, final String name, String cat) {
		CategoryDTO subCategory = new CategoryDTO(BeinSportConf.NAME, name, cat, BeinSportConf.EXTENSION);
		subCategory.setDownloadable(true);
		return subCategory;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, findUrlDownload(downloadParam.getDownloadInput())),
		        downloaders);
	}

	private static Pattern VIDEO = Pattern.compile("'video': '(.*)'");

	private String findUrlDownload(String downloadInput) {
		org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.parse(getInputStreamFromUrl(downloadInput), "UTF-8", downloadInput);
			String blockScript = doc.select(".block-video script").first().html();
			Matcher matcher = VIDEO.matcher(blockScript);
			matcher.find();
			String group = matcher.group(1);
			return "http://www.dailymotion.com/video/" + group;
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.startsWith(BeinSportConf.HOME_URL) ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
