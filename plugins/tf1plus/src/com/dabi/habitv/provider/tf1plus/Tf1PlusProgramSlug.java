package com.dabi.habitv.provider.tf1plus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Maps catalogue or page URL slugs to GraphQL {@code programSlug} values when TF1 uses
 * different identifiers (for example {@code automoto} vs {@code auto-moto}).
 */
final class Tf1PlusProgramSlug {

	private static final Map<String, String> GRAPHQL_ALIASES;

	static {
		final Map<String, String> aliases = new HashMap<String, String>();
		aliases.put("automoto", "auto-moto");
		GRAPHQL_ALIASES = Collections.unmodifiableMap(aliases);
	}

	private Tf1PlusProgramSlug() {
	}

	static String resolveForGraphql(final String programSlug) {
		if (StringUtils.isEmpty(programSlug)) {
			return programSlug;
		}
		final String normalized = programSlug.toLowerCase(Locale.ROOT);
		final String alias = GRAPHQL_ALIASES.get(normalized);
		return alias != null ? alias : programSlug;
	}

}
