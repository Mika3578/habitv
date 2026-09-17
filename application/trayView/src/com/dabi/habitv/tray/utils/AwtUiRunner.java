package com.dabi.habitv.tray.utils;

import javax.swing.SwingUtilities;

/**
 * Marshals UI-triggered AWT/Swing work onto the Event Dispatch Thread.
 */
public final class AwtUiRunner {

	private AwtUiRunner() {
	}

	/**
	 * Runs {@code work} on the AWT Event Dispatch Thread. If already on the
	 * EDT, runs immediately; otherwise schedules with
	 * {@link SwingUtilities#invokeLater(Runnable)}.
	 *
	 * @param work non-null runnable
	 */
	public static void runLater(final Runnable work) {
		if (work == null) {
			throw new NullPointerException("work");
		}
		if (SwingUtilities.isEventDispatchThread()) {
			work.run();
		} else {
			SwingUtilities.invokeLater(work);
		}
	}
}
