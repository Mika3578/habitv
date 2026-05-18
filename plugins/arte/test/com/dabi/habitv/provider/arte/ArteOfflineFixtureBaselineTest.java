package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ArteOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		String fixturePath = "test/resources/fixtures/arte/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		try (InputStream input = new FileInputStream(fixturePath)) {
			String content = readUtf8(input);
			assertTrue("fixture metadata must mention provider", content.contains("provider=arte"));
			assertTrue("fixture metadata must disable network access", content.contains("network=disabled"));
			assertTrue("fixture metadata must document parser boundary",
					content.contains("parserBoundary=legacy-rss-html"));
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
