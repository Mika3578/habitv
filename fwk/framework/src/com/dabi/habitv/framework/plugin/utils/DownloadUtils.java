package com.dabi.habitv.framework.plugin.utils;

import java.util.LinkedList;
import java.util.List;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;

public class DownloadUtils {

	private static final String HTTP_PREFIX = "http://";

	private static final String HTTPS_PREFIX = "https://";

	private static final String FTP_PREFIX = "ftp://";

	public static boolean isHttpUrl(String url) {
		return url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX);
	}

	public static boolean isFtpUrl(final String downloadInput) {
		return downloadInput.startsWith(FTP_PREFIX);
	}

	public static ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final PluginDownloaderInterface downloader = getDownloader(
				downloadParam, downloaders);
		return downloader.download(downloadParam, downloaders);
	}

	public static PluginDownloaderInterface getDownloader(
			final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders) {
		final String downloaderName = downloadParam
				.getParam(FrameworkConf.DOWNLOADER_PARAM);
		final PluginDownloaderInterface downloader;
		if (downloaderName == null) {
			downloader = findDownloaderByUrl(downloaders,
					downloadParam.getDownloadInput());
		} else {
			downloader = downloaders.getPlugin(downloaderName);
		}
		return downloader;
	}

	public static ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders,
			final String downloaderName) throws DownloadFailedException {
		final PluginDownloaderInterface pluginDownloader = downloaders
				.getPlugin(downloaderName);
		return pluginDownloader.download(downloadParam, downloaders);
	}

	private static PluginDownloaderInterface findDownloaderByUrl(
			final DownloaderPluginHolder downloaders, final String url) {
		final List<PluginDownloaderInterface> possibleDownloaders = new LinkedList<>();
		for (final PluginDownloaderInterface downloader : downloaders
				.getPlugins()) {
			final DownloadableState downloadableState = downloader
					.canDownload(url);
			switch (downloadableState) {
			case SPECIFIC:
				return downloader;
			case POSSIBLE:
				possibleDownloaders.add(downloader);
				break;
			default:
				break;
			}
		}

		if (possibleDownloaders.isEmpty()) {
			return downloaders.getPlugin(FrameworkConf.DEFAULT_DOWNLOADER);
		}

		return possibleDownloaders.iterator().next();
	}

}
