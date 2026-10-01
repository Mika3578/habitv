package com.dabi.habitv.tray.utils;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Central vector icon set (own simple paths, no external asset dependency).
 * <p>
 * Icons are state-colored so meaning never relies on color alone (shape +
 * tooltip). Kept monochrome per state for visual coherence.
 */
public final class Icons {

	/** Neutral action accent (download). */
	public static final Color COLOR_ACTION = Color.web("#3b7bd4");

	/** Queued state accent (amber). */
	public static final Color COLOR_QUEUED = Color.web("#e08a00");

	/** Active state accent (blue). */
	public static final Color COLOR_ACTIVE = Color.web("#2f7ddb");

	/** Downloaded state accent (green). */
	public static final Color COLOR_DONE = Color.web("#2e9e44");

	/** Error state accent (red). */
	public static final Color COLOR_ERROR = Color.web("#d23b3b");

	private Icons() {
	}

	/** Download arrow onto a tray (available state / download action). */
	public static SVGPath download() {
		return icon("M11 3h2v8.2h3.1L12 15.8l-4.1-4.6H11V3z"
				+ "M4 18h16v2H4z", COLOR_ACTION);
	}

	/** Check inside a circle (downloaded state). */
	public static SVGPath checkCircle() {
		return icon("M12 2a10 10 0 1 0 .001 20.001A10 10 0 0 0 12 2z"
				+ "M12 4a8 8 0 1 1 .001 16.001A8 8 0 0 1 12 4z"
				+ "M10.5 16.2L6.3 12l1.4-1.4l2.8 2.8l5.8-5.8L17.7 9z", COLOR_DONE);
	}

	/** Clock (queued state). */
	public static SVGPath clock() {
		return icon("M12 2a10 10 0 1 0 .001 20.001A10 10 0 0 0 12 2z"
				+ "M12 4a8 8 0 1 1 .001 16.001A8 8 0 0 1 12 4z"
				+ "M11 6.5h2v5.6l4 2.3l-1 1.7l-5-2.9z", COLOR_QUEUED);
	}

	/** Open arc (activity / downloading). */
	public static SVGPath progress() {
		return icon("M12 2a10 10 0 1 1-7.07 2.93l1.42 1.42A8 8 0 1 0 12 4z",
				COLOR_ACTIVE);
	}

	/** Exclamation in a circle (error state). */
	public static SVGPath error() {
		return icon("M12 2a10 10 0 1 0 .001 20.001A10 10 0 0 0 12 2z"
				+ "M12 4a8 8 0 1 1 .001 16.001A8 8 0 0 1 12 4z"
				+ "M11 6.5h2v7h-2zM11 15.5h2v2h-2z", COLOR_ERROR);
	}

	/** External-link / open-in-browser arrow. */
	public static SVGPath externalLink() {
		return icon("M14 3h7v7h-2V6.4l-8.3 8.3l-1.4-1.4L17.6 5H14z"
				+ "M3 5h7v2H5v12h12v-5h2v7H3z", COLOR_ACTION);
	}

	private static SVGPath icon(final String path, final Color color) {
		final SVGPath svg = new SVGPath();
		svg.setContent(path);
		svg.setFill(color);
		svg.setStroke(null);
		return svg;
	}

	/** Attaches an accessible tooltip to an icon button. */
	public static void tooltip(final javafx.scene.control.Control control,
			final String text) {
		if (text == null || text.isEmpty()) {
			control.setTooltip(null);
		} else {
			control.setTooltip(new Tooltip(text));
		}
	}
}
