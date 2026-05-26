package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Stable episode identity for duplicate detection across queue and index.
 */
public final class EpisodeIdentity {

	private final String plugin;
	private final String categoryName;
	private final String episodeId;
	private final String episodeName;

	public EpisodeIdentity(final String plugin, final String categoryName,
			final String episodeId, final String episodeName) {
		this.plugin = plugin;
		this.categoryName = categoryName;
		this.episodeId = episodeId;
		this.episodeName = episodeName;
	}

	public static EpisodeIdentity fromEpisode(final EpisodeDTO episode) {
		if (episode == null) {
			return null;
		}
		final CategoryDTO category = episode.getCategory();
		final String plugin = category == null ? null : category.getPlugin();
		final String categoryName = category == null ? null : category.getName();
		return new EpisodeIdentity(plugin, categoryName, episode.getId(),
				episode.getName());
	}

	public String getPlugin() {
		return plugin;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getEpisodeId() {
		return episodeId;
	}

	public String getEpisodeName() {
		return episodeName;
	}

	public boolean matchesEpisode(final EpisodeDTO episode) {
		if (episode == null) {
			return false;
		}
		final EpisodeIdentity other = fromEpisode(episode);
		if (episodeId != null && episodeId.equals(other.episodeId)) {
			return true;
		}
		return episode.equals(buildEpisodeStub(other));
	}

	private static EpisodeDTO buildEpisodeStub(final EpisodeIdentity identity) {
		final CategoryDTO category = new CategoryDTO(identity.plugin,
				identity.categoryName, identity.categoryName, null);
		return new EpisodeDTO(category, identity.episodeName, identity.episodeId);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof EpisodeIdentity)) {
			return false;
		}
		final EpisodeIdentity other = (EpisodeIdentity) obj;
		if (episodeId != null && episodeId.equals(other.episodeId)) {
			return true;
		}
		return safeEquals(plugin, other.plugin)
				&& safeEquals(categoryName, other.categoryName)
				&& safeEquals(episodeName, other.episodeName);
	}

	@Override
	public int hashCode() {
		if (episodeId != null) {
			return episodeId.hashCode();
		}
		int hash = 17;
		hash = 31 * hash + safeHash(plugin);
		hash = 31 * hash + safeHash(categoryName);
		hash = 31 * hash + safeHash(episodeName);
		return hash;
	}

	private static boolean safeEquals(final String left, final String right) {
		if (left == null) {
			return right == null;
		}
		return left.equals(right);
	}

	private static int safeHash(final String value) {
		return value == null ? 0 : value.hashCode();
	}
}
