package com.dabi.habitv.plugin.youtube;

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

public class YoutubePluginDownloader extends BaseUpdatablePlugin implements PluginDownloaderInterface {

	private static final Pattern VERSION_PATTERN = Pattern.compile("([\\-0-9A-Za-z.-]*)");

	@Override
	public String getName() {
		return YoutubeConf.NAME;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		final String binParam = getBinParam(downloaders);
		String cmd = binParam + " ";
		final String cmdParam = downloadParam.getParam(FrameworkConf.PARAMETER_ARGS);
		if (cmdParam == null) {
			cmd += YoutubeConf.DUMP_CMD;
		} else {
			cmd += cmdParam;
		}
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_INPUT, Matcher.quoteReplacement(downloadParam.getDownloadInput()));
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_DESTINATION, Matcher.quoteReplacement(downloadParam.getDownloadOutput()));

		// yt-dlp supports downloading videos through a proxy, by
		// setting the http_proxy environment variable to the proxy URL, as in
		// http://proxy_machine_name:port/.

		try {
			return (new YoutubeDLCmdExecutor(downloaders.getCmdProcessor(), cmd));
		} catch (final ExecutorFailedException e) {
			throw new DownloadFailedException(e);
		}
	}

	@Override
	protected String getLinuxDefaultBuildPath() {
		return YoutubeConf.DEFAULT_LINUX_BIN_PATH;
	}

	@Override
	protected String getWindowsDefaultExe() {
		return YoutubeConf.DEFAULT_WINDOWS_EXE;
	}

	@Override
	protected Pattern getVersionPattern() {
		return VERSION_PATTERN;
	}

	@Override
	protected String getVersionParam() {
		return " --version";
	}

	@Override
	protected String[] getFilesToUpdate() {
		return new String[] { "yt-dlp" };
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (!downloadInput.startsWith("mp3:") && ( downloadInput.contains("youtube.") || downloadInput.contains("youtu.be") || downloadInput.contains("dailymotion.") || downloadInput.contains("vimeo.")
		        || downloadInput.contains("dailymotion.") || downloadInput.contains("tf1.") || downloadInput.contains("wat.tv")
		        || downloadInput.contains("clubic.") || downloadInput.contains("6play."))) {
			return DownloadableState.SPECIFIC;
		} else {
			return DownloadableState.IMPOSSIBLE;
		}
	}

}
