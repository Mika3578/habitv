package com.dabi.habitv.provider.sfr;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class SFRPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	@Override
	public String getName() {
		return SFRConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		for (int i = 0; i <= SFRConf.MAX_PAGE; i++) {
			String pageUrl = String.format(SFRConf.VIDEOS_URL, getCatId(category), i);
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
		final Document doc = Jsoup.parse(getUrlContent(SFRConf.HOME_URL));

		for (final Element li : doc.select(".navigation__categories .category--hasCategory")) {
			Element aHref = li.child(0);
			final String name = aHref.text();

			final CategoryDTO categoryDTO = new CategoryDTO(SFRConf.NAME, name, name, SFRConf.EXTENSION);
			categoryDTO.setDownloadable(false);
			categoryDTOs.add(categoryDTO);

			for (final Element aHrefCat : li.select("a")) {
				final String text = aHrefCat.text();
				String hrefCat = aHrefCat.attr("href");
				String id = toId(hrefCat);
				if (!StringUtils.isEmpty(id) && !hrefCat.equals("#")) {
					final CategoryDTO subCategoryDTO = new CategoryDTO(SFRConf.NAME, text, id, SFRConf.EXTENSION);
					subCategoryDTO.setDownloadable(true);
					categoryDTO.addSubCategory(subCategoryDTO);
				}
			}
		}
		return categoryDTOs;
	}

	private String toId(String hrefCat) {
		StringBuilder str = new StringBuilder();
		for (String urlPart : hrefCat.split("/")) {
			if (!StringUtils.isEmpty(urlPart) && !urlPart.endsWith("sfr.fr")) {
				str.append(urlPart);
			}
		}
		return str.toString().replace("-", "");
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final String videoUrl = "http:" + findDownloadlink(downloadParam.getDownloadInput());
		return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, videoUrl), downloaders);

	}

	private static final Pattern URL_PATTERN = Pattern.compile("var url = \"([^\"]*)\";");

	private String findDownloadlink(String url) {
		return extractDeclaredVideoUrl(getUrlContent(url));
	}

	static String extractDeclaredVideoUrl(final String content) {
		if (content == null) {
			return null;
		}
		final Matcher matcher = URL_PATTERN.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void findEpisodeByUrl(final CategoryDTO category, final Set<EpisodeDTO> episodeList, final String pageUrl) {
		final ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> userData;
		try {
			userData = mapper.readValue(getUrlContent(pageUrl), List.class);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		for (Map<String, Object> map : userData) {
			String title = (String) map.get("title");
			if (title.contains("VIDEO")) {
				episodeList.add(new EpisodeDTO(category, title, SFRConf.HOME_URL + (String) map.get("pageUrl")));
			}
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return DownloadableState.IMPOSSIBLE;
	}

}
