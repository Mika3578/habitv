package com.dabi.habitv.core.updater;

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
		if (!Boolean.getBoolean(FrameworkConf.UPDATE_ENABLED_PROPERTY)) {
			LOG.debug("Plugin update check is disabled.");
			return;
		}
		final String updateSite = System.getProperty(
				FrameworkConf.UPDATE_URL_PROPERTY, site);
		if (updateSite == null || updateSite.trim().isEmpty()) {
			LOG.warn("Plugin update check is enabled but no update URL is configured.");
			return;
		}
		try {
			LOG.info("Checking plugin updates...");
			String[] toUpdate = RetrieverUtils.getUrlContent(
					updateSite + "/plugins.txt", null).split("\\r\\n");
			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.STARTING_ALL, toUpdate.length));
			final Updater updater = new JarUpdater(pluginFolder, groupId,
					coreVersion, autoriseSnapshot, updatePublisher);
			updater.update(toUpdate);

			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.ALL_DONE));
			LOG.info("Update done");
		} catch (Exception e) {
			LOG.error("Erreur lors de la mise à jour : ", e);
		}
	}

	public Publisher<UpdatePluginEvent> getUpdatePublisher() {
		return updatePublisher;
	}

}
