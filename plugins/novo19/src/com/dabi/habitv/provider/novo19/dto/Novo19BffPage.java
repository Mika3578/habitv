package com.dabi.habitv.provider.novo19.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Novo19BffPage {

	private final String type;

	private final String id;

	private final String title;

	private final List<Novo19Rail> rails;

	private final List<Novo19Season> seasons;

	private final Novo19Tile content;

	private final List<String> contentCategories;

	public Novo19BffPage(final String type, final String id, final String title, final List<Novo19Rail> rails,
			final List<Novo19Season> seasons, final Novo19Tile content) {
		this(type, id, title, rails, seasons, content, null);
	}

	public Novo19BffPage(final String type, final String id, final String title, final List<Novo19Rail> rails,
			final List<Novo19Season> seasons, final Novo19Tile content, final List<String> contentCategories) {
		this.type = type;
		this.id = id;
		this.title = title;
		this.rails = rails == null ? Collections.<Novo19Rail>emptyList() : Collections.unmodifiableList(new ArrayList<>(rails));
		this.seasons = seasons == null ? Collections.<Novo19Season>emptyList()
				: Collections.unmodifiableList(new ArrayList<>(seasons));
		this.content = content;
		this.contentCategories = contentCategories == null ? Collections.<String>emptyList()
				: Collections.unmodifiableList(new ArrayList<>(contentCategories));
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public List<Novo19Rail> getRails() {
		return rails;
	}

	public List<Novo19Season> getSeasons() {
		return seasons;
	}

	public Novo19Tile getContent() {
		return content;
	}

	public List<String> getContentCategories() {
		return contentCategories;
	}

}
