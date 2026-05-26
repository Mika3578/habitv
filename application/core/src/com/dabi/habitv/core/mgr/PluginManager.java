package com.dabi.habitv.core.mgr;

import java.util.Collection;
import java.util.Map;

import com.dabi.habitv.api.plugin.api.PluginBaseInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginExporterInterface;
import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.api.PluginWithProxyInterface;
import com.dabi.habitv.api.plugin.api.UpdatablePluginInterface;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ExporterPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProviderPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent.UpdatablePluginStateEnum;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.core.plugin.PluginFactory;
import com.dabi.habitv.core.updater.UpdateManager;

public class PluginManager {

	private final UpdateManager updateManager;

	private final Publisher<UpdatablePluginEvent> updatablePluginPublisher = new Publisher<>();

	private final PluginFactory pluginFactory;

	private final DownloaderPluginHolder downloadersHolder;

	private final ExporterPluginHolder exportersHolder;

	private final ProviderPluginHolder providersHolder;

	public PluginManager(final UserConfig config) {
		updateManager = new UpdateManager(config.getPluginDir(),
				config.autoriseSnapshot());
		pluginFactory = new PluginFactory(config.getPluginDir());

		downloadersHolder = new DownloaderPluginHolder(
				config.getCmdProcessor(),
				pluginFactory.loadPlugins(PluginDownloaderInterface.class),
				config.getDownloader(), config.getDownloadOuput(),
				config.getIndexDir(), config.getBinDir(), config.getPluginDir(),
				config.getEmbedSubtitles());
		providersHolder = new ProviderPluginHolder(
				pluginFactory.loadPlugins(PluginProviderInterface.class));
		exportersHolder = new ExporterPluginHolder(
				pluginFactory.loadPlugins(PluginExporterInterface.class),
				config.getExporter());
	}

	public void update() {
		updateManager.process();
		loadPlugins();
		pluginAutoUpdate();
	}

	private void pluginAutoUpdate() {
		updatablePluginPublisher.addNews(new UpdatablePluginEvent(
				UpdatablePluginStateEnum.STARTING_ALL, countPlugins()));

		for (final PluginDownloaderInterface downloader : downloadersHolder
				.getPlugins()) {
			updateUpdatablePlugin(downloader);
		}
		for (final PluginExporterInterface exporter : exportersHolder
				.getPlugins()) {
			updateUpdatablePlugin(exporter);
		}
		for (final PluginProviderInterface provider : providersHolder
				.getPlugins()) {
			updateUpdatablePlugin(provider);
		}

		updatablePluginPublisher.addNews(new UpdatablePluginEvent(
				UpdatablePluginStateEnum.ALL_DONE));
	}

	private String countPlugins() {
		return String.valueOf(downloadersHolder.getPlugins().size()
				+ exportersHolder.getPlugins().size()
				+ providersHolder.getPlugins().size());
	}

	private void updateUpdatablePlugin(final PluginBaseInterface plugin) {
		if (UpdatablePluginInterface.class.isInstance(plugin)) {
			((UpdatablePluginInterface) plugin).update(
					updatablePluginPublisher, downloadersHolder);
		}
	}

	private void loadPlugins() {
		pluginFactory.init();
		providersHolder.setPlugins(pluginFactory
				.loadPlugins(PluginProviderInterface.class));
		downloadersHolder.setPlugins(pluginFactory
				.loadPlugins(PluginDownloaderInterface.class));
		exportersHolder.setPlugins(pluginFactory
				.loadPlugins(PluginExporterInterface.class));
	}

	public void setProxy(final Map<ProtocolEnum, ProxyDTO> defaultProxyMap,
			final Map<String, Map<ProtocolEnum, ProxyDTO>> plugin2protocol2proxy) {
		setProxy(defaultProxyMap, plugin2protocol2proxy,
				providersHolder.getPlugins());
	}

	private void setProxy(
			final Map<ProtocolEnum, ProxyDTO> defaultProxyMap,
			final Map<String, Map<ProtocolEnum, ProxyDTO>> plugin2protocol2proxy,
			final Collection<? extends PluginBaseInterface> plugins) {
		// set the proxy to each plugin
		for (final PluginBaseInterface plugin : plugins) {
			if (PluginWithProxyInterface.class.isInstance(plugin)) {
				final PluginWithProxyInterface pluginWithProxy = (PluginWithProxyInterface) plugin;
				Map<ProtocolEnum, ProxyDTO> proxies = plugin2protocol2proxy
						.get(plugin.getName());
				if (proxies == null) {
					proxies = defaultProxyMap;
				}
				pluginWithProxy.setProxies(proxies);
			}
		}
	}

	public DownloaderPluginHolder getDownloadersHolder() {
		return downloadersHolder;
	}

	public ExporterPluginHolder getExportersHolder() {
		return exportersHolder;
	}

	public ProviderPluginHolder getProvidersHolder() {
		return providersHolder;
	}

	public Publisher<UpdatablePluginEvent> getUpdatablePluginPublisher() {
		return updatablePluginPublisher;
	}

	public Publisher<UpdatePluginEvent> getUpdatePluginPublisher() {
		return updateManager.getUpdatePublisher();
	}

}
