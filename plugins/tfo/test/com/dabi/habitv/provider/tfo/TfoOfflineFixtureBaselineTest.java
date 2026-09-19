package com.dabi.habitv.provider.tfo;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class TfoOfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsHtmlJwplayerStrategy() throws IOException {
		final String content = read("test/resources/fixtures/tfo/fixture-baseline.txt");
		assertTrue(content.contains("provider=tfo"));
		assertTrue(content.contains("strategy=html-catalog-jwplayer-hls"));
		assertTrue(content.contains("channels=TFO"));
	}

	private static String read(final String path) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toString("UTF-8");
		}
	}
}
