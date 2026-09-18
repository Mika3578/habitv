package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class Tf1PlusOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		final String fixturePath = "test/resources/fixtures/tf1plus/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		final InputStream input = new FileInputStream(fixturePath);
		try {
			final String content = Tf1PlusHttpClient.readUtf8(input);
			assertTrue(content.contains("provider=tf1plus"));
			assertTrue(content.contains("network=disabled"));
			assertTrue(content.contains("site=https://www.tf1.fr"));
		} finally {
			input.close();
		}
	}

}
