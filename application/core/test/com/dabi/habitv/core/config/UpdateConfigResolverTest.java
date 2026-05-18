package com.dabi.habitv.core.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.framework.FrameworkConf;

public class UpdateConfigResolverTest {

	private String previousProperty;

	@Before
	public void setUp() {
		previousProperty = System.getProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY);
	}

	@After
	public void tearDown() {
		restoreProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY, previousProperty);
	}

	@Test
	public void xmlFalseWithoutPropertyRemainsFalse() {
		System.clearProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY);
		assertFalse(UpdateConfigResolver.resolveAutoriseSnapshot(false));
	}

	@Test
	public void xmlFalseWithTruePropertyOverridesToTrue() {
		System.setProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY, "true");
		assertTrue(UpdateConfigResolver.resolveAutoriseSnapshot(false));
	}

	@Test
	public void xmlTrueWithFalsePropertyOverridesToFalse() {
		System.setProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY, "false");
		assertFalse(UpdateConfigResolver.resolveAutoriseSnapshot(true));
	}

	@Test
	public void invalidPropertyValueKeepsXmlValueAndDoesNotEnableSnapshots() {
		System.setProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY, "yes");
		assertFalse(UpdateConfigResolver.resolveAutoriseSnapshot(false));
	}

	@Test
	public void invalidPropertyValueDoesNotDisableWhenXmlTrue() {
		System.setProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY, "1");
		assertTrue(UpdateConfigResolver.resolveAutoriseSnapshot(true));
	}

	private static void restoreProperty(final String key, final String previousValue) {
		if (previousValue == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, previousValue);
		}
	}
}
