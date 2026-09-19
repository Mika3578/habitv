package com.dabi.habitv.provider.t18;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

/**
 * T18 diagnostics-first provider: public HTML catalogue discovery with
 * non-blocking download refusal for private Dailymotion embeds.
 */
public class T18PluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	interface PageLoader {
		String load(String url);
	}

	private final PageLoader pageLoader;

	public T18PluginManager() {
		this.pageLoader = new PageLoader() {
			@Override
			public String load(final String url) {
				return getUrlContent(url, "UTF-8");
			}
		};
	}

	T18PluginManager(final PageLoader pageLoader) {
		this.pageLoader = pageLoader;
	}

	@Override
	public String getName() {
		return T18Conf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final T18Diagnostics diagnostics = new T18Diagnostics("catalogue");
		diagnostics.setSourceUrl(T18Conf.HOME_URL);
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		try {
			final String html = pageLoader.load(T18Conf.HOME_URL);
			final Map<String, String> programs = T18Html.extractProgramCategories(html);
			final CategoryDTO root = new CategoryDTO(T18Conf.NAME, "T18", T18Conf.HOME_URL, T18Conf.EXTENSION);
			root.setDownloadable(false);
			for (final Map.Entry<String, String> entry : programs.entrySet()) {
				final String programUrl = T18Html.absoluteUrl("/prog/" + entry.getKey());
				final CategoryDTO program = new CategoryDTO(T18Conf.NAME, entry.getValue(), programUrl,
						T18Conf.EXTENSION);
				program.setDownloadable(true);
				root.addSubCategory(program);
			}
			diagnostics.setCreatedItems(root.getSubCategories().size());
			if (root.getSubCategories().isEmpty()) {
				diagnostics.setRootCauseSummary("empty-catalog");
			} else {
				categories.add(root);
			}
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("listing-failed:" + e.getClass().getSimpleName());
			getLog().warn("T18 catalogue discovery failed safely: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || StringUtils.isEmpty(category.getId())) {
			return episodes;
		}
		final T18Diagnostics diagnostics = new T18Diagnostics("episodes");
		diagnostics.setSourceUrl(category.getId());
		try {
			final String html = pageLoader.load(category.getId());
			final String programSlug = T18Html.programSlug(category.getId().replace(T18Conf.HOME_URL, ""));
			final Map<String, String> episodePaths = T18Html.extractEpisodePaths(html, programSlug);
			if (episodePaths.isEmpty() && T18Html.isEpisodePath(category.getId().replace(T18Conf.HOME_URL, ""))) {
				final String title = firstNonEmpty(T18Html.pageTitle(html), category.getName());
				episodes.add(buildEpisode(category, title, category.getId()));
			} else {
				for (final Map.Entry<String, String> entry : episodePaths.entrySet()) {
					episodes.add(buildEpisode(category, entry.getValue(), T18Html.absoluteUrl(entry.getKey())));
				}
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-episode-list");
			} else if (T18Html.mentionsPrivateDailymotionEmbed(html)) {
				diagnostics.setRootCauseSummary("private-dailymotion-embed-detected");
			}
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("listing-failed:" + e.getClass().getSimpleName());
			getLog().warn("T18 episode listing failed safely: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final T18Diagnostics diagnostics = new T18Diagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		diagnostics.setRootCauseSummary("private-dailymotion-unsupported");
		getLog().warn(diagnostics.formatLogLine());
		// Public T18 pages embed private Dailymotion media. Do not reconstruct
		// authorization tokens; fail closed with the standard unavailable message.
		throw new DownloadFailedException(T18Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (T18Html.isT18Url(downloadInput) && downloadInput.contains("/prog/")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static EpisodeDTO buildEpisode(final CategoryDTO category, final String title, final String url) {
		final EpisodeDTO episode = new EpisodeDTO(category, title, url);
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle(category.getName());
		metadata.setEpisodeTitle(title);
		metadata.setSourceUrl(url);
		metadata.setChannel("T18");
		episode.setMetadata(metadata);
		return episode;
	}

	private static String firstNonEmpty(final String first, final String second) {
		if (StringUtils.isNotEmpty(first)) {
			return first;
		}
		return second;
	}
}
