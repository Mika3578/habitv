package com.dabi.habitv.framework.plugin.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.exception.HungProcessException;

public class CmdExecutorTest {

	private CmdExecutor cmd;

	private boolean hang;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		cmd = new CmdExecutor("", "", 500) {

			@Override
			protected Process buildProcess() throws ExecutorFailedException {
				return new Process() {

					@Override
					public int waitFor() throws InterruptedException {
						return 0;
					}

					@Override
					public OutputStream getOutputStream() {
						return null;
					}

					@Override
					public InputStream getInputStream() {
						final StringBuilder printStream = new StringBuilder("");
						printStream.append("0\n");
						printStream.append("1\n");
						printStream.append("2\n");
						printStream.append("3\n");
						printStream.append("3\n");
						printStream.append("3\n");
						printStream.append("3\n");
						printStream.append("3\n");
						final InputStream is = new ByteArrayInputStream(printStream.toString().getBytes());
						return is;
					}

					@Override
					public InputStream getErrorStream() {
						final StringBuilder printStream = new StringBuilder("");
						final InputStream is = new ByteArrayInputStream(printStream.toString().getBytes());
						return is;
					}

					@Override
					public int exitValue() {
						return 0;
					}

					@Override
					public void destroy() {

					}
				};
			}

			@Override
			protected String handleProgression(final String line) {
				if (hang) {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						throw new TechnicalException(e);
					}
				}
				return line;
			}

		};
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = HungProcessException.class)
	public final void testExecuteWithHungCmd() throws ExecutorFailedException {
		hang = true;
		cmd.start();
	}

	public final void testExecuteNormal() throws ExecutorFailedException {
		hang = false;
		cmd.start();
	}

	@Test
	public void progressionStalledWhenActivityTokenUnchanged() {
		final long now = 10_000L;
		final long lastTime = 0L;
		assertTrue(CmdExecutor.isProgressionStalled("stage:MERGING", "stage:MERGING", now, lastTime, 500L));
	}

	@Test
	public void progressionNotStalledWhenActivityTokenChanges() {
		final long now = 10_000L;
		final long lastTime = 0L;
		assertFalse(CmdExecutor.isProgressionStalled("stage:MERGING", "line:[Merger] step 2", now, lastTime, 500L));
	}

	@Test
	public void progressionStalledUsesActivityChangeTimeNotLogSampleTime() {
		final long activityChangeTime = 1_000L;
		final long now = activityChangeTime + 600L;
		final long recentLogSampleTime = now - 100L;
		assertTrue(CmdExecutor.isProgressionStalled("out:1", "out:1", now, activityChangeTime, 500L));
		assertFalse(CmdExecutor.isProgressionStalled("out:1", "out:1", now, recentLogSampleTime, 500L));
	}

	@Test
	public void concurrentStreamsSameActivityTokenDoesNotFalseStall() throws ExecutorFailedException {
		final long delayBeforeStdoutMs = 4_000L;
		final long delayBeforeStderrMs = 4_020L;
		final CmdExecutor concurrentCmd = new CmdExecutor("", "", 500) {

			@Override
			protected Process buildProcess() throws ExecutorFailedException {
				return new Process() {

					@Override
					public int waitFor() throws InterruptedException {
						return 0;
					}

					@Override
					public OutputStream getOutputStream() {
						return null;
					}

					@Override
					public InputStream getInputStream() {
						return new DelayedLineInputStream("prog:stdout\n", delayBeforeStdoutMs);
					}

					@Override
					public InputStream getErrorStream() {
						return new DelayedLineInputStream("prog:stderr\n", delayBeforeStderrMs);
					}

					@Override
					public int exitValue() {
						return 0;
					}

					@Override
					public void destroy() {
					}
				};
			}

			@Override
			protected String handleProgression(final String line) {
				if (line.startsWith("prog:")) {
					return line;
				}
				return null;
			}

			@Override
			protected String progressionActivityTokenFor(final String line, final String progressionResult) {
				return "shared-activity";
			}
		};
		concurrentCmd.start();
	}

	@Test(timeout = 8000)
	public void stallDetectionDestroysProcessSoBlockedReadersExit() throws ExecutorFailedException {
		final AtomicBoolean destroyed = new AtomicBoolean(false);
		final BlockingUntilClosedInputStream stderr = new BlockingUntilClosedInputStream();
		final CmdExecutor stallCmd = new CmdExecutor("", "", 200) {

			@Override
			protected Process buildProcess() throws ExecutorFailedException {
				return new Process() {

					@Override
					public int waitFor() throws InterruptedException {
						return 0;
					}

					@Override
					public OutputStream getOutputStream() {
						return null;
					}

					@Override
					public InputStream getInputStream() {
						return new TwoLineDelayedInputStream("1\n", "1\n", 2300L);
					}

					@Override
					public InputStream getErrorStream() {
						return stderr;
					}

					@Override
					public int exitValue() {
						return 0;
					}

					@Override
					public void destroy() {
						destroyed.set(true);
						stderr.closeQuietly();
					}
				};
			}

			@Override
			protected String handleProgression(final String line) {
				return line;
			}
		};
		try {
			stallCmd.start();
			fail("expected hung process");
		} catch (final HungProcessException expected) {
			assertTrue(destroyed.get());
		}
	}

	private static final class TwoLineDelayedInputStream extends InputStream {

		private final byte[] first;

		private final byte[] second;

		private final long delayBetweenMs;

		private int phase;

		private int position;

		TwoLineDelayedInputStream(final String firstLine, final String secondLine, final long delayBetweenMs) {
			this.first = firstLine.getBytes();
			this.second = secondLine.getBytes();
			this.delayBetweenMs = delayBetweenMs;
		}

		@Override
		public int read() throws IOException {
			if (phase == 0) {
				if (position < first.length) {
					return first[position++] & 0xff;
				}
				phase = 1;
				position = 0;
			}
			if (phase == 1) {
				try {
					Thread.sleep(delayBetweenMs);
				} catch (final InterruptedException e) {
					throw new IOException(e);
				}
				phase = 2;
			}
			if (position < second.length) {
				return second[position++] & 0xff;
			}
			return -1;
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			if (len == 0) {
				return 0;
			}
			final int value = read();
			if (value == -1) {
				return -1;
			}
			b[off] = (byte) value;
			return 1;
		}
	}

	private static final class BlockingUntilClosedInputStream extends InputStream {

		private final Object lock = new Object();

		private boolean closed;

		@Override
		public int read() throws IOException {
			synchronized (lock) {
				while (!closed) {
					try {
						lock.wait();
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						return -1;
					}
				}
				return -1;
			}
		}

		void closeQuietly() {
			synchronized (lock) {
				closed = true;
				lock.notifyAll();
			}
		}
	}

	private static final class DelayedLineInputStream extends InputStream {

		private final byte[] data;

		private int position;

		private final long delayMs;

		private boolean delayApplied;

		DelayedLineInputStream(final String content, final long delayMs) {
			this.data = content.getBytes();
			this.delayMs = delayMs;
		}

		@Override
		public int read() throws IOException {
			if (!delayApplied && delayMs > 0) {
				delayApplied = true;
				try {
					Thread.sleep(delayMs);
				} catch (final InterruptedException e) {
					throw new IOException(e);
				}
			}
			if (position >= data.length) {
				return -1;
			}
			return data[position++] & 0xff;
		}
	}
}
