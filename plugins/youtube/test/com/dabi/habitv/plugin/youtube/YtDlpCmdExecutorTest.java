package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;

public class YtDlpCmdExecutorTest {

	private static final class TestableExecutor extends YtDlpCmdExecutor {

		TestableExecutor() {
			super("/bin/sh -c #CMD#", "yt-dlp");
		}

		String parseProgress(final String line) {
			return handleProgression(line);
		}

		ExecutorFailedException classifyFailure(final String fullOutput, final String lastLine) {
			return buildFailureException("yt-dlp", fullOutput, lastLine, null);
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

	@Test
	public void classifyDrmProtectedFailureMessage() {
		final TestableExecutor executor = new TestableExecutor();
		final String output = "ERROR: [wat.tv] 12345: This video is DRM protected";
		final ExecutorFailedException failure = executor.classifyFailure(output, output);
		assertEquals("This TF1+ video is DRM protected and cannot be downloaded by yt-dlp.", failure.getLastLine());
		assertTrue(failure.getFullOuput().contains("DRM protected"));
	}

}
