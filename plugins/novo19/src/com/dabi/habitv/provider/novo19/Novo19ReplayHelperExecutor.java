package com.dabi.habitv.provider.novo19;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

final class Novo19ReplayHelperExecutor {

	private static final Logger LOG = Logger.getLogger(Novo19ReplayHelperExecutor.class);

	private static final long MAX_HUNG_TIME = 900000L;

	private static final String SCRIPT_NAME = "novo19_helper.py";

	private static final String BUNDLED_SCRIPT_ENTRY = "scripts/novo19_helper.py";

	private Novo19ReplayHelperExecutor() {
	}

	static ProcessHolder download(final String assetId, final DownloadParamDTO downloadParam,
			final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		if (!Novo19ReplayHelperConfig.isConfigured()) {
			throw new DownloadFailedException(
					new IllegalStateException(Novo19ReplayHelperConfig.userFacingConfigurationMessage()));
		}
		if (StringUtils.isEmpty(assetId)) {
			throw new DownloadFailedException(new IllegalStateException("missing NOVO19 asset id for helper download"));
		}
		final String scriptPath = resolveScriptPath(downloaders == null ? null : downloaders.getPluginDir());
		final String ffmpeg = resolveFfmpegPath(downloaders);
		final String[] argv = buildCommandArgv(assetId, scriptPath, downloadParam.getDownloadOutput(), ffmpeg);
		return new Novo19ReplayHelperCmdExecutor(
				downloaders == null ? null : downloaders.getCmdProcessor(), argv, MAX_HUNG_TIME);
	}

	static String[] buildCommandArgv(final String assetId, final String scriptPath, final String outputPath,
			final String ffmpeg) {
		final List<String> argv = new ArrayList<String>();
		addPythonTokens(argv, Novo19ReplayHelperConfig.pythonCommand());
		argv.add(scriptPath);
		argv.add("--asset-id");
		argv.add(assetId);
		argv.add("--output");
		argv.add(outputPath);
		argv.add("--ffmpeg");
		argv.add(ffmpeg);
		final String rePath = Novo19ReplayHelperConfig.nM3u8DlRePath();
		if (StringUtils.isNotEmpty(rePath)) {
			argv.add("--n-m3u8dl-re");
			argv.add(rePath);
		}
		return argv.toArray(new String[argv.size()]);
	}

	private static void addPythonTokens(final List<String> argv, final String pythonCommand) {
		final String command = StringUtils.isEmpty(pythonCommand) ? "python" : pythonCommand.trim();
		final File asFile = new File(command);
		if (command.indexOf(' ') >= 0 && !asFile.isFile()) {
			for (final String token : command.split("\\s+")) {
				argv.add(token);
			}
			return;
		}
		argv.add(command);
	}

	private static String resolveFfmpegPath(final DownloaderPluginHolder downloaders) {
		if (downloaders != null) {
			final String configured = downloaders.getBinPath("ffmpeg");
			if (StringUtils.isNotEmpty(configured)) {
				return configured;
			}
			final String binDir = downloaders.getBinDir();
			if (StringUtils.isNotEmpty(binDir)) {
				final File windows = new File(binDir, "ffmpeg.exe");
				if (windows.isFile()) {
					return windows.getAbsolutePath();
				}
				final File unix = new File(binDir, "ffmpeg");
				if (unix.isFile()) {
					return unix.getAbsolutePath();
				}
			}
		}
		return "ffmpeg";
	}

	private static String resolveScriptPath(final String pluginDir) throws DownloadFailedException {
		if (StringUtils.isNotEmpty(pluginDir)) {
			final File besidePlugins = new File(pluginDir, SCRIPT_NAME);
			if (besidePlugins.isFile()) {
				LOG.info("NOVO19 helper script path (plugin dir): " + besidePlugins.getAbsolutePath());
				return besidePlugins.getAbsolutePath();
			}
			final File extracted = materializeBundledScript(new File(pluginDir, SCRIPT_NAME));
			if (extracted != null && extracted.isFile()) {
				LOG.info("NOVO19 helper script path (extracted): " + extracted.getAbsolutePath());
				return extracted.getAbsolutePath();
			}
		}
		final File devScript = new File("plugins/novo19/scripts/" + SCRIPT_NAME);
		if (devScript.isFile()) {
			LOG.info("NOVO19 helper script path (dev tree): " + devScript.getAbsolutePath());
			return devScript.getAbsolutePath();
		}
		final File cwdScript = new File(SCRIPT_NAME);
		if (cwdScript.isFile()) {
			return cwdScript.getAbsolutePath();
		}
		throw new DownloadFailedException(new IllegalStateException(SCRIPT_NAME + " was not found"));
	}

	private static File materializeBundledScript(final File target) {
		final InputStream bundled = Novo19ReplayHelperExecutor.class.getClassLoader()
				.getResourceAsStream(BUNDLED_SCRIPT_ENTRY);
		if (bundled == null) {
			return null;
		}
		try {
			copyStream(bundled, target);
			return target;
		} catch (final IOException e) {
			LOG.warn("Unable to extract bundled NOVO19 helper script: " + e.getMessage());
			return null;
		} finally {
			try {
				bundled.close();
			} catch (final IOException ignored) {
				// ignore
			}
		}
	}

	private static void copyStream(final InputStream input, final File target) throws IOException {
		final File parent = target.getParentFile();
		if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
			throw new IOException("Unable to create script directory: " + parent.getAbsolutePath());
		}
		final OutputStream output = new FileOutputStream(target);
		try {
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) >= 0) {
				output.write(buffer, 0, read);
			}
		} finally {
			output.close();
		}
	}

}
