package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Tf1PlusPremiumCmdExecutorTest {

	@Test
	public void shouldParseDownloadProgressLines() throws Exception {
		final Tf1PlusPremiumCmdExecutor executor = new Tf1PlusPremiumCmdExecutor("cmd",
				new String[] { "python", "ignored.py" }, 60000L);
		assertEquals("10.0", invokeHandleProgression(executor, "[download] 10.0%"));
		assertEquals("45.5", invokeHandleProgression(executor, "Vid 1280x720 | 45.5% | 12.34Mbps"));
		assertNull(invokeHandleProgression(executor, "TF1+ probe login OK"));
	}

	private static String invokeHandleProgression(final Tf1PlusPremiumCmdExecutor executor, final String line)
			throws Exception {
		final java.lang.reflect.Method method = Tf1PlusPremiumCmdExecutor.class.getDeclaredMethod("handleProgression",
				String.class);
		method.setAccessible(true);
		return (String) method.invoke(executor, line);
	}
}
