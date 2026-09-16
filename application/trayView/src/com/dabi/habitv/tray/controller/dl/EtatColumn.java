package com.dabi.habitv.tray.controller.dl;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import com.dabi.habitv.tray.controller.ViewController;
import com.dabi.habitv.tray.model.ActionProgress;

public class EtatColumn extends TableColumn<ActionProgress, Node> {

	private Map<String, DownloadBox> downloadBoxs = new HashMap<>();
	private ViewController viewController;

	public EtatColumn(final ViewController viewController) {
		super("Etat");
		setPrefWidth(160);
		this.viewController = viewController;
		setCellValueFactory(new Callback<CellDataFeatures<ActionProgress, Node>, ObservableValue<Node>>() {

			@Override
			public ObservableValue<Node> call(
					CellDataFeatures<ActionProgress, Node> param) {
				ActionProgress actionProgress = param.getValue();
				return new SimpleObjectProperty<Node>(
						buildOrUpdateDownloadBox(actionProgress));
			}
		});
	}

	public DownloadBox buildOrUpdateDownloadBox(ActionProgress actionProgress) {
		DownloadBox downloadBox = downloadBoxs.get(actionProgress.getEpisode()
				.getId());
		if (downloadBox == null) {
			final DownloadBox newDownloadBox = new DownloadBox(viewController,
					actionProgress);
			downloadBox = newDownloadBox;
			newDownloadBox.setPadding(new Insets(2));
			downloadBoxs.put(actionProgress.getEpisode().getId(), downloadBox);
		} else {
			downloadBox.update(actionProgress);
		}
		return downloadBox;
	}

}
