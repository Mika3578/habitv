package com.dabi.habitv.plugin.rclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RcloneCmdExecutorTest {

	private static final class TestableExecutor extends RcloneCmdExecutor {

		TestableExecutor() {
			super("/bin/sh -c #CMD#", "rclone copy /tmp/foo remote:bar");
		}

		String parseProgress(final String line) {
			return handleProgression(line);
		}

		boolean parseSuccess(final String fullOutput) {
			return isSuccess(fullOutput);
		}
	}

	@Test
	public void parseProgressFromRcloneTransferredLine() {
		final TestableExecutor executor = new TestableExecutor();
		assertEquals("42", executor.parseProgress(
				"Transferred: 4.200 MiB / 10.000 MiB, 42%, 1.234 MiB/s, ETA 5s"));
	}

	@Test
	public void parseProgressReturnsNullWhenNoPercentage() {
		final TestableExecutor executor = new TestableExecutor();
		assertNull(executor.parseProgress("INFO  : Starting transfer"));
	}

	@Test
	public void isSuccessOnCleanTransferredOutput() {
		final TestableExecutor executor = new TestableExecutor();
		assertTrue(executor.parseSuccess(
				"Transferred: 10.000 MiB / 10.000 MiB, 100%, 2 MiB/s, ETA 0s\nTransferred: 1 / 1, 100%"));
	}

	@Test
	public void isSuccessFalseWhenErrorPresent() {
		final TestableExecutor executor = new TestableExecutor();
		assertFalse(executor.parseSuccess(
				"Transferred: 0 / 1, 0%\nERROR : remote: Failed to copy: connection refused"));
	}

	@Test
	public void isSuccessFalseOnNullOrEmptyOutput() {
		final TestableExecutor executor = new TestableExecutor();
		assertFalse(executor.parseSuccess(null));
		assertFalse(executor.parseSuccess(""));
	}
}
