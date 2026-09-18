package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

public class Tf1PlusPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final Tf1PlusCatalogClient catalogClient;

	public Tf1PlusPluginManager() {
		this.catalogClient = new Tf1PlusCatalogClient(Tf1PlusHttpClient.pluginTransport(new Tf1PlusHttpClient.ProxySource() {
			@Override
			public Proxy current() {
				return getHttpProxy();
			}
		}));
	}

	Tf1PlusPluginManager(final Tf1PlusCatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@Override
	public String getName() {
		return Tf1PlusConf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Map<String, List<Map<String, Object>>> programsByChannel = new LinkedHashMap<String, List<Map<String, Object>>>();
		for (final String slug : Tf1PlusConf.CHANNEL_SLUGS) {
			try {
				programsByChannel.put(slug, catalogClient.fetchPrograms(slug));
			} catch (final IOException e) {
				getLog().warn("TF1+ program discovery failed safely for channel " + slug + ": " + e.getMessage());
				programsByChannel.put(slug, java.util.Collections.<Map<String, Object>>emptyList());
			} catch (final RuntimeException e) {
				getLog().warn("TF1+ program discovery failed safely for channel " + slug + ": " + e.getMessage());
				programsByChannel.put(slug, java.util.Collections.<Map<String, Object>>emptyList());
			}
		}
		return Tf1PlusCatalogMapper.mapChannels(programsByChannel);
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		if (category == null || !Tf1PlusConf.KIND_PROGRAM.equals(category.getParameter(Tf1PlusConf.PARAMETER_KIND))) {
			return new java.util.LinkedHashSet<EpisodeDTO>();
		}
		final String programSlug = category.getParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG);
		if (StringUtils.isEmpty(programSlug)) {
			return new java.util.LinkedHashSet<EpisodeDTO>();
		}
		try {
			return Tf1PlusCatalogMapper.mapEpisodes(category, catalogClient.fetchReplayVideos(programSlug));
		} catch (final IOException e) {
			getLog().warn("TF1+ episode listing failed safely for " + programSlug + ": " + e.getMessage());
			return new java.util.LinkedHashSet<EpisodeDTO>();
		} catch (final RuntimeException e) {
			getLog().warn("TF1+ episode listing failed safely for " + programSlug + ": " + e.getMessage());
			return new java.util.LinkedHashSet<EpisodeDTO>();
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		if (downloadParam == null || !Tf1PlusUrls.isApprovedPublicDownloadUrl(downloadParam.getDownloadInput())) {
			throw new DownloadFailedException(Tf1PlusConf.DOWNLOAD_UNAVAILABLE_MESSAGE);
		}
		return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return Tf1PlusUrls.isApprovedPublicDownloadUrl(downloadInput) ? DownloadableState.SPECIFIC
				: DownloadableState.IMPOSSIBLE;
	}

}
