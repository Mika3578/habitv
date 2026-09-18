package com.dabi.habitv.provider.tf1plus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Tf1PlusFixtureSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private Tf1PlusFixtureSupport() {
	}

	static Map<String, Object> readMap(final String name) throws IOException {
		final File file = new File("test/resources/fixtures/tf1plus/" + name);
		final InputStream input = new FileInputStream(file);
		try {
			return MAPPER.readValue(input, MAP_TYPE);
		} finally {
			input.close();
		}
	}

	static String readRaw(final String name) throws IOException {
		final File file = new File("test/resources/fixtures/tf1plus/" + name);
		final FileInputStream input = new FileInputStream(file);
		try {
			return Tf1PlusHttpClient.readUtf8(input);
		} finally {
			input.close();
		}
	}

}
