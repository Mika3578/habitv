package com.dabi.habitv.tray.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class DownloadProgressFormatterTest {

	@Test
	public void percentageUsesFrenchDecimalAndNoArtificialDotZero() {
		assertEquals("11,4 %", DownloadProgressFormatter.formatPercentage(Double.valueOf(0.114d)));
		assertEquals("45,2 %", DownloadProgressFormatter.formatPercentage(Double.valueOf(0.452d)));
		assertEquals("11 %", DownloadProgressFormatter.formatPercentage(Double.valueOf(0.11d)));
		assertFalse("11.0%".equals(DownloadProgressFormatter.formatPercentage(Double.valueOf(0.11d))));
	}

	@Test
	public void percentageFromExactByteRatioIsNotElevenDotZero() {
		final Double ratio = DownloadProgressSnapshot.ratioFromBytes(Long.valueOf(18_200_000L),
				Long.valueOf(159_600_000L));
		final String formatted = DownloadProgressFormatter.formatPercentage(ratio);
		assertTrue(formatted.startsWith("11,"));
		assertFalse(formatted.startsWith("11,0"));
		assertFalse("11.0%".equals(formatted));
	}

	@Test
	public void byteAndSpeedFormatting() {
		assertEquals("18,2 Mo", DownloadProgressFormatter.formatBytes(Long.valueOf(19_083_264L)));
		assertEquals("7,8 Mo/s",
				DownloadProgressFormatter.formatSpeed(Double.valueOf(7.8d * 1024d * 1024d)));
	}

	@Test
	public void etaFormatting() {
		assertEquals("00:18", DownloadProgressFormatter.formatEta(Long.valueOf(18L)));
		assertEquals("01:04:32", DownloadProgressFormatter.formatEta(Long.valueOf(1L * 3600L + 4L * 60L + 32L)));
	}

	@Test
	public void stageLabelsAreFrench() {
		assertEquals("Téléchargement", DownloadProgressFormatter.stageLabel(DownloadStage.DOWNLOADING));
		assertEquals("Fusion audio / vidéo", DownloadProgressFormatter.stageLabel(DownloadStage.MERGING));
		assertEquals("Remux", DownloadProgressFormatter.stageLabel(DownloadStage.REMUXING));
		assertEquals("Traitement des sous-titres", DownloadProgressFormatter.stageLabel(DownloadStage.SUBTITLES));
		assertEquals("Écriture des métadonnées", DownloadProgressFormatter.stageLabel(DownloadStage.METADATA));
		assertEquals("Finalisation", DownloadProgressFormatter.stageLabel(DownloadStage.FINALIZING));
	}

	@Test
	public void cellTextPutsPercentageFirstAndUsesStreamLabel() {
		final DownloadProgressSnapshot downloading = DownloadProgressSnapshot.of(DownloadStage.DOWNLOADING,
				Double.valueOf(0.114d), Long.valueOf(19_083_264L), Long.valueOf(167_352_730L),
				Double.valueOf(7.8d * 1024d * 1024d), Long.valueOf(18L), "Vidéo");
		assertEquals("11,4 % · Vidéo", DownloadProgressFormatter.formatCellText(downloading));

		final DownloadProgressSnapshot merging = DownloadProgressSnapshot.indeterminate(DownloadStage.MERGING,
				null);
		assertEquals("Fusion audio / vidéo…", DownloadProgressFormatter.formatCellText(merging));
	}

	@Test
	public void tooltipIncludesSpeedAndEta() {
		final DownloadProgressSnapshot downloading = DownloadProgressSnapshot.of(DownloadStage.DOWNLOADING,
				Double.valueOf(0.114d), Long.valueOf(19_083_264L), Long.valueOf(167_352_730L),
				Double.valueOf(7.8d * 1024d * 1024d), Long.valueOf(18L), "Vidéo");
		final String tooltip = DownloadProgressFormatter.formatTooltip(downloading);
		assertTrue(tooltip.contains("Vidéo"));
		assertTrue(tooltip.contains("11,4 %"));
		assertTrue(tooltip.contains("Mo/s"));
		assertTrue(tooltip.contains("Temps restant : 00:18"));
	}
}
