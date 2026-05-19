package com.dabi.habitv.core.updater;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.framework.FrameworkConf;

public class UpdateManagerTest {

	private String previousUpdateEnabled;
	private String previousUpdateUrl;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		previousUpdateEnabled = System.getProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY);
		previousUpdateUrl = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
	}

	@After
	public void tearDown() throws Exception {
		restoreProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, previousUpdateEnabled);
		restoreProperty(FrameworkConf.UPDATE_URL_PROPERTY, previousUpdateUrl);
	}

	@Test
	public void processSkipsWhenUpdatesDisabled() {
		System.setProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, "false");
		System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, "https://updates.must-not-be-used.example/repository/");

		final AtomicInteger notifications = new AtomicInteger();
		final UpdateManager updateManager = new UpdateManager("plugins", true);
		updateManager.getUpdatePublisher().attach(new Subscriber<UpdatePluginEvent>() {
			@Override
			public void update(final UpdatePluginEvent event) {
				notifications.incrementAndGet();
			}
		});
		updateManager.process();

		assertEquals(0, notifications.get());
	}

	@Test
	public void resolveUpdateEnabledDefaultsToTrueWhenPropertyMissing()
			throws Exception {
		System.clearProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY);
		final Method resolveUpdateEnabled = UpdateManager.class
				.getDeclaredMethod("resolveUpdateEnabled");
		resolveUpdateEnabled.setAccessible(true);

		assertTrue((Boolean) resolveUpdateEnabled.invoke(null));
	}

	@Test
	public void resolveUpdateEnabledSupportsExplicitTrueFalse()
			throws Exception {
		final Method resolveUpdateEnabled = UpdateManager.class
				.getDeclaredMethod("resolveUpdateEnabled");
		resolveUpdateEnabled.setAccessible(true);

		System.setProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, "true");
		assertTrue((Boolean) resolveUpdateEnabled.invoke(null));

		System.setProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, "false");
		assertFalse((Boolean) resolveUpdateEnabled.invoke(null));
	}

	@Test
	public void splitPluginLinesIgnoresBomPrefixedCommentHeader() throws Exception {
		final Method splitPluginLines = UpdateManager.class.getDeclaredMethod("splitPluginLines", String.class);
		splitPluginLines.setAccessible(true);

		final String plugins = "\uFEFF# header\narte\n6play\n";
		final String[] parsed = (String[]) splitPluginLines.invoke(null, plugins);

		assertArrayEquals(new String[] { "arte", "6play" }, parsed);
	}

	private static void restoreProperty(final String key, final String previousValue) {
		if (previousValue == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, previousValue);
		}
	}

}
