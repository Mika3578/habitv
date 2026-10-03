package com.dabi.habitv.tray;

import java.io.IOException;

import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.tray.PopinController.ButtonHandler;
import com.dabi.habitv.tray.controller.TrayController;
import com.dabi.habitv.tray.theme.UiThemeApplier;

public class Popin extends Application {

	private static final Logger LOG = Logger.getLogger(TrayController.class);

	private PopinController controller = new PopinController();

	private Double width;

	private Double height;

	public Popin() {

	}

	public Popin(double width, double height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Platform.setImplicitExit(false);
		primaryStage.getIcons().add(new Image(ClassLoader.getSystemResource("fixe.gif").openStream()));
		try {
			// primaryStage.setTitle("habiTv");
			FXMLLoader fxmlLoader = new FXMLLoader(HabitvViewMain.class.getResource("popin.fxml"));
			fxmlLoader.setController(controller);

			GridPane root = (GridPane) fxmlLoader.load();
			Scene scene = width==null? new Scene(root) : new Scene(root, width, height);
			UiThemeApplier.apply(scene, UiThemeApplier.getCurrentTheme());
			primaryStage.setScene(scene);
			primaryStage.show();
			controller.init(primaryStage);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void show(final String title, final String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				try {
					start(new Stage());
					controller.show(title, message);
				} catch (Exception e) {
					LOG.error("", e);
					throw new TechnicalException(e);
				}
			}
		});
	}

	public Popin show(final String title, final Node node) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				try {
					start(new Stage());
					controller.show(title, node);
				} catch (Exception e) {
					LOG.error("", e);
					throw new TechnicalException(e);
				}
			}
		});
		return this;
	}

	public Popin setOkButtonHandler(ButtonHandler okButtonHandler) {
		controller.setOkButtonHandler(okButtonHandler);
		return this;
	}

	public Popin setCancelButtonHandler(ButtonHandler cancelButtonHandler) {
		controller.setCancelButtonHandler(cancelButtonHandler);
		return this;
	}

	private static final ButtonHandler SYSTEM_CLOSE_HANDLER = new ButtonHandler() {

		@Override
		public void onAction() {
			System.exit(1);
		}
	};

	public static void fatalError() {
		fatalError("Une erreur est survenue habiTv va fermer.\n Consulter la log pour plus de détails.");
	}

	public static void fatalError(String message) {
		(new Popin()).setOkButtonHandler(SYSTEM_CLOSE_HANDLER).setCancelButtonHandler(SYSTEM_CLOSE_HANDLER).show("Erreur", message);
	}

	public static void error(String message) {
		(new Popin()).show("Erreur", "Une erreur est survenue. Consulter la log pour plus de détail. " + message);
	}
}
