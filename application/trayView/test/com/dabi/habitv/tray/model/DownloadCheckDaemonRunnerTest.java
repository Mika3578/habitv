package com.dabi.habitv.tray.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class DownloadCheckDaemonRunnerTest {

	@Test
	public void continuesAfterTransientCheckFailure() {
		final AtomicInteger checks = new AtomicInteger();
		final AtomicInteger errors = new AtomicInteger();
		final AtomicBoolean running = new AtomicBoolean(true);
		final List<Long> sleeps = new ArrayList<Long>();

		final DownloadCheckDaemonRunner runner = new DownloadCheckDaemonRunner(1000L, 5000L);
		runner.run(new DownloadCheckDaemonRunner.Hooks() {
			@Override
			public boolean isRunning() {
				return running.get() && checks.get() < 3;
			}

			@Override
			public void runCheck() throws Exception {
				final int n = checks.incrementAndGet();
				if (n == 1) {
					throw new RuntimeException("transient");
				}
				if (n >= 3) {
					running.set(false);
				}
			}

			@Override
			public void onError(final Exception error) {
				errors.incrementAndGet();
			}

			@Override
			public void sleep(final long millis) {
				sleeps.add(Long.valueOf(millis));
			}
		});

		assertEquals(1, errors.get());
		assertTrue("expected more than one check after failure", checks.get() >= 3);
		assertTrue("error backoff should use max(demon, backoff)", sleeps.contains(Long.valueOf(5000L)));
	}

	@Test
	public void interruptDuringSleepSkipsNextCheckOnce() {
		final AtomicInteger checks = new AtomicInteger();
		final AtomicBoolean running = new AtomicBoolean(true);
		final AtomicInteger sleepCount = new AtomicInteger();

		final DownloadCheckDaemonRunner runner = new DownloadCheckDaemonRunner(10L, 10L);
		runner.run(new DownloadCheckDaemonRunner.Hooks() {
			@Override
			public boolean isRunning() {
				return running.get() && checks.get() < 2;
			}

			@Override
			public void runCheck() {
				checks.incrementAndGet();
			}

			@Override
			public void onError(final Exception error) {
				// unused
			}

			@Override
			public void sleep(final long millis) throws InterruptedException {
				if (sleepCount.getAndIncrement() == 0) {
					throw new InterruptedException("manual search");
				}
				running.set(false);
			}
		});

		assertEquals("interrupted sleep should skip one check", 1, checks.get());
	}

	@Test
	public void stopsWhenRunningBecomesFalse() {
		final AtomicInteger checks = new AtomicInteger();
		final AtomicBoolean running = new AtomicBoolean(true);

		final DownloadCheckDaemonRunner runner = new DownloadCheckDaemonRunner(1L, 1L);
		runner.run(new DownloadCheckDaemonRunner.Hooks() {
			@Override
			public boolean isRunning() {
				return running.get();
			}

			@Override
			public void runCheck() {
				checks.incrementAndGet();
				running.set(false);
			}

			@Override
			public void onError(final Exception error) {
				// unused
			}

			@Override
			public void sleep(final long millis) {
				// unused when stopped after first check before sleep if loop rechecks
			}
		});

		assertEquals(1, checks.get());
		assertFalse(running.get());
	}
}
