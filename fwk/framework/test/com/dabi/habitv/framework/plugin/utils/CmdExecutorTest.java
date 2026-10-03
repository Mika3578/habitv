package com.dabi.habitv.framework.plugin.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
