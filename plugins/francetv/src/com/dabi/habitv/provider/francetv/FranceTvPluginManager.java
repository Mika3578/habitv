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
		if (FranceTvUrls.isPublicHubContainerUrl(category.getId())) {
			logHubContainerEpisodeSkip(category.getId());
			return episodes;
		}
		final String programPath = FranceTvUrls.programPathFromCategoryUrl(category.getId());
		if (programPath != null) {
			return findApiEpisodes(category, programPath, episodes);
		}
		if (FranceTvUrls.isPublicCollectionPageUrl(category.getId())) {
			return findPublicCollectionEpisodesFallback(category);
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
		addPublicPageRoots(categories);
		return categories;
	}

	/**
	 * HTML fallback for pasted public collection URLs only; not used during normal
	 * public tree construction.
	 */
	FranceTvDiscoveryResult discoverPublicCollectionPage(final String sourceUrl, final String html) {
		final FranceTvDiscoveryResult result = FranceTvPublicPageDiscovery.discover(sourceUrl, html);
		logHtmlDiscoveryDiagnostics(result, sourceUrl);
		return result;
	}

	private Set<EpisodeDTO> findApiEpisodes(final CategoryDTO category, final String programPath,
			final Set<EpisodeDTO> episodes) {
		final FranceTvTaxonomyDiagnostics diagnostics = new FranceTvTaxonomyDiagnostics(programPath);
		final String sourceUrl = apiClient.taxonomySourceUrl(programPath, 0);
		try {
			final List<Map<String, Object>> items = apiClient.fetchEpisodes(programPath);
			for (final Map<String, Object> item : items) {
				final Object type = item.get("type");
				if (type == null || !FranceTvUrls.isReplayVideoType(String.valueOf(type))) {
					continue;
				}
				final String pageUrl = FranceTvUrls.episodePageUrl(item, programPath);
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
				diagnostics.incrementCreatedReplayItems();
			}
			diagnostics.setPage(0);
			logTaxonomyDiagnostics(diagnostics, sourceUrl);
		} catch (IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			logTaxonomyDiagnostics(diagnostics, sourceUrl);
			getLog().error("Failed to fetch france.tv episodes for " + programPath, e);
		}
		return episodes;
	}

	private Set<EpisodeDTO> findPublicCollectionEpisodesFallback(final CategoryDTO category) {
		final String collectionUrl = FranceTvUrls.collectionUrlFromCategoryId(category.getId());
		try {
			final FranceTvDiscoveryResult result = discoverPublicCollectionPage(collectionUrl,
					getUrlContent(collectionUrl));
			return FranceTvDiscoveryMapper.toEpisodes(result, category);
		} catch (RuntimeException e) {
			getLog().warn("France.tv HTML fallback discovery failed safely for " + collectionUrl + ": "
					+ e.getMessage());
			return new LinkedHashSet<>();
		}
	}

	private void addPublicPageRoots(final Set<CategoryDTO> categories) {
		final FranceTvPublicCategoryTreeBuilder builder = new FranceTvPublicCategoryTreeBuilder(
				new FranceTvPublicCategoryTreeBuilder.ChannelHubLoader() {
					@Override
					public Map<String, Object> loadChannelHub(final String hubSlug) throws IOException {
						return apiClient.fetchChannelHub(hubSlug);
					}
				}, new FranceTvPublicCategoryTreeBuilder.HubDiagnosticsListener() {
					@Override
					public void onHubDiscovery(final FranceTvPublicHubDiagnostics diagnostics,
							final String sourceUrl) {
						logHubDiscoveryDiagnostics(diagnostics, sourceUrl);
					}
				}, getLog());
		final CategoryDTO publicRoot = builder.buildPublicRootCategory();
		if (getLog().isDebugEnabled()) {
			getLog().debug("France.tv public hub tree before grabconfig merge:\n"
					+ FranceTvPublicCategoryTreeBuilder.formatPublicHubDebugReport(publicRoot));
		}
		if (!publicRoot.getSubCategories().isEmpty()) {
			categories.add(publicRoot);
		}
	}

	private void logHubDiscoveryDiagnostics(final FranceTvPublicHubDiagnostics diagnostics,
			final String sourceUrl) {
		if (diagnostics == null) {
			return;
		}
		final String line = diagnostics.formatLogLine(sourceUrl);
		if ("ok".equals(diagnostics.getRootCauseSummary())
				|| "configured-seed-fallback".equals(diagnostics.getRootCauseSummary())) {
			getLog().info(line);
		} else {
			getLog().warn(line);
		}
	}

	private void logHubContainerEpisodeSkip(final String hubUrl) {
		final String hubSlug = hubSlugFromContainerUrl(hubUrl);
		final FranceTvPublicHubDiagnostics diagnostics = new FranceTvPublicHubDiagnostics(
				hubSlug == null ? "unknown" : hubSlug);
		diagnostics.setRootCauseSummary("hub-container-no-episodes");
		logHubDiscoveryDiagnostics(diagnostics, hubUrl);
	}

	private static String hubSlugFromContainerUrl(final String hubUrl) {
		if (StringUtils.isEmpty(hubUrl) || !hubUrl.startsWith(FranceTvConf.HOME_URL)) {
			return null;
		}
		String path = hubUrl.substring(FranceTvConf.HOME_URL.length());
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		final int slash = path.indexOf('/');
		if (slash >= 0) {
			return null;
		}
		return path;
	}

	private void logHtmlDiscoveryDiagnostics(final FranceTvDiscoveryResult result, final String sourceUrl) {
		if (result == null || result.getDiagnostics() == null) {
			return;
		}
		final String line = result.getDiagnostics().formatLogLine(sourceUrl);
		if (result.isSuccessful()) {
			getLog().info(line);
		} else {
			getLog().warn(line);
		}
	}

	private void logTaxonomyDiagnostics(final FranceTvTaxonomyDiagnostics diagnostics, final String sourceUrl) {
		if (diagnostics == null) {
			return;
		}
		final String line = diagnostics.formatLogLine(sourceUrl);
		if ("ok".equals(diagnostics.getRootCauseSummary())) {
			getLog().info(line);
		} else {
			getLog().warn(line);
		}
	}

	private Collection<CategoryDTO> findPrograms(final String channelSlug) {
		try {
			return FranceTvProgramCatalog.groupProgramsBySection(channelSlug,
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
