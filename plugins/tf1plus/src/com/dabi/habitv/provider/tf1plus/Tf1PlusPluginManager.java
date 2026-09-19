package com.dabi.habitv.provider.tf1plus;

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
 * TF1+ provider: public GraphQL catalogue discovery and yt-dlp download of
 * public replay page URLs. Does not implement login, DRM, or geo bypass.
 */
public class Tf1PlusPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final Tf1PlusGraphqlClient graphqlClient;

	public Tf1PlusPluginManager() {
		this.graphqlClient = new Tf1PlusGraphqlClient(new Tf1PlusGraphqlClient.ContentLoader() {
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

	Tf1PlusPluginManager(final Tf1PlusGraphqlClient graphqlClient) {
		this.graphqlClient = graphqlClient;
	}

	@Override
	public String getName() {
		return Tf1PlusConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		for (final String[] channel : Tf1PlusConf.CHANNELS) {
			final String channelSlug = channel[0];
			final String channelLabel = channel[1];
			final Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics("catalogue");
			diagnostics.setChannel(channelSlug);
			diagnostics.setSourceUrl(graphqlClient.programsSourceUrl(channelSlug));
			final CategoryDTO channelCat = new CategoryDTO(Tf1PlusConf.NAME, channelLabel,
					Tf1PlusUrls.channelCategoryId(channelSlug), Tf1PlusConf.EXTENSION);
			channelCat.setDownloadable(false);
			try {
				final List<Map<String, Object>> programs = graphqlClient.fetchPrograms(channelSlug);
				int created = 0;
				for (final Map<String, Object> program : programs) {
					if (program == null) {
						continue;
					}
					final String programSlug = Tf1PlusUrls.programSlug(program);
					final String programName = Tf1PlusUrls.programName(program);
					if (StringUtils.isEmpty(programSlug) || StringUtils.isEmpty(programName)) {
						continue;
					}
					if (programSlug.toLowerCase(Locale.ROOT).contains("novo19")) {
						continue;
					}
					final CategoryDTO programCat = new CategoryDTO(Tf1PlusConf.NAME, programName,
							Tf1PlusUrls.programCategoryId(channelSlug, programSlug), Tf1PlusConf.EXTENSION);
					programCat.setDownloadable(true);
					channelCat.addSubCategory(programCat);
					created++;
				}
				diagnostics.setCreatedItems(created);
				if (created == 0) {
					diagnostics.setRootCauseSummary("empty-catalog");
				}
			} catch (final IOException e) {
				diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
				getLog().warn("TF1+ catalogue failed for channel " + channelSlug + ": " + e.getMessage());
			} catch (final RuntimeException e) {
				diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
				getLog().warn("TF1+ catalogue failed for channel " + channelSlug + ": " + e.getMessage());
			}
			getLog().info(diagnostics.formatLogLine());
			categories.add(channelCat);
		}
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !Tf1PlusUrls.isProgramCategory(category.getId())) {
			return episodes;
		}
		final String channelSlug = Tf1PlusUrls.channelSlugFromCategoryId(category.getId());
		final String programSlug = Tf1PlusUrls.programSlugFromCategoryId(category.getId());
		final Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics("episodes");
		diagnostics.setChannel(channelSlug);
		diagnostics.setProgramSlug(programSlug);
		diagnostics.setSourceUrl(graphqlClient.videosSourceUrl(programSlug));
		try {
			final List<Map<String, Object>> videos = graphqlClient.fetchReplayVideos(programSlug);
			for (final Map<String, Object> video : videos) {
				if (video == null) {
					continue;
				}
				final String url = Tf1PlusUrls.videoUrl(video);
				final String title = Tf1PlusUrls.videoTitle(video);
				if (StringUtils.isEmpty(url) || StringUtils.isEmpty(title) || !Tf1PlusUrls.isTf1PlusVideoPageUrl(url)) {
					continue;
				}
				final EpisodeDTO episode = new EpisodeDTO(category, title, url);
				final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
				metadata.setSeriesTitle(category.getName());
				metadata.setEpisodeTitle(title);
				metadata.setSourceUrl(url);
				if (StringUtils.isNotEmpty(channelSlug)) {
					metadata.setChannel(channelSlug);
				}
				final String description = Tf1PlusUrls.videoDescription(video);
				if (StringUtils.isNotEmpty(description)) {
					metadata.setDescription(description);
				}
				final Date publicationDate = Tf1PlusUrls.videoPublicationDate(video);
				if (publicationDate != null) {
					// GraphQL "date" is catalogue publication, not broadcast airDate.
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
			getLog().warn("TF1+ episode listing failed for " + programSlug + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("TF1+ episode listing failed for " + programSlug + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			if (!Tf1PlusUrls.isTf1PlusVideoPageUrl(downloadParam.getDownloadInput())) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(Tf1PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setRootCauseSummary("delegate-ytdlp");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			if (Tf1PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			getLog().warn(DownloadFailureDiagnostics.formatLogLine(
					new EpisodeDTO(null, downloadParam.getDownloadInput(), downloadParam.getDownloadInput()),
					Tf1PlusConf.NAME, e));
			throw new DownloadFailedException(Tf1PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(Tf1PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (Tf1PlusUrls.isTf1PlusVideoPageUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}
}
