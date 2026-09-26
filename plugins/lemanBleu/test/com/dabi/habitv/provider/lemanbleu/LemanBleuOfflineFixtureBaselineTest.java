package com.dabi.habitv.provider.lemanbleu;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class LemanBleuOfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsHtmlInfomaniakCurlStrategy() throws IOException {
		final String content = read("test/resources/fixtures/lemanBleu/fixture-baseline.txt");
		assertTrue(content.contains("provider=lemanBleu"));
		assertTrue(content.contains("strategy=html-infomaniak-curl"));
		assertTrue(content.contains("channels=Léman Bleu"));
		assertTrue(content.contains("videos.lemanbleu.ch"));
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
