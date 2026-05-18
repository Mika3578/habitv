package com.dabi.habitv.core.updater;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UpdateManagerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testProcess() {
		final UpdateManager updateManager = new UpdateManager("plugins",true);
		updateManager.process();
	}

	@Test
	public void splitPluginLinesIgnoresBomPrefixedCommentHeader() throws Exception {
		final Method splitPluginLines = UpdateManager.class.getDeclaredMethod("splitPluginLines", String.class);
		splitPluginLines.setAccessible(true);

		final String plugins = "\uFEFF# header\narte\n6play\n";
		final String[] parsed = (String[]) splitPluginLines.invoke(null, plugins);

		assertArrayEquals(new String[] { "arte", "6play" }, parsed);
	}

}
