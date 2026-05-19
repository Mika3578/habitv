package com.dabi.habitv.framework.plugin.utils.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.utils.AlphanumComparator;
import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils.ArtifactVersion;

public abstract class Updater {

	private static final Logger LOG = Logger.getLogger(Updater.class);

	private final String folderToUpdate;

	private final String groupId;

	private final String coreVersion;

	private final boolean autoriseSnapshot;

	public Updater(final String folderToUpdate, final String groupId, final String coreVersion, final boolean autoriseSnapshot) {
		this.folderToUpdate = folderToUpdate;
		this.groupId = groupId;
		this.coreVersion = coreVersion;
		this.autoriseSnapshot = autoriseSnapshot;
	}

	public void update(final String... filesToUpdate) {

		final File currentFolder = new File(folderToUpdate);
		if (!currentFolder.exists()) {
			currentFolder.mkdir();
		} else {
			if (deleteFiles()) {
				final List<String> filesToUpdateList = Arrays.asList(filesToUpdate);
				for (final File folderFile : currentFolder.listFiles()) {
					if (!filesToUpdateList.contains(folderFile.getName().replace("." + getLocalExtension(), ""))) {
						folderFile.delete();
					}
				}
			}
		}

		for (final String fileToUpdate : filesToUpdate) {
			try {
				updateFile(currentFolder, fileToUpdate);
			} catch (final Exception e) {
				LOG.error("", e);
			}
		}

	}

	protected abstract boolean deleteFiles();

	private void updateFile(final File currentFolder, final String fileToUpdate) {
		onChecking(fileToUpdate);
		LOG.info("Resolving update artifact: artifactId=" + fileToUpdate + ", extension=" + getServerExtension());
		final ArtifactVersion artifactNewVersion = FindArtifactUtils.findLastVersionUrl(groupId, fileToUpdate, coreVersion,
				autoriseSnapshot, getServerExtension());
		if (artifactNewVersion == null) {
			LOG.warn("Skipping artifact " + fileToUpdate + " because no downloadable URL could be resolved.");
			return;
		}
		LOG.info("Resolved artifact " + fileToUpdate + " from " + artifactNewVersion.getSource()
				+ " with version " + artifactNewVersion.getVersion() + " at " + artifactNewVersion.getUrl());
		final File currentFile = new File(folderToUpdate + "/" + fileToUpdate + "." + getLocalExtension());
		if (currentFile.exists()) {
			final String currentVersion = getCurrentVersion(currentFile);//
			if (currentVersion == null || currentVersion.contains("-SNAPSHOT")
					|| AlphanumComparator.INSTANCE.compare(currentVersion, artifactNewVersion.getVersion()) < 0) {
				updateFile(artifactNewVersion, currentFile);
			}
		} else {
			updateFile(artifactNewVersion, currentFile);
		}
	}

	protected abstract void onChecking(String fileToUpdate);

	protected abstract String getCurrentVersion(File currentFile);

	protected abstract String getLocalExtension();

	protected abstract String getServerExtension();

	private void updateFile(final ArtifactVersion artifactNewVersion, final File current) {
		if (performUpdate(current, artifactNewVersion)) {
			onUpdate(current, artifactNewVersion);
			File newVersion;
			try {
				LOG.info("Downloading artifact from " + artifactNewVersion.getUrl() + " to " + current.getPath() + ".tmp");
				newVersion = new File(downloadFile(artifactNewVersion.getUrl(), current.getPath() + ".tmp"));
			} catch (final IOException e) {
				onUpdateError(current, artifactNewVersion);
				throw new TechnicalException(e);
			}
			updateFile(current, newVersion);

			onUpdateDone(current, artifactNewVersion);
		}
	}

	protected abstract void onUpdateError(File current, ArtifactVersion artifactNewVersion);

	protected abstract void onUpdateDone(File current, ArtifactVersion artifactNewVersion);

	protected abstract void onUpdate(File current, ArtifactVersion artifactNewVersion);

	protected abstract boolean performUpdate(File current, ArtifactVersion artifactNewVersion);

	protected void updateFile(final File current, final File newVersion) {
		if (newVersion.exists()) {
			if (current.exists()) {
				try {
					Files.delete(current.toPath());
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			}
			newVersion.renameTo(current);
		}
	}

	/**
	 * Cette méthode télécharge un fichier sur internet et le stocke en local
	 * 
	 * @param filePath
	 *            , chemin du fichier à télécharger
	 * @param destination
	 *            , chemin du fichier en local
	 * @return
	 * @throws IOException
	 */
	private String downloadFile(final String filePath, final String destination) throws IOException {
		final URL website = new URL(filePath);
		try (final ReadableByteChannel rbc = Channels.newChannel(website.openStream());) {
			try (FileOutputStream fos = new FileOutputStream(destination);) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				return destination;
			}
		}
	}

	public String getFolderToUpdate() {
		return folderToUpdate;
	}

}
