package com.dabi.habitv.tray.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.tray.model.ActionProgress;

/**
 * Deterministic French UI formatting for download progress cells and tooltips.
 */
public final class DownloadProgressFormatter {

	private static final Locale FR = Locale.FRANCE;
	private static final DecimalFormat ONE_DECIMAL;
	private static final DecimalFormat INTEGER;

	static {
		final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(FR);
		ONE_DECIMAL = new DecimalFormat("0.0", symbols);
		INTEGER = new DecimalFormat("0", symbols);
	}

	private DownloadProgressFormatter() {
	}

	public static String formatPercentage(final Double progressRatio) {
		if (progressRatio == null) {
			return null;
		}
		final double percent = progressRatio.doubleValue() * 100.0d;
		if (Math.abs(percent - Math.rint(percent)) < 0.05d) {
			return INTEGER.format(Math.round(percent)) + " %";
		}
		return ONE_DECIMAL.format(percent) + " %";
	}

	public static String formatBytes(final Long bytes) {
		if (bytes == null || bytes.longValue() < 0L) {
			return null;
		}
		final double value = bytes.doubleValue();
		if (value < 1024d) {
			return INTEGER.format(value) + " o";
		}
		if (value < 1024d * 1024d) {
			return ONE_DECIMAL.format(value / 1024d) + " Ko";
		}
		if (value < 1024d * 1024d * 1024d) {
			return ONE_DECIMAL.format(value / (1024d * 1024d)) + " Mo";
		}
		return ONE_DECIMAL.format(value / (1024d * 1024d * 1024d)) + " Go";
	}

	public static String formatSpeed(final Double bytesPerSecond) {
		if (bytesPerSecond == null || bytesPerSecond.doubleValue() < 0d) {
			return null;
		}
		final String size = formatBytes(Long.valueOf(Math.round(bytesPerSecond.doubleValue())));
		return size == null ? null : size + "/s";
	}

	public static String formatEta(final Long etaSeconds) {
		if (etaSeconds == null || etaSeconds.longValue() < 0L) {
			return null;
		}
		long remaining = etaSeconds.longValue();
		final long hours = remaining / 3600L;
		remaining %= 3600L;
		final long minutes = remaining / 60L;
		final long seconds = remaining % 60L;
		if (hours > 0L) {
			return String.format(Locale.ROOT, "%02d:%02d:%02d", Long.valueOf(hours), Long.valueOf(minutes),
					Long.valueOf(seconds));
		}
		return String.format(Locale.ROOT, "%02d:%02d", Long.valueOf(minutes), Long.valueOf(seconds));
	}

	public static String stageLabel(final DownloadStage stage) {
		if (stage == null) {
			return "Téléchargement";
		}
		switch (stage) {
		case QUEUED:
			return "En attente";
		case PREPARING:
			return "Préparation";
		case DOWNLOADING:
			return "Téléchargement";
		case MERGING:
			return "Fusion audio / vidéo";
		case REMUXING:
			return "Remux";
		case SUBTITLES:
			return "Traitement des sous-titres";
		case METADATA:
			return "Écriture des métadonnées";
		case POST_PROCESSING:
			return "Post-traitement";
		case FINALIZING:
			return "Finalisation";
		case COMPLETED:
			return "Terminé";
		case FAILED:
			return "Échec";
		case CANCELLED:
			return "Annulé";
		default:
			return "Téléchargement";
		}
	}

	/**
	 * Short stage name for the progress cell. Prefer Vidéo/Audio when known so the
	 * two yt-dlp transfer cycles are understandable.
	 */
	public static String cellStageLabel(final DownloadProgressSnapshot snapshot) {
		if (snapshot == null) {
			return stageLabel((DownloadStage) null);
		}
		final String detail = snapshot.getDetail();
		if ("Vidéo".equals(detail) || "Audio".equals(detail)) {
			return detail;
		}
		return stageLabel(snapshot.getStage());
	}

