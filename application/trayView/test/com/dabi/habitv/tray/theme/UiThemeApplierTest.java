package com.dabi.habitv.tray.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.core.config.UiThemePreference;

public class UiThemeApplierTest {

	@Test
	public void stylesheetForDefaultsToLight() {
		assertTrue(UiThemeApplier.stylesheetFor(null).endsWith("habitv-light.css"));
		assertTrue(UiThemeApplier.stylesheetFor("").endsWith("habitv-light.css"));
		assertTrue(UiThemeApplier.stylesheetFor(UiThemePreference.LIGHT)
				.endsWith("habitv-light.css"));
	}

	@Test
	public void stylesheetForSelectsDark() {
		assertTrue(UiThemeApplier.stylesheetFor(UiThemePreference.DARK)
				.endsWith("habitv-dark.css"));
	}

	@Test
	public void stylesheetResourcesAreOnClasspath() {
		assertNotNull(UiThemeApplier.class
				.getResource("/com/dabi/habitv/tray/css/habitv-common.css"));
		assertNotNull(UiThemeApplier.class
				.getResource("/com/dabi/habitv/tray/css/habitv-light.css"));
		assertNotNull(UiThemeApplier.class
				.getResource("/com/dabi/habitv/tray/css/habitv-dark.css"));
		assertEquals("/com/dabi/habitv/tray/css/habitv-dark.css",
				UiThemeApplier.stylesheetFor("DARK"));
	}
}
