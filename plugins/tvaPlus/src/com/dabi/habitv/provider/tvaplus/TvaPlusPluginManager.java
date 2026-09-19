package com.dabi.habitv.provider.tvaplus;

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
 * TVA+ provider: free TVA catch-up discovery via Next.js HTML and yt-dlp
 * download (Brightcove). Subscription and non-PUBLIC catalogues are excluded.
 * Canada geo limits fail closed without bypass.
 */
public class TvaPlusPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final TvaPlusClient client;

	public TvaPlusPluginManager() {
		this.client = new TvaPlusClient(new TvaPlusClient.ContentLoader() {
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

	TvaPlusPluginManager(final TvaPlusClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return TvaPlusConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final TvaPlusDiagnostics diagnostics = new TvaPlusDiagnostics("catalogue");
		diagnostics.setSourceUrl(TvaPlusUrls.tvaChannelUrl());
		try {
			final CategoryDTO recent = new CategoryDTO(TvaPlusConf.NAME, TvaPlusConf.RECENT_CATEGORY_NAME,
					TvaPlusConf.CATEGORY_RECENT, TvaPlusConf.EXTENSION);
			recent.setDownloadable(true);
			categories.add(recent);

			final List<TvaPlusHtml.ShowRef> shows = client.loadTvaShows();
			for (final TvaPlusHtml.ShowRef show : shows) {
				final CategoryDTO category = new CategoryDTO(TvaPlusConf.NAME, show.title,
						TvaPlusUrls.showCategoryId(show.slug), TvaPlusConf.EXTENSION);
				category.setDownloadable(true);
				categories.add(category);
			}
			diagnostics.setCreatedItems(categories.size());
			if (categories.size() <= 1) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TVA+ catalogue failed: " + e.getClass().getSimpleName());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TVA+ catalogue failed: " + e.getClass().getSimpleName());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		final TvaPlusDiagnostics diagnostics = new TvaPlusDiagnostics("episodes");
		if (category == null) {
			diagnostics.setRootCauseSummary("null-category");
			getLog().info(diagnostics.formatLogLine());
			return episodes;
		}
		try {
			final List<TvaPlusHtml.EpisodeRef> refs;
			if (TvaPlusUrls.isRecentCategory(category.getId())) {
				diagnostics.setSourceUrl(TvaPlusUrls.recentUrl());
				refs = client.loadRecentTvaEpisodes();
			} else if (TvaPlusUrls.isShowCategory(category.getId())) {
				final String slug = TvaPlusUrls.showSlugFromCategoryId(category.getId());
				diagnostics.setShowSlug(slug);
				if (!TvaPlusUrls.isSafeCategoryShowSlug(slug)) {
					diagnostics.setRootCauseSummary("invalid-show-slug");
					getLog().info(diagnostics.formatLogLine());
					return episodes;
				}
				diagnostics.setSourceUrl(TvaPlusUrls.pageUrl(slug));
				refs = client.loadShowEpisodes(slug);
			} else {
				diagnostics.setRootCauseSummary("unsupported-category");
				getLog().info(diagnostics.formatLogLine());
				return episodes;
			}
			for (final TvaPlusHtml.EpisodeRef ref : refs) {
				final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.watchUrl);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				if (!TvaPlusUrls.isRecentCategory(category.getId())) {
					metadata.setSeriesTitle(category.getName());
				}
				metadata.setEpisodeTitle(ref.title);
				metadata.setSourceUrl(ref.watchUrl);
				metadata.setChannel(TvaPlusConf.CHANNEL_LABEL);
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
			getLog().warn("TVA+ episode listing failed: " + e.getClass().getSimpleName());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TVA+ episode listing failed: " + e.getClass().getSimpleName());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final TvaPlusDiagnostics diagnostics = new TvaPlusDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		final String sanitized = TvaPlusUrls.sanitizeEpisodeUrl(downloadParam.getDownloadInput());
		try {
			if (sanitized == null) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TvaPlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			final DownloadParamDTO effectiveParam = sanitized.equals(downloadParam.getDownloadInput())
					? downloadParam
					: DownloadParamDTO.buildDownloadParam(downloadParam, sanitized);
			// yt-dlp tvaplus extractor → Brightcove; may require CA network.
			diagnostics.setRootCauseSummary("delegate-ytdlp");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(effectiveParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			if (TvaPlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			final String safeId = sanitized != null ? sanitized : TvaPlusConf.NAME;
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, safeId, safeId),
					TvaPlusConf.NAME, e));
			throw new DownloadFailedException(TvaPlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TvaPlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (TvaPlusUrls.sanitizeEpisodeUrl(downloadInput) != null) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
