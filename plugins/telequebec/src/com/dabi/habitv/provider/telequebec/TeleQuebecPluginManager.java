package com.dabi.habitv.provider.telequebec;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
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
 * Télé-Québec provider: public GraphQL catalogue discovery. Playback streams are
 * often Canada-geo-limited; Habitv reports standard unavailable errors without
 * geo bypass or DRM unlock.
 */
public class TeleQuebecPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final TeleQuebecClient client;

	public TeleQuebecPluginManager() {
		this.client = new TeleQuebecClient(new TeleQuebecClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				try {
					return getUrlContent(url);
				} catch (final RuntimeException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
		}, new TeleQuebecClient.GraphqlPoster() {
			@Override
			public String post(final String url, final String jsonBody) throws IOException {
				return postJson(url, jsonBody);
			}
		});
	}

	TeleQuebecPluginManager(final TeleQuebecClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return TeleQuebecConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		final TeleQuebecDiagnostics diagnostics = new TeleQuebecDiagnostics("catalogue");
		diagnostics.setSourceUrl(client.homeSourceUrl());
		try {
			final String homeHtml = client.fetchHomeHtml();
			final List<String> slugs = TeleQuebecUrls.parseShowSlugs(homeHtml);
			int created = 0;
			for (final String slug : slugs) {
				try {
					final Map<String, Object> collection = client.fetchCollection(slug);
					if (collection == null || collection.isEmpty()) {
						continue;
					}
					final String title = TeleQuebecClient.asString(collection, "title");
					if (StringUtils.isEmpty(title)) {
						continue;
					}
					final CategoryDTO show = new CategoryDTO(TeleQuebecConf.NAME, title,
							TeleQuebecUrls.showCategoryId(slug), TeleQuebecConf.EXTENSION);
					show.setDownloadable(true);
					categories.add(show);
					created++;
				} catch (final IOException e) {
					getLog().warn("Télé-Québec show skipped (" + slug + "): " + e.getMessage());
				}
			}
			diagnostics.setCreatedItems(created);
			if (created == 0) {
				diagnostics.setRootCauseSummary("empty-catalog");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("Télé-Québec catalogue failed: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Télé-Québec catalogue failed: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || !TeleQuebecUrls.isShowCategory(category.getId())) {
			return episodes;
		}
		final String slug = TeleQuebecUrls.showSlugFromCategoryId(category.getId());
		final TeleQuebecDiagnostics diagnostics = new TeleQuebecDiagnostics("episodes");
		diagnostics.setShowSlug(slug);
		diagnostics.setSourceUrl(TeleQuebecConf.GRAPHQL_URL);
		try {
			final Map<String, Object> collection = client.fetchCollection(slug);
			final List<Map<String, Object>> seasons = TeleQuebecClient.asObjectList(collection, "seasons");
			int seasonIndex = 0;
			for (final Map<String, Object> season : seasons) {
				if (seasonIndex >= TeleQuebecConf.MAX_SEASONS) {
					break;
				}
				seasonIndex++;
				final Integer seasonNumber = TeleQuebecClient.asInt(season, "seasonNumber");
				final Integer episodeCount = TeleQuebecClient.asInt(season, "episodeCount");
				if (seasonNumber == null || episodeCount == null || episodeCount.intValue() <= 0) {
					continue;
				}
				final List<Map<String, Object>> seasonEpisodes = client.fetchSeasonEpisodes(slug,
						seasonNumber.intValue(), episodeCount.intValue());
				for (final Map<String, Object> item : seasonEpisodes) {
					final String title = TeleQuebecClient.asString(item, "title");
					String url = TeleQuebecClient.asString(item, "videoCanonicalUrl");
					final Integer epNum = TeleQuebecClient.asInt(item, "episodeNumber");
					final Integer seNum = TeleQuebecClient.asInt(item, "seasonNumber");
					if (StringUtils.isEmpty(url) && epNum != null && seNum != null) {
						url = TeleQuebecUrls.watchUrl(slug, seNum.intValue(), epNum.intValue());
					}
					if (StringUtils.isEmpty(title) || StringUtils.isEmpty(url)
							|| !TeleQuebecUrls.isTeleQuebecWatchUrl(url)) {
						continue;
					}
					final String availability = TeleQuebecClient.asString(item, "availabilityStatus");
					if (availability != null && !"AVAILABLE".equalsIgnoreCase(availability)) {
						continue;
					}
					final EpisodeDTO episode = new EpisodeDTO(category, title, url);
					final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
					metadata.setSeriesTitle(category.getName());
					metadata.setEpisodeTitle(title);
					metadata.setSourceUrl(url);
					metadata.setChannel(TeleQuebecConf.CHANNEL_LABEL);
					if (seNum != null) {
						metadata.setSeasonNumber(seNum);
					}
					if (epNum != null) {
						metadata.setEpisodeNumber(epNum);
					}
					episode.setMetadata(metadata);
					episodes.add(episode);
				}
			}
			diagnostics.setCreatedItems(episodes.size());
			if (episodes.isEmpty()) {
				diagnostics.setRootCauseSummary("empty-replay-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("Télé-Québec episode listing failed for " + slug + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("Télé-Québec episode listing failed for " + slug + ": " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final TeleQuebecDiagnostics diagnostics = new TeleQuebecDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		try {
			final WatchRef watchRef = parseWatchRef(downloadParam.getDownloadInput());
			if (watchRef == null) {
				diagnostics.setRootCauseSummary("unsupported-url");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setShowSlug(watchRef.slug);
			final Map<String, Object> episode = client.fetchEpisode(watchRef.slug, watchRef.season, watchRef.episode);
			if (TeleQuebecClient.isPlaybackUnavailable(episode)) {
				diagnostics.setRootCauseSummary("geo-or-rights-unavailable");
				getLog().warn(diagnostics.formatLogLine());
				throw new DownloadFailedException(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			final String streamUrl = TeleQuebecClient.extractStreamUrl(episode);
			if (StringUtils.isNotEmpty(streamUrl)) {
				final String downloader = streamUrl.contains(".m3u8") ? FrameworkConf.FFMPEG : FrameworkConf.CURL;
				diagnostics.setRootCauseSummary("ok-stream");
				getLog().info(diagnostics.formatLogLine());
				return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, streamUrl), downloaders,
						downloader);
			}
			// Fallback: public watch page via yt-dlp when upstream supports it.
			diagnostics.setRootCauseSummary("delegate-ytdlp");
			getLog().info(diagnostics.formatLogLine());
			return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
		} catch (final DownloadFailedException e) {
			if (TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE.equals(e.getMessage())) {
				throw e;
			}
			diagnostics.setRootCauseSummary(DownloadFailureDiagnostics.getClassificationKey(e) == null
					? "download-failed"
					: DownloadFailureDiagnostics.getClassificationKey(e));
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn(diagnostics.formatLogLine());
			throw new DownloadFailedException(TeleQuebecConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (TeleQuebecUrls.isTeleQuebecWatchUrl(downloadInput)) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static WatchRef parseWatchRef(final String url) {
		if (!TeleQuebecUrls.isTeleQuebecWatchUrl(url)) {
			return null;
		}
		try {
			final String path = new URL(url).getPath();
			final String[] parts = path.split("/");
			// /regarder/{slug}/{season}/{episode}
			int regarder = -1;
			for (int i = 0; i < parts.length; i++) {
				if ("regarder".equalsIgnoreCase(parts[i])) {
					regarder = i;
					break;
				}
			}
			if (regarder < 0 || regarder + 3 >= parts.length) {
				return null;
			}
			final String slug = parts[regarder + 1];
			final int season = Integer.parseInt(parts[regarder + 2]);
			final int episode = Integer.parseInt(parts[regarder + 3]);
			if (!TeleQuebecUrls.isUsableShowSlug(slug)) {
				return null;
			}
			return new WatchRef(slug, season, episode);
		} catch (final Exception e) {
			return null;
		}
	}

	private static String postJson(final String url, final String jsonBody) throws IOException {
		final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "HabiTV");
		final byte[] bytes = jsonBody.getBytes(Charset.forName("UTF-8"));
		connection.setFixedLengthStreamingMode(bytes.length);
		final OutputStream output = connection.getOutputStream();
		try {
			output.write(bytes);
		} finally {
			output.close();
		}
		final int status = connection.getResponseCode();
		final java.io.InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
		if (stream == null) {
			throw new IOException("empty-response status=" + status);
		}
		try {
			final byte[] buffer = new byte[4096];
			final StringBuilder response = new StringBuilder();
			int read;
			while ((read = stream.read(buffer)) != -1) {
				response.append(new String(buffer, 0, read, Charset.forName("UTF-8")));
			}
			if (status >= 400) {
				throw new IOException("http-" + status);
			}
			return response.toString();
		} finally {
			stream.close();
			connection.disconnect();
		}
	}

	private static final class WatchRef {
		private final String slug;
		private final int season;
		private final int episode;

		private WatchRef(final String slug, final int season, final int episode) {
			this.slug = slug;
			this.season = season;
			this.episode = episode;
		}
	}
}
