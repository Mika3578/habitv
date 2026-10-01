package com.dabi.habitv.provider.rtbfauvio;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class RtbfAuvioOfflineFixtureBaselineTest {

	@Test
	public void baselineDocumentsDrmUnsupportedStrategy() throws IOException {
		final String content = read("test/resources/fixtures/rtbfAuvio/fixture-baseline.txt");
		assertTrue(content.contains("provider=rtbfAuvio"));
		assertTrue(content.contains("strategy=bff-catalog-redbee-drm-unsupported"));
		assertTrue(content.contains("channels=La Une,Tipik,La Trois"));
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
