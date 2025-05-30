package com.dabi.habitv.core.updater;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.core.event.UpdatePluginStateEnum;
import com.dabi.habitv.framework.FWKProperties;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.framework.plugin.utils.update.Updater;

public class UpdateManager {

	private static final Logger LOG = Logger.getLogger(UpdateManager.class);

	// Liste des plugins à exclure de la mise à jour automatique
	private static final Set<String> EXCLUDED_PLUGINS = new HashSet<>(Arrays.asList("youtube"));

	private final String site;

	private final String groupId;

	private final String coreVersion;

	private final boolean autoriseSnapshot;

	private final Publisher<UpdatePluginEvent> updatePublisher = new Publisher<>();

	private final String pluginFolder;

	private UpdateManager(final String site, final String pluginFolder,
			final String groupId, final String coreVersion,
			final boolean autoriseSnapshot) {
		this.site = site;
		this.pluginFolder = pluginFolder;
		this.groupId = groupId;
		this.coreVersion = coreVersion;
		this.autoriseSnapshot = autoriseSnapshot;
	}

	public UpdateManager(final String pluginDir, final boolean autoriseSnapshot) {
		this(FrameworkConf.UPDATE_URL, pluginDir, FrameworkConf.GROUP_ID,
				FWKProperties.getVersion(), autoriseSnapshot);
	}

	public void process() {
		try {
			LOG.info("Checking plugin updates...");
			String[] toUpdate = RetrieverUtils.getUrlContent(
					site + "/plugins.txt", null).split("\\r\\n");

			// Filtrer les plugins exclus de la mise à jour
			String[] filteredPlugins = Arrays.stream(toUpdate)
					.filter(plugin -> !isExcluded(plugin))
					.toArray(String[]::new);

			LOG.info("Excluded plugins from update: " + EXCLUDED_PLUGINS);

			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.STARTING_ALL, filteredPlugins.length));
			final Updater updater = new JarUpdater(pluginFolder, groupId,
					coreVersion, autoriseSnapshot, updatePublisher);
			updater.update(filteredPlugins);

			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.ALL_DONE));
			LOG.info("Update done");
		} catch (Exception e) {
			LOG.error("Erreur lors de la mise à jour : ", e);
		}
	}

	/**
	 * Vérifie si un plugin doit être exclu de la mise à jour automatique
	 * @param pluginName Nom du plugin à vérifier
	 * @return true si le plugin est dans la liste des exclusions
	 */
	private boolean isExcluded(String pluginName) {
		for (String excludedPlugin : EXCLUDED_PLUGINS) {
			if (pluginName.toLowerCase().contains(excludedPlugin.toLowerCase())) {
				LOG.info("Skipping update for excluded plugin: " + pluginName);
				return true;
			}
		}
		return false;
	}

	public Publisher<UpdatePluginEvent> getUpdatePublisher() {
		return updatePublisher;
	}

}
