package com.dabi.habitv.api.plugin.holder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DownloadProgressSnapshotTest {

	@Test
	public void fromProgressionStringPreservesFractionalRatio() {
		final DownloadProgressSnapshot snapshot = DownloadProgressSnapshot.fromProgressionString("11.4");
		assertEquals(DownloadStage.DOWNLOADING, snapshot.getStage());
		assertNotNull(snapshot.getProgressRatio());
		assertEquals(0.114d, snapshot.getProgressRatio().doubleValue(), 0.000001d);
		assertFalse(snapshot.isIndeterminate());
	}

	@Test
	public void fromProgressionStringHandlesIntegerPercent() {
		final DownloadProgressSnapshot snapshot = DownloadProgressSnapshot.fromProgressionString("11");
		assertEquals(0.11d, snapshot.getProgressRatio().doubleValue(), 0.000001d);
	}

	@Test
	public void fromProgressionStringNullIsIndeterminate() {
		final DownloadProgressSnapshot snapshot = DownloadProgressSnapshot.fromProgressionString(null);
		assertTrue(snapshot.isIndeterminate());
		assertEquals(DownloadStage.PREPARING, snapshot.getStage());
	}

	@Test
	public void ratioFromBytesUsesExactDivision() {
		final Double ratio = DownloadProgressSnapshot.ratioFromBytes(Long.valueOf(18_200_000L),
				Long.valueOf(159_600_000L));
		assertNotNull(ratio);
		assertEquals(18_200_000d / 159_600_000d, ratio.doubleValue(), 0.0000001d);
		assertFalse(Math.abs(ratio.doubleValue() * 100.0d - 11.0d) < 0.0001d);
	}

	@Test
	public void ratioFromBytesNullWhenTotalUnknown() {
		assertNull(DownloadProgressSnapshot.ratioFromBytes(Long.valueOf(10L), null));
		assertNull(DownloadProgressSnapshot.ratioFromBytes(null, Long.valueOf(10L)));
		assertNull(DownloadProgressSnapshot.ratioFromBytes(Long.valueOf(10L), Long.valueOf(0L)));
	}

	@Test
	public void processHolderDefaultUsesProgressionString() {
		final ProcessHolder holder = new ProcessHolder() {
			@Override
			public void start() {
			}

			@Override
			public void stop() {
			}

			@Override
			public String getProgression() {
				return "45.2";
			}
		};
		final DownloadProgressSnapshot snapshot = holder.getProgressSnapshot();
		assertEquals(0.452d, snapshot.getProgressRatio().doubleValue(), 0.000001d);
	}
}
