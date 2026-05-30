package com.dabi.habitv.core.mgr;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;
import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.config.YoutubeApiKeyConfig;
import com.dabi.habitv.core.token.TokenReplacer;
import com.dabi.habitv.framework.FWKProperties;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.utils.DirUtils;

public final class CoreManager {
	private static final String YOUTUBE_API_KEY_PROPERTY = "habitv.youtube.apiKey";

	private final CategoryManager categoryManager;

	private final EpisodeManager episodeManager;

	private final Map<String, Integer> taskName2PoolSizeMap;

	private static final Logger LOG = Logger.getLogger(CoreManager.class);

	private final PluginManager pluginManager;

	public CoreManager(final UserConfig config) {
		stat();
		LOG.info("habitv version " + FWKProperties.getVersion());
		taskName2PoolSizeMap = config.getTaskDefinition();
		TokenReplacer.setCutSize(config.getFileNameCutSize());
		applyYoutubeApiKey(config.getYoutubeApiKey());
		pluginManager = new PluginManager(config);
		episodeManager = new EpisodeManager(pluginManager.getDownloadersHolder(), pluginManager.getExportersHolder(),
		        pluginManager.getProvidersHolder(), taskName2PoolSizeMap, config.getMaxAttempts(),
		        config.getMaxConcurrentDownloads(), DirUtils.getAppDir());
		categoryManager = new CategoryManager(pluginManager.getProvidersHolder(), taskName2PoolSizeMap);

		setProxy(config);
	}

	private void stat() {
		if (!Boolean.getBoolean(HabitTvConf.STAT_ENABLED_PROPERTY)) {
			LOG.debug("Startup telemetry ping is disabled.");
			return;
		}
		final String statUrl = System.getProperty(HabitTvConf.STAT_URL_PROPERTY, HabitTvConf.STAT_URL_DISABLED);
		if (statUrl == null || statUrl.trim().isEmpty()) {
			LOG.warn("Startup telemetry ping is enabled but no URL is configured.");
			return;
		}
		new Thread() {

			@Override
			public void run() {
				RetrieverUtils.getUrlContent(statUrl, null);
			}

		}.start();
	}

	private void setProxy(final UserConfig config) {
		final Map<String, Map<ProtocolEnum, ProxyDTO>> plugin2protocol2proxy = config.getProxy();

		// set the defaut http proxy
		final Map<ProtocolEnum, ProxyDTO> defaultProxyMap = plugin2protocol2proxy.get(null);
		if (defaultProxyMap != null) {
			final ProxyDTO httpProxy = defaultProxyMap.get(ProxyDTO.ProtocolEnum.HTTP);
			if (httpProxy != null) {
				setHttpProxy(httpProxy);
			}
		}

		pluginManager.setProxy(defaultProxyMap, plugin2protocol2proxy);
	}

	private void setHttpProxy(final ProxyDTO httpProxy) {
		System.setProperty("http.proxyHost", httpProxy.getHost());
		System.setProperty("http.proxyPort", String.valueOf(httpProxy.getPort()));
	}

	private void applyYoutubeApiKey(String youtubeApiKey) {
		final String sanitized = YoutubeApiKeyConfig.sanitizePlainConfigValue(youtubeApiKey);
		if (sanitized != null) {
			System.setProperty(YOUTUBE_API_KEY_PROPERTY, sanitized);
		} else {
			System.clearProperty(YOUTUBE_API_KEY_PROPERTY);
		}
	}

	public CategoryManager getCategoryManager() {
		return categoryManager;
	}

	public EpisodeManager getEpisodeManager() {
		return episodeManager;
	}

	public void retreiveEpisode(final Map<String, CategoryDTO> categoriesToGrab) {
		getEpisodeManager().retreiveEpisode(categoriesToGrab);
	}

	public Map<String, CategoryDTO> findCategory() {
		return getCategoryManager().findCategory();
	}

	public Map<String, CategoryDTO> findCategory(List<String> pluginList) {
		return getCategoryManager().findCategory(pluginList);
	}

	public void forceEnd() {
		if (episodeManager != null) {
			episodeManager.forceEnd();
		}
		if (categoryManager != null) {
			categoryManager.forceEnd();
		}
	}

	public void reTryExport() {
		episodeManager.reTryExport();
	}

	public void reTryExport(List<String> pluginList) {
		episodeManager.reTryExport(pluginList);
	}

	public boolean hasExportToResume() {
		return episodeManager.hasExportToResume();
	}

	public void clearExport() {
		episodeManager.clearExport();
	}

	public void update() {
		pluginManager.update();
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setDownloaded(EpisodeDTO episode) {
		episodeManager.setDownloaded(episode);
	}

	public void restart(EpisodeDTO episode, boolean exportOnly) {
		episodeManager.restart(episode, exportOnly);
	}

	public com.dabi.habitv.core.task.BatchEnqueueResult enqueueEpisodesForDownload(
			final Collection<EpisodeDTO> episodes) {
		return episodeManager.enqueueEpisodesForDownload(episodes);
	}

	public Collection<EpisodeDTO> findEpisodeByCategory(CategoryDTO category) {
		return episodeManager.findEpisodeByCategory(category);
	}

	public Set<String> findDownloadedEpisodes(CategoryDTO category) {
		return episodeManager.findDownloadedEpisodes(category);
	}

	public void cancel(EpisodeDTO episode) {
		episodeManager.cancelTask(episode);
	}

}
