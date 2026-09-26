package com.dabi.habitv.provider.icitoutv;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class IciToutTvOfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsNextjsYtdlpStrategy() throws IOException {
		final String content = read("test/resources/fixtures/icitoutv/fixture-baseline.txt");
		assertTrue(content.contains("provider=iciToutTv"));
		assertTrue(content.contains("strategy=nextjs-catalog-ytdlp"));
		assertTrue(content.contains("channels=ICI TOU.TV"));
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
