package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;

public class YtDlpRuntimeDiagnosticsTest {

	private static final String PYINSTALLER_STDERR = "[PYI-17924:ERROR] Failed to extract entry: Cryptodome\\PublicKey\\_ec_ws.pyd.";

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
	public void runPreflightAllowsStartupLongerThanOneSecond() {
		final File parent = new File(System.getProperty("java.io.tmpdir"),
				"habitv-test-" + UUID.randomUUID());
		final File binDir = new File(parent, "bin");
		final File javaHome = new File(System.getProperty("java.home"));
		final String javaExecName = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
		final String javaExec = new File(javaHome, "bin" + File.separator + javaExecName).getAbsolutePath();
		final String classPath = System.getProperty("java.class.path");
		final String quotedJavaExec = "\"" + javaExec + "\"";
		final String quotedClassPath = "\"" + classPath + "\"";
		final String executablePath = quotedJavaExec + " -cp " + quotedClassPath + " "
				+ SlowVersionMain.class.getName();

		try {
			final long startedAt = System.currentTimeMillis();
			YtDlpRuntimeDiagnostics.runPreflight("", executablePath, binDir.getAbsolutePath());
			final long elapsedMs = System.currentTimeMillis() - startedAt;

			assertTrue("preflight should allow command startup longer than one second, elapsed ms=" + elapsedMs,
					elapsedMs >= 1200L);
		} finally {
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

	public static class SlowVersionMain {

		public static void main(final String[] args) throws InterruptedException {
			Thread.sleep(1500L);
			System.out.println("2026.05.21");
		}
	}

}
