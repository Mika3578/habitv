package com.dabi.habitv.provider.icitoutv;

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
 * ICI TOU.TV provider: free-collection discovery via Next.js HTML and yt-dlp
 * download for public/Member catch-up. Premium EXTRA is excluded. Canada geo
 * and account/session limits fail closed without bypass.
 */
public class IciToutTvPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final IciToutTvClient client;

	public IciToutTvPluginManager() {
		this.client = new IciToutTvClient(new IciToutTvClient.ContentLoader() {
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

	IciToutTvPluginManager(final IciToutTvClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return IciToutTvConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final IciToutTvDiagnostics diagnostics = new IciToutTvDiagnostics("catalogue");
		diagnostics.setSourceUrl(IciToutTvUrls.freeCollectionUrl());
		try {
			final List<IciToutTvHtml.ShowRef> shows = client.loadFreeShows();
			for (final IciToutTvHtml.ShowRef show : shows) {
				final CategoryDTO category = new CategoryDTO(IciToutTvConf.NAME, show.title,
						IciToutTvUrls.showCategoryId(show.slug), IciToutTvConf.EXTENSION);
				category.setDownloadable(true);
				categories.add(category);
			}
			diagnostics.setCreatedItems(categories.size());
			if (categories.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("ICI TOU.TV catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("ICI TOU.TV catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !IciToutTvUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String slug = IciToutTvUrls.showSlugFromCategoryId(category.getId());
		final IciToutTvDiagnostics diagnostics = new IciToutTvDiagnostics("episodes");
		diagnostics.setShowSlug(slug);
		diagnostics.setSourceUrl(IciToutTvUrls.showPageUrl(slug));
		try {
			final List<IciToutTvHtml.EpisodeRef> refs = client.loadShowEpisodes(slug);
			for (final IciToutTvHtml.EpisodeRef ref : refs) {
				final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.watchUrl);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(ref.title);
				metadata.setSourceUrl(ref.watchUrl);
				metadata.setChannel(IciToutTvConf.CHANNEL_LABEL);
				episode.setMetadata(metadata);
				episodes.add(episode);
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("ICI TOU.TV episode listing failed for " + slug + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("ICI TOU.TV episode listing failed for " + slug + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final IciToutTvDiagnostics diagnostics = new IciToutTvDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			if (!IciToutTvUrls.isIciToutTvEpisodeUrl(downloadParam.getDownloadInput())) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(IciToutTvConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			// yt-dlp tou.tv extractor; may require CA network and/or optional cookies.
			diagnostics.setRootCauseSummary("delegate-ytdlp");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			if (IciToutTvConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					IciToutTvConf.NAME, e));
			throw new DownloadFailedException(IciToutTvConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(IciToutTvConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (IciToutTvUrls.isIciToutTvEpisodeUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
