package com.dabi.habitv.plugin.adobeHDS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginWithProxyInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.update.BaseUpdatablePlugin;

/**
 * Deprecated since Adobe Flash and Adobe HDS reached end-of-life on
 * 2020-12-31; the bundled AdobeHDS.php requires an undocumented PHP
 * runtime on the user's machine and no maintained Windows binary is
 * published upstream (see scripts/static-repo/tool-sources.properties:
 * adobeHDS.type=skip). Scheduled for removal under a dedicated
 * provider-inventory follow-up.
 */
@Deprecated
public class AdobeHDSPluginDownloader extends BaseUpdatablePlugin implements
		PluginDownloaderInterface, PluginWithProxyInterface {

	private static final Logger LOG = Logger
			.getLogger(AdobeHDSPluginDownloader.class);

	private static final Pattern VERSION_PATTERN = Pattern
			.compile("AdobeHDS v([0-9A-Za-z.-]*) .*");
	private Map<ProtocolEnum, ProxyDTO> protocol2proxy;

	public AdobeHDSPluginDownloader() {
		LOG.warn("adobeHDS downloader is deprecated (Flash/HDS EOL 2020-12-31) "
				+ "and scheduled for removal; prefer yt-dlp or ffmpeg "
				+ "for current providers.");
	}

	@Override
	public String getName() {
		return AdobeHDSConf.NAME;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {

		final String binParam = getBinParam(downloaders);
		String cmd = binParam + " ";
		final String cmdParam = downloadParam
				.getParam(FrameworkConf.PARAMETER_ARGS);
		if (cmdParam == null) {
			cmd += AdobeHDSConf.CMD;
		} else {
			cmd += cmdParam;
		}
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_INPUT,
				Matcher.quoteReplacement(downloadParam.getDownloadInput()));
		cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_DESTINATION,
				Matcher.quoteReplacement(downloadParam.getDownloadOutput()));
		if (protocol2proxy != null) {
			final ProxyDTO sockProxy = protocol2proxy
					.get(ProxyDTO.ProtocolEnum.SOCKS);
			if (sockProxy != null) {
				cmd += " --proxy " + sockProxy.getHost() + ":"
						+ sockProxy.getPort();
			}
		}
		try {
			return (new AdobeHDSCmdExecutor(downloaders.getCmdProcessor(), cmd));
		} catch (final ExecutorFailedException e) {
			throw new DownloadFailedException(e);
		}
	}

	@Override
	protected Pattern getVersionPattern() {
		return VERSION_PATTERN;
	}

	@Override
	protected String getVersionParam() {
		return " --help";
	}

	@Override
	public void setProxies(final Map<ProtocolEnum, ProxyDTO> protocol2proxy) {
		this.protocol2proxy = protocol2proxy;
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.contains("\\.f4m")
				|| downloadInput.contains("\\.f4f") ? DownloadableState.SPECIFIC
				: DownloadableState.IMPOSSIBLE;
	}

	protected String getLinuxDefaultBuildPath() {
		return "php " + getAdobePHPScriptPath();
	}

	public static String getAdobePHPScriptPath() {
		try {
			File temp = File.createTempFile("AdobeHDS", ".php");
			try (InputStream inAdobeScript = AdobeHDSPluginDownloader.class
					.getResourceAsStream("AdobeHDS.php")) {
				try (OutputStream outTemp = new FileOutputStream(temp)) {
					int i;
					while ((i = inAdobeScript.read()) != -1) {
						outTemp.write(i);
					}
					outTemp.flush();
				}
			}

			return "php \"" + temp.getAbsolutePath() + "\" ";
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	protected String getWindowsDefaultExe() {
		//return "php " + getAdobePHPScriptPath();
		return "AdobeHDS.exe";
	}

}
