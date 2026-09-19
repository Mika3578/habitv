package com.dabi.habitv.provider.lequipe;

import java.net.HttpURLConnection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.dabi.habitv.framework.plugin.utils.SoccerUtils;

public class LEquipePluginManager extends BasePluginWithProxy implements
		PluginProviderDownloaderInterface {

	public LEquipePluginManager() {
		HttpURLConnection.setFollowRedirects(true);
	}
	
	@Override
	public String getName() {
		return LEquipeConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		final String baseUrl = LEquipeConf.VIDEOS_URL + "/"
				+ getCatId(category);
		String pageUrl;
		for (int i = 1; i <= LEquipeConf.MAX_PAGE; i++) {
			pageUrl = baseUrl + "/" + i;
			findEpisodeByUrl(category, episodeList, pageUrl);
		}

		return episodeList;
	}

	private String getCatId(final CategoryDTO category) {
		String[] split = category.getId().split("/");
		return split[split.length - 1];
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<>();
		try {
			final Document doc = Jsoup
					.parse(getUrlContent(LEquipeConf.VIDEO_HOME_URL));

			for (final Element aHref : doc.select("div#nav-inside-global ul li a")) {
				final String href = aHref.attr("href");
				if (href.length() > 1) {
					final String content = aHref.text();
					final CategoryDTO categoryDTO = new CategoryDTO(
							LEquipeConf.NAME, content, href, LEquipeConf.EXTENSION);
					categoryDTO.setDownloadable(true);
					// categoryDTO.addSubCategories(findSubCategories(href));
					categoryDTOs.add(categoryDTO);
				}

			}
			if (categoryDTOs.isEmpty()) {
				getLog().warn("provider=lequipe operation=catalogue sourceUrl=" + LEquipeConf.VIDEO_HOME_URL
						+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
						+ " note=public-video-html-no-longer-matches-legacy-scraper");
			}
			return categoryDTOs;
		} catch (RuntimeException e) {
			if (LEquipeEndpointAvailability.isUnavailable(e)) {
				getLog().warn(LEquipeEndpointAvailability.buildCategoryUnavailableMessage(getName(), e));
				return categoryDTOs;
			}
			throw e;
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final String videoUrl = findDownloadlink(downloadParam
				.getDownloadInput());
		return DownloadUtils.download(
				DownloadParamDTO.buildDownloadParam(downloadParam, videoUrl),
				downloaders);

	}

	private String findDownloadlink(String url) {
		final Document doc = Jsoup.parse(getUrlContent(url));
		Elements elementsByClass = doc.select("#laVideo iframe");
		return elementsByClass.get(0).attr("src").replace("//", "");
	}

	private void findEpisodeByUrl(final CategoryDTO category,
			final Set<EpisodeDTO> episodeList, final String pageUrl) {
		final Document doc = Jsoup.parse(getUrlContent(pageUrl));
		for (final Element li : doc.select(".items_last_vids")) {
			Element aResult = li.child(0);
			final String hRef = aResult.attr("href");
			StringBuilder name = new StringBuilder();
			for (Element spanElement : aResult.select(".brique span")) {
				if (name.length() > 0) {
					name.append(" - ");
				}
				name.append(spanElement.text());
			}
			if (name.length() > 0) {
				name.append(" - ");
			}
			name.append(aResult.select("p").get(0).text());
			final String nameWithoutScore = SoccerUtils.maskScore(name
					.toString());
			if (checkName(nameWithoutScore)) {
				episodeList.add(new EpisodeDTO(category, nameWithoutScore,
						LEquipeConf.VIDEO_HOME_URL + hRef));
			}
		}
	}

	private boolean checkName(final String nameWithoutScore) {
		return nameWithoutScore != null && !nameWithoutScore.isEmpty();
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return DownloadableState.IMPOSSIBLE;
	}

}
