package com.dabi.habitv.provider.tvcom;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class TvComOfflineFixtureBaselineTest {

	@Test
	public void fixtureBaselineDocumentsStrategy() throws IOException {
		final String baseline = read("test/resources/fixtures/tvcom/fixture-baseline.txt");
		assertTrue(baseline.contains("provider=tvCom"));
		assertTrue(baseline.contains("strategy=html-freecaster-hls"));
		assertTrue(baseline.contains("Freecaster"));
	}

	private static String read(final String path) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}
}
