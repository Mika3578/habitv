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

	private static final double MIN_BAR_HEIGHT = 18.0d;

	ProgressIndicatorBar() {
		// Width comes from the Etat column / cell; never from label text.
		setMinWidth(0.0d);
		setPrefWidth(0.0d);
		setMaxWidth(Double.MAX_VALUE);
		setMinHeight(MIN_BAR_HEIGHT);
		bar.setMinWidth(0.0d);
		bar.setPrefWidth(0.0d);
		bar.setMaxWidth(Double.MAX_VALUE);
		bar.setMinHeight(MIN_BAR_HEIGHT);
		bar.setMaxHeight(Double.MAX_VALUE);
		text.setAlignment(Pos.CENTER);
		text.setMinWidth(0.0d);
		text.setPrefWidth(0.0d);
		text.setMaxWidth(Double.MAX_VALUE);
		text.setTextOverrun(OverrunStyle.CLIP);
		text.setStyle("-fx-font-size: 11px;");
		text.maxWidthProperty().bind(widthProperty());
		getChildren().setAll(bar, text);
	}

	@Override
	protected double computeMinWidth(final double height) {
		return 0.0d;
	}

	@Override
	protected double computePrefWidth(final double height) {
		return 0.0d;
	}

	@Override
	protected double computeMaxWidth(final double height) {
		return Double.MAX_VALUE;
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
