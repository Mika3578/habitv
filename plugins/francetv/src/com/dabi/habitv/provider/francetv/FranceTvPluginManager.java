package com.dabi.habitv.provider.francetv;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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

public class FranceTvPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final FranceTvApiClient apiClient = new FranceTvApiClient(this);

	@Override
	public String getName() {
		return FranceTvConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String programPath = FranceTvUrls.programPathFromCategoryUrl(category.getId());
		if (programPath == null) {
			return episodes;
		}

		try {
			final List<Map<String, Object>> items = apiClient.fetchEpisodes(programPath);
			for (final Map<String, Object> item : items) {
				final Object type = item.get("type");
				if (type == null || !FranceTvUrls.isReplayVideoType(String.valueOf(type))) {
					continue;
				}
				final String pageUrl = FranceTvUrls.episodePageUrl(item);
				if (StringUtils.isEmpty(pageUrl)) {
					continue;
				}
				final String name = episodeDisplayName(item);
				if (StringUtils.isEmpty(name)) {
					continue;
				}
				final EpisodeDTO episode = new EpisodeDTO(category, name, pageUrl);
				FranceTvEpisodeMetadata.apply(item, episode);
				episodes.add(episode);
			}
		} catch (IOException e) {
			getLog().error("Failed to fetch france.tv episodes for " + programPath, e);
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		for (final String slug : FranceTvConf.CHANNEL_SLUGS) {
			final String channelUrl = FranceTvConf.HOME_URL + "/" + slug + "/";
			final CategoryDTO channel = new CategoryDTO(FranceTvConf.NAME, FranceTvUrls.channelLabel(slug), channelUrl,
					FranceTvConf.EXTENSION);
			channel.setDownloadable(false);
			channel.addSubCategories(findPrograms(slug));
			categories.add(channel);
		}
		return categories;
	}

	private Collection<CategoryDTO> findPrograms(final String channelSlug) {
		try {
			return FranceTvProgramCatalog.groupProgramsByRubrique(channelSlug,
					apiClient.fetchPrograms(channelSlug));
		} catch (IOException e) {
			getLog().error("Failed to fetch france.tv programs for channel " + channelSlug, e);
			return new LinkedHashSet<>();
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput.contains("france.tv") || downloadInput.contains("france2.")
				|| downloadInput.contains("france3.") || downloadInput.contains("france4.")
				|| downloadInput.contains("france5.") || downloadInput.contains("franceo.")
				|| downloadInput.contains("franceinfo.fr") || downloadInput.contains("francetvinfo.fr")
				|| downloadInput.contains("pluzz.")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static String episodeDisplayName(final Map<String, Object> item) {
		final Object episodeTitle = item.get("episode_title");
		if (episodeTitle != null && StringUtils.isNotEmpty(String.valueOf(episodeTitle))) {
			return String.valueOf(episodeTitle).trim();
		}
		final Object title = item.get("title");
		if (title != null && StringUtils.isNotEmpty(String.valueOf(title))) {
			return String.valueOf(title).trim();
		}
		return "";
	}

}
