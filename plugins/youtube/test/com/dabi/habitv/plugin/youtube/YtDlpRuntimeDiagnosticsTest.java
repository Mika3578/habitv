package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.framework.plugin.exception.HungProcessException;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;
import com.dabi.habitv.framework.plugin.utils.OSUtils;

public class YtDlpRuntimeDiagnosticsTest {

	private static final String PYINSTALLER_STDERR = "[PYI-17924:ERROR] Failed to extract entry: Cryptodome\\PublicKey\\_ec_ws.pyd.";

	@Test
	public void supportsProgressTemplateForRecentYtDlp() {
		assertTrue(YtDlpRuntimeDiagnostics.supportsProgressTemplate("2024.08.01"));
		assertFalse(YtDlpRuntimeDiagnostics.supportsProgressTemplate("2023.09.01"));
		assertFalse(YtDlpRuntimeDiagnostics.supportsProgressTemplate("2021.01.01 youtube-dl"));
	}

	@Test
	public void detectsPyInstallerBootstrapFailure() {
		assertTrue(YtDlpRuntimeDiagnostics.isBootstrapExtractionFailure(PYINSTALLER_STDERR));
	}

	@Test
	public void bootstrapUserMessageContainsRecoveryHints() {
		final String message = YtDlpRuntimeDiagnostics.buildBootstrapFailureUserMessage("C:\\habitv\\bin\\yt-dlp.exe");
		assertTrue(message.contains("yt-dlp failed before the download started"));
		assertTrue(message.contains("clear TEMP _MEI folders"));
		assertTrue(message.contains("replace yt-dlp.exe"));
		assertTrue(message.contains("run yt-dlp.exe --version"));
	}

	@Test
	public void genericDownloaderErrorRemainsUnclassified() {
		final String genericError = "ERROR: unable to download video data: HTTP Error 403: Forbidden";
		assertFalse(YtDlpRuntimeDiagnostics.isBootstrapExtractionFailure(genericError));
		final ExecutorFailedException generic = new ExecutorFailedException("yt-dlp \"url\"", genericError,
				genericError, null);
		final ExecutorFailedException result = YtDlpRuntimeDiagnostics.asBootstrapFailureIfNeeded("yt-dlp \"url\"",
				genericError, "C:\\habitv\\bin\\yt-dlp.exe", generic);
		assertEquals(generic, result);
		assertEquals(genericError, result.getLastLine());
	}

	@Test
	public void asBootstrapFailureUpgradesPyInstallerOutput() {
		final ExecutorFailedException cause = new ExecutorFailedException("yt-dlp --version", PYINSTALLER_STDERR,
				PYINSTALLER_STDERR, null);
		final ExecutorFailedException upgraded = YtDlpRuntimeDiagnostics.asBootstrapFailureIfNeeded("yt-dlp --version",
				PYINSTALLER_STDERR, "C:\\habitv\\bin\\yt-dlp.exe", cause);
		assertNotNull(upgraded);
		assertTrue(upgraded.getLastLine().contains("yt-dlp failed before the download started"));
		assertTrue(upgraded.getLastLine().contains("clear TEMP _MEI folders"));
		assertTrue(upgraded.getLastLine().contains("replace yt-dlp.exe"));
		assertTrue(upgraded.getLastLine().contains("run yt-dlp.exe --version"));
	}

	@Test
	public void computeYtDlpTempDirDoesNotCreateDirectories() {
		final File parent = new File(System.getProperty("java.io.tmpdir"),
				"habitv-test-" + UUID.randomUUID());
		final File binDir = new File(parent, "bin");
		final File expectedTemp = new File(parent, "tmp" + File.separator + "yt-dlp");

		final File computed = YtDlpRuntimeDiagnostics.computeYtDlpTempDir(binDir.getAbsolutePath());

		assertEquals(expectedTemp.getAbsolutePath(), computed.getAbsolutePath());
		assertFalse("compute must not create the parent directory", parent.exists());
		assertFalse("compute must not create the temp directory", computed.exists());
	}

	@Test
	public void runPreflightDisabledHasNoFilesystemSideEffects() {
		final File parent = new File(System.getProperty("java.io.tmpdir"),
				"habitv-test-" + UUID.randomUUID());
		final File binDir = new File(parent, "bin");
		final File expectedTemp = new File(parent, "tmp" + File.separator + "yt-dlp");

		YtDlpRuntimeDiagnostics.setPreflightEnabled(false);
		try {
			YtDlpRuntimeDiagnostics.runPreflight("", binDir.getAbsolutePath() + File.separator + "yt-dlp",
					binDir.getAbsolutePath());
		} finally {
			YtDlpRuntimeDiagnostics.setPreflightEnabled(true);
		}

		assertFalse("disabled preflight must not create the temp directory", expectedTemp.exists());
		assertFalse("disabled preflight must not create the parent directory", parent.exists());
	}

	@Test
	public void preflightHungProcessTimeoutAllowsSlowStartup() {
		assertTrue(YtDlpRuntimeDiagnostics.preflightHungProcessTimeoutMillis() >= 5000L);
	}

	@Test
	public void preflightVersionExecutorHonorsHungTimeout() throws Exception {
		final File parent = new File(System.getProperty("java.io.tmpdir"),
				"habitv-preflight-" + UUID.randomUUID());
		final File binDir = new File(parent, "bin");
		assertTrue(binDir.mkdirs());
		final String slowCmd = OSUtils.isWindows() ? "ping -n 6 127.0.0.1" : "sleep 5";
		YtDlpRuntimeDiagnostics.setPreflightTimeoutMillisForTests(400L);
		final CmdExecutor executor = YtDlpRuntimeDiagnostics.createPreflightVersionExecutor("", slowCmd,
				binDir.getAbsolutePath());
		final long startedAt = System.currentTimeMillis();
		try {
			try {
				executor.start();
				fail("expected hung preflight executor");
			} catch (HungProcessException expected) {
				assertTrue(System.currentTimeMillis() - startedAt < 3000L);
			}
		} finally {
			YtDlpRuntimeDiagnostics.setPreflightTimeoutMillisForTests(null);
			deleteRecursively(parent);
		}
	}

	private static void deleteRecursively(final File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					deleteRecursively(child);
				}
			}
		}
		file.delete();
	}

	@Test
	public void preflightHungTimeoutHonorsTestOverride() {
		final long defaultTimeout = YtDlpRuntimeDiagnostics.preflightHungProcessTimeoutMillis();
		assertTrue(defaultTimeout >= 5000L);
		YtDlpRuntimeDiagnostics.setPreflightTimeoutMillisForTests(777L);
		try {
			assertEquals(777L, YtDlpRuntimeDiagnostics.preflightHungProcessTimeoutMillis());
		} finally {
			YtDlpRuntimeDiagnostics.setPreflightTimeoutMillisForTests(null);
		}
	}

}
