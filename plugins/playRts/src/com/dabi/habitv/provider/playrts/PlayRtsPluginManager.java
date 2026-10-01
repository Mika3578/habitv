package com.dabi.habitv.provider.playrts;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
 * Play RTS provider: public v3/IL catalogue discovery and yt-dlp download of
 * public replay page URLs. Does not implement login, DRM, or geo bypass.
 */
public class PlayRtsPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final PlayRtsClient client;

	public PlayRtsPluginManager() {
		this.client = new PlayRtsClient(new PlayRtsClient.ContentLoader() {
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

	PlayRtsPluginManager(final PlayRtsClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return PlayRtsConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final PlayRtsDiagnostics diagnostics = new PlayRtsDiagnostics("catalogue");
		diagnostics.setSourceUrl(client.showsSourceUrl());
		final CategoryDTO emissions = new CategoryDTO(PlayRtsConf.NAME, PlayRtsConf.EMISSIONS_LABEL,
				PlayRtsConf.CATEGORY_EMISSIONS_ID, PlayRtsConf.EXTENSION);
		emissions.setDownloadable(false);
		try {
			final List<Map<String, Object>> shows = client.fetchShows();
			int created = 0;
			for (final Map<String, Object> show : shows) {
				if (show == null) {
					continue;
				}
				final String transmission = show.get("transmission") == null ? ""
						: String.valueOf(show.get("transmission")).trim();
				if (StringUtils.isNotEmpty(transmission)
						&& !"TV".equalsIgnoreCase(transmission)) {
					continue;
				}
				final String showId = PlayRtsUrls.showId(show);
				final String title = PlayRtsUrls.showTitle(show);
				if (StringUtils.isEmpty(showId) || StringUtils.isEmpty(title)) {
					continue;
				}
				final CategoryDTO showCat = new CategoryDTO(PlayRtsConf.NAME, title,
						PlayRtsUrls.showCategoryId(showId), PlayRtsConf.EXTENSION);
				showCat.setDownloadable(true);
				emissions.addSubCategory(showCat);
				created++;
			}
			diagnostics.setCreatedItems(created);
			if (created == 0) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("Play RTS catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Play RTS catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		categories.add(emissions);
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !PlayRtsUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String showId = PlayRtsUrls.showIdFromCategoryId(category.getId());
		final PlayRtsDiagnostics diagnostics = new PlayRtsDiagnostics("episodes");
		diagnostics.setShowId(showId);
		diagnostics.setSourceUrl(client.mediaListSourceUrl(showId));
		try {
			final List<Map<String, Object>> videos = client.fetchVideosByShowId(showId);
			for (final Map<String, Object> video : videos) {
				if (video == null) {
					continue;
				}
				final String mediaType = video.get("mediaType") == null ? ""
						: String.valueOf(video.get("mediaType")).trim().toUpperCase(Locale.ROOT);
				if (StringUtils.isNotEmpty(mediaType) && !"VIDEO".equals(mediaType)) {
					continue;
				}
				final String url = PlayRtsUrls.videoPageUrl(video);
				final String title = PlayRtsUrls.videoTitle(video);
				if (StringUtils.isEmpty(url) || StringUtils.isEmpty(title)
						|| !PlayRtsUrls.isPlayRtsVideoPageUrl(url)) {
					continue;
				}
				final EpisodeDTO episode = new EpisodeDTO(category, title, url);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(title);
				metadata.setSourceUrl(url);
				metadata.setChannel(PlayRtsConf.NAME);
				final String description = PlayRtsUrls.videoDescription(video);
				if (StringUtils.isNotEmpty(description)) {
					metadata.setDescription(description);
				}
				final Date publicationDate = PlayRtsUrls.videoPublicationDate(video);
				if (publicationDate != null) {
					metadata.setPublicationDate(publicationDate);
					episode.setEpisodeDate(publicationDate);
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
			getLog().warn("Play RTS episode listing failed for show " + showId + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Play RTS episode listing failed for show " + showId + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		try {
			return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			final PlayRtsDiagnostics diagnostics = new PlayRtsDiagnostics("download");
			diagnostics.setSourceUrl(downloadParam.getDownloadInput());
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					PlayRtsConf.NAME, e));
			throw new DownloadFailedException(PlayRtsConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			final PlayRtsDiagnostics diagnostics = new PlayRtsDiagnostics("download");
			diagnostics.setSourceUrl(downloadParam.getDownloadInput());
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(PlayRtsConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (PlayRtsUrls.isPlayRtsVideoPageUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
