package com.dabi.habitv.api.plugin.holder;

import java.util.Collection;
import java.util.Map;

import com.dabi.habitv.api.plugin.api.PluginBaseInterface;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class AbstractPluginHolder<E extends PluginBaseInterface> {

	protected Map<String, E> pluginName2Plugin;

	public AbstractPluginHolder(final Map<String, E> pluginName2Plugin) {
		super();
		this.pluginName2Plugin = pluginName2Plugin;
	}

	public E getPlugin(final String pluginName) {
		return getPlugin(pluginName, (String) null);
	}

	public E getPlugin(final String pluginName, final String defaultPluginName) {
		E plugin = pluginName2Plugin.get(pluginName);
		if (plugin == null) {
			plugin = pluginName2Plugin.get(defaultPluginName);
		}
		if (plugin == null) {
			throw new TechnicalException("No plugin found for " + pluginName
					+ "and no default plugin " + defaultPluginName);
		}
		return plugin;
	}

	public E getPlugin(final String pluginName, final E defaultPlugin) {
		E plugin = pluginName2Plugin.get(pluginName);
		if (plugin == null) {
			plugin = defaultPlugin;
		}
		return plugin;
	}

	public void setPlugins(final Map<String, E> pluginName2PluginToSet) {
		this.pluginName2Plugin = pluginName2PluginToSet;
	}

	public Map<String, E> getPluginName2Plugin() {
		return pluginName2Plugin;
	}

	public Collection<E> getPlugins() {
		// Deduplicate instances registered under several names (legacy aliasing).
		return new java.util.LinkedHashSet<E>(pluginName2Plugin.values());
	}
}