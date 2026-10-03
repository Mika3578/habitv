package com.dabi.habitv.tray.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

import com.dabi.habitv.core.config.UiThemePreference;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.tray.theme.UiThemeApplier;

/**
 * Compact Light/Dark toggle bound to persisted user configuration.
 */
public class ThemeController extends BaseController {

	private static final String LIGHT_GLYPH = "☀";
	private static final String DARK_GLYPH = "☾";

	private final ToggleButton themeToggle;

	public ThemeController(final ToggleButton themeToggle) {
		this.themeToggle = themeToggle;
	}

	public void init() {
		themeToggle.getStyleClass().add("theme-toggle");
		final String currentTheme = getController().loadUserConfig().getUiTheme();
		final boolean dark = UiThemePreference.isDark(currentTheme);
		themeToggle.setSelected(dark);
		refreshTogglePresentation(dark);
		UiThemeApplier.applyToAllWindows(currentTheme);

		themeToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable,
					final Boolean wasSelected, final Boolean isSelected) {
				final boolean useDark = Boolean.TRUE.equals(isSelected);
				final String theme = useDark ? UiThemePreference.DARK
						: UiThemePreference.LIGHT;
				refreshTogglePresentation(useDark);
				UiThemeApplier.applyToAllWindows(theme);
				persistTheme(theme);
			}
		});
	}

	private void refreshTogglePresentation(final boolean dark) {
		themeToggle.setText(dark ? LIGHT_GLYPH : DARK_GLYPH);
		final String tooltipText = dark ? "Passer en thème clair"
				: "Passer en thème sombre";
		themeToggle.setTooltip(new Tooltip(tooltipText));
		themeToggle.setAccessibleText(tooltipText);
	}

	private void persistTheme(final String theme) {
		final UserConfig userConfig = getController().loadUserConfig();
		if (userConfig == null) {
			return;
		}
		if (UiThemePreference.normalize(theme).equals(userConfig.getUiTheme())) {
			return;
		}
		userConfig.setUiTheme(theme);
		getController().saveConfig(userConfig);
	}
}
