package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19ReplayHelperConfigTest {

	@Test
	public void disabledPropertyForcesUnconfigured() {
		final String previous = System.getProperty("habitv.novo19.helper.disabled");
		System.setProperty("habitv.novo19.helper.disabled", "true");
		try {
			assertTrue(Novo19ReplayHelperConfig.isDisabledByProperty());
			assertFalse(Novo19ReplayHelperConfig.isConfigured());
		} finally {
			if (previous == null) {
				System.clearProperty("habitv.novo19.helper.disabled");
			} else {
				System.setProperty("habitv.novo19.helper.disabled", previous);
			}
		}
	}

	@Test
	public void buildCommandIncludesAssetAndOutput() {
		final String[] argv = Novo19ReplayHelperExecutor.buildCommandArgv(
				"asset-1", "helper.py", "out.mp4", "ffmpeg");
		assertTrue(containsPair(argv, "--asset-id", "asset-1"));
		assertTrue(containsPair(argv, "--output", "out.mp4"));
		assertTrue(containsPair(argv, "--ffmpeg", "ffmpeg"));
	}

	private static boolean containsPair(final String[] argv, final String key, final String value) {
		for (int i = 0; i + 1 < argv.length; i++) {
			if (key.equals(argv[i]) && value.equals(argv[i + 1])) {
				return true;
			}
		}
		return false;
	}

}
