package com.dabi.habitv.tray.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class FxBackgroundRunnerTest {

	@Test
	public void startRunsWorkOffCallingThread() throws Exception {
		final Thread callingThread = Thread.currentThread();
		final CountDownLatch started = new CountDownLatch(1);
		final AtomicReference<Thread> workerThread = new AtomicReference<Thread>();

		final Thread launched = FxBackgroundRunner.start(new Runnable() {
			@Override
			public void run() {
				workerThread.set(Thread.currentThread());
				started.countDown();
			}
		});

		assertNotNull(launched);
		assertTrue("background work did not start in time",
				started.await(2L, TimeUnit.SECONDS));
		assertNotSame("work must not run on the calling (FX) thread", callingThread,
				workerThread.get());
	}

	@Test(expected = NullPointerException.class)
	public void startRejectsNullWork() {
		FxBackgroundRunner.start(null);
	}
}
