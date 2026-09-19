package com.dabi.habitv.provider.rtbfauvio;

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
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

/**
 * RTBF Auvio diagnostics-first provider: public BFF/HTML catalogue for La Une,
 * Tipik and La Trois. RedBee/Widevine download paths are unsupported.
 */
public class RtbfAuvioPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final RtbfAuvioClient client;

	public RtbfAuvioPluginManager() {
		this.client = new RtbfAuvioClient(new RtbfAuvioClient.ContentLoader() {
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

	RtbfAuvioPluginManager(final RtbfAuvioClient client) {
		this.client = client;
	}

	@Override
	public String getName() {
		return RtbfAuvioConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<CategoryDTO>();
		for (final String[] channel : RtbfAuvioConf.CHANNELS) {
			final CategoryDTO cat = new CategoryDTO(RtbfAuvioConf.NAME, channel[1],
					RtbfAuvioClient.channelPageUrl(channel[0]), RtbfAuvioConf.EXTENSION);
			cat.setDownloadable(true);
			categories.add(cat);
		}
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (category == null || StringUtils.isEmpty(category.getId())) {
			return episodes;
		}
		final RtbfAuvioDiagnostics diagnostics = new RtbfAuvioDiagnostics("episodes");
		diagnostics.setChannel(category.getName());
		diagnostics.setSourceUrl(category.getId());
		try {
			final String channelSlug = channelSlugFromCategoryUrl(category.getId());
			final String html = channelSlug == null ? load(category.getId()) : client.fetchChannelPage(channelSlug);
			final List<String> widgetIds = client.findMediaListWidgetIds(html);
			int created = 0;
			for (final String widgetId : widgetIds) {
				final List<Map<String, Object>> items = client.fetchMediaListItems(widgetId);
				for (final Map<String, Object> item : items) {
					if (!"MEDIA".equals(String.valueOf(item.get("resourceType")))) {
						continue;
					}
					final String path = asString(item.get("path"));
					final String title = buildTitle(item);
					if (StringUtils.isEmpty(path) || StringUtils.isEmpty(title)) {
						continue;
					}
					final String url = path.startsWith("http") ? path : RtbfAuvioConf.HOME_URL + path;
					final EpisodeDTO episode = new EpisodeDTO(category, title, url);
					final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
					metadata.setSeriesTitle(firstNonEmpty(asString(item.get("title")), category.getName()));
					metadata.setEpisodeTitle(title);
					metadata.setSourceUrl(url);
					metadata.setChannel(category.getName());
					final String description = asString(item.get("description"));
					if (StringUtils.isNotEmpty(description)) {
						metadata.setDescription(description);
					}
					episode.setMetadata(metadata);
					episodes.add(episode);
					created++;
				}
			}
			diagnostics.setCreatedItems(created);
			if (created == 0) {
				diagnostics.setRootCauseSummary("empty-media-list");
			}
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			getLog().warn("RTBF Auvio episode listing failed safely: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("runtime:" + e.getClass().getSimpleName());
			getLog().warn("RTBF Auvio episode listing failed safely: " + e.getMessage());
		}
		getLog().info(diagnostics.formatLogLine());
		return episodes;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final RtbfAuvioDiagnostics diagnostics = new RtbfAuvioDiagnostics("download");
		diagnostics.setSourceUrl(downloadParam.getDownloadInput());
		diagnostics.setRootCauseSummary("redbee-drm-unsupported");
		getLog().warn(diagnostics.formatLogLine());
		throw new DownloadFailedException(RtbfAuvioConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (StringUtils.isNotEmpty(downloadInput) && downloadInput.contains("auvio.rtbf.be/media/")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static String channelSlugFromCategoryUrl(final String categoryUrl) {
		if (StringUtils.isEmpty(categoryUrl)) {
			return null;
		}
		final String marker = "/chaine/";
		final int idx = categoryUrl.indexOf(marker);
		if (idx < 0) {
			return null;
		}
		String slug = categoryUrl.substring(idx + marker.length());
		final int q = slug.indexOf('?');
		if (q >= 0) {
			slug = slug.substring(0, q);
		}
		final int slash = slug.indexOf('/');
		if (slash >= 0) {
			slug = slug.substring(0, slash);
		}
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	private String load(final String url) throws IOException {
		try {
			return getUrlContent(url);
		} catch (final RuntimeException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private static String buildTitle(final Map<String, Object> item) {
		final String title = asString(item.get("title"));
		final String subtitle = asString(item.get("subtitle"));
		if (StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(subtitle) && !subtitle.equals(title)) {
			return title + " - " + subtitle;
		}
		return firstNonEmpty(title, subtitle);
	}

	private static String asString(final Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private static String firstNonEmpty(final String first, final String second) {
		if (StringUtils.isNotEmpty(first)) {
			return first;
		}
		return second == null ? "" : second;
	}
}
