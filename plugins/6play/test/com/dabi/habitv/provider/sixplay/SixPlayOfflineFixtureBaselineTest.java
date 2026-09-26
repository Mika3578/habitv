package com.dabi.habitv.provider.sixplay;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class SixPlayOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		String fixturePath = "test/resources/fixtures/6play/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		try (InputStream input = new FileInputStream(fixturePath)) {
			String content = readUtf8(input);
			assertTrue("fixture metadata must mention provider", content.contains("provider=6play"));
			assertTrue("fixture metadata must document offline-only mode",
					content.contains("network=disabled"));
			assertTrue("fixture metadata must document SPA/legacy parser boundary",
					content.contains("parserBoundary=legacy-html-spa-obsolete"));
			assertTrue("fixture metadata must document https home URL",
					content.contains("homeUrl=https://www.6play.fr/"));
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
