package com.dabi.habitv.provider.rtbfauvio;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class RtbfAuvioFixtureSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private RtbfAuvioFixtureSupport() {
	}

	static String playbackScenario(final String sectionKey) throws IOException {
		final JsonNode root = MAPPER.readTree(readClasspath("test/resources/fixtures/rtbfAuvio/playback-scenarios.json"));
		final JsonNode section = root.path("sections").path(sectionKey);
		if (section.isMissingNode()) {
			throw new IOException("missing-playback-scenario:" + sectionKey);
		}
		return section.toString();
	}

	static String readClasspath(final String path) throws IOException {
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
