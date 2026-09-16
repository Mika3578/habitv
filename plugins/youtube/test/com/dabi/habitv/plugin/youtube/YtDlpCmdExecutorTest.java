package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class YtDlpCmdExecutorTest {

	private static final class TestableExecutor extends YtDlpCmdExecutor {

		TestableExecutor() {
			super("/bin/sh -c #CMD#", "yt-dlp");
		}

		String parseProgress(final String line) {
			return handleProgression(line);
		}

		DownloadProgressSnapshot snapshot() {
			return getProgressSnapshot();
		}
	}

	@Test
	public void parseProgressFromYtDlpDownloadLine() {
		final TestableExecutor executor = new TestableExecutor();
		assertEquals("45.2", executor.parseProgress("[download]  45.2% of 10.00MiB at 1.23MiB/s ETA 00:05"));
		assertEquals(DownloadStage.DOWNLOADING, executor.snapshot().getStage());
		assertEquals(0.452d, executor.snapshot().getProgressRatio().doubleValue(), 0.000001d);
	}

	@Test
	public void preparingLineUpdatesIndeterminateSnapshot() {
		final TestableExecutor executor = new TestableExecutor();
		final String token = executor.parseProgress("[info] Downloading webpage");
		assertEquals("stage:PREPARING", token);
		assertNull(executor.getProgression());
		assertTrue(executor.snapshot().isIndeterminate());
		assertEquals(DownloadStage.PREPARING, executor.snapshot().getStage());
	}

	@Test
	public void unrelatedLineDoesNotChangeProgression() {
		final TestableExecutor executor = new TestableExecutor();
		assertNull(executor.parseProgress("WARNING: something happened"));
	}

	@Test
	public void mergerTransitionsToIndeterminateSnapshot() {
		final TestableExecutor executor = new TestableExecutor();
		executor.parseProgress("[download] 100.0% of 10.00MiB at 1.00MiB/s ETA 00:00");
		final String stageToken = executor.parseProgress("[Merger] Merging formats into \"out.mp4\"");
		assertEquals("stage:MERGING", stageToken);
		assertNull(executor.getProgression());
		assertTrue(executor.snapshot().isIndeterminate());
		assertEquals(DownloadStage.MERGING, executor.snapshot().getStage());
	}
}
