package com.dabi.habitv.provider.tf1plus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

final class Tf1PlusPremiumDownloadExecutor {

	private static final Logger LOG = Logger.getLogger(Tf1PlusPremiumDownloadExecutor.class);

	private static final long MAX_HUNG_TIME = 900000L;

	private static final String SCRIPT_NAME = "tf1plus_premium_download.py";

	private static final String BUNDLED_SCRIPT_ENTRY = "scripts/" + SCRIPT_NAME;

	private Tf1PlusPremiumDownloadExecutor() {
	}

	static ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		if (!Tf1PlusPremiumDownloadConfig.isConfigured()) {
			throw new DownloadFailedException(
					new IllegalStateException(Tf1PlusPremiumDownloadConfig.userFacingConfigurationMessage()));
		}
		final String streamId = Tf1PlusEpisodeUrl.parsePremiumStreamId(downloadParam.getDownloadInput());
		if (StringUtils.isEmpty(streamId)) {
			throw new DownloadFailedException(
					new IllegalStateException("missing TF1 stream id for premium replay download"));
		}
		final String pluginDir = downloaders.getPluginDir();
		final String scriptPath = resolveScriptPath(pluginDir);
		final String ffmpegPath = resolveFfmpegPath(downloaders);
		final String[] commandArgv = buildCommandArgv(streamId, scriptPath, downloadParam.getDownloadOutput(),
				ffmpegPath);
		try {
			return new Tf1PlusPremiumCmdExecutor(downloaders.getCmdProcessor(), commandArgv, MAX_HUNG_TIME);
		} catch (final ExecutorFailedException e) {
			throw new DownloadFailedException(e);
		}
	}

	static String[] buildCommandArgv(final String streamId, final String scriptPath, final String outputPath,
			final String ffmpegPath) {
		final List<String> argv = new ArrayList<String>();
		addPythonTokens(argv, Tf1PlusPremiumDownloadConfig.pythonCommand());
		argv.add(scriptPath);
		argv.add("--stream-id");
		argv.add(streamId);
		argv.add("--output");
		argv.add(outputPath);
		argv.add("--ffmpeg");
		argv.add(ffmpegPath);
		final String nM3u8 = Tf1PlusPremiumDownloadConfig.nM3u8DlRePath();
		if (StringUtils.isNotEmpty(nM3u8)) {
			argv.add("--n-m3u8dl-re");
			argv.add(nM3u8);
		}
		return argv.toArray(new String[argv.size()]);
	}

	private static void addPythonTokens(final List<String> argv, final String pythonCommand) {
		final String command = StringUtils.isEmpty(pythonCommand) ? "python" : pythonCommand.trim();
		final File asPath = new File(command);
		if (command.indexOf(' ') >= 0 && !asPath.isFile()) {
			final String[] tokens = command.split("\\s+");
			for (final String token : tokens) {
				argv.add(token);
			}
			return;
		}
		argv.add(command);
	}

	private static String resolveFfmpegPath(final DownloaderPluginHolder downloaders) {
		final String configured = downloaders.getBinPath(com.dabi.habitv.framework.FrameworkConf.FFMPEG);
		if (StringUtils.isNotEmpty(configured)) {
			return configured;
		}
		return "ffmpeg";
	}

	private static String resolveScriptPath(final String pluginDir) throws DownloadFailedException {
		final File extractedScript = resolveExtractedScriptFile(pluginDir);
		if (StringUtils.isNotEmpty(pluginDir)) {
			try {
				if (shouldRefreshExtractedScript(pluginDir, extractedScript)) {
					final File fromJar = extractScriptFromTf1PlusJar(pluginDir, extractedScript);
					if (fromJar != null) {
						LOG.info("TF1+ premium script refreshed from plugin jar: "
								+ extractedScript.getAbsolutePath());
					}
				}
			} catch (IOException e) {
				throw new DownloadFailedException(e);
			}
			final File pluginTreeScript = new File(pluginDir, "tf1plus/scripts/" + SCRIPT_NAME);
			if (pluginTreeScript.isFile()) {
				LOG.info("TF1+ premium script path (plugin tree): " + pluginTreeScript.getAbsolutePath());
				return pluginTreeScript.getAbsolutePath();
			}
		}
		if (extractedScript.isFile() && extractedScript.length() > 0L) {
			LOG.info("TF1+ premium script path (plugin dir): " + extractedScript.getAbsolutePath());
			return extractedScript.getAbsolutePath();
		}
		if (StringUtils.isNotEmpty(pluginDir)) {
			try {
				final File fromJar = extractScriptFromTf1PlusJar(pluginDir, extractedScript);
				if (fromJar != null) {
					LOG.info("TF1+ premium script path (extracted from jar): " + fromJar.getAbsolutePath());
					return fromJar.getAbsolutePath();
				}
			} catch (IOException e) {
				throw new DownloadFailedException(e);
			}
		}
		final File devScript = new File("plugins/tf1plus/scripts/" + SCRIPT_NAME);
		if (devScript.isFile()) {
			LOG.info("TF1+ premium script path (dev tree): " + devScript.getAbsolutePath());
			return devScript.getAbsolutePath();
		}
		final InputStream resource = openBundledScriptStream();
		if (resource != null) {
			try {
				final String path = materializeBundledScript(resource, extractedScript);
				LOG.info("TF1+ premium script path (classloader): " + path);
				return path;
			} catch (IOException e) {
				throw new DownloadFailedException(e);
			} finally {
				try {
					resource.close();
				} catch (IOException e) {
					// ignore close failure after read
				}
			}
		}
		LOG.warn("TF1+ premium script not found; pluginDir=" + pluginDir);
		throw new DownloadFailedException(new IllegalStateException(SCRIPT_NAME + " was not found"));
	}

	/**
	 * Extracts the bundled helper next to the tf1plus plugin jar (not under
	 * {@code habitv/scripts} in the user profile).
	 */
	private static File resolveExtractedScriptFile(final String pluginDir) {
		if (StringUtils.isNotEmpty(pluginDir)) {
			return new File(pluginDir, SCRIPT_NAME);
		}
		return new File(System.getProperty("java.io.tmpdir"), "habitv-tf1plus-" + SCRIPT_NAME);
	}

	private static boolean shouldRefreshExtractedScript(final String pluginDir, final File extractedScript) {
		final File directory = new File(pluginDir);
		if (!directory.isDirectory()) {
			return false;
		}
		final File[] candidates = directory.listFiles();
		if (candidates == null) {
			return false;
		}
		long newestJarTime = 0L;
		for (final File candidate : candidates) {
			if (candidate.isFile() && candidate.getName().startsWith("tf1plus")
					&& candidate.getName().endsWith(".jar")) {
				newestJarTime = Math.max(newestJarTime, candidate.lastModified());
			}
		}
		if (newestJarTime <= 0L) {
			return false;
		}
		return !extractedScript.isFile() || extractedScript.lastModified() < newestJarTime;
	}

	private static File extractScriptFromTf1PlusJar(final String pluginDir, final File targetScript)
			throws IOException {
		final File directory = new File(pluginDir);
		if (!directory.isDirectory()) {
			return null;
		}
		final File[] candidates = directory.listFiles();
		if (candidates == null) {
			return null;
		}
		for (final File candidate : candidates) {
			if (!candidate.isFile() || !candidate.getName().startsWith("tf1plus")
					|| !candidate.getName().endsWith(".jar")) {
				continue;
			}
			JarFile jarFile = null;
			InputStream input = null;
			try {
				jarFile = new JarFile(candidate);
				final JarEntry entry = jarFile.getJarEntry(BUNDLED_SCRIPT_ENTRY);
				if (entry == null) {
					continue;
				}
				input = jarFile.getInputStream(entry);
				if (input == null) {
					continue;
				}
				copyStream(input, targetScript);
				return targetScript;
			} finally {
				if (input != null) {
					input.close();
				}
				if (jarFile != null) {
					jarFile.close();
				}
			}
		}
		return null;
	}

	private static InputStream openBundledScriptStream() {
		final ClassLoader loader = Tf1PlusPremiumDownloadExecutor.class.getClassLoader();
		if (loader == null) {
			return null;
		}
		return loader.getResourceAsStream("scripts/" + SCRIPT_NAME);
	}

	private static String materializeBundledScript(final InputStream resource, final File targetScript)
			throws IOException {
		final File parent = targetScript.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IOException("unable to create directory: " + parent.getAbsolutePath());
		}
		copyStream(resource, targetScript);
		return targetScript.getAbsolutePath();
	}

	private static void copyStream(final InputStream input, final File target) throws IOException {
		OutputStream output = null;
		try {
			output = Files.newOutputStream(target.toPath());
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) >= 0) {
				output.write(buffer, 0, read);
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}
}
