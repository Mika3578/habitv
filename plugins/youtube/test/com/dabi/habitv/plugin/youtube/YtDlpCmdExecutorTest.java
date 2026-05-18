package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class YtDlpCmdExecutorTest {

	private static final class TestableExecutor extends YtDlpCmdExecutor {

		TestableExecutor() {
			super("/bin/sh -c #CMD#", "yt-dlp");
		}

		String parseProgress(final String line) {
			return handleProgression(line);
		}
	}

	@Test
	public void parseProgressFromYtDlpDownloadLine() {
		final TestableExecutor executor = new TestableExecutor();
		assertEquals("45.2", executor.parseProgress("[download]  45.2% of 10.00MiB at 1.23MiB/s ETA 00:05"));
	}

	@Test
	public void parseProgressReturnsNullWhenNoPercentage() {
		final TestableExecutor executor = new TestableExecutor();
		assertNull(executor.parseProgress("[info] Downloading webpage"));
	}

}
