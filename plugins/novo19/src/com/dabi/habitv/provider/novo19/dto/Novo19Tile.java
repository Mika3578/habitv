package com.dabi.habitv.provider.novo19.dto;

public final class Novo19Tile {

	private final String id;

	private final String type;

	private final String title;

	private final String subtitle;

	private final String description;

	private final Long durationSeconds;

	private final String href;

	private final String assetId;

	public Novo19Tile(final String id, final String type, final String title, final String subtitle,
			final String description, final Long durationSeconds, final String href, final String assetId) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.description = description;
		this.durationSeconds = durationSeconds;
		this.href = href;
		this.assetId = assetId;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public String getDescription() {
		return description;
	}

	public Long getDurationSeconds() {
		return durationSeconds;
	}

	public String getHref() {
		return href;
	}

	public String getAssetId() {
		return assetId;
	}

}
