package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class Tf1PlusOfflineFixtureBaselineTest {

	private static final String BASELINE = "test/resources/fixtures/tf1plus/fixture-baseline.txt";

	@Test
	public void baselineDocumentsGraphqlStrategyAndNativeChannels() throws IOException {
		final String content = read(BASELINE);
		assertTrue(content.contains("provider=tf1plus"));
		assertTrue(content.contains("strategy=graphql-ytdlp"));
		assertTrue(content.contains("channels=tf1,tmc,tfx,tf1-series-films,lci"));
		assertTrue(content.contains("excluded=novo19"));
		assertEquals("https://www.tf1.fr", Tf1PlusConf.HOME_URL);
		assertEquals("https://www.tf1.fr/graphql/web", Tf1PlusConf.GRAPHQL_URL);
		assertEquals(5, Tf1PlusConf.CHANNELS.length);
	}

	private static String read(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
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
