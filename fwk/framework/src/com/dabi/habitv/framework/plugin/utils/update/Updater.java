package com.dabi.habitv.framework.plugin.utils.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
			} catch (final InvalidUpdatePathException e) {
				LOG.warn("Skipping unsafe update entry for artifactId="
						+ UpdatePathValidator.sanitizeForLog(fileToUpdate) + " under root "
						+ UpdatePathValidator.sanitizeForLog(folderToUpdate) + ": " + e.getMessage());
			} catch (final Exception e) {
				LOG.error("", e);
			}
		}

	}

	protected abstract boolean deleteFiles();

	private void updateFile(final File currentFolder, final String fileToUpdate) {
		onChecking(fileToUpdate);
		final Path trustedRoot = UpdatePathValidator.normalizeTrustedRoot(currentFolder);
		final Path currentPath = UpdatePathValidator.resolveArtifactFile(currentFolder, fileToUpdate, getLocalExtension());
		LOG.info("Resolving update artifact: artifactId=" + UpdatePathValidator.sanitizeForLog(fileToUpdate)
				+ ", extension=" + getLocalExtension());
		final ArtifactVersion artifactNewVersion = FindArtifactUtils.findLastVersionUrl(groupId, fileToUpdate, coreVersion,
				autoriseSnapshot, getServerExtension());
		if (artifactNewVersion == null) {
			LOG.warn("Skipping artifact " + UpdatePathValidator.sanitizeForLog(fileToUpdate)
					+ " because no downloadable URL could be resolved.");
			return;
		}
		LOG.info("Resolved artifact " + UpdatePathValidator.sanitizeForLog(fileToUpdate) + " from "
				+ artifactNewVersion.getSource() + " with version " + artifactNewVersion.getVersion());
		final File currentFile = currentPath.toFile();
		if (currentFile.exists()) {
			final String currentVersion = getCurrentVersion(currentFile);
			if (currentVersion == null || currentVersion.contains("-SNAPSHOT")
					|| AlphanumComparator.INSTANCE.compare(currentVersion, artifactNewVersion.getVersion()) < 0) {
				updateFile(artifactNewVersion, currentPath, trustedRoot);
			}
		} else {
			updateFile(artifactNewVersion, currentPath, trustedRoot);
		}
	}

	protected abstract void onChecking(String fileToUpdate);

	protected abstract String getCurrentVersion(File currentFile);

	protected abstract String getLocalExtension();

	protected abstract String getServerExtension();

	private void updateFile(final ArtifactVersion artifactNewVersion, final Path currentPath, final Path trustedRoot) {
		final File current = currentPath.toFile();
		if (performUpdate(current, artifactNewVersion)) {
			onUpdate(current, artifactNewVersion);
			final Path tempPath = UpdatePathValidator.resolveDownloadTempFile(currentPath, trustedRoot);
			try {
				LOG.info("Downloading artifact from " + artifactNewVersion.getUrl() + " to " + tempPath);
				downloadFile(artifactNewVersion.getUrl(), tempPath);
			} catch (final IOException e) {
				onUpdateError(current, artifactNewVersion);
				throw new TechnicalException(e);
			}
			updateFile(currentPath, tempPath);

			onUpdateDone(current, artifactNewVersion);
		}
	}

	protected abstract void onUpdateError(File current, ArtifactVersion artifactNewVersion);

	protected abstract void onUpdateDone(File current, ArtifactVersion artifactNewVersion);

	protected abstract void onUpdate(File current, ArtifactVersion artifactNewVersion);

	protected abstract boolean performUpdate(File current, ArtifactVersion artifactNewVersion);

	protected void updateFile(final File current, final File newVersion) {
		updateFile(current.toPath(), newVersion.toPath());
	}

	protected void updateFile(final Path current, final Path newVersion) {
		if (Files.exists(newVersion)) {
			if (Files.exists(current)) {
				try {
					Files.delete(current);
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			}
			try {
				Files.move(newVersion, current, StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException e) {
				throw new TechnicalException(e);
			}
		}
	}

	/**
	 * Cette méthode télécharge un fichier sur internet et le stocke en local
	 *
	 * @param filePath
	 *            , chemin du fichier à télécharger
	 * @param destination
	 *            , chemin du fichier en local
	 * @throws IOException
	 */
	private void downloadFile(final String filePath, final Path destination) throws IOException {
		final URL website = new URL(filePath);
		try (final InputStream inputStream = website.openStream();
				final ReadableByteChannel rbc = Channels.newChannel(inputStream)) {
			try (java.nio.channels.FileChannel destinationChannel = java.nio.channels.FileChannel.open(destination,
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE,
					java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
				destinationChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		}
	}

	public String getFolderToUpdate() {
		return folderToUpdate;
	}

}
