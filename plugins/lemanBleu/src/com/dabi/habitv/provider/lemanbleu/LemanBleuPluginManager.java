package com.dabi.habitv.provider.lemanbleu;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
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
import com.dabi.habitv.provider.lemanbleu.LemanBleuHtml.EpisodeRef;
import com.dabi.habitv.provider.lemanbleu.LemanBleuHtml.ProgramRef;

/**
 * Léman Bleu provider: public HTML catalogue discovery and curl download of
 * Infomaniak progressive MP4 URLs. Does not implement login, DRM, or geo bypass.
 */
public class LemanBleuPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	public LemanBleuPluginManager() {
		this.contentLoader = new ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				try {
					return getUrlContent(url);
				} catch (final RuntimeException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
		};
	}

	LemanBleuPluginManager(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	@Override
	public String getName() {
		return LemanBleuConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final LemanBleuDiagnostics diagnostics = new LemanBleuDiagnostics("catalogue");
		diagnostics.setSourceUrl(LemanBleuHtml.programsUrl());
		try {
			final String html = contentLoader.load(LemanBleuHtml.programsUrl());
			final List<ProgramRef> programs = LemanBleuHtml.parsePrograms(html);
			for (final ProgramRef program : programs) {
				final CategoryDTO show = new CategoryDTO(LemanBleuConf.NAME, program.title,
						LemanBleuHtml.showCategoryId(program.id), LemanBleuConf.EXTENSION);
				show.setDownloadable(true);
				categories.add(show);
			}
			diagnostics.setCreatedItems(categories.size());
			if (categories.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("Léman Bleu catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Léman Bleu catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !LemanBleuHtml.isShowCategory(category.getId())) {
			return episodes;
		}
		final String emissionId = LemanBleuHtml.emissionIdFromCategoryId(category.getId());
		final LemanBleuDiagnostics diagnostics = new LemanBleuDiagnostics("episodes");
		diagnostics.setShowId(emissionId);
		final String archiveUrl = LemanBleuHtml.archiveUrl(emissionId);
		diagnostics.setSourceUrl(archiveUrl);
		try {
			final String html = contentLoader.load(archiveUrl);
			final List<EpisodeRef> refs = LemanBleuHtml.parseArchiveEpisodes(html);
			for (final EpisodeRef ref : refs) {
				if (StringUtils.isEmpty(ref.url) || StringUtils.isEmpty(ref.title)
						|| !LemanBleuHtml.isLemanBleuEpisodeUrl(ref.url)) {
					continue;
				}
				final EpisodeDTO episode = new EpisodeDTO(category, ref.title, ref.url);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(ref.title);
				metadata.setSourceUrl(ref.url);
				metadata.setChannel(LemanBleuConf.CHANNEL_LABEL);
				episode.setMetadata(metadata);
				episodes.add(episode);
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("Léman Bleu episode listing failed for " + emissionId + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Léman Bleu episode listing failed for " + emissionId + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final LemanBleuDiagnostics diagnostics = new LemanBleuDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			final String pageUrl = LemanBleuHtml.toVideosHostUrl(downloadParam.getDownloadInput());
			final String pageHtml = contentLoader.load(pageUrl);
			final String mp4Url = LemanBleuHtml.extractBestMp4Url(pageHtml);
			if (StringUtils.isEmpty(mp4Url)) {
				diagnostics.setRootCauseSummary("mp4-not-found");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setRootCauseSummary("ok");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, mp4Url), downloaders,
					FrameworkConf.CURL);
		} catch (final DownloadFailedException e) {
			if (LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					LemanBleuConf.NAME, e));
			throw new DownloadFailedException(LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(LemanBleuConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (LemanBleuHtml.isLemanBleuEpisodeUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
