package com.dabi.habitv.tray.model;

/**
 * Download-check daemon loop that survives transient cycle failures.
 */
public final class DownloadCheckDaemonRunner {

	/** Minimum wait after a failed cycle before the next attempt. */
	public static final long DEFAULT_ERROR_BACKOFF_MS = 30_000L;

	/**
	 * Hooks used by the loop so production and tests can inject behavior.
	 */
	public interface Hooks {
		boolean isRunning();

		void runCheck() throws Exception;

		void onError(Exception error);

		void sleep(long millis) throws InterruptedException;
	}

	private final long demonTimeMs;
	private final long errorBackoffMs;

	public DownloadCheckDaemonRunner(final long demonTimeMs) {
		this(demonTimeMs, DEFAULT_ERROR_BACKOFF_MS);
	}

	public DownloadCheckDaemonRunner(final long demonTimeMs, final long errorBackoffMs) {
		if (demonTimeMs < 0L) {
			throw new IllegalArgumentException("demonTimeMs must be >= 0");
		}
		if (errorBackoffMs < 0L) {
			throw new IllegalArgumentException("errorBackoffMs must be >= 0");
		}
		this.demonTimeMs = demonTimeMs;
		this.errorBackoffMs = errorBackoffMs;
	}

	/**
	 * Runs until {@link Hooks#isRunning()} is false. Recoverable exceptions from
	 * {@link Hooks#runCheck()} are reported via {@link Hooks#onError(Exception)}
	 * and followed by an error backoff sleep; the loop continues.
	 * {@link InterruptedException} during sleep skips the next check once (manual
	 * search interrupt) without stopping the daemon.
	 */
	public void run(final Hooks hooks) {
		if (hooks == null) {
			throw new NullPointerException("hooks");
		}
		boolean skipNextCheck = false;
		while (hooks.isRunning()) {
			if (!skipNextCheck) {
				try {
					hooks.runCheck();
				} catch (final Exception e) {
					hooks.onError(e);
					if (!sleepQuietly(hooks, Math.max(demonTimeMs, errorBackoffMs))) {
						skipNextCheck = true;
					}
					continue;
				}
			} else {
				skipNextCheck = false;
			}
			if (!sleepQuietly(hooks, demonTimeMs)) {
				skipNextCheck = true;
			}
		}
	}

	/**
	 * @return {@code true} if sleep completed; {@code false} if interrupted
	 */
	private static boolean sleepQuietly(final Hooks hooks, final long millis) {
		try {
			hooks.sleep(millis);
			return true;
		} catch (final InterruptedException e) {
			return false;
		}
	}
}
