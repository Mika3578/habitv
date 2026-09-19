package com.dabi.habitv.provider.tvcom;

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
 * TVCom provider: HTML emissions catalogue and Freecaster HLS download via ffmpeg.
 */
public class TvComPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final TvComClient client;

	public TvComPluginManager() {
		this.client = new TvComClient(new TvComClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				try {
					return getUrlContent(url);
				} catch (final RuntimeException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
		});
	}

	TvComPluginManager(final TvComClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return TvComConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final TvComDiagnostics diagnostics = new TvComDiagnostics("catalogue");
		diagnostics.setSourceUrl(TvComUrls.emissionsIndexUrl());
		try {
			final List<TvComHtml.ShowRef> shows = client.loadShows();
			for (final TvComHtml.ShowRef show : shows) {
				final CategoryDTO category = new CategoryDTO(TvComConf.NAME, show.title,
						TvComUrls.showCategoryId(show.slug), TvComConf.EXTENSION);
				category.setDownloadable(true);
				categories.add(category);
			}
			diagnostics.setCreatedItems(categories.size());
			if (categories.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TVCom catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TVCom catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !TvComUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String slug = TvComUrls.showSlugFromCategoryId(category.getId());
		final TvComDiagnostics diagnostics = new TvComDiagnostics("episodes");
		diagnostics.setShowSlug(slug);
		diagnostics.setSourceUrl(TvComUrls.showPageUrl(slug));
		try {
			final List<TvComHtml.EpisodeRef> refs = client.loadShowEpisodes(slug);
			for (final TvComHtml.EpisodeRef ref : refs) {
				final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.watchUrl);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(ref.title);
				metadata.setSourceUrl(ref.watchUrl);
				metadata.setChannel(TvComConf.CHANNEL_LABEL);
				episode.setMetadata(metadata);
				episodes.add(episode);
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TVCom episode listing failed for " + slug + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TVCom episode listing failed for " + slug + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final TvComDiagnostics diagnostics = new TvComDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		final String sanitizedInput = TvComUrls.sanitizeEpisodeUrl(downloadParam.getDownloadInput());
		try {
			if (sanitizedInput == null) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			final String hlsUrl = client.resolveHlsUrl(sanitizedInput);
			if (hlsUrl == null) {
				diagnostics.setRootCauseSummary("missing-hls");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setRootCauseSummary("delegate-ffmpeg");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, hlsUrl), downloaders,
					FrameworkConf.FFMPEG);
		} catch (final DownloadFailedException e) {
			if (TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			final String safeId = sanitizedInput != null ? sanitizedInput : TvComConf.NAME;
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, safeId, safeId), TvComConf.NAME, e));
			throw new DownloadFailedException(TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TvComConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (TvComUrls.sanitizeEpisodeUrl(downloadInput) != null) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
