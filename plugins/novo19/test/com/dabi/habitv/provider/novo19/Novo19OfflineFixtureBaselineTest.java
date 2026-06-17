package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class Novo19OfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		final String fixturePath = "test/resources/fixtures/novo19/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		try (InputStream input = new FileInputStream(fixturePath)) {
			final String content = readUtf8(input);
			assertTrue(content.contains("provider=novo19"));
			assertTrue(content.contains("network=disabled"));
			assertTrue(content.contains("catalogueSource=bff-json"));
		}
	}

	private static String readUtf8(final InputStream input) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			builder.append(new String(buffer, 0, read, "UTF-8"));
		}
		return builder.toString();
	}

}
