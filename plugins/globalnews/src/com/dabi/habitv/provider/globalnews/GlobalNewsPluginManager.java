package com.dabi.habitv.provider.globalnews;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalNewsPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	public GlobalNewsPluginManager() {
		HttpURLConnection.setFollowRedirects(true);
	}

	@Override
	public String getName() {
		return GlobalNewsConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(category.getId()));
		for (final Element div : doc.select("div.video-browse ul li a>div")) {
			Element aResult = div.parent();
			final String hRef = aResult.attr("href");

			Elements h5 = aResult.select("h5");
			if (h5.size() > 0) {
				episodeList.add(new EpisodeDTO(category, h5.get(0).text(), hRef));
			}
		}
		return episodeList;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<>();
		final Document doc = Jsoup.parse(getUrlContent(GlobalNewsConf.VIDEO_HOME_URL));

		for (final Element aHref : doc.select("div.video-navigation a")) {
			final String href = aHref.attr("href");
			if (href.length() > 1) {
				final String content = aHref.text();
				final CategoryDTO categoryDTO = new CategoryDTO(GlobalNewsConf.NAME, content, href, GlobalNewsConf.EXTENSION);
				categoryDTO.setDownloadable(true);
				categoryDTOs.add(categoryDTO);
			}

		}
		if (categoryDTOs.isEmpty()) {
			getLog().warn("provider=globalnews operation=catalogue sourceUrl=" + GlobalNewsConf.VIDEO_HOME_URL
					+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
					+ " note=public-video-html-no-longer-matches-legacy-scraper");
		}
		return categoryDTOs;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		String videoUrl;
		try {
			videoUrl = findDownloadlink(downloadParam.getDownloadInput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		downloadParam.addParam(FrameworkConf.DOWNLOADER_PARAM, FrameworkConf.YOUTUBE);
		return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, videoUrl), downloaders);

	}

	private final ObjectMapper mapper = new ObjectMapper();

	private String findDownloadlink(String url) throws JsonProcessingException, IOException {
		String jsonLink = findJsonLink(url);
		final JsonNode root = mapper.readTree(getInputStreamFromUrl(jsonLink));
		return root.elements().next().get("sources").elements().next().get("file").textValue();
	}

	private static final Pattern MEDIAID_FEEDURL = Pattern.compile("\"mediaId\":\"([^\"]+)\".*\"feedUrl\":\"([^\"]+)\"");

	private String findJsonLink(String url) {
		final Document doc = Jsoup.parse(getUrlContent(url));
		Elements scriptTags = doc.select("div.video-controls script");
		for (Element scriptTag : scriptTags) {
			String data = scriptTag.data();
			if (!StringUtils.isEmpty(data)) {
				Matcher matcher = MEDIAID_FEEDURL.matcher(data);
				if (matcher.find()) {
					String mediaId = matcher.group(1);
					String feedUrl = matcher.group(2).replace("\\", "");
					return "http:" + feedUrl + "?q=id.exact:" + mediaId;
				}
			}
		}
		throw new RuntimeException("mediaId not found");
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.contains("globalnews") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
