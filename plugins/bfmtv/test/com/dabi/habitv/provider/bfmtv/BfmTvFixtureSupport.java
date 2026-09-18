package com.dabi.habitv.provider.bfmtv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class BfmTvFixtureSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private BfmTvFixtureSupport() {
	}

	static Map<String, Object> readMap(final String name) throws IOException {
		final File file = new File("test/resources/fixtures/bfmtv/" + name);
		final InputStream input = new FileInputStream(file);
		try {
			return MAPPER.readValue(input, MAP_TYPE);
		} finally {
			input.close();
		}
	}

	static String readRaw(final String name) throws IOException {
		final File file = new File("test/resources/fixtures/bfmtv/" + name);
		final FileInputStream input = new FileInputStream(file);
		try {
			return BfmTvHttpClient.readUtf8(input);
		} finally {
			input.close();
		}
	}

}
