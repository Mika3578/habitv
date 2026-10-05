package com.dabi.habitv.plugin.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class FFMPEGCmdExecutorTest {

	@Test
	public void legacyProgressSnapshotUsesRemuxingStage() {
		final DownloadProgressSnapshot snapshot = FFMPEGCmdExecutor.snapshotFromLegacyProgressionString("50");
		assertEquals(DownloadStage.REMUXING, snapshot.getStage());
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.0001d);
	}

	@Test
	public void getProgressionHidesIndeterminateStageTokens() {
		final FFMpegTestExecutor executor = new FFMpegTestExecutor("", "");
		executor.progressLine("duration=10.000000");
		assertNull(executor.legacyProgression());
	}

	@Test
	public void legacyProgressSnapshotFromStderrPath() {
		final FFMpegTestExecutor executor = new FFMpegTestExecutor("", "");
		executor.progressLine("  Duration: 00:00:10.00, start: 0.000000, bitrate: 100 kb/s");
		executor.progressLine("size=       1kB time=00:00:05.00 bitrate=   1.0kbits/s speed=1x");
		final DownloadProgressSnapshot snapshot = executor.readSnapshot();
		assertEquals(DownloadStage.REMUXING, snapshot.getStage());
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.05d);
	}

	@Test
	public void durationNaLeavesProgressIndeterminateThenAcceptsLaterProgress() {
		final FFMpegTestExecutor executor = new FFMpegTestExecutor("", "");
		assertNull(executor.progressLine("  Duration: N/A, start: 0.000000, bitrate: N/A"));
		DownloadProgressSnapshot snapshot = executor.readSnapshot();
		assertTrue(snapshot == null || snapshot.isIndeterminate());

		assertNull(executor.progressLine("size=       1kB time=00:00:05.00 bitrate=   1.0kbits/s speed=1x"));
		snapshot = executor.readSnapshot();
		assertTrue(snapshot == null || snapshot.isIndeterminate());

		assertEquals("50.0",
				executor.progressLine("frame=  10 fps=10 q=0.0 size=    1kB time=00:00:01.00 bitrate=N/A speed=1x (50.0%)"));
		snapshot = FFMPEGCmdExecutor.snapshotFromLegacyProgressionString("50.0");
		assertEquals(DownloadStage.REMUXING, snapshot.getStage());
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.0001d);
	}

	private static final class FFMpegTestExecutor extends FFMPEGCmdExecutor {

		FFMpegTestExecutor(final String cmdProcessor, final String cmd) {
			super(cmdProcessor, cmd);
		}

		String progressLine(final String line) {
			return handleProgression(line);
		}

		DownloadProgressSnapshot readSnapshot() {
			return getProgressSnapshot();
		}

		String legacyProgression() {
			return getProgression();
		}
	}
}
