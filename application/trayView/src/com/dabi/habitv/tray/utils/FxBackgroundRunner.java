package com.dabi.habitv.tray.utils;

/**
 * Starts UI-triggered work on a background thread so JavaFX event handlers stay
 * responsive. Callers must marshal any subsequent UI updates with
 * {@code Platform.runLater}.
 */
public final class FxBackgroundRunner {

	private FxBackgroundRunner() {
	}

	/**
	 * Starts {@code work} asynchronously. Unlike {@link Thread#run()}, this does
	 * not execute on the calling thread.
	 *
	 * @param work non-null runnable
	 * @return the started thread
	 */
	public static Thread start(final Runnable work) {
		if (work == null) {
			throw new NullPointerException("work");
		}
		final Thread thread = new Thread(work);
		thread.start();
		return thread;
	}
}
