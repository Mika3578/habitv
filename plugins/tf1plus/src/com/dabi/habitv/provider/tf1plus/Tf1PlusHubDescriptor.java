package com.dabi.habitv.provider.tf1plus;

/**
 * Stable TF1+ catalogue hub descriptor. Programme listings are discovered dynamically;
 * only hub roots and tree layout are configured here.
 */
final class Tf1PlusHubDescriptor {

	enum TreeStyle {
		GROUPED_BY_RUBRIC,
		FLAT_PROGRAMMES
	}

	private final String hubId;
	private final String displayLabel;
	private final String urlSlug;
	private final String graphqlChannelSlug;
	private final String replayUrl;
	private final TreeStyle treeStyle;
	private final boolean enabled;

	private Tf1PlusHubDescriptor(final String hubId, final String displayLabel, final String urlSlug,
			final String graphqlChannelSlug, final String replayUrl, final TreeStyle treeStyle, final boolean enabled) {
		this.hubId = hubId;
		this.displayLabel = displayLabel;
		this.urlSlug = urlSlug;
		this.graphqlChannelSlug = graphqlChannelSlug;
		this.replayUrl = replayUrl;
		this.treeStyle = treeStyle;
		this.enabled = enabled;
	}

	String getHubId() {
		return hubId;
	}

	String getDisplayLabel() {
		return displayLabel;
	}

	String getUrlSlug() {
		return urlSlug;
	}

	String getGraphqlChannelSlug() {
		return graphqlChannelSlug;
	}

	String getReplayUrl() {
		return replayUrl;
	}

	TreeStyle getTreeStyle() {
		return treeStyle;
	}

	boolean isEnabled() {
		return enabled;
	}

	static Tf1PlusHubDescriptor groupedTf1() {
		return new Tf1PlusHubDescriptor("tf1", "TF1", "tf1", "tf1", Tf1PlusConf.TF1_REPLAY_URL,
				TreeStyle.GROUPED_BY_RUBRIC, true);
	}

	static Tf1PlusHubDescriptor flat(final String hubId, final String displayLabel, final String slug,
			final String replayUrl) {
		return new Tf1PlusHubDescriptor(hubId, displayLabel, slug, slug, replayUrl, TreeStyle.FLAT_PROGRAMMES, true);
	}

	static Tf1PlusHubDescriptor flatWithGraphqlSlug(final String hubId, final String displayLabel,
			final String urlSlug, final String graphqlChannelSlug, final String replayUrl) {
		return new Tf1PlusHubDescriptor(hubId, displayLabel, urlSlug, graphqlChannelSlug, replayUrl,
				TreeStyle.FLAT_PROGRAMMES, true);
	}

}