	/**
	 * Compact label shown inside the progress bar.
	 * Percentage comes first so it stays visible in a narrow Etat column.
	 */
	public static String formatCellText(final DownloadProgressSnapshot snapshot) {
		if (snapshot == null) {
			return "";
		}
		final String stage = cellStageLabel(snapshot);
		if (snapshot.isIndeterminate() || snapshot.getProgressRatio() == null) {
			return stage + "…";
		}
		final String percent = formatPercentage(snapshot.getProgressRatio());
		if (percent == null) {
			return stage;
		}
		// Keep the cell short: percent + stream kind. Bytes/speed/ETA stay in the tooltip.
		return percent + " · " + stage;
	}

	/**
	 * Multi-line tooltip content.
	 */
	public static String formatTooltip(final DownloadProgressSnapshot snapshot) {
		if (snapshot == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(cellStageLabel(snapshot));
		if (snapshot.getProgressRatio() != null) {
			builder.append('\n').append(formatPercentage(snapshot.getProgressRatio()));
		}
		final String downloaded = formatBytes(snapshot.getDownloadedBytes());
		final String total = formatBytes(snapshot.getTotalBytes());
		if (downloaded != null && total != null) {
			builder.append('\n').append(downloaded).append(" / ").append(total);
		}
		final String speed = formatSpeed(snapshot.getBytesPerSecond());
		if (speed != null) {
			builder.append('\n').append(speed);
		}
		final String eta = formatEta(snapshot.getEtaSeconds());
		if (eta != null) {
			builder.append("\nTemps restant : ").append(eta);
		}
		return builder.toString();
	}

	/**
	 * Resolves the snapshot to display for an action row, combining episode state and process holder.
	 */
	public static DownloadProgressSnapshot resolveSnapshot(final ActionProgress actionProgress) {
		if (actionProgress == null) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.QUEUED, null);
		}
		final EpisodeStateEnum state = actionProgress.getState();
		if (state == EpisodeStateEnum.DOWNLOAD_STARTING || state == EpisodeStateEnum.EXPORT_STARTING) {
			if (actionProgress.getProcessHolder() != null) {
				final DownloadProgressSnapshot fromProcess = actionProgress.getProcessHolder().getProgressSnapshot();
				if (state == EpisodeStateEnum.EXPORT_STARTING) {
					final String exportDetail = actionProgress.getInfo();
					if (fromProcess != null && fromProcess.getProgressRatio() != null) {
						return DownloadProgressSnapshot.of(DownloadStage.POST_PROCESSING,
								fromProcess.getProgressRatio(), fromProcess.getDownloadedBytes(),
								fromProcess.getTotalBytes(), fromProcess.getBytesPerSecond(),
								fromProcess.getEtaSeconds(), exportDetail);
					}
					return DownloadProgressSnapshot.indeterminate(DownloadStage.POST_PROCESSING, exportDetail);
				}
				return fromProcess;
			}
			return DownloadProgressSnapshot.indeterminate(DownloadStage.PREPARING, null);
		}
		if (state == EpisodeStateEnum.TO_DOWNLOAD) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.QUEUED, null);
		}
		if (state == EpisodeStateEnum.DOWNLOADED) {
			return DownloadProgressSnapshot.of(DownloadStage.COMPLETED, Double.valueOf(1.0d), null, null, null, null,
					null);
		}
		if (state == EpisodeStateEnum.READY) {
			return DownloadProgressSnapshot.of(DownloadStage.COMPLETED, Double.valueOf(1.0d), null, null, null, null,
					null);
		}
		if (state == EpisodeStateEnum.STOPPED) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.CANCELLED, actionProgress.getInfo());
		}
		if (state == EpisodeStateEnum.DOWNLOAD_FAILED || state == EpisodeStateEnum.EXPORT_FAILED
				|| state == EpisodeStateEnum.FAILED || state == EpisodeStateEnum.TO_MANY_FAILED) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.FAILED, actionProgress.getInfo());
		}
		return DownloadProgressSnapshot.fromProgressionString(actionProgress.getProgress());
	}
}
