package com.dabi.habitv.plugin.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class FfmpegProgressParserTest {

	@Test
	public void progressPipeReportsRatioWhenDurationKnown() {
		final FfmpegProgressParser.State state = new FfmpegProgressParser.State();
		assertTrue(FfmpegProgressParser.parseLine("duration=10.000000", state).isIndeterminate());
		final DownloadProgressSnapshot atHalf = FfmpegProgressParser.parseLine("out_time_us=5000000", state);
		assertNotNull(atHalf);
		assertEquals(DownloadStage.REMUXING, atHalf.getStage());
		assertEquals(0.5d, atHalf.getProgressRatio().doubleValue(), 0.0001d);
		assertEquals("50", FfmpegProgressParser.toProgressionString(atHalf));
	}

	@Test
	public void progressUnknownUntilDurationSet() {
		final FfmpegProgressParser.State state = new FfmpegProgressParser.State();
		final DownloadProgressSnapshot beforeDuration = FfmpegProgressParser.parseLine("out_time_us=1000000", state);
		assertNotNull(beforeDuration);
		assertTrue(beforeDuration.isIndeterminate());
		assertNull(beforeDuration.getProgressRatio());
	}

	@Test
	public void stderrDurationAndTimeFallback() {
		final FfmpegProgressParser.State state = new FfmpegProgressParser.State();
		FfmpegProgressParser.parseLine("  Duration: 00:00:10.00, start: 0.000000, bitrate: 100 kb/s", state);
		final DownloadProgressSnapshot snapshot = FfmpegProgressParser.parseLine("size=       1kB time=00:00:05.00 bitrate=   1.0kbits/s speed=1x", state);
		assertNotNull(snapshot);
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.05d);
	}

	@Test
	public void outTimeMsTreatedAsMicroseconds() {
		final FfmpegProgressParser.State state = new FfmpegProgressParser.State();
		FfmpegProgressParser.parseLine("duration=10.000000", state);
		final DownloadProgressSnapshot snapshot = FfmpegProgressParser.parseLine("out_time_ms=5000000", state);
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.0001d);
	}

	@Test
	public void outTimeStringParsed() {
		final FfmpegProgressParser.State state = new FfmpegProgressParser.State();
		FfmpegProgressParser.parseLine("duration=2.000000", state);
		final DownloadProgressSnapshot snapshot = FfmpegProgressParser.parseLine("out_time=00:00:01.000000", state);
		assertEquals(0.5d, snapshot.getProgressRatio().doubleValue(), 0.01d);
	}

	@Test
	public void parseHhMmSsDurationMinutesAndFractionalSeconds() {
		assertEquals(Long.valueOf(65_500_000L),
				FfmpegProgressParser.parseHhMmSsDurationToMicros("01:05.5"));
	}

	@Test
	public void parseHhMmSsDurationHoursMinutesSecondsAndFraction() {
		assertEquals(Long.valueOf(3_723_250_000L),
				FfmpegProgressParser.parseHhMmSsDurationToMicros("1:02:03.25"));
	}
}
