package com.dabi.habitv.plugin.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.update.BaseUpdatablePlugin;

public class YoutubePluginDownloader extends BaseUpdatablePlugin implements PluginDownloaderInterface {

	private static final Logger LOG = Logger.getLogger(YoutubePluginDownloader.class);
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
			LOG.info("Executing YouTube download command: " + cmd);
			return (new YoutubeDLCmdExecutor(downloaders.getCmdProcessor(), cmd));
		} catch (final ExecutorFailedException e) {
			LOG.error("Failed to execute YouTube download command: " + e.getMessage());
			throw new DownloadFailedException(e);
		}
	}

	@Override
	protected String getLinuxDefaultBuildPath() {
		return "yt-dlp"; // Fallback to yt-dlp if available
	}

	@Override
	protected String getWindowsDefaultExe() {
		return "yt-dlp.exe"; // Fallback to yt-dlp if available
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
		// Use yt-dlp instead of youtube-dl for better compatibility
		return new String[] { "yt-dlp" };
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput == null) {
			return DownloadableState.IMPOSSIBLE;
		}
		
		// Check for various video platforms that yt-dlp supports
		String input = downloadInput.toLowerCase();
		if (input.startsWith("mp3:")) {
			return DownloadableState.IMPOSSIBLE;
		}
		
		// List of supported platforms
		String[] supportedPlatforms = {
			"youtube.", "youtu.be", "dailymotion.", "vimeo.", "tf1.", "wat.tv",
			"clubic.", "6play.", "arte.", "france.tv", "pluzz.", "canalplus.",
			"rtl.", "m6.", "w9.", "nrj12.", "nt1.", "d8.", "bfmtv.", "cnews.",
			"lequipe.", "rmc.", "eurosport.", "beinsport.", "sport24.", "lci.",
			"c8.", "cstar.", "gulli.", "gameone.", "j-one.", "cherie25.",
			"tv5monde.", "arte.tv", "france24.", "franceinfo.", "franceinter.",
			"franceculture.", "fip.", "tsf.", "nova.", "skyrock.", "virgin.",
			"fun.", "rfm.", "rtl2.", "europe1.", "sud.", "rts.", "rtbf.",
			"vrt.", "npo.", "zdf.", "ard.", "rai.", "rtve.", "rtp.", "rts.",
			"svt.", "nrk.", "dr.", "yle.", "mtv.", "vice.", "redbull.", "ted.",
			"khanacademy.", "coursera.", "edx.", "udemy.", "skillshare.",
			"masterclass.", "pluralsight.", "lynda.", "treehouse.", "codecademy."
		};
		
		for (String platform : supportedPlatforms) {
			if (input.contains(platform)) {
				LOG.debug("YouTube plugin can download from: " + downloadInput);
				return DownloadableState.SPECIFIC;
			}
		}
		
		LOG.debug("YouTube plugin cannot download from: " + downloadInput);
		return DownloadableState.IMPOSSIBLE;
	}
}
