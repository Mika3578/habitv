package com.dabi.habitv.provider.novo19.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Novo19TilesResponse {

	private final List<Novo19Tile> tiles;

	private final String moreHref;

	public Novo19TilesResponse(final List<Novo19Tile> tiles, final String moreHref) {
		this.tiles = tiles == null ? Collections.<Novo19Tile>emptyList()
				: Collections.unmodifiableList(new ArrayList<>(tiles));
		this.moreHref = moreHref;
	}

	public List<Novo19Tile> getTiles() {
		return tiles;
	}

	public String getMoreHref() {
		return moreHref;
	}

}
