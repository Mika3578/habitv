package com.dabi.habitv.provider.novo19;

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
 * Regression guard: production code must not embed fixture-specific catalogue
 * identities. Named programmes belong in tests and fixtures only.
 */
public class Novo19ProductionContentAgnosticTest {

	private static final String[] FORBIDDEN_LITERALS = new String[] { "cuisinons", "bucheron", "elysee", "inferno",
			"fbi-portes", "on-a-de-l-info", "on-a-du-nouveau", "un-plan-d-enfer", "la-france-des-mysteres",
			"ma-belle-soeur", "le-royaume-des-contes", "vos-objets-valent-de-l-or", "la sélection brut" };

	private static final Pattern FORBIDDEN_ASSET_ID = Pattern.compile("OF-\\d{8}-\\d{2}-\\d{4}_565BFFb");

	private static final Pattern FORBIDDEN_SECTION_UUID = Pattern
			.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

	@Test
	public void productionSourcesDoNotEmbedFixtureSpecificCatalogueIdentities() throws IOException {
		final File sourceRoot = new File("src");
		if (!sourceRoot.isDirectory()) {
			return;
		}
		final List<String> violations = new ArrayList<String>();
		collectViolations(sourceRoot, violations);
		if (!violations.isEmpty()) {
			fail("Fixture-specific catalogue identities found in production code:\n"
					+ String.join("\n", violations));
		}
	}

	private static void collectViolations(final File directory, final List<String> violations) throws IOException {
		final File[] children = directory.listFiles();
		if (children == null) {
			return;
		}
		for (final File child : children) {
			if (child.isDirectory()) {
				collectViolations(child, violations);
				continue;
			}
			if (!child.getName().endsWith(".java")) {
				continue;
			}
			scanFile(child, violations);
		}
	}

	private static void scanFile(final File file, final List<String> violations) throws IOException {
		final String relativePath = file.getPath().replace('\\', '/');
		final String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
		for (final String literal : FORBIDDEN_LITERALS) {
			if (content.contains(literal)) {
				violations.add(relativePath + ": forbidden literal \"" + literal + "\"");
			}
		}
		if (FORBIDDEN_ASSET_ID.matcher(content).find()) {
			violations.add(relativePath + ": forbidden RedBee asset id pattern");
		}
		if (FORBIDDEN_SECTION_UUID.matcher(content).find() && !relativePath.endsWith("Novo19Conf.java")) {
			violations.add(relativePath + ": forbidden section UUID pattern");
		}
		if (content.contains("/details/") && content.matches("(?s).*\"/details/[^$\\{][^\"]+\".*")) {
			final int index = content.indexOf("/details/");
			if (index >= 0) {
				final int end = content.indexOf('"', index + 1);
				if (end > index) {
					final String slug = content.substring(index, end);
					if (!slug.equals("/details/") && slug.indexOf('{') < 0) {
						violations.add(relativePath + ": hardcoded details slug \"" + slug + "\"");
					}
				}
			}
		}
	}

}
