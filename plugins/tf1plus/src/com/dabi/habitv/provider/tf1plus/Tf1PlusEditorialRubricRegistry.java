package com.dabi.habitv.provider.tf1plus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Known TF1 editorial rubrics and minimal slug-based fallback classification.
 */
final class Tf1PlusEditorialRubricRegistry {

	static final String FALLBACK_LABEL = "Autres";

	private static final List<Tf1PlusEditorialRubric> RUBRICS = Collections.unmodifiableList(Arrays.asList(
			new Tf1PlusEditorialRubric("Séries", "MAIN_SERIES_AND_FICTIONS"),
			new Tf1PlusEditorialRubric("Films", "MAIN_MOVIES"),
			new Tf1PlusEditorialRubric("Divertissements", "MAIN_ENTERTAINEMENT"),
			new Tf1PlusEditorialRubric("Jeunesse", "MAIN_YOUTH"),
			new Tf1PlusEditorialRubric("Infos & Mag", "MAIN_INFOS_MAGAZINE_SPORTS")));

	private static final Pattern INFO_SLUG_PATTERN = Pattern.compile("^(jt-|journal-|info|meteo)", Pattern.CASE_INSENSITIVE);

	private static final Pattern SPORT_SLUG_PATTERN = Pattern.compile("^(auto-moto|automoto|sport|foot|rugby)",
			Pattern.CASE_INSENSITIVE);

	private Tf1PlusEditorialRubricRegistry() {
	}

	static List<Tf1PlusEditorialRubric> knownRubrics() {
		return RUBRICS;
	}

	static Tf1PlusEditorialRubric resolveFromApiTypes(final List<String> apiTypes) {
		if (apiTypes == null || apiTypes.isEmpty()) {
			return null;
		}
		for (final Tf1PlusEditorialRubric rubric : RUBRICS) {
			for (final String apiType : apiTypes) {
				if (rubric.getApiType().equals(apiType)) {
					return rubric;
				}
			}
		}
		return null;
	}

	static Tf1PlusEditorialRubric resolveSlugFallback(final String programmeSlug) {
		if (programmeSlug == null || programmeSlug.isEmpty()) {
			return null;
		}
		final String normalized = programmeSlug.toLowerCase(Locale.ROOT);
		if (INFO_SLUG_PATTERN.matcher(normalized).find()) {
			return findByApiType("MAIN_INFOS_MAGAZINE_SPORTS");
		}
		if (SPORT_SLUG_PATTERN.matcher(normalized).find()) {
			return findByApiType("MAIN_INFOS_MAGAZINE_SPORTS");
		}
		return null;
	}

	private static Tf1PlusEditorialRubric findByApiType(final String apiType) {
		for (final Tf1PlusEditorialRubric rubric : RUBRICS) {
			if (apiType.equals(rubric.getApiType())) {
				return rubric;
			}
		}
		return null;
	}

}
