package com.dabi.habitv.provider.tv5plus;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadFailureDiagnostics;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

/**
 * TV5+ Canada provider: GraphQL catalogue of AVAILABLE TV5/Unis catch-up and
 * yt-dlp download via tv5unis.ca URLs. Canada geo and non-AVAILABLE products
 * fail closed without bypass.
 */
public class Tv5PlusPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final Tv5PlusClient client;

	public Tv5PlusPluginManager() {
		this.client = new Tv5PlusClient();
	}

	Tv5PlusPluginManager(final Tv5PlusClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return Tv5PlusConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final Tv5PlusDiagnostics diagnostics = new Tv5PlusDiagnostics("catalogue");
		diagnostics.setSourceUrl(Tv5PlusConf.GRAPHQL_URL);
		try {
			final List<Tv5PlusClient.ShowRef> shows = client.loadShows();
			for (final Tv5PlusClient.ShowRef show : shows) {
				final CategoryDTO category = new CategoryDTO(Tv5PlusConf.NAME, show.title,
						Tv5PlusUrls.showCategoryId(show.slug), Tv5PlusConf.EXTENSION);
				category.setDownloadable(true);
				categories.add(category);
			}
			diagnostics.setCreatedItems(categories.size());
			if (categories.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TV5+ catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TV5+ catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !Tv5PlusUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String slug = Tv5PlusUrls.showSlugFromCategoryId(category.getId());
		final Tv5PlusDiagnostics diagnostics = new Tv5PlusDiagnostics("episodes");
		diagnostics.setShowSlug(slug);
		diagnostics.setSourceUrl(Tv5PlusConf.GRAPHQL_URL);
		try {
			final List<Tv5PlusClient.EpisodeRef> refs = client.loadShowEpisodes(slug);
			for (final Tv5PlusClient.EpisodeRef ref : refs) {
				final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.watchUrl);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(ref.title);
				metadata.setSourceUrl(ref.watchUrl);
				metadata.setChannel(Tv5PlusConf.CHANNEL_LABEL);
				if (ref.seasonNumber != null) {
					metadata.setSeasonNumber(ref.seasonNumber);
				}
				if (ref.episodeNumber != null) {
					metadata.setEpisodeNumber(ref.episodeNumber);
				}
				episode.setMetadata(metadata);
				episodes.add(episode);
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TV5+ episode listing failed for " + slug + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TV5+ episode listing failed for " + slug + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final Tv5PlusDiagnostics diagnostics = new Tv5PlusDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			final String sanitized = Tv5PlusUrls.sanitizeEpisodeUrl(downloadParam.getDownloadInput());
			if (sanitized == null) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(Tv5PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			final DownloadParamDTO effectiveParam = sanitized.equals(downloadParam.getDownloadInput())
					? downloadParam
					: DownloadParamDTO.buildDownloadParam(downloadParam, sanitized);
			// yt-dlp tv5unis extractors; Canada geo may block playback.
			diagnostics.setRootCauseSummary("delegate-ytdlp");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(effectiveParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			if (Tv5PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					Tv5PlusConf.NAME, e));
			throw new DownloadFailedException(Tv5PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(Tv5PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (Tv5PlusUrls.sanitizeEpisodeUrl(downloadInput) != null) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
