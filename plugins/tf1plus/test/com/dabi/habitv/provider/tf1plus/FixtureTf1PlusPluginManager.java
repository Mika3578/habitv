package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FixtureTf1PlusPluginManager extends Tf1PlusPluginManager {

	@Override
	protected String getUrlContent(String url) {
		try {
			if (url.contains("/replay")) {
				return new String(Files.readAllBytes(Paths.get("test/resources/fixtures/tf1plus/tf1-channel-replay.html")), StandardCharsets.UTF_8);
			}
			if (url.contains("/tf1/demain-nous-appartient")) {
				return new String(Files.readAllBytes(Paths.get("test/resources/fixtures/tf1plus/tf1-program-page.html")), StandardCharsets.UTF_8);
			}
			return "";
		} catch (IOException e) {
			throw new IllegalStateException("unable to read tf1plus fixture", e);
		}
	}
}
