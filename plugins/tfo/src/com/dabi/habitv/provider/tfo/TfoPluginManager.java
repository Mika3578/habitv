package com.dabi.habitv.provider.tfo;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
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
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadFailureDiagnostics;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

/**
 * TFO provider: public HTML catalogue discovery and JWPlayer HLS download.
 * Streams are often Canada-geo-limited; Habitv reports standard unavailable
 * errors without geo bypass, DRM unlock, or credential handling.
 */
public class TfoPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final TfoClient client;

	public TfoPluginManager() {
		this.client = new TfoClient(new TfoClient.ContentLoader() {
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

	TfoPluginManager(final TfoClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return TfoConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final TfoDiagnostics diagnostics = new TfoDiagnostics("catalogue");
		int created = 0;
		boolean hadError = false;
		for (final String[] catalog : TfoConf.CATALOGS) {
			final String catalogSlug = catalog[0];
			final String catalogName = catalog[1];
			final CategoryDTO root = new CategoryDTO(TfoConf.NAME, catalogName,
					TfoUrls.catalogCategoryId(catalogSlug), TfoConf.EXTENSION);
			root.setDownloadable(false);
			diagnostics.setCatalog(catalogSlug);
			diagnostics.setSourceUrl(TfoUrls.catalogPageUrl(catalogSlug));
			try {
				final List<TfoHtml.CatalogItem> items = client.loadCatalogItems(catalogSlug);
				for (final TfoHtml.CatalogItem item : items) {
					final CategoryDTO show = new CategoryDTO(TfoConf.NAME, item.title,
							TfoUrls.showCategoryId(item.path), TfoConf.EXTENSION);
					show.setDownloadable(true);
					root.addSubCategory(show);
					created++;
				}
			} catch (final IOException e) {
				hadError = true;
				diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
				getLog().warn("TFO catalogue failed for " + catalogSlug + ": " + e.getMessage());
			} catch (final RuntimeException e) {
				hadError = true;
				diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
				getLog().warn("TFO catalogue failed for " + catalogSlug + ": " + e.getMessage());
			}
			categories.add(root);
		}
		diagnostics.setCreatedItems(created);
		if (!hadError && created == 0) {
			diagnostics.setRootCauseSummary("empty-catalog");
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !TfoUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String path = TfoUrls.showPathFromCategoryId(category.getId());
		final TfoDiagnostics diagnostics = new TfoDiagnostics("episodes");
		diagnostics.setShowPath(path);
		diagnostics.setSourceUrl(TfoUrls.absoluteUrl(path));
		try {
			if (TfoUrls.isSeriePath(path)) {
				final List<Map<String, Object>> seasons = client.loadSeasons(path);
				final List<TfoHtml.EpisodeRef> refs = TfoHtml.episodesFromSeasons(seasons);
				for (final TfoHtml.EpisodeRef ref : refs) {
					episodes.add(toEpisode(category, ref));
				}
			} else if (TfoUrls.isProductPath(path)) {
				final String watchUrl = TfoUrls.regarderUrlFromProductPath(path);
				if (TfoUrls.isTfoWatchUrl(watchUrl)) {
					final TfoHtml.EpisodeRef ref = new TfoHtml.EpisodeRef(category.getName(), watchUrl, null, null,
							null);
					episodes.add(toEpisode(category, ref));
				}
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("TFO episode listing failed for " + path + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TFO episode listing failed for " + path + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final TfoDiagnostics diagnostics = new TfoDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			if (!TfoUrls.isTfoWatchUrl(downloadParam.getDownloadInput())) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			final String watchHtml = client.fetchWatchPage(downloadParam.getDownloadInput());
			final TfoHtml.WatchMedia media = TfoHtml.extractWatchMedia(watchHtml);
			if (media == null || StringUtils.isEmpty(media.playlistUrl)) {
				diagnostics.setRootCauseSummary("missing-jwplayer-playlist");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setRootCauseSummary("ok-hls");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, media.playlistUrl),
					downloaders, FrameworkConf.FFMPEG);
		} catch (final DownloadFailedException e) {
			if (TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					TfoConf.NAME, e));
			throw new DownloadFailedException(TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TfoConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (TfoUrls.isTfoWatchUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static EpisodeDTO toEpisode(final CategoryDTO category, final TfoHtml.EpisodeRef ref) {
		final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.watchUrl);
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle(category.getName());
		metadata.setEpisodeTitle(ref.title);
		metadata.setSourceUrl(ref.watchUrl);
		metadata.setChannel(TfoConf.CHANNEL_LABEL);
		if (ref.seasonNumber != null) {
			metadata.setSeasonNumber(ref.seasonNumber);
		}
		if (ref.episodeNumber != null) {
			metadata.setEpisodeNumber(ref.episodeNumber);
		}
		if (StringUtils.isNotEmpty(ref.description)) {
			metadata.setDescription(ref.description);
		}
		episode.setMetadata(metadata);
		return episode;
	}
}
