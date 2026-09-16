package com.dabi.habitv.api.plugin.holder;

public interface ProcessHolder {

	public static final ProcessHolder EMPTY_PROCESS_HOLDER = new ProcessHolder() {

		@Override
		public void stop() {

		}

		@Override
		public String getProgression() {
			return null;
		}

		@Override
		public void start() {

		}
	};

	void start();

	void stop();

	/**
	 * Legacy percentage string (for example {@code "45.2"}), or {@code null} when unknown.
	 */
	String getProgression();

	/**
	 * Richer progress snapshot. Default implementation derives a snapshot from
	 * {@link #getProgression()} so existing downloaders keep working unchanged.
	 *
	 * @return never {@code null}
	 */
	default DownloadProgressSnapshot getProgressSnapshot() {
		return DownloadProgressSnapshot.fromProgressionString(getProgression());
	}

}
