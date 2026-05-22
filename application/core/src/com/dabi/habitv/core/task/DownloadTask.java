package com.dabi.habitv.core.task;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.ProtectedContentStatus;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorStoppedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.token.TokenReplacer;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class DownloadTask extends AbstractEpisodeTask {

	private static final String TMP = ".tmp";

	private final PluginProviderInterface provider;

	private final DownloaderPluginHolder downloaders;

	private final Publisher<RetreiveEvent> publisher;

	private final DownloadedDAO downloadedDAO;

	private boolean manual;

	public DownloadTask(final EpisodeDTO episode,
			final PluginProviderInterface provider,
			final DownloaderPluginHolder downloaders,
			final Publisher<RetreiveEvent> publisher,
			final DownloadedDAO downloadedDAO, boolean manual) {
		super(episode);
		this.provider = provider;
		this.downloaders = downloaders;
		this.publisher = publisher;
		this.downloadedDAO = downloadedDAO;
		this.manual = manual;
	}

	@Override
	protected void adding() {
		LOG.info("Waiting for download of " + getEpisode());
	}

	@Override
	protected void failed(final Throwable e) {
		final DRMProtectedException drm = unwrapDrm(e);
		if (drm != null) {
			LOG.info("Direct download skipped (DRM boundary) for "
					+ getEpisode() + " — " + drm.getMessage());
			publisher.addNews(new RetreiveEvent(getEpisode(),
					drm.getState(), "drm-boundary"));
			return;
		}
		LOG.error("Download failed for " + getEpisode(), e);
		if (e instanceof ExecutorStoppedException) {
			publisher.addNews(new RetreiveEvent(getEpisode(),
					EpisodeStateEnum.STOPPED, e, "download"));
		} else {
			publisher.addNews(new RetreiveEvent(getEpisode(),
					EpisodeStateEnum.DOWNLOAD_FAILED, e, "download"));
		}
	}

	private static DRMProtectedException unwrapDrm(final Throwable e) {
		Throwable cursor = e;
		while (cursor != null) {
			if (cursor instanceof DRMProtectedException) {
				return (DRMProtectedException) cursor;
			}
			cursor = cursor.getCause();
		}
		return null;
	}

	@Override
	protected void ended() {
		LOG.info("Download of " + getEpisode() + " done");
		downloadedDAO.addDownloadedFiles(manual, getEpisode());
		publisher.addNews(new RetreiveEvent(getEpisode(),
				EpisodeStateEnum.DOWNLOADED));
	}

	@Override
	protected void started() {
		LOG.info("Download of " + getEpisode() + " is starting");
	}

	@Override
	protected void canceled() {
		LOG.info("Cancel of " + getEpisode() + " done");
		publisher.addNews(new RetreiveEvent(getEpisode(),
				EpisodeStateEnum.STOPPED));
	}

	@Override
	protected Object doCall() throws DownloadFailedException {
		final String outputFilename = TokenReplacer.replaceAll(
				downloaders.getDownloadOutput(), getEpisode());
		final String outputTmpFileName = outputFilename + TMP;
		// delete to prevent resuming since most of the download can't resume
		final File outputFile = new File(outputFilename);
		// create download dir if doesn't exist
		if (outputFile.getParentFile() != null
				&& !outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdir();
		}
		if (outputFile.exists()) {
			if (!outputFile.delete()) {
				throw new TechnicalException("can't delete file "
						+ outputFile.getAbsolutePath());
			}
		}
		download(outputTmpFileName);
		File file = new File(outputTmpFileName);
		if (!file.exists()) {
			file = findFileWithoutExtension(outputFilename);
		}
		try {
			Files.move(file.toPath(), new File(outputFilename).toPath());
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return null;
	}

	static File findFileWithoutExtension(String outputFilename) {
		int lastIndexOfSlash = outputFilename.lastIndexOf("/");
		String folder = outputFilename.substring(
				0,
				lastIndexOfSlash > 0 ? lastIndexOfSlash : outputFilename
						.lastIndexOf("\\"));
		File dir = new File(folder);

		String fileNameNoFolder = outputFilename.replace(folder, "");
		String fileName = fileNameNoFolder.substring(1,fileNameNoFolder.length());
		final String outputFilenameNoExtension = fileName.substring(0,
				fileName.lastIndexOf("."));

		File[] matchingFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fileName) {
				return fileName.contains(outputFilenameNoExtension) && (fileName.contains(TMP) || fileName.contains(".mp3"));
			}
		});
		return matchingFiles.length < 1 ? null : matchingFiles[0];
	}

	private ProcessHolder download(final String outputTmpFileName)
			throws DownloadFailedException {
		enforceDrmBoundary();
		final DownloadParamDTO downloadParam = buildDownloadParam(outputTmpFileName);

		final PluginDownloaderInterface downloader;
		if (PluginDownloaderInterface.class.isInstance(provider)) {
			downloader = (PluginDownloaderInterface) provider;
		} else {
			downloader = DownloadUtils
					.getDownloader(downloadParam, downloaders);
		}
		ProcessHolder downloadProcessHolder = downloader.download(
				downloadParam, downloaders);
		publisher.addNews(new RetreiveEvent(getEpisode(),
				EpisodeStateEnum.DOWNLOAD_STARTING, downloadProcessHolder));
		downloadProcessHolder.start();
		return downloadProcessHolder;
	}

	private DownloadParamDTO buildDownloadParam(final String outputTmpFileName) {
		final CategoryDTO category = getEpisode().getCategory();
		final DownloadParamDTO downloadParam = new DownloadParamDTO(
				getEpisode().getId(), outputTmpFileName,
				category.getExtension());
		downloadParam.getParams().putAll(category.getParameters());
		return downloadParam;
	}

	/**
	 * Enforce Habitv's official DRM/CDM boundary before any direct download
	 * is attempted. For episodes classified as DRM-protected, the direct
	 * downloader is skipped and a non-failure DRM state is reported instead.
	 *
	 * <p>Public metadata stays visible (the EpisodeDTO is unchanged) and the
	 * official playback URL, when present, is logged and included on the
	 * raised exception so the UI can surface it. The rest of the queue is
	 * unaffected: only this single task short-circuits.
	 */
	private void enforceDrmBoundary() throws DRMProtectedException {
		final EpisodeDTO episode = getEpisode();
		final ProtectedContentStatus status = episode.getProtectedContentStatus();
		if (status == null) {
			return;
		}
		final EpisodeStateEnum mapped = mapToRuntimeState(status);
		if (mapped == null) {
			return;
		}
		final String officialUrl = episode.getOfficialPlaybackUrl();
		throw new DRMProtectedException(mapped,
				"Direct download is not available for DRM-protected content under Habitv's official DRM/CDM boundary.",
				officialUrl);
	}

	private static EpisodeStateEnum mapToRuntimeState(final ProtectedContentStatus status) {
		switch (status) {
		case OFFICIAL_PLAYBACK_ONLY:
			return EpisodeStateEnum.DIRECT_DOWNLOAD_UNSUPPORTED_DRM;
		case OFFICIAL_DRM_INTEGRATION_REQUIRED:
			return EpisodeStateEnum.DRM_REQUIRED_OFFICIAL_INTEGRATION_MISSING;
		case OFFICIAL_DRM_INTEGRATION_AVAILABLE:
			return EpisodeStateEnum.DRM_SUPPORTED_BY_OFFICIAL_INTEGRATION;
		case METADATA_ONLY_DRM_PROTECTED:
			return EpisodeStateEnum.METADATA_ONLY_DRM_PROTECTED;
		case AUTH_REQUIRED_UNSUPPORTED:
			return EpisodeStateEnum.PROVIDER_REQUIRES_AUTH;
		case REGION_LOCKED_UNSUPPORTED:
			return EpisodeStateEnum.PROVIDER_REGION_LOCKED;
		case BROKEN_OR_OBSOLETE:
			return EpisodeStateEnum.PROVIDER_BROKEN_OR_OBSOLETE;
		case DIRECT_DOWNLOAD_SUPPORTED:
		default:
			return null;
		}
	}

	@Override
	public int hashCode() {
		return getEpisode().hashCode() + downloaders.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (obj instanceof DownloadTask) {
			final DownloadTask downloadTask = (DownloadTask) obj;
			ret = getEpisode().equals(downloadTask.getEpisode());
			if (getCategory() != null) {
				ret = ret && getCategory().equals(downloadTask.getCategory());
			}
		} else {
			ret = false;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "DL" + getEpisode() + " "
				+ (provider == null ? "no provider" : provider.getName()) + " "
				+ downloaders.getDownloadOutput();
	}

}
