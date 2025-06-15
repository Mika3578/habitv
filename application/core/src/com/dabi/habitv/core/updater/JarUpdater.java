package com.dabi.habitv.core.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.core.event.UpdatePluginStateEnum;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils.ArtifactVersion;
import com.dabi.habitv.framework.plugin.utils.update.Updater;

public class JarUpdater extends Updater {

	private static final String JAR = "jar";

	private static final Logger LOG = Logger.getLogger(JarUpdater.class);

	private final Publisher<UpdatePluginEvent> updatePublisher;

	public JarUpdater(final String currentDir, final String groupId, final String coreVersion, final boolean autoriseSnapshot,
			final Publisher<UpdatePluginEvent> updatePublisher) {
		super(currentDir, groupId, coreVersion, autoriseSnapshot);
		this.updatePublisher = updatePublisher;
	}

	@Override
	protected String getCurrentVersion(final File currentFile) {
		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(currentFile));) {
			final Manifest mf = jarStream.getManifest();
			return (String) mf.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION);
		} catch (final IOException e) {
			throw new TechnicalException(e);
		}
	}

	@Override
	protected String getLocalExtension() {
		return JAR;
	}

	@Override
	protected String getServerExtension() {
		return JAR;
	}

	@Override
	protected boolean performUpdate(final File current, final ArtifactVersion artifactVersion) {
		return true;
	}

	@Override
	protected void onUpdate(final File current, final ArtifactVersion artifactNewVersion) {
		LOG.info("Update of plugin " + artifactNewVersion.getArtifactId() + " version " + artifactNewVersion.getVersion());
		updatePublisher.addNews(new UpdatePluginEvent(artifactNewVersion.getArtifactId(), artifactNewVersion.getVersion(), UpdatePluginStateEnum.DOWNLOADING));
	}

	@Override
	protected void onUpdateDone(final File current, final ArtifactVersion artifactNewVersion) {
		LOG.info("Update of plugin " + artifactNewVersion.getArtifactId() + " version " + artifactNewVersion.getVersion() + " done");
		updatePublisher.addNews(new UpdatePluginEvent(artifactNewVersion.getArtifactId(), artifactNewVersion.getVersion(), UpdatePluginStateEnum.DONE));

	}

	@Override
	protected void onUpdateError(final File current, final ArtifactVersion artifactNewVersion) {
		LOG.error("Error while updating plugin " + artifactNewVersion.getArtifactId() + " version " + artifactNewVersion.getVersion());
		updatePublisher.addNews(new UpdatePluginEvent(artifactNewVersion.getArtifactId(), artifactNewVersion.getVersion(), UpdatePluginStateEnum.ERROR));
	}

	@Override
	protected void onChecking(final String fileToUpdate) {
		updatePublisher.addNews(new UpdatePluginEvent(fileToUpdate, null, UpdatePluginStateEnum.CHECKING));
	}

	@Override
	protected boolean deleteFiles() {
		return true;
	}

	/**
	 * Override to use direct GitHub Pages URLs instead of complex HTML parsing
	 */
	@Override
	public void update(final String... filesToUpdate) {
		final File currentFolder = new File(getFolderToUpdate());
		if (!currentFolder.exists()) {
			currentFolder.mkdir();
		} else {
			if (deleteFiles()) {
				for (final File folderFile : currentFolder.listFiles()) {
					if (folderFile.getName().endsWith("." + getLocalExtension())) {
						String pluginName = folderFile.getName().replace("." + getLocalExtension(), "");
						boolean shouldKeep = false;
						for (String fileToUpdate : filesToUpdate) {
							if (pluginName.equals(fileToUpdate)) {
								shouldKeep = true;
								break;
							}
						}
						if (!shouldKeep) {
							folderFile.delete();
						}
					}
				}
			}
		}

		for (final String fileToUpdate : filesToUpdate) {
			try {
				updatePluginDirectly(currentFolder, fileToUpdate);
			} catch (final Exception e) {
				LOG.error("Error updating plugin " + fileToUpdate, e);
			}
		}
	}

	private void updatePluginDirectly(final File currentFolder, final String pluginName) {
		onChecking(pluginName);
		
		// Use GitHub Pages URL directly
		String groupIdUrl = FrameworkConf.GROUP_ID.replace(".", "/");
		String pluginUrl = FrameworkConf.UPDATE_URL + "/" + groupIdUrl + "/" + pluginName + "/5.0.0-SNAPSHOT/" + pluginName + "-5.0.0-SNAPSHOT.jar";
		
		File currentFile = new File(currentFolder, pluginName + "." + getLocalExtension());
		
		// Create ArtifactVersion using reflection since constructor is not visible
		ArtifactVersion artifactVersion = createArtifactVersion(pluginUrl, "5.0.0-SNAPSHOT", pluginName);
		
		try {
			// Always download the latest version
			LOG.info("Downloading plugin " + pluginName + " from " + pluginUrl);
			onUpdate(currentFile, artifactVersion);
			
			// Download the file
			String tempFile = currentFile.getPath() + ".tmp";
			downloadFile(pluginUrl, tempFile);
			
			// Replace the old file
			File newVersion = new File(tempFile);
			if (newVersion.exists()) {
				if (currentFile.exists()) {
					Files.delete(currentFile.toPath());
				}
				newVersion.renameTo(currentFile);
				LOG.info("Successfully updated plugin " + pluginName);
				onUpdateDone(currentFile, artifactVersion);
			} else {
				throw new IOException("Downloaded file not found: " + tempFile);
			}
			
		} catch (Exception e) {
			LOG.error("Failed to update plugin " + pluginName, e);
			onUpdateError(currentFile, artifactVersion);
		}
	}

	private ArtifactVersion createArtifactVersion(String url, String version, String artifactId) {
		try {
			// Use reflection to create ArtifactVersion since constructor is not visible
			ArtifactVersion artifactVersion = (ArtifactVersion) ArtifactVersion.class
				.getDeclaredConstructor(String.class, String.class)
				.newInstance(url, version);
			artifactVersion.setArtifactId(artifactId);
			return artifactVersion;
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}

	private void downloadFile(final String filePath, final String destination) throws IOException {
		final URL website = new URL(filePath);
		try (final ReadableByteChannel rbc = Channels.newChannel(website.openStream());) {
			try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destination);) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		}
	}

}
