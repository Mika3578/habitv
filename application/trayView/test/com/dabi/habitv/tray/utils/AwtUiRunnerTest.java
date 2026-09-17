package com.dabi.habitv.tray.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.Test;

public class AwtUiRunnerTest {

	@Test
	public void runLaterExecutesOnEdt() throws Exception {
		final CountDownLatch done = new CountDownLatch(1);
		final AtomicBoolean onEdt = new AtomicBoolean(false);

		AwtUiRunner.runLater(new Runnable() {
			@Override
			public void run() {
				onEdt.set(SwingUtilities.isEventDispatchThread());
				done.countDown();
			}
		});

		assertTrue("EDT work did not run in time", done.await(2L, TimeUnit.SECONDS));
		assertTrue("work must run on the AWT Event Dispatch Thread", onEdt.get());
	}

	@Test
	public void runLaterRunsImmediatelyWhenAlreadyOnEdt() throws Exception {
		final CountDownLatch done = new CountDownLatch(1);
		final AtomicBoolean ranInline = new AtomicBoolean(false);

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				AwtUiRunner.runLater(new Runnable() {
					@Override
					public void run() {
						ranInline.set(true);
						done.countDown();
					}
				});
				assertTrue("expected synchronous run when already on EDT", ranInline.get());
			}
		});

		assertTrue(done.await(2L, TimeUnit.SECONDS));
		assertNotNull(Boolean.valueOf(ranInline.get()));
	}

	@Test(expected = NullPointerException.class)
	public void runLaterRejectsNullWork() {
		AwtUiRunner.runLater(null);
	}
}
