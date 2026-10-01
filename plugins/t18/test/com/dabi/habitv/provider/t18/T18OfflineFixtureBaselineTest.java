package com.dabi.habitv.provider.t18;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class T18OfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsPrivateDailymotionStrategy() throws IOException {
		final String content = read("test/resources/fixtures/t18/fixture-baseline.txt");
		assertTrue(content.contains("provider=t18"));
		assertTrue(content.contains("strategy=html-catalog-private-dailymotion"));
		assertTrue(content.contains("no auth-token reconstruction"));
		assertTrue(T18Html.mentionsPrivateDailymotionEmbed(
				read("test/resources/fixtures/t18/episode.html")));
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
