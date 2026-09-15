package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Regression guard: the NOVO19 module must stay content-agnostic. Real individual
 * catalogue identities belong in live provider responses only, not in source,
 * tests, fixtures, or fixture filenames.
 */
public class Novo19ModuleContentAgnosticTest {

	private static final Pattern REDBEE_CATALOGUE_ASSET_ID = Pattern
			.compile("OF-\\d{8}-\\d{2}-\\d{4}_[A-Za-z0-9]+");

	private static final Pattern LEGACY_LIVE_ASSET_SUFFIX = Pattern.compile("(?:^|[^a-z])novo19_565[Bb][Ff][Bb]");

	private static final Pattern SIGNED_STREAM_URL = Pattern
			.compile("https://[^\"'\\s]+\\.(?:m3u8|mpd)\\?[^\"'\\s]{8,}");

	private static final Pattern BEARER_TOKEN = Pattern
			.compile("Bearer (?!offline-session-token)[A-Za-z0-9._-]{8,}");

	private static final Pattern REAL_SESSION_TOKEN_VALUE = Pattern
			.compile("\"sessionToken\"\\s*:\\s*\"(?!offline-session-token)[^\"]{12,}\"");

	private static final Pattern SET_COOKIE_HEADER = Pattern.compile("Set-Cookie:\\s*[^\\s\"]{8,}");

	private static final Pattern SYNTHETIC_DETAILS_SLUG = Pattern
			.compile("/details/(?:programme|series|film|podcast|collection|talk-programme|documentary)-[a-z0-9-]+");

	private static final Pattern SYNTHETIC_PLAYER_SLUG = Pattern
			.compile("/player/(?:programme|series|film|podcast|collection|talk-programme|documentary)-[a-z0-9-]+");

	@Test
	public void productionSourcesStayContentAgnostic() throws IOException {
		final List<String> violations = new ArrayList<String>();
		scanModuleTree(new File("src"), true, violations);
		if (!violations.isEmpty()) {
			fail("Content-specific catalogue identities found in NOVO19 production code:\n"
					+ String.join("\n", violations));
		}
	}

	@Test
	public void testsFixturesAndNamesStayContentAgnostic() throws IOException {
		final List<String> violations = new ArrayList<String>();
		scanModuleTree(new File("test"), false, violations);
		scanFixtureDirectory(new File("test/resources/fixtures/novo19"), violations);
		if (!violations.isEmpty()) {
			fail("Content-specific catalogue identities found in NOVO19 tests or fixtures:\n"
					+ String.join("\n", violations));
		}
	}

	@Test
	public void detectsMixedCaseRedBeeAssetIdOnOriginalContent() {
		final List<String> violations = new ArrayList<String>();
		// Must scan original-case content: lowercasing would turn OF- into of- and miss it.
		scanContent("probe.java", "asset OF-00000080-00-0000_565BFFb", false, violations);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void allowsSyntheticFixtureAssetIds() {
		final List<String> violations = new ArrayList<String>();
		scanContent("fixture.json", "\"id\": \"asset-alpha\"", false, violations);
		scanContent("fixture.json", "\"assetId\": \"asset-podcast-alpha-episode-alpha\"", false, violations);
		assertTrue(violations.isEmpty());
	}

	private static void scanModuleTree(final File directory, final boolean productionOnly,
			final List<String> violations) throws IOException {
		if (!directory.isDirectory()) {
			return;
		}
		final File[] children = directory.listFiles();
		if (children == null) {
			return;
		}
		for (final File child : children) {
			if (child.isDirectory()) {
				scanModuleTree(child, productionOnly, violations);
				continue;
			}
			if (child.getName().endsWith(".java")
					&& !child.getName().equals("Novo19ModuleContentAgnosticTest.java")) {
				scanFile(child, productionOnly, violations);
			} else if (!productionOnly && child.getName().endsWith(".json")) {
				scanFile(child, false, violations);
			}
		}
	}

	private static void scanFixtureDirectory(final File directory, final List<String> violations) throws IOException {
		if (!directory.isDirectory()) {
			return;
		}
		final File[] children = directory.listFiles();
		if (children == null) {
			return;
		}
		for (final File child : children) {
			if (child.isDirectory()) {
				scanFixtureDirectory(child, violations);
				continue;
			}
			scanFixtureFilename(child, violations);
		}
	}

	private static void scanFixtureFilename(final File file, final List<String> violations) {
		final String name = file.getName().toLowerCase(Locale.ROOT);
		if (name.contains("bucheron") || name.contains("inferno") || name.contains("elysee")
				|| name.contains("cuisinons") || name.contains("on-a-de-l-info") || name.contains("on-a-du-nouveau")
				|| name.contains("fbi-portes") || name.contains("royaume-des-contes")
				|| name.contains("vos-objets")) {
			violations.add(file.getPath().replace('\\', '/') + ": fixture filename embeds real catalogue identity");
		}
	}

	private static void scanFile(final File file, final boolean productionOnly, final List<String> violations)
			throws IOException {
		final String relativePath = file.getPath().replace('\\', '/');
		final String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		scanContent(relativePath, content, productionOnly, violations);
	}

	private static void scanContent(final String relativePath, final String content, final boolean productionOnly,
			final List<String> violations) {
		if (REDBEE_CATALOGUE_ASSET_ID.matcher(content).find()) {
			violations.add(relativePath + ": RedBee catalogue asset id pattern");
		}
		if (LEGACY_LIVE_ASSET_SUFFIX.matcher(content).find()) {
			violations.add(relativePath + ": legacy live asset id suffix");
		}
		if (SIGNED_STREAM_URL.matcher(content).find()) {
			violations.add(relativePath + ": signed playback or CDN stream URL");
		}
		if (BEARER_TOKEN.matcher(content).find()) {
			violations.add(relativePath + ": bearer token value");
		}
		if (REAL_SESSION_TOKEN_VALUE.matcher(content).find()) {
			violations.add(relativePath + ": session token value");
		}
		if (SET_COOKIE_HEADER.matcher(content).find()) {
			violations.add(relativePath + ": cookie header value");
		}
		if (productionOnly) {
			scanProductionHardcodedSlugs(relativePath, content, violations);
		}
	}

	private static void scanProductionHardcodedSlugs(final String relativePath, final String content,
			final List<String> violations) {
		int index = 0;
		while ((index = content.indexOf("/details/", index)) >= 0) {
			final int end = content.indexOf('"', index);
			if (end > index) {
				final String slug = content.substring(index, end);
				if (!slug.equals("/details/") && slug.indexOf('{') < 0 && !SYNTHETIC_DETAILS_SLUG.matcher(slug).find()) {
					violations.add(relativePath + ": hardcoded details slug \"" + slug + "\"");
				}
			}
			index++;
		}
		index = 0;
		while ((index = content.indexOf("/player/", index)) >= 0) {
			final int end = content.indexOf('"', index);
			if (end > index) {
				final String slug = content.substring(index, end);
				if (!slug.equals("/player/") && slug.indexOf('{') < 0 && !isAllowedProductionPlayerSlug(slug)
						&& !SYNTHETIC_PLAYER_SLUG.matcher(slug).find()) {
					violations.add(relativePath + ": hardcoded player slug \"" + slug + "\"");
				}
			}
			index++;
		}
	}

	private static boolean isAllowedProductionPlayerSlug(final String slug) {
		return "/player/novo19".equals(slug) || slug.startsWith("/player/novo19/");
	}

}
