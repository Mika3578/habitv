package com.dabi.habitv.utils;

import java.util.List;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Utility class for filtering operations.
 * This class provides static methods for common filtering operations.
 */
public final class FilterUtils {

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private FilterUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Filters an episode based on include and exclude patterns and downloaded status.
	 * 
	 * @param episode the episode to filter
	 * @param includeList list of include patterns
	 * @param excludeList list of exclude patterns
	 * @return true if the episode should be included, false otherwise
	 */
	public static boolean filterByIncludeExcludeAndDownloaded(final EpisodeDTO episode, final List<String> includeList, final List<String> excludeList) {
		boolean include = true;
		// manage include
		include = filterInclude(includeList, episode.getName());
		if (include) {
			// manage exclude
			include = filterExclude(excludeList, episode.getName());
		}
		return include;
	}

	/**
	 * Filters episodes based on exclude patterns.
	 * 
	 * @param excludeList list of exclude patterns
	 * @param episodeName the episode name to check
	 * @return true if the episode should be included, false if it matches any exclude pattern
	 */
	private static boolean filterExclude(final List<String> excludeList, final String episodeName) {
		boolean include = true;
		if (excludeList != null && !excludeList.isEmpty()) {
			boolean match = false;
			for (final String excludePattern : excludeList) {
				match =  Pattern.compile(excludePattern, Pattern.CASE_INSENSITIVE).matcher(episodeName).find();
				if (match) {
					include = false;
					break;
				}
			}

		}
		return include;
	}

	/**
	 * Filters episodes based on include patterns.
	 * 
	 * @param includeList list of include patterns
	 * @param episodeName the episode name to check
	 * @return true if the episode matches any include pattern, false otherwise
	 */
	private static boolean filterInclude(final List<String> includeList, final String episodeName) {
		boolean include = true;
		if (includeList != null && !includeList.isEmpty()) {
			include = false;
			for (final String includePattern : includeList) {
				//include = episodeName.matches(includePattern);
				include =  Pattern.compile(includePattern, Pattern.CASE_INSENSITIVE).matcher(episodeName).find(); 
				if (include) {
					break;
				}
			}
		}
		return include;
	}

}
