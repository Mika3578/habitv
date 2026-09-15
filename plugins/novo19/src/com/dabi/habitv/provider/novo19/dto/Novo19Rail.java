package com.dabi.habitv.provider.novo19.dto;

public final class Novo19Rail {

	private final String id;

	private final String type;

	private final String title;

	private final String src;

	private final String moreHref;

	public Novo19Rail(final String id, final String title, final String src, final String moreHref) {
		this(id, null, title, src, moreHref);
	}

	public Novo19Rail(final String id, final String type, final String title, final String src,
			final String moreHref) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.src = src;
		this.moreHref = moreHref;
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

	public String getSrc() {
		return src;
	}

	public String getMoreHref() {
		return moreHref;
	}

}
