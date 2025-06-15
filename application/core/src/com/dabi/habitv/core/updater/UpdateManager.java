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
		try {
			LOG.info("Checking plugin updates...");
			String[] rawLines = RetrieverUtils.getUrlContent(site + "/plugins.txt", null).split("\\r\\n");
			java.util.List<String> toUpdateList = new java.util.ArrayList<>();
			for (String line : rawLines) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
					toUpdateList.add(trimmed);
				}
			}
			String[] toUpdate = toUpdateList.toArray(new String[0]);
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
