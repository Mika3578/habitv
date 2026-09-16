package com.dabi.habitv.tray.controller.dl;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.tray.controller.ViewController;
import com.dabi.habitv.tray.model.ActionProgress;
import com.dabi.habitv.tray.utils.DownloadProgressFormatter;
import com.dabi.habitv.tray.utils.LabelUtils;

public class DownloadBox extends Pane {

	private EpisodeDTO episode;

	public DownloadBox(final ViewController viewController, final ActionProgress actionProgress) {
		super();
		this.episode = actionProgress.getEpisode();
		getChildren().add(getStateWidget(null, actionProgress));
	}

	public void update(final ActionProgress actionProgress) {
		final Node oldWidget = getChildren().get(0);
		final Node newWidget = getStateWidget(oldWidget, actionProgress);
		if (!oldWidget.equals(newWidget)) {
			getChildren().clear();
			getChildren().add(newWidget);
		}
	}

	private Node getStateWidget(final Node oldWidget, final ActionProgress actionProgress) {
		final EpisodeStateEnum state = actionProgress.getState();
		if (state == EpisodeStateEnum.DOWNLOAD_STARTING || state == EpisodeStateEnum.EXPORT_STARTING) {
			return getProgressBarWidget(oldWidget, actionProgress);
		}
		return getLabelWidget(oldWidget, actionProgress);
	}

	private Node getProgressBarWidget(final Node oldWidget, final ActionProgress actionProgress) {
		final ProgressIndicatorBar progressBar;
		if (oldWidget == null || !(oldWidget instanceof ProgressIndicatorBar)) {
			progressBar = new ProgressIndicatorBar();
		} else {
			progressBar = (ProgressIndicatorBar) oldWidget;
		}

		final DownloadProgressSnapshot snapshot = DownloadProgressFormatter.resolveSnapshot(actionProgress);
		final String cellText;
		if (actionProgress.getState() == EpisodeStateEnum.EXPORT_STARTING) {
			cellText = formatExportCell(actionProgress, snapshot);
		} else {
			cellText = DownloadProgressFormatter.formatCellText(snapshot);
		}
		final Double ratio = snapshot.isIndeterminate() ? null : snapshot.getProgressRatio();
		final String tooltip = buildTooltip(actionProgress, snapshot);
		progressBar.setProgress(ratio, cellText, tooltip);
		return progressBar;
	}

	private static String formatExportCell(final ActionProgress actionProgress,
			final DownloadProgressSnapshot snapshot) {
		final String operation = actionProgress.getInfo();
		final StringBuilder builder = new StringBuilder("Export");
		if (operation != null && !operation.isEmpty()) {
			builder.append(" · ").append(operation);
		}
		if (!snapshot.isIndeterminate() && snapshot.getProgressRatio() != null) {
			final String percent = DownloadProgressFormatter.formatPercentage(snapshot.getProgressRatio());
			if (percent != null) {
				builder.append(" · ").append(percent);
			}
		} else {
			builder.append("…");
		}
		return builder.toString();
	}

	private static String buildTooltip(final ActionProgress actionProgress,
			final DownloadProgressSnapshot snapshot) {
		if (actionProgress.getState() == EpisodeStateEnum.EXPORT_STARTING) {
			final String operation = actionProgress.getInfo();
			final String progressTooltip = DownloadProgressFormatter.formatTooltip(snapshot);
			if (operation == null || operation.isEmpty()) {
				return progressTooltip;
			}
			if (progressTooltip == null) {
				return "Export\n" + operation;
			}
			return "Export · " + operation + "\n" + progressTooltip;
		}
		return DownloadProgressFormatter.formatTooltip(snapshot);
	}

	private Node getLabelWidget(final Node oldWidget, final ActionProgress actionProgress) {
		final Label label;
		if (oldWidget == null || !(oldWidget instanceof Label)) {
			label = new Label();
		} else {
			label = (Label) oldWidget;
		}
		label.setText(LabelUtils.buildStateLabel(actionProgress));
		final DownloadProgressSnapshot snapshot = DownloadProgressFormatter.resolveSnapshot(actionProgress);
		final String tooltip = DownloadProgressFormatter.formatTooltip(snapshot);
		if (tooltip != null && !tooltip.isEmpty()) {
			label.setTooltip(new Tooltip(tooltip));
		} else {
			label.setTooltip(null);
		}
		return label;
	}

	@Override
	public int hashCode() {
		return episode.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DownloadBox other = (DownloadBox) obj;
		return episode.equals(other.episode);
	}

}
