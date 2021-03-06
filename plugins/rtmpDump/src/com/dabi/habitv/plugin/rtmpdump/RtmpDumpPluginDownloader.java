package com.dabi.habitv.plugin.rtmpdump;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginWithProxyInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.update.BaseUpdatablePlugin;

public class RtmpDumpPluginDownloader extends BaseUpdatablePlugin implements
		PluginDownloaderInterface, PluginWithProxyInterface {

	private static final String RTMPDUMP_PREFIX = "rtmp:";

	private static final Pattern VERSION_PATTERN = Pattern
			.compile("RTMPDump ([0-9A-Za-z.-]*) .*");
	private Map<ProtocolEnum, ProxyDTO> protocol2proxy;

	@Override
	public String getName() {
		return RtmpDumpConf.NAME;
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
			cmd += RtmpDumpConf.DUMP_CMD;
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
				cmd += " --socks " + sockProxy.getHost() + ":"
						+ sockProxy.getPort();
			}
		}
		try {
			return (new RtmpDumpCmdExecutor(downloaders.getCmdProcessor(), cmd));
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
		return " -h";
	}

	@Override
	public void setProxies(final Map<ProtocolEnum, ProxyDTO> protocol2proxy) {
		this.protocol2proxy = protocol2proxy;
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		return downloadInput.startsWith(RTMPDUMP_PREFIX) ? DownloadableState.SPECIFIC
				: DownloadableState.IMPOSSIBLE;
	}

}
