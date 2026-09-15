package com.dabi.habitv.provider.novo19.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Novo19TilesResponse {

	private final List<Novo19Tile> tiles;

	private final String moreHref;

	private final boolean envelopeParsed;

	public Novo19TilesResponse(final List<Novo19Tile> tiles, final String moreHref) {
		this(tiles, moreHref, true);
	}

	public Novo19TilesResponse(final List<Novo19Tile> tiles, final String moreHref, final boolean envelopeParsed) {
		this.tiles = tiles == null ? Collections.<Novo19Tile>emptyList()
				: Collections.unmodifiableList(new ArrayList<>(tiles));
		this.moreHref = moreHref;
		this.envelopeParsed = envelopeParsed;
	}

	public List<Novo19Tile> getTiles() {
		return tiles;
	}

	public String getMoreHref() {
		return moreHref;
	}

	public boolean isEnvelopeParsed() {
		return envelopeParsed;
	}

}
