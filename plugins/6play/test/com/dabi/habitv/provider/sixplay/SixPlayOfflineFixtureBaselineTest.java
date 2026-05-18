package com.dabi.habitv.provider.sixplay;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class SixPlayOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		String fixturePath = "test/resources/fixtures/6play/fixture-baseline.txt";
		try (InputStream input = new FileInputStream(fixturePath)) {
			assertNotNull("missing local fixture: " + fixturePath, input);
			String content = readUtf8(input);
			assertTrue("fixture metadata must mention provider", content.contains("provider=6play"));
			assertTrue("fixture metadata must document offline-only mode",
					content.contains("network=disabled"));
		}
	}

	private String readUtf8(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}
}
