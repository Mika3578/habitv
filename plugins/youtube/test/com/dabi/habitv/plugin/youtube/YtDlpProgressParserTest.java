package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class YtDlpProgressParserTest {

	@Test
	public void parseDownloadLineWithSizeSpeedAndEta() {
		final DownloadProgressSnapshot snapshot = YtDlpProgressParser.parse(
				"[download]  11.4% of 159.60MiB at 7.80MiB/s ETA 00:18", null);
		assertNotNull(snapshot);
		assertEquals(DownloadStage.DOWNLOADING, snapshot.getStage());
		assertEquals(0.114d, snapshot.getProgressRatio().doubleValue(), 0.000001d);
		assertNotNull(snapshot.getTotalBytes());
		assertNotNull(snapshot.getDownloadedBytes());
		assertNotNull(snapshot.getBytesPerSecond());
		assertEquals(Long.valueOf(18L), snapshot.getEtaSeconds());
		assertEquals("Vidéo", snapshot.getDetail());
	}

	@Test
	public void parseSimplePercentageStillWorks() {
		final DownloadProgressSnapshot snapshot = YtDlpProgressParser
				.parse("[download]  45.2% of 10.00MiB at 1.23MiB/s ETA 00:05", null);
		assertNotNull(snapshot);
		assertEquals("45.2", YtDlpProgressParser.toProgressionString(snapshot));
	}

	@Test
	public void parseMergerClearsNumericProgress() {
		final DownloadProgressSnapshot previous = YtDlpProgressParser
				.parse("[download] 100.0% of 10.00MiB at 1.00MiB/s ETA 00:00", null);
		final DownloadProgressSnapshot merged = YtDlpProgressParser.parse(
				"[Merger] Merging formats into \"episode.mp4\"", previous);
		assertNotNull(merged);
		assertEquals(DownloadStage.MERGING, merged.getStage());
		assertTrue(merged.isIndeterminate());
		assertNull(YtDlpProgressParser.toProgressionString(merged));
	}

	@Test
	public void parseFfmpegAsRemux() {
		final DownloadProgressSnapshot snapshot = YtDlpProgressParser
				.parse("[ffmpeg] Destination: episode.mp4", null);
		assertEquals(DownloadStage.REMUXING, snapshot.getStage());
		assertTrue(snapshot.isIndeterminate());
	}

	@Test
	public void parseMetadataAndSubtitlesAndFinalize() {
		assertEquals(DownloadStage.METADATA,
				YtDlpProgressParser.parse("[Metadata] Adding metadata to \"episode.mp4\"", null).getStage());
		assertEquals(DownloadStage.SUBTITLES,
				YtDlpProgressParser.parse("[EmbedSubtitle] Embedding subtitles in \"episode.mp4\"", null)
						.getStage());
		assertEquals(DownloadStage.FINALIZING,
				YtDlpProgressParser.parse("[MoveFiles] Moving file to final destination", null).getStage());
	}

	@Test
	public void parsePreparingBeforeDownload() {
		final DownloadProgressSnapshot snapshot = YtDlpProgressParser.parse("[info] Downloading webpage", null);
		assertEquals(DownloadStage.PREPARING, snapshot.getStage());
		assertTrue(snapshot.isIndeterminate());
	}

	@Test
	public void preparingDoesNotOverrideActiveDownload() {
		final DownloadProgressSnapshot previous = YtDlpProgressParser
				.parse("[download]  50.0% of 10.00MiB at 1.00MiB/s ETA 00:05", null);
		assertNull(YtDlpProgressParser.parse("[info] Downloading webpage", previous));
	}

	@Test
	public void unrelatedLineReturnsNull() {
		assertNull(YtDlpProgressParser.parse("WARNING: something happened", null));
		assertNull(YtDlpProgressParser.parse("", null));
		assertNull(YtDlpProgressParser.parse(null, null));
	}

	@Test
	public void parseSizeSpeedEtaHelpers() {
		assertEquals(Long.valueOf(Math.round(159.6d * 1024d * 1024d)),
				YtDlpProgressParser.parseSizeToBytes("159.6MiB"));
		assertEquals(7.8d * 1024d * 1024d, YtDlpProgressParser.parseSpeedToBytesPerSecond("7.8MiB/s").doubleValue(),
				1.0d);
		assertEquals(Long.valueOf(18L), YtDlpProgressParser.parseEtaToSeconds("00:18"));
		assertEquals(Long.valueOf(1L * 3600L + 4L * 60L + 32L),
				YtDlpProgressParser.parseEtaToSeconds("01:04:32"));
	}

	@Test
	public void toProgressionStringAvoidsArtificialDotZero() {
		final DownloadProgressSnapshot integerPercent = DownloadProgressSnapshot.of(DownloadStage.DOWNLOADING,
				Double.valueOf(0.11d), null, null, null, null, null);
		assertEquals("11", YtDlpProgressParser.toProgressionString(integerPercent));

		final DownloadProgressSnapshot fractional = DownloadProgressSnapshot.of(DownloadStage.DOWNLOADING,
				Double.valueOf(0.114d), null, null, null, null, null);
		assertEquals("11.4", YtDlpProgressParser.toProgressionString(fractional));
	}

	@Test
	public void destinationSetsVideoOrAudioLabel() {
		assertEquals("Vidéo", YtDlpProgressParser
				.parse("[download] Destination: episode.f137.mp4", null).getDetail());
		assertEquals("Audio", YtDlpProgressParser
				.parse("[download] Destination: episode.f140.m4a", null).getDetail());
	}

	@Test
	public void destinationIsDownloadingWithIndeterminateProgress() {
		final DownloadProgressSnapshot video = YtDlpProgressParser
				.parse("[download] Destination: episode.f137.mp4", null);
		assertNotNull(video);
		assertEquals(DownloadStage.DOWNLOADING, video.getStage());
		assertTrue(video.isIndeterminate());
		assertNull(video.getProgressRatio());
		assertNull(YtDlpProgressParser.toProgressionString(video));
		assertEquals("Vidéo", video.getDetail());

		final DownloadProgressSnapshot audio = YtDlpProgressParser
				.parse("[download] Destination: episode.f140.m4a", null);
		assertEquals(DownloadStage.DOWNLOADING, audio.getStage());
		assertTrue(audio.isIndeterminate());
		assertEquals("Audio", audio.getDetail());
	}

	@Test
	public void preparingDoesNotOverrideDestination() {
		final DownloadProgressSnapshot destination = YtDlpProgressParser
				.parse("[download] Destination: episode.f137.mp4", null);
		assertNull(YtDlpProgressParser.parse("[info] Downloading webpage", destination));
	}

	@Test
	public void secondTransferCycleSwitchesToAudio() {
		DownloadProgressSnapshot snapshot = YtDlpProgressParser.parse(
				"[download] Destination: episode.f137.mp4", null);
		snapshot = YtDlpProgressParser.parse("[download]  99.0% of 10.00MiB at 1.00MiB/s ETA 00:01", snapshot);
		assertEquals("Vidéo", snapshot.getDetail());
		snapshot = YtDlpProgressParser.parse("[download] Destination: episode.f140.m4a", snapshot);
		assertEquals("Audio", snapshot.getDetail());
		snapshot = YtDlpProgressParser.parse("[download]  12.0% of 2.00MiB at 1.00MiB/s ETA 00:02", snapshot);
		assertEquals("Audio", snapshot.getDetail());
		assertEquals(0.12d, snapshot.getProgressRatio().doubleValue(), 0.0001d);
	}

	@Test
	public void progressResetWithoutDestinationStillSwitchesToAudio() {
		DownloadProgressSnapshot snapshot = YtDlpProgressParser.parse(
				"[download]  95.0% of 10.00MiB at 1.00MiB/s ETA 00:01", null);
		assertEquals("Vidéo", snapshot.getDetail());
		snapshot = YtDlpProgressParser.parse("[download]   5.0% of 2.00MiB at 1.00MiB/s ETA 00:02", snapshot);
		assertEquals("Audio", snapshot.getDetail());
	}
}
