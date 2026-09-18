package com.dabi.habitv.provider.bfmtv;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BfmTvOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		final String fixturePath = "test/resources/fixtures/bfmtv/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		final InputStream input = new FileInputStream(fixturePath);
		try {
			final String content = BfmTvHttpClient.readUtf8(input);
			assertTrue(content.contains("provider=bfmtv"));
			assertTrue(content.contains("network=disabled"));
			assertTrue(content.contains("site=https://www.bfmtv.com"));
		} finally {
			input.close();
		}
	}

}
