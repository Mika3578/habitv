package com.dabi.habitv.plugin.rclone;

import com.dabi.habitv.api.plugin.api.PluginExporterInterface;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.ExportFailedException;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

public class RclonePluginExporterManager implements PluginExporterInterface {

	@Override
	public String getName() {
		return RcloneConf.NAME;
	}

	@Override
	public ProcessHolder export(final String cmdProcessor, final String cmd)
			throws ExportFailedException {
		try {
			return new RcloneCmdExecutor(cmdProcessor, cmd);
		} catch (final ExecutorFailedException e) {
			throw new ExportFailedException(e);
		}
	}
}
