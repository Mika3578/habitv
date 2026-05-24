package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FixtureTf1PlusPluginManager extends Tf1PlusPluginManager {

	@Override
	protected String getUrlContent(String url) {
		try {
			return new String(Files.readAllBytes(Paths.get("test/resources/fixtures/tf1plus/tf1-replay.html")), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("unable to read tf1plus fixture", e);
		}
	}
}
