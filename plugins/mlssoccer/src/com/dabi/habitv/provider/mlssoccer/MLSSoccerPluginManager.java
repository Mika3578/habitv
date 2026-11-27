package com.dabi.habitv.provider.mlssoccer;

import java.net.HttpURLConnection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class MLSSoccerPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	public MLSSoccerPluginManager() {
		HttpURLConnection.setFollowRedirects(true);
	}

	@Override
	public String getName() {
		return MLSSoccerConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(category.getId()));
		for (final Element aHref : doc.select("div.content div.node-title a")) {
			final String hRef = aHref.attr("href");
			final String name = aHref.text();
			episodeList.add(new EpisodeDTO(category, name, MLSSoccerConf.VIDEO_HOME_URL + hRef));
		}
		if (episodeList.isEmpty()) {
			getLog().warn("provider=mlssoccer operation=episodes sourceUrl=" + category.getId()
					+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
					+ " note=public-highlights-html-no-longer-matches-legacy-scraper");
		}
		return episodeList;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<>();
		CategoryDTO cat = new CategoryDTO(MLSSoccerConf.NAME, "Highlights", "https://www.mlssoccer.com/videos/full-highlights",
		        MLSSoccerConf.EXTENSION);
		cat.setDownloadable(true);
		categoryDTOs.add(cat);
		return categoryDTOs;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		downloadParam.addParam(FrameworkConf.DOWNLOADER_PARAM, FrameworkConf.YOUTUBE);
		return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, downloadParam.getDownloadInput()), downloaders);

	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.contains("mlssoccer") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
