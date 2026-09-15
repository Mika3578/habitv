package com.dabi.habitv.provider.novo19.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Novo19Season {

	private final String title;

	private final int index;

	private final List<Novo19Tile> episodes;

	public Novo19Season(final String title, final int index, final List<Novo19Tile> episodes) {
		this.title = title;
		this.index = index;
		this.episodes = episodes == null ? Collections.<Novo19Tile>emptyList()
				: Collections.unmodifiableList(new ArrayList<>(episodes));
	}

	public String getTitle() {
		return title;
	}

	public int getIndex() {
		return index;
	}

	public List<Novo19Tile> getEpisodes() {
		return episodes;
	}

}
