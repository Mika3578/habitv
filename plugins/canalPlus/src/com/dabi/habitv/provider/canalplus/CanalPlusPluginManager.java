package com.dabi.habitv.provider.canalplus;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
		if (CanalPlusEndpointAvailability.isUnavailablePlaceholder(category)) {
			return new LinkedHashSet<>();
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final Map<String, Object> catData = mapper.readValue(getInputStreamFromUrl(category.getId()), Map.class);

			List<Object> strates = CanalPlusHodorParser.extractStrates(catData);
			if (strates == null) {
				strates = (List<Object>) catData.get("strates");
			}
			Set<EpisodeDTO> epList = new LinkedHashSet<>();
			if (strates == null) {
				return epList;
			}
			for (Object strateObject : strates) {
				Map<String, Object> strateMap = (Map<String, Object>) strateObject;
				String type = (String) strateMap.get("type");
				if ("contentGrid".equals(type) || "contentRow".equals(type)) {
					epList.addAll(findEpisodes(category, (List<Object>) strateMap.get("contents")));
				}
			}
			return epList;
		} catch (RuntimeException e) {
			if (CanalPlusEndpointAvailability.isUnavailable(e)) {
				getLog().warn(CanalPlusEndpointAvailability.buildEpisodeUnavailableMessage(getName(), category, e));
				return new LinkedHashSet<>();
			}
			throw e;
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
		Map<String, Object> onClick = (Map<String, Object>) mapEpisode.get("onClick");
		if (onClick == null) {
			return null;
		}
		String urlPage = (String) onClick.get("URLPage");
		if (StringUtils.isEmpty(urlPage)) {
			return null;
		}
		if (CanalPlusContentIdParser.isModernCanalPlusUrl(urlPage)) {
			return new EpisodeDTO(category, title + (subTitle == null ? "" : (" " + subTitle)), urlPage);
		}
		String url = CanalUtils.findUrl(this, urlPage);
		return url == null ? null : new EpisodeDTO(category, title + (subTitle == null ? "" : (" " + subTitle)), url);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<CategoryDTO> findCategory() {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final Map<String, Object> mainData = mapper.readValue(getInputStreamFromUrl(CanalPlusConf.URL_HOME), Map.class);
			String urlMainPage = getUrlMainPage(mainData);
			if (urlMainPage != null) {
				return findCategoriesFromUrl(null, urlMainPage);
			}
			getLog().warn("Canal+ legacy OnDemand entry missing; exposing protected-endpoint placeholder.");
			return CanalPlusEndpointAvailability.buildUnavailablePlaceholderCategories(CanalPlusConf.NAME,
					CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL);
		} catch (RuntimeException e) {
			if (CanalPlusEndpointAvailability.isUnavailable(e)) {
				getLog().warn(CanalPlusEndpointAvailability.buildCategoryUnavailableMessage(getName(), e));
				return CanalPlusEndpointAvailability.buildUnavailablePlaceholderCategories(CanalPlusConf.NAME,
						CanalPlusEndpointAvailability.CANAL_PLUS_UNAVAILABLE_LABEL);
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
		List<Object> strates = CanalPlusHodorParser.extractStrates(catData);
		if (strates == null) {
			strates = (List<Object>) catData.get("strates");
		}
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
		if (CanalPlusModernStreamSupport.isModernInput(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		if (downloadInput.contains("canalplus.")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	@Override
	public ProcessHolder download(DownloadParamDTO downloadInput, DownloaderPluginHolder downloaders) throws DownloadFailedException {
		if (CanalPlusModernStreamSupport.isModernInput(downloadInput.getDownloadInput())) {
			return downloadModernStream(downloadInput);
		}
		return CanalUtils.doDownload(downloadInput, downloaders, this, CanalPlusConf.VIDEO_INFO_URL, getName().toLowerCase());
	}

	private ProcessHolder downloadModernStream(final DownloadParamDTO downloadInput) {
		final String input = downloadInput.getDownloadInput();
		final String contentId = CanalPlusContentIdParser.fromInput(input);
		try {
			final CanalPlusHodorParser.CanalPlusUnitMetadata metadata = CanalPlusModernStreamSupport.loadUnitMetadata(this, input);
			if (metadata != null && metadata.getDisplayName() != null) {
				getLog().info("Canal+ modern unit metadata: " + metadata.getDisplayName()
						+ (contentId == null ? "" : " (contentId=" + contentId + ")"));
			}
			final String resolvedContentId = metadata != null && metadata.getContentId() != null ? metadata.getContentId() : contentId;
			final CanalPlusPlaysetParser.CanalPlusPlaysetItem playsetItem = CanalPlusModernStreamSupport
					.loadSelectedPlaysetItem(this, resolvedContentId);
			if (playsetItem == null) {
				throw new DownloadFailedException("Canal+ playset did not expose a PlayReady download item for contentId="
						+ resolvedContentId);
			}
			getLog().info("Canal+ selected playset: drmType=" + playsetItem.getDrmType() + " quality=" + playsetItem.getQuality());
		} catch (IOException e) {
			if (input.contains(CanalPlusModernConf.PAGE_HOST)) {
				throw new DownloadFailedException(
						"Canal+ page fetch failed (HTTP 403 is expected without protected network access). "
								+ "Pass a hodor detail API URL as the episode id, or retry after page-access support is added.",
						e);
			}
			throw new DownloadFailedException(e);
		}
		CanalPlusModernStreamSupport.assertDrmDownloadSupported();
		return null;
	}

}
