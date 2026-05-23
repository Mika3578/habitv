package com.dabi.habitv.api.plugin.holder;

import java.util.Map;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;

public final class DownloaderPluginHolder extends AbstractPluginHolder<PluginDownloaderInterface> {

	private final Map<String, String> downloaderName2BinPath;

	private final String downloadOutput;

	private final String indexDir;

	private final String cmdProcessor;

	private final String binDir;

	private final String pluginDir;

	private final boolean embedSubtitles;

	public DownloaderPluginHolder(final String cmdProcessor, final Map<String, PluginDownloaderInterface> downloaderName2downloader,
			final Map<String, String> downloaderName2BinPath, final String downloadOutputDir, final String indexDir, final String binDir, final String pluginDir) {
		this(cmdProcessor, downloaderName2downloader, downloaderName2BinPath,
				downloadOutputDir, indexDir, binDir, pluginDir, false);
	}

	public DownloaderPluginHolder(final String cmdProcessor, final Map<String, PluginDownloaderInterface> downloaderName2downloader,
			final Map<String, String> downloaderName2BinPath, final String downloadOutputDir, final String indexDir, final String binDir, final String pluginDir,
			final boolean embedSubtitles) {
		super(downloaderName2downloader);
		this.cmdProcessor = cmdProcessor;
		this.downloaderName2BinPath = downloaderName2BinPath;
		this.downloadOutput = downloadOutputDir;
		this.indexDir = indexDir;
		this.binDir = binDir;
		this.pluginDir = pluginDir;
		this.embedSubtitles = embedSubtitles;
	}

	public String getBinPath(final String downloaderName) {
		return downloaderName2BinPath == null ? null : downloaderName2BinPath.get(downloaderName);
	}

	public String getDownloadOutput() {
		return downloadOutput;
	}

	public String getIndexDir() {
		return indexDir;
	}

	public String getCmdProcessor() {
		return cmdProcessor;
	}

	public String getBinDir() {
		return binDir;
	}

	public String getPluginDir() {
		return pluginDir;
	}

	public boolean isEmbedSubtitlesEnabled() {
		return embedSubtitles;
	}

}
