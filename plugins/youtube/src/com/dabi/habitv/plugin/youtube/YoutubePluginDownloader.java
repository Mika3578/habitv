package com.dabi.habitv.plugin.youtube;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
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
	private static final String YT_DLP_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
	private static final String YT_DLP_BIN_PATH = "bin/yt-dlp.exe";
	private static final Logger LOGGER = Logger.getLogger(YoutubePluginDownloader.class.getName());

	@Override
	public String getName() {
		return YoutubeConf.NAME;
	}

	private void ensureYtDlpPresentAndUpdated() {
		try {
			File binFile = new File(YT_DLP_BIN_PATH);
			if (!binFile.exists()) {
				LOGGER.info("yt-dlp.exe not found, downloading...");
				binFile.getParentFile().mkdirs();
				URL url = new URL(YT_DLP_URL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0");
				try (InputStream in = connection.getInputStream();
					 FileOutputStream out = new FileOutputStream(binFile)) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
				}
				binFile.setExecutable(true);
				LOGGER.info("yt-dlp.exe downloaded successfully.");
			} else {
				LOGGER.info("yt-dlp.exe found, updating...");
				ProcessBuilder pb = new ProcessBuilder(YT_DLP_BIN_PATH, "-U");
				pb.inheritIO();
				Process process = pb.start();
				process.waitFor();
				LOGGER.info("yt-dlp.exe update check complete.");
			}
		} catch (Exception e) {
			LOGGER.warning("Failed to ensure yt-dlp.exe is present and updated: " + e.getMessage());
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		ensureYtDlpPresentAndUpdated();
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

		// if (proxyDTO!=null){
		// youtube-dl supports downloading videos through a proxy, by
		// setting the http_proxy environment variable to the proxy URL, as in
		// http://proxy_machine_name:port/.
		// }

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
