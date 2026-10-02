package com.dabi.habitv.plugin.ffmpeg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.update.BaseUpdatablePlugin;
import com.dabi.habitv.framework.plugin.utils.OSUtils;

public class FFMPEGPluginDownloader extends BaseUpdatablePlugin implements
		PluginDownloaderInterface {

	private static final Pattern VERSION_PATTERN = Pattern
			.compile("ffmpeg version ([\\-0-9A-Za-z.-]*).*");

	@Override
	public String getName() {
		return FFMPEGConf.NAME;
	}

	@Override
	protected String getLinuxDefaultBuildPath() {
		return FFMPEGConf.DEFAULT_LINUX_BIN_PATH;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {

		final String downloaderBin = getBinParam(downloaders);
		String cmd = downloaderBin + getCmd(downloaderBin);
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_INPUT,
				Matcher.quoteReplacement(downloadParam.getDownloadInput()));
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_DESTINATION,
				Matcher.quoteReplacement(downloadParam.getDownloadOutput()));
		cmd = cmd.replaceFirst(FrameworkConf.EXTENSION,
				Matcher.quoteReplacement(downloadParam.getExtension()));

		try {
			return (new FFMPEGCmdExecutor(downloaders.getCmdProcessor(), cmd));
		} catch (final ExecutorFailedException e) {
			throw new DownloadFailedException(e);
		}
	}

	private String getCmd(final String executablePath) {
		if (OSUtils.isWindows()) {
			return FFMPEGConf.augmentRemuxCommand(FFMPEGConf.FFMPEG_CMD_WINDOWS, executablePath);
		}
		return FFMPEGConf.augmentRemuxCommand(FFMPEGConf.FFMPEG_CMD_LINUX, executablePath);
	}

	@Override
	protected Pattern getVersionPattern() {
		return VERSION_PATTERN;
	}

	@Override
	protected String getVersionParam() {
		return " -version";
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		String mainUrl = downloadInput.split("\\?")[0];
		return (mainUrl.endsWith(FrameworkConf.M3U8) || mainUrl.endsWith("m4u8")) ? DownloadableState.SPECIFIC
				: DownloadableState.IMPOSSIBLE;
	}

}
