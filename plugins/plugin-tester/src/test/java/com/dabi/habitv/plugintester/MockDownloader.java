package com.dabi.habitv.plugintester;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

public class MockDownloader implements PluginDownloaderInterface {

	private static final Logger LOG = Logger.getLogger(MockDownloader.class);

	@Override
	public String getName() {
		return MockDownloader.class.getSimpleName();
	}

	@Override
	public PluginDownloaderInterface.DownloadableState canDownload(final String downloadInput) {
		return PluginDownloaderInterface.DownloadableState.SPECIFIC;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		LOG.info("download of " + downloadParam + " ");
		return ProcessHolder.EMPTY_PROCESS_HOLDER;
	}
} 
