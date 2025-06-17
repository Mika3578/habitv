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
import com.dabi.habitv.core.token.TokenReplacer;
import com.dabi.habitv.core.utils.FWKProperties;
import com.dabi.habitv.core.utils.RetrieverUtils;
import com.dabi.habitv.utils.DirUtils;
import com.dabi.habitv.api.logging.HabitvLogger;

public final class CoreManager {

	private final CategoryManager categoryManager;

	private final EpisodeManager episodeManager;

	private final Map<String, Integer> taskName2PoolSizeMap;

	private static final Logger LOG = HabitvLogger.getLogger(CoreManager.class);

	private final PluginManager pluginManager;

	public CoreManager(final UserConfig config) {
		stat();
		LOG.info("habiTv version " + FWKProperties.getVersion());
		LOG.info("Initializing CoreManager with configuration");
		taskName2PoolSizeMap = config.getTaskDefinition();
		TokenReplacer.setCutSize(config.getFileNameCutSize());
		pluginManager = new PluginManager(config);
		episodeManager = new EpisodeManager(pluginManager.getDownloadersHolder(), pluginManager.getExportersHolder(),
		        pluginManager.getProvidersHolder(), taskName2PoolSizeMap, config.getMaxAttempts(), DirUtils.getAppDir());
		categoryManager = new CategoryManager(pluginManager.getProvidersHolder(), taskName2PoolSizeMap);

		setProxy(config);
		LOG.info("CoreManager initialization completed");
	}

	private void stat() {
		new Thread() {

			@Override
			public void run() {
				try {
					RetrieverUtils.getUrlContent(HabitTvConf.STAT_URL, null);
				} catch (Exception e) {
					// Non-critical operation, log and continue
					LOG.warn("Failed to send statistics: " + e.getMessage());
				}
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
