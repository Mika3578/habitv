package com.dabi.habitv.tray.view;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.tray.Popin;
import com.dabi.habitv.tray.PopinController.ButtonHandler;
import com.dabi.habitv.tray.controller.ViewController;

public class ManualDlPopin extends Popin {

	private static class ManualForm extends HBox {

		private TextField textField = new TextField();

		public ManualForm() {
			super(3);
			getChildren().add(new Label("URL :"));
			getChildren().add(textField);
		}
	}

	private ManualForm manualForm = new ManualForm();

	private ViewController viewController;

	public ManualDlPopin(ViewController viewController) {
		this.viewController = viewController;
	}

	public void show() {
		setOkButtonHandler(new ButtonHandler() {

			@Override
			public void onAction() {
				String url = manualForm.textField.getText();
				String name = url.startsWith("http://") ? RetrieverUtils.getTitleByUrl(url) : url;
				viewController.restart(new EpisodeDTO(new CategoryDTO("Manuel", "Manuel", "Manuel", "mp4"), name, url), false);
			}

		});
		show("Téléchargement manuel", manualForm);
	}

}
