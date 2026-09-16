package com.dabi.habitv.tray.controller.dl;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import com.dabi.habitv.tray.utils.DownloadProgressFormatter;

class ProgressIndicatorBar extends StackPane {
	private final ProgressBar bar = new ProgressBar();
	private final Label text = new Label();
	private final Tooltip tooltip = new Tooltip();

	private static final int DEFAULT_LABEL_PADDING = 5;

	ProgressIndicatorBar() {
		bar.setMaxWidth(Double.MAX_VALUE);
		text.setAlignment(Pos.CENTER);
		text.setMaxWidth(Double.MAX_VALUE);
		text.setTextOverrun(OverrunStyle.CLIP);
		text.setStyle("-fx-font-size: 11px;");
		getChildren().setAll(bar, text);
	}

	/**
	 * Updates the bar fill and overlay label.
	 *
	 * @param progressRatio {@code null} for indeterminate progress
	 * @param label         text drawn over the bar; may be empty
	 * @param tooltipText   optional tooltip; {@code null} or empty clears the tooltip
	 */
	public void setProgress(final Double progressRatio, final String label, final String tooltipText) {
		if (progressRatio == null) {
			bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		} else {
			final double bounded = Math.max(0.0d, Math.min(1.0d, progressRatio.doubleValue()));
			bar.setProgress(bounded);
		}
		text.setText(label == null ? "" : label);
		if (tooltipText == null || tooltipText.isEmpty()) {
			Tooltip.uninstall(this, tooltip);
		} else {
			tooltip.setText(tooltipText);
			Tooltip.install(this, tooltip);
		}

		bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_LABEL_PADDING * 2);
		bar.setMinWidth(Math.max(80.0d, text.getBoundsInLocal().getWidth() + DEFAULT_LABEL_PADDING * 2));
	}

	/**
	 * Legacy entry point retained for compatibility with older call sites.
	 */
	public void setProgress(final Double progress) {
		if (progress == null) {
			setProgress(null, "", null);
		} else {
			final String percentLabel = DownloadProgressFormatter.formatPercentage(progress);
			setProgress(progress, percentLabel == null ? "" : percentLabel, null);
		}
	}

}
