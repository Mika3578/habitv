package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Tf1PlusPremiumDownloadExecutorTest {

	@Test
	public void shouldBuildArgvWithSeparatePythonExecutableAndFlags() {
		final String[] argv = Tf1PlusPremiumDownloadExecutor.buildCommandArgv("14510214",
				"C:\\scripts\\tf1plus_premium_download.py", "C:\\out.mp4.tmp", "ffmpeg");
		assertEquals("python", argv[0]);
		assertEquals("C:\\scripts\\tf1plus_premium_download.py", argv[1]);
		assertEquals("--stream-id", argv[2]);
		assertEquals("14510214", argv[3]);
	}

	@Test
	public void shouldSplitPyLauncherStylePythonCommand() {
		final String previous = System.getProperty(Tf1PlusConf.PROPERTY_PYTHON);
		System.setProperty(Tf1PlusConf.PROPERTY_PYTHON, "py -3");
		try {
			final String[] argv = Tf1PlusPremiumDownloadExecutor.buildCommandArgv("1", "script.py", "out.tmp",
					"ffmpeg");
			assertEquals("py", argv[0]);
			assertEquals("-3", argv[1]);
			assertEquals("script.py", argv[2]);
		} finally {
			if (previous == null) {
				System.clearProperty(Tf1PlusConf.PROPERTY_PYTHON);
			} else {
				System.setProperty(Tf1PlusConf.PROPERTY_PYTHON, previous);
			}
		}
	}
}
