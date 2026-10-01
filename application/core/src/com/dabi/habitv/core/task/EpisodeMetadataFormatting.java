package com.dabi.habitv.core.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public final class EpisodeMetadataFormatting {

	private static final String UNKNOWN = "Inconnu";
	private static final int MAX_SHORT_URL_LENGTH = 48;
	private static final String PROGRAM_URL_PARAM = "PROGRAM_URL";

	private EpisodeMetadataFormatting() {
	}

	public static String formatDate(final Date date) {
		if (date == null) {
			return UNKNOWN;
		}
		return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
	}

	public static String formatDuration(final Long durationSeconds) {
		if (durationSeconds == null || durationSeconds.longValue() < 0) {
			return UNKNOWN;
		}
		final long total = durationSeconds.longValue();
		final long hours = total / 3600;
		final long minutes = (total % 3600) / 60;
		final long seconds = total % 60;
		if (hours > 0) {
			return String.format(Locale.ENGLISH, "%d:%02d:%02d", Long.valueOf(hours),
					Long.valueOf(minutes), Long.valueOf(seconds));
		}
		return String.format(Locale.ENGLISH, "%d:%02d", Long.valueOf(minutes),
				Long.valueOf(seconds));
	}

	public static String formatSize(final Long sizeBytes) {
		if (sizeBytes == null || sizeBytes.longValue() < 0) {
			return UNKNOWN;
		}
		final long bytes = sizeBytes.longValue();
		if (bytes < 1024) {
			return bytes + " B";
		}
		if (bytes < 1024 * 1024) {
			return String.format(Locale.ENGLISH, "%.1f KB", Double.valueOf(bytes / 1024.0));
		}
		if (bytes < 1024L * 1024L * 1024L) {
			return String.format(Locale.ENGLISH, "%.1f MB",
					Double.valueOf(bytes / (1024.0 * 1024.0)));
		}
		return String.format(Locale.ENGLISH, "%.1f GB",
				Double.valueOf(bytes / (1024.0 * 1024.0 * 1024.0)));
	}

	/**
	 * Program (emission) page URL from the episode category, when the provider
	 * stores an HTTP(S) identifier on the downloadable category node or in
	 * {@link #PROGRAM_URL_PARAM}.
	 */
	public static String programPageUrl(final EpisodeDTO episode) {
		if (episode == null || episode.getCategory() == null) {
			return null;
		}
		final CategoryDTO category = episode.getCategory();
		final String explicitUrl = trimToHttpUrl(category.getParameter(PROGRAM_URL_PARAM));
		if (explicitUrl != null) {
			return explicitUrl;
		}
		return trimToHttpUrl(category.getId());
	}

	/**
	 * Exact episode page URL: metadata source URL first, HTTP(S) episode id as
	 * fallback. Never invents a URL.
	 */
	public static String episodePageUrl(final EpisodeDTO episode) {
		if (episode == null) {
			return null;
		}
		if (episode.getMetadata() != null) {
			final String source = trimToHttpUrl(episode.getMetadata().getSourceUrl());
			if (source != null) {
				return source;
			}
		}
		return trimToHttpUrl(episode.getId());
	}

	/**
	 * Episode description for tooltips / details, truncated to {@code maxChars}
	 * (including the ellipsis). Null when absent or blank.
	 * <p>
	 * Provider descriptions are plain HTML in the wild: tags are stripped,
	 * common entities decoded and whitespace normalized.
	 */
	public static String formatDescription(final EpisodeDTO episode, final int maxChars) {
		if (episode == null || episode.getMetadata() == null
				|| maxChars < ELLIPSIS.length()) {
			return null;
		}
		final String description = episode.getMetadata().getDescription();
		final String cleaned = cleanDescription(description);
		if (cleaned == null) {
			return null;
		}
		if (cleaned.length() <= maxChars) {
			return cleaned;
		}
		return cleaned.substring(0, maxChars - ELLIPSIS.length()) + ELLIPSIS;
	}

	/** Strips simple HTML markup, decodes common entities, normalizes spaces. */
	public static String cleanDescription(final String rawHtml) {
		if (rawHtml == null) {
			return null;
		}
		String text = rawHtml.replaceAll("(?i)<br\\s*/?>", " ")
				.replaceAll("(?i)</p>\\s*<p[^>]*>", " ")
				.replaceAll("<[^>]*>", " ");
		text = decodeEntities(text);
		text = text.replaceAll("\\s+", " ").trim();
		return text.isEmpty() ? null : text;
	}

	private static String decodeEntities(final String text) {
		return text
				.replace("&nbsp;", " ")
				.replace("&amp;", "&")
				.replace("&quot;", "\"")
				.replace("&apos;", "'")
				.replace("&#39;", "'")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&#34;", "\"");
	}

	/**
	 * Wraps cleaned text on word boundaries at {@code maxLineLength} for
	 * plain-text tooltips (JavaFX 8 tooltips do not wrap reliably).
	 */
	public static String wrapLines(final String text, final int maxLineLength) {
		if (text == null || maxLineLength <= 0) {
			return text;
		}
		final String[] words = text.split(" ");
		final StringBuilder wrapped = new StringBuilder(text.length() + 16);
		int lineLength = 0;
		for (final String word : words) {
			if (lineLength > 0 && lineLength + 1 + word.length() > maxLineLength) {
				wrapped.append('\n');
				lineLength = 0;
			}
			if (lineLength > 0) {
				wrapped.append(' ');
				lineLength++;
			}
			wrapped.append(word);
			lineLength += word.length();
		}
		return wrapped.toString();
	}

	private static final String ELLIPSIS = "…";


	public static String formatProgramLinkLabel(final EpisodeDTO episode) {
		if (episode != null && episode.getCategory() != null
				&& episode.getCategory().getName() != null) {
			final String trimmedCategoryName = episode.getCategory().getName().trim();
			if (!trimmedCategoryName.isEmpty()) {
				return trimmedCategoryName;
			}
		}
		final String url = programPageUrl(episode);
		if (url == null) {
			return UNKNOWN;
		}
		return shortenUrl(url);
	}

	public static String formatSource(final EpisodeDTO episode) {
		final String programUrl = programPageUrl(episode);
		if (programUrl != null) {
			return programUrl;
		}
		if (episode == null || episode.getId() == null
				|| episode.getId().trim().isEmpty()) {
			return UNKNOWN;
		}
		return episode.getId().trim();
	}

	private static String shortenUrl(final String url) {
		if (url.length() <= MAX_SHORT_URL_LENGTH) {
			return url;
		}
		final int ellipsisLength = 3;
		final int prefixLength = MAX_SHORT_URL_LENGTH - ellipsisLength;
		return url.substring(0, prefixLength) + "...";
	}

	public static String formatStatusLabel(final String status) {
		if (status == null || status.trim().isEmpty()) {
			return UNKNOWN;
		}
		return status.trim();
	}

	private static String trimToHttpUrl(final String value) {
		if (value == null) {
			return null;
		}
		final String trimmed = value.trim();
		if (trimmed.isEmpty() || !DownloadUtils.isHttpUrl(trimmed)) {
			return null;
		}
		return trimmed;
	}
}
