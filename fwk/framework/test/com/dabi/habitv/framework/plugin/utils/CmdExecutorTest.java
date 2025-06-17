package com.dabi.habitv.framework.plugin.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.exception.HungProcessException;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

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
}
