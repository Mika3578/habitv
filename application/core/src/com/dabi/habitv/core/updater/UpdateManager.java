package com.dabi.habitv.core.updater;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.core.event.UpdatePluginStateEnum;
import com.dabi.habitv.framework.FWKProperties;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest;
import com.dabi.habitv.framework.plugin.utils.update.UpdateRepositoryUrls;
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
		final boolean updateEnabled = Boolean.getBoolean(FrameworkConf.UPDATE_ENABLED_PROPERTY);
		final String updateSite = System.getProperty(
				FrameworkConf.UPDATE_URL_PROPERTY, site);
		final String baseUrl = updateSite == null ? null
				: UpdateRepositoryUrls.normalizeBaseUrl(updateSite.trim());
		LOG.info("Runtime plugin update configuration: enabled=" + updateEnabled
				+ ", autoriseSnapshot=" + autoriseSnapshot + ", updateUrl="
				+ baseUrl);
		if (!updateEnabled) {
			LOG.debug("Plugin update check is disabled.");
			return;
		}
		if (updateSite == null || updateSite.trim().isEmpty()) {
			LOG.warn("Plugin update check is enabled but no update URL is configured.");
			return;
		}
		try {
			LOG.info("Checking plugin updates...");
			final String[] toUpdate = resolvePluginsToUpdate(baseUrl);
			if (toUpdate.length == 0) {
				LOG.warn("No plugins listed for update; keeping local plugins.");
				return;
			}
			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.STARTING_ALL, toUpdate.length));
			final Updater updater = new JarUpdater(pluginFolder, groupId,
					coreVersion, autoriseSnapshot, updatePublisher);
			updater.update(toUpdate);

			updatePublisher.addNews(new UpdatePluginEvent(
					UpdatePluginStateEnum.ALL_DONE));
			LOG.info("Update done");
		} catch (Exception e) {
			LOG.error("Plugin update failed; keeping local plugins.", e);
		}
	}

	public Publisher<UpdatePluginEvent> getUpdatePublisher() {
		return updatePublisher;
	}

	private String[] resolvePluginsToUpdate(final String baseUrl) {
		final HabitvUpdateManifest manifest = HabitvUpdateManifest.loadFromRepository(baseUrl);
		if (manifest.isEmpty()) {
			LOG.info("Update manifest not loaded from " + baseUrl + "/"
					+ FrameworkConf.UPDATE_MANIFEST_FILE);
		} else {
			LOG.info("Update manifest loaded from " + baseUrl + "/"
					+ FrameworkConf.UPDATE_MANIFEST_FILE + " with "
					+ manifest.getEntries().size() + " entries.");
		}

		try {
			final String pluginsList = RetrieverUtils.getUrlContent(
					baseUrl + "/" + FrameworkConf.PLUGINS_LIST_FILE, null);
			if (pluginsList != null && !pluginsList.trim().isEmpty()) {
				final String[] pluginIds = splitPluginLines(pluginsList);
				LOG.info("plugins.txt loaded from " + baseUrl + "/"
						+ FrameworkConf.PLUGINS_LIST_FILE + " with "
						+ pluginIds.length + " plugin ids.");
				return pluginIds;
			}
			LOG.info("plugins.txt loaded from " + baseUrl + "/"
					+ FrameworkConf.PLUGINS_LIST_FILE + " but it is empty.");
		} catch (final RuntimeException e) {
			LOG.info("plugins.txt not loaded from " + baseUrl + "/"
					+ FrameworkConf.PLUGINS_LIST_FILE + ": " + e.getMessage());
		}
		if (!manifest.isEmpty()) {
			LOG.info("Falling back to manifest artifact list because plugins.txt is unavailable.");
			return manifest.getPluginArtifactIds();
		}
		return new String[0];
	}

	private static String[] splitPluginLines(final String pluginsList) {
		final String[] lines = pluginsList.split("\\r?\\n");
		int count = 0;
		for (final String line : lines) {
			if (isPluginLine(line)) {
				count++;
			}
		}
		final String[] result = new String[count];
		int index = 0;
		for (final String line : lines) {
			if (isPluginLine(line)) {
				result[index++] = normalizePluginLine(line);
			}
		}
		return result;
	}

	private static boolean isPluginLine(final String line) {
		final String trimmed = normalizePluginLine(line);
		if (trimmed == null) {
			return false;
		}
		return !trimmed.isEmpty() && !trimmed.startsWith("#");
	}

	private static String normalizePluginLine(final String line) {
		if (line == null) {
			return null;
		}
		String trimmed = line.trim();
		if (trimmed.startsWith("\uFEFF")) {
			trimmed = trimmed.substring(1).trim();
		}
		return trimmed;
	}

}
