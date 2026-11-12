package com.dabi.habitv.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UiThemePreferenceTest {

	@Test
	public void normalizeDefaultsToLight() {
		assertEquals(UiThemePreference.LIGHT, UiThemePreference.normalize(null));
		assertEquals(UiThemePreference.LIGHT, UiThemePreference.normalize(""));
		assertEquals(UiThemePreference.LIGHT, UiThemePreference.normalize(" unknown "));
		assertEquals(UiThemePreference.LIGHT, UiThemePreference.normalize("LIGHT"));
	}

	@Test
	public void normalizeAcceptsDark() {
		assertEquals(UiThemePreference.DARK, UiThemePreference.normalize("dark"));
		assertEquals(UiThemePreference.DARK, UiThemePreference.normalize(" Dark "));
	}

	@Test
	public void isDarkMatchesNormalizedValue() {
		assertTrue(UiThemePreference.isDark("dark"));
		assertFalse(UiThemePreference.isDark(null));
		assertFalse(UiThemePreference.isDark("light"));
	}
}
