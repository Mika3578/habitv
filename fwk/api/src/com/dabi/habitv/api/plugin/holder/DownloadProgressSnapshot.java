package com.dabi.habitv.api.plugin.holder;

/**
 * Immutable progress snapshot for downloaders. Null numeric fields mean unknown.
 * {@code progressRatio == null} means indeterminate progress for the current stage.
 */
public final class DownloadProgressSnapshot {

	private final DownloadStage stage;
	private final Double progressRatio;
	private final Long downloadedBytes;
	private final Long totalBytes;
	private final Double bytesPerSecond;
	private final Long etaSeconds;
	private final String detail;

	private DownloadProgressSnapshot(final DownloadStage stage, final Double progressRatio,
			final Long downloadedBytes, final Long totalBytes, final Double bytesPerSecond,
			final Long etaSeconds, final String detail) {
		this.stage = stage == null ? DownloadStage.DOWNLOADING : stage;
		this.progressRatio = progressRatio;
		this.downloadedBytes = downloadedBytes;
		this.totalBytes = totalBytes;
		this.bytesPerSecond = bytesPerSecond;
		this.etaSeconds = etaSeconds;
		this.detail = detail;
	}

	public static DownloadProgressSnapshot of(final DownloadStage stage, final Double progressRatio,
			final Long downloadedBytes, final Long totalBytes, final Double bytesPerSecond,
			final Long etaSeconds, final String detail) {
		return new DownloadProgressSnapshot(stage, progressRatio, downloadedBytes, totalBytes,
				bytesPerSecond, etaSeconds, detail);
	}

	public static DownloadProgressSnapshot indeterminate(final DownloadStage stage, final String detail) {
		return new DownloadProgressSnapshot(stage, null, null, null, null, null, detail);
	}

	/**
	 * Builds a snapshot from a legacy percentage string such as {@code "45.2"} or {@code "100"}.
	 *
	 * @param progression percentage string, or {@code null}
	 * @return snapshot, never {@code null}
	 */
	public static DownloadProgressSnapshot fromProgressionString(final String progression) {
		if (progression == null || progression.trim().isEmpty()) {
			return indeterminate(DownloadStage.PREPARING, null);
		}
		try {
			final double percent = Double.parseDouble(progression.trim().replace(',', '.'));
			if (Double.isNaN(percent) || percent < 0) {
				return indeterminate(DownloadStage.PREPARING, null);
			}
			final double ratio = Math.min(1.0d, percent / 100.0d);
			return of(DownloadStage.DOWNLOADING, Double.valueOf(ratio), null, null, null, null, null);
		} catch (final NumberFormatException e) {
			return indeterminate(DownloadStage.PREPARING, progression);
		}
	}

	/**
	 * Computes a ratio from byte counts when both are known and total is positive.
	 */
	public static Double ratioFromBytes(final Long downloadedBytes, final Long totalBytes) {
		if (downloadedBytes == null || totalBytes == null || totalBytes.longValue() <= 0L) {
			return null;
		}
		final double ratio = downloadedBytes.doubleValue() / totalBytes.doubleValue();
		if (ratio < 0) {
			return Double.valueOf(0);
		}
		if (ratio > 1) {
			return Double.valueOf(1);
		}
		return Double.valueOf(ratio);
	}

	public DownloadStage getStage() {
		return stage;
	}

	/**
	 * @return progress in {@code [0, 1]}, or {@code null} when indeterminate
	 */
	public Double getProgressRatio() {
		return progressRatio;
	}

	public Long getDownloadedBytes() {
		return downloadedBytes;
	}

	public Long getTotalBytes() {
		return totalBytes;
	}

	public Double getBytesPerSecond() {
		return bytesPerSecond;
	}

	public Long getEtaSeconds() {
		return etaSeconds;
	}

	public String getDetail() {
		return detail;
	}

	public boolean isIndeterminate() {
		return progressRatio == null;
	}

	public DownloadProgressSnapshot withStage(final DownloadStage newStage) {
		return of(newStage, progressRatio, downloadedBytes, totalBytes, bytesPerSecond, etaSeconds, detail);
	}

	public DownloadProgressSnapshot withDetail(final String newDetail) {
		return of(stage, progressRatio, downloadedBytes, totalBytes, bytesPerSecond, etaSeconds, newDetail);
	}
}
