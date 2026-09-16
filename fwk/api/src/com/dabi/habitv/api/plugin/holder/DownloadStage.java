package com.dabi.habitv.api.plugin.holder;

/**
 * Observable download / post-processing stages exposed to the UI.
 * Only stages that HabiTV can actually observe from a downloader should be used.
 */
public enum DownloadStage {
	/** Waiting before the downloader process starts. */
	QUEUED,
	/** Source preparation / extractor analysis without byte progress. */
	PREPARING,
	/** Network transfer with known or estimated progress. */
	DOWNLOADING,
	/** yt-dlp (or similar) merging audio and video streams. */
	MERGING,
	/** Remux / conversion (typically ffmpeg via yt-dlp). */
	REMUXING,
	/** Subtitle download or embedding. */
	SUBTITLES,
	/** Metadata / thumbnail embedding. */
	METADATA,
	/** Generic post-processing when a more specific stage is unknown. */
	POST_PROCESSING,
	/** File move / rename / finalization. */
	FINALIZING,
	COMPLETED,
	FAILED,
	CANCELLED;

	/**
	 * @return true when the UI should use an indeterminate progress bar
	 */
	public boolean isIndeterminate() {
		switch (this) {
		case QUEUED:
		case PREPARING:
		case MERGING:
		case REMUXING:
		case SUBTITLES:
		case METADATA:
		case POST_PROCESSING:
		case FINALIZING:
			return true;
		default:
			return false;
		}
	}

	/**
	 * @return true for yt-dlp-style work after the byte transfer
	 */
	public boolean isPostProcessing() {
		switch (this) {
		case MERGING:
		case REMUXING:
		case SUBTITLES:
		case METADATA:
		case POST_PROCESSING:
		case FINALIZING:
			return true;
		default:
			return false;
		}
	}
}
