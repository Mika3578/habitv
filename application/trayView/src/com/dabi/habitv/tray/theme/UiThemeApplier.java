package com.dabi.habitv.tray.theme;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.core.config.UiThemePreference;

/**
 * Applies Habitv light/dark stylesheets to JavaFX scenes.
 * Tracks scenes explicitly for JavaFX 8 compatibility (no Window.getWindows()).
 */
public final class UiThemeApplier {

	private static final String STYLE_CLASS_LIGHT = "theme-light";
	private static final String STYLE_CLASS_DARK = "theme-dark";

	private static final String COMMON_CSS = "/com/dabi/habitv/tray/css/habitv-common.css";
	private static final String LIGHT_CSS = "/com/dabi/habitv/tray/css/habitv-light.css";
	private static final String DARK_CSS = "/com/dabi/habitv/tray/css/habitv-dark.css";

	private static final List<WeakReference<Scene>> trackedScenes = new ArrayList<WeakReference<Scene>>();

	private static volatile String currentTheme = UiThemePreference.DEFAULT;

	private UiThemeApplier() {
	}

	public static String stylesheetFor(final String theme) {
		return UiThemePreference.isDark(theme) ? DARK_CSS : LIGHT_CSS;
	}

	public static String getCurrentTheme() {
		return currentTheme;
	}

	public static void apply(final Scene scene, final String theme) {
		if (scene == null) {
			return;
		}
		final String normalized = UiThemePreference.normalize(theme);
		currentTheme = normalized;
		track(scene);

		final ObservableList<String> stylesheets = scene.getStylesheets();
		removeHabitvStylesheets(stylesheets);
		stylesheets.add(toExternalForm(COMMON_CSS));
		stylesheets.add(toExternalForm(stylesheetFor(normalized)));

		final Parent root = scene.getRoot();
		if (root != null) {
			root.getStyleClass().removeAll(STYLE_CLASS_LIGHT, STYLE_CLASS_DARK);
			root.getStyleClass().add(
					UiThemePreference.isDark(normalized) ? STYLE_CLASS_DARK
							: STYLE_CLASS_LIGHT);
		}
	}

	public static void applyToAllWindows(final String theme) {
		final String normalized = UiThemePreference.normalize(theme);
		currentTheme = normalized;
		final Iterator<WeakReference<Scene>> iterator = trackedScenes.iterator();
		while (iterator.hasNext()) {
			final Scene scene = iterator.next().get();
			if (scene == null) {
				iterator.remove();
			} else {
				apply(scene, normalized);
			}
		}
	}

	private static void track(final Scene scene) {
		final Iterator<WeakReference<Scene>> iterator = trackedScenes.iterator();
		while (iterator.hasNext()) {
			final Scene tracked = iterator.next().get();
			if (tracked == null) {
				iterator.remove();
			} else if (tracked == scene) {
				return;
			}
		}
		trackedScenes.add(new WeakReference<Scene>(scene));
	}

	private static void removeHabitvStylesheets(final ObservableList<String> stylesheets) {
		final Iterator<String> iterator = stylesheets.iterator();
		while (iterator.hasNext()) {
			final String stylesheet = iterator.next();
			if (stylesheet != null
					&& (stylesheet.contains("habitv-common.css")
							|| stylesheet.contains("habitv-light.css")
							|| stylesheet.contains("habitv-dark.css"))) {
				iterator.remove();
			}
		}
	}

	private static String toExternalForm(final String classpathResource) {
		final URL url = UiThemeApplier.class.getResource(classpathResource);
		if (url == null) {
			throw new TechnicalException("Missing stylesheet resource: "
					+ classpathResource);
		}
		return url.toExternalForm();
	}
}
