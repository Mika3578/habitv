package com.dabi.habitv.provider.bfmtv;

import java.io.IOException;
import java.net.Proxy;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class BfmTvPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final BfmTvCatalogClient catalogClient;

	public BfmTvPluginManager() {
		this.catalogClient = new BfmTvCatalogClient(BfmTvHttpClient.pluginTransport(new BfmTvHttpClient.ProxySource() {
			@Override
			public Proxy current() {
				return getHttpProxy();
			}
		}));
	}

	BfmTvPluginManager(final BfmTvCatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@Override
	public String getName() {
		return BfmTvConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> channels = new LinkedHashSet<CategoryDTO>();
		for (final String channelId : BfmTvConf.CHANNEL_IDS) {
			try {
				channels.add(BfmTvCatalogMapper.mapChannel(channelId, catalogClient.fetchReplayPrograms(channelId)));
			} catch (final IOException e) {
				getLog().warn("BFMTV catalog discovery failed safely for " + channelId + ": " + e.getMessage());
				channels.add(BfmTvCatalogMapper.mapChannel(channelId, java.util.Collections.<java.util.Map<String, Object>>emptyList()));
			} catch (final RuntimeException e) {
				getLog().warn("BFMTV catalog discovery failed safely for " + channelId + ": " + e.getMessage());
				channels.add(BfmTvCatalogMapper.mapChannel(channelId, java.util.Collections.<java.util.Map<String, Object>>emptyList()));
			}
		}
		return channels;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		if (category == null || !BfmTvConf.KIND_PROGRAM.equals(category.getParameter(BfmTvConf.PARAMETER_KIND))) {
			return new LinkedHashSet<EpisodeDTO>();
		}
		final String channelId = category.getParameter(BfmTvConf.PARAMETER_CHANNEL);
		final String categoryId = category.getParameter(BfmTvConf.PARAMETER_CATEGORY);
		if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(categoryId)) {
			return new LinkedHashSet<EpisodeDTO>();
		}
		try {
			return BfmTvCatalogMapper.mapEpisodes(category, catalogClient.fetchVideos(channelId, categoryId));
		} catch (final IOException e) {
			getLog().warn("BFMTV episode listing failed safely for " + categoryId + ": " + e.getMessage());
			return new LinkedHashSet<EpisodeDTO>();
		} catch (final RuntimeException e) {
			getLog().warn("BFMTV episode listing failed safely for " + categoryId + ": " + e.getMessage());
			return new LinkedHashSet<EpisodeDTO>();
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		if (downloadParam == null || !BfmTvUrls.isApprovedPublicDownloadUrl(downloadParam.getDownloadInput())) {
			throw new DownloadFailedException(BfmTvConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
		}
		return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return BfmTvUrls.isApprovedPublicDownloadUrl(downloadInput) ? DownloadableState.SPECIFIC
				: DownloadableState.IMPOSSIBLE;
	}

}
