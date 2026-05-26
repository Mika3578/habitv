package com.dabi.habitv.provider.canalplus;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CanalPlusPluginManager extends BasePluginWithProxy implements PluginProviderInterface, PluginDownloaderInterface { // NO_UCD

	@Override
	@SuppressWarnings("unchecked")
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final Map<String, Object> catData = mapper.readValue(getInputStreamFromUrl(category.getId()), Map.class);

			List<Object> strates = (List<Object>) catData.get("strates");
			Set<EpisodeDTO> epList = new HashSet<>();
			for (Object strateObject : strates) {
				Map<String, Object> strateMap = (Map<String, Object>) strateObject;
				String type = (String) strateMap.get("type");
				if ("contentGrid".equals(type) || "contentRow".equals(type)) {
					epList.addAll(findEpisodes(category, (List<Object>) strateMap.get("contents")));
				}
			}
			return epList;
		} catch (IOException e) {
			throw new DownloadFailedException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Set<EpisodeDTO> findEpisodes(CategoryDTO category, List<Object> objectContent) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		for (Object objectEpisode : objectContent) {
			EpisodeDTO episode = buildEpisode(category, (Map<String, Object>) objectEpisode);
			if (episode != null) {
				episodes.add(episode);
			}
		}

		return episodes;
	}

	@SuppressWarnings("unchecked")
	private EpisodeDTO buildEpisode(CategoryDTO category, Map<String, Object> mapEpisode) {
		String title = (String) mapEpisode.get("title");
		String subTitle = (String) mapEpisode.get("subtitle");
		String url = CanalUtils.findUrl(this, (String) ((Map<String, Object>) mapEpisode.get("onClick")).get("URLPage"));
		return url == null ? null : new EpisodeDTO(category, title + (subTitle == null ? "" : (" " + subTitle)), url);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<CategoryDTO> findCategory() {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final Map<String, Object> mainData = mapper.readValue(getInputStreamFromUrl(CanalPlusConf.URL_HOME), Map.class);
			String urlMainPage = getUrlMainPage(mainData);
			return findCategoriesFromUrl(null, urlMainPage);
		} catch (RuntimeException e) {
			if (CanalPlusEndpointAvailability.isUnavailable(e)) {
				getLog().warn(CanalPlusEndpointAvailability.buildCategoryUnavailableMessage(getName(), e));
				return new LinkedHashSet<>();
			}
			throw e;
		} catch (IOException e) {
			throw new DownloadFailedException(e);
		}
	}

	private Set<CategoryDTO> findCategoriesFromUrl(CategoryDTO fatherCat, String urlMainPage) throws IOException, JsonParseException,
			JsonMappingException {
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		final Map<String, Object> catData = mapper.readValue(getInputStreamFromUrl(urlMainPage), Map.class);
		return findCategories(fatherCat, catData);
	}

	@SuppressWarnings("unchecked")
	private Set<CategoryDTO> findCategories(CategoryDTO fatherCat, Map<String, Object> catData) throws JsonParseException,
			JsonMappingException, IOException {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		List<Object> strates = (List<Object>) catData.get("strates");
		if (strates != null) {
			for (Object data : strates) {
				Map<String, Object> dataMap = (Map<String, Object>) data;
				addCategory(fatherCat, categories, dataMap);
			}
		}
		return categories;
	}

	@SuppressWarnings("unchecked")
	private void addCategory(final CategoryDTO fatherCat, final Set<CategoryDTO> categories, Map<String, Object> dataMap)
			throws JsonParseException, JsonMappingException, IOException {
		String type = (String) dataMap.get("type");
		Map<String, Object> onClick = (Map<String, Object>) dataMap.get("onClick");
		String urlPage = onClick == null ? null : (String) onClick.get("URLPage");
		// String displayTemplate = onClick == null ? null : (String)
		// onClick.get("displayTemplate");
		if ("landing".equals(type)) {
			CategoryDTO leafCategory = buildLeafCategory(fatherCat, dataMap);
			if (leafCategory != null) {
				categories.add(leafCategory);
			}
		} else if ("contentRow".equals(type) || "textList".equals(type) || "contentGrid".equals(type)) {
			List<Object> contents = (List<Object>) dataMap.get("contents");
			if (contents != null) {
				for (Object object : contents) {
					Map<String, Object> subDataMap = (Map<String, Object>) object;
					addCategory(fatherCat, categories, subDataMap);
				}
			}
		} else if (type == null && urlPage != null) {
			CategoryDTO category = buildNodeCategory(dataMap);
			category.setDownloadable(true);
			category.addSubCategories(findCategoriesFromUrl(category, urlPage));
			categories.add(category);
		}
	}

	@SuppressWarnings("unchecked")
	private CategoryDTO buildNodeCategory(Map<String, Object> dataMap) throws JsonParseException, JsonMappingException, IOException {
		String title = (String) dataMap.get("title");
		title = (String) (title == null ? dataMap.get("type") : title);
		String identifier = (String) ((Map<String, Object>) dataMap.get("onClick")).get("URLPage");
		CategoryDTO categoryDTO = new CategoryDTO(CanalPlusConf.NAME, title, identifier, FrameworkConf.MP4);
		categoryDTO.setDownloadable(false);
		List<Object> contents = (List<Object>) dataMap.get("contents");
		if (contents != null) {
			for (Object subDataMap : contents) {
				addCategory(categoryDTO, categoryDTO.getSubCategories(), (Map<String, Object>) subDataMap);
			}
		}
		return categoryDTO;
	}

	@SuppressWarnings("unchecked")
	private CategoryDTO buildLeafCategory(CategoryDTO fatherCat, Map<String, Object> dataMap) {
		String title = (String) dataMap.get("title");
		String identifier = (String) ((Map<String, Object>) dataMap.get("onClick")).get("URLPage");
		if (fatherCat != null && fatherCat.getName().equals(title)) {
			fatherCat.setDownloadable(true);
			return null;
		}
		CategoryDTO categoryDTO = new CategoryDTO(CanalPlusConf.NAME, title, identifier, FrameworkConf.MP4);
		categoryDTO.setDownloadable(true);
		// System.out.println(categoryDTO.getId());
		return categoryDTO;
	}

	@SuppressWarnings("unchecked")
	private String getUrlMainPage(Map<String, Object> userData) {
		List<Object> arbList = (List<Object>) userData.get("arborescence");
		for (Object catObject : arbList) {
			Map<String, Object> catMap = (Map<String, Object>) catObject;
			if ("OnDemand".equals(catMap.get("picto"))) {
				return (String) ((Map<String, Object>) catMap.get("onClick")).get("URLPage");
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return CanalPlusConf.NAME;
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		if (downloadInput.contains("canalplus.")) {
			return DownloadableState.SPECIFIC;
		} else {
			return DownloadableState.IMPOSSIBLE;
		}
	}

	@Override
	public ProcessHolder download(DownloadParamDTO downloadInput, DownloaderPluginHolder downloaders) throws DownloadFailedException {
		return CanalUtils.doDownload(downloadInput, downloaders, this, CanalPlusConf.VIDEO_INFO_URL, getName().toLowerCase());
	}

}
