package com.dabi.habitv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HabitvLauncherTest {

	@Test
	public void nullArgsSelectsGuiMode() {
		assertTrue(HabitvLauncher.isGuiMode(null));
	}

	@Test
	public void emptyArgsSelectsGuiMode() {
		assertTrue(HabitvLauncher.isGuiMode(new String[0]));
	}

	@Test
	public void nonEmptyArgsSelectsCliMode() {
		assertFalse(HabitvLauncher.isGuiMode(new String[]{"--help"}));
	}

	@Test
	public void multipleArgsSelectsCliMode() {
		assertFalse(HabitvLauncher.isGuiMode(new String[]{"--daemon", "--output", "/tmp"}));
	}

	@Test
	public void javaFxAvailableReturnsTrueInBuildEnvironment() {
		assertTrue(HabitvLauncher.isJavaFxAvailable());
	}

	@Test
	public void javaFxMissingMessageContainsInstructions() {
		String msg = HabitvLauncher.getJavaFxMissingMessage();
		assertTrue(msg.contains("JavaFX"));
		assertTrue(msg.contains("openjfx.io"));
		assertTrue(msg.contains("CLI mode"));
	}
}
