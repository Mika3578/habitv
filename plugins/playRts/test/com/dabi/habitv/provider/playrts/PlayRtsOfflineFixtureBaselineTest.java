package com.dabi.habitv.provider.playrts;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class PlayRtsOfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsPlayApiYtdlpStrategy() throws IOException {
		final String content = read("test/resources/fixtures/playRts/fixture-baseline.txt");
		assertTrue(content.contains("provider=playRts"));
		assertTrue(content.contains("strategy=play-v3-api-ytdlp"));
		assertTrue(content.contains("channels=RTS 1,RTS 2"));
		assertTrue(content.contains("homeUrl=https://www.rts.ch/play/tv"));
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
