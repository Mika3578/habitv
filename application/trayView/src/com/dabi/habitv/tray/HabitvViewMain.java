package com.dabi.habitv.tray;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.tray.controller.WindowController;
import com.dabi.habitv.tray.model.HabitTvViewManager;
import com.dabi.habitv.api.logging.LogUtils;

public class HabitvViewMain extends Application {

	private HabitTvViewManager manager;

	public HabitvViewMain() {
		manager = new HabitTvViewManager();
	}

	public HabitvViewMain(HabitTvViewManager model) {
		super();
		this.manager = model;
	}

	@Override
	public void start(Stage primaryStage) {
		run(primaryStage);
	}

	public void run(Stage primaryStage) {
		Platform.setImplicitExit(false);		
		try {
			primaryStage.getIcons().add(
					new Image(ClassLoader.getSystemResource("fixe.gif")
							.openStream()));
			primaryStage.setTitle("habiTv");
			FXMLLoader fxmlLoader = new FXMLLoader(
					HabitvViewMain.class.getResource("habitv.fxml"));
			WindowController controller = new WindowController();
			fxmlLoader.setController(controller);

			VBox root = (VBox) fxmlLoader.load();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
			controller.init(manager, primaryStage);
		} catch (IOException exception) {
			throw new TechnicalException(exception);
		}
	}

	public static void main(String[] args) {
		LogUtils.updateLog4jConfiguration();
		launch(args);
	}
}
