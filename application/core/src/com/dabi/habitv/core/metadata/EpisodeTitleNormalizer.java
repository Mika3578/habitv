package com.dabi.habitv.core.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

/**
 * Global (provider-agnostic) episode-title cleanup for filenames and semantic
 * tokens.
 * <p>
 * Providers may embed season/episode markers in display titles (for example
 * {@code S8 E2013 - Épisode du vendredi…}). When canonical
 * {@code seasonNumber}/{@code episodeNumber} are already known, those markers
 * are stripped so {@link MediaServerNamingPolicy} does not repeat them.
 * <p>
 * This is intentionally <strong>not</strong> a per-provider heuristic catalogue:
 * only patterns that match the known numeric season/episode are removed.
 */
public final class EpisodeTitleNormalizer {

	private static final Pattern LEADING_S_E_SPACED = Pattern
			.compile("^S\\s*(\\d+)\\s+E\\s*(\\d+)\\s*[-–—:]\\s*", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEADING_SXXEXX = Pattern
			.compile("^S(\\d+)E(\\d+)\\s*[-–—:]\\s*", Pattern.CASE_INSENSITIVE);

	private EpisodeTitleNormalizer() {
	}

	/**
	 * Title suitable for MEDIA_SERVER path components and {@code #EPISODE_TITLE#}.
	 */
	public static String forFilename(final EpisodeMetadataDTO metadata) {
		if (metadata == null) {
			return null;
		}
		String title = metadata.getEpisodeTitle();
		if (title == null) {
			return null;
		}
		title = title.trim();
		if (title.isEmpty()) {
			return null;
		}
		if (metadata.hasSeasonAndEpisode()) {
			title = stripMatchingSeasonEpisodePrefix(title, metadata.getSeasonNumber().intValue(),
					metadata.getEpisodeNumber().intValue());
		}
		title = title.trim();
		return title.isEmpty() ? null : title;
	}

	static String stripMatchingSeasonEpisodePrefix(final String title, final int season,
			final int episode) {
		String remaining = title;
		remaining = stripIfMatches(remaining, LEADING_S_E_SPACED, season, episode);
		remaining = stripIfMatches(remaining, LEADING_SXXEXX, season, episode);
		return remaining;
	}

	private static String stripIfMatches(final String title, final Pattern pattern, final int season,
			final int episode) {
		final Matcher matcher = pattern.matcher(title);
		if (!matcher.find()) {
			return title;
		}
		final Integer foundSeason = tryParseInt(matcher.group(1));
		final Integer foundEpisode = tryParseInt(matcher.group(2));
		if (foundSeason == null || foundEpisode == null) {
			// Out-of-range digit sequences are treated as a non-match.
			return title;
		}
		if (foundSeason.intValue() == season && foundEpisode.intValue() == episode) {
			return title.substring(matcher.end());
		}
		return title;
	}

	/**
	 * @return parsed int, or {@code null} when the digits are not a valid {@code int}
	 */
	private static Integer tryParseInt(final String raw) {
		try {
			return Integer.valueOf(Integer.parseInt(raw));
		} catch (final NumberFormatException e) {
			return null;
		}
	}
}
