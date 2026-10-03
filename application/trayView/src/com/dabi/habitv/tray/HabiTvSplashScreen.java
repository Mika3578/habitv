package com.dabi.habitv.tray;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.core.config.UiThemePreference;
import com.dabi.habitv.core.config.XMLUserConfig;
import com.dabi.habitv.tray.controller.UpdateController;
import com.dabi.habitv.tray.theme.UiThemeApplier;
import com.dabi.habitv.utils.DirUtils;

public class HabiTvSplashScreen {
	private Pane splashLayout;
	private ProgressBar loadProgress;
	private Label progressText;
	private static final int SPLASH_WIDTH = 676;
	private static final int SPLASH_HEIGHT = 227;
	private UpdateController updateController;

	public HabiTvSplashScreen() {
		try {
			init();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	private void init() throws IOException {

		ImageView splash = new ImageView(new Image(ClassLoader
				.getSystemResource("logo.png").openStream()));
		loadProgress = new ProgressBar();
		loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
		progressText = new Label("Chargement ...");
		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash, loadProgress, progressText);
		progressText.setAlignment(Pos.CENTER);
		applySplashChrome(UiThemePreference.DEFAULT);
		splashLayout.setEffect(new DropShadow());
	}

	private void applySplashChrome(final String theme) {
		if (UiThemePreference.isDark(theme)) {
			splashLayout
					.setStyle("-fx-padding: 5; -fx-background-color: #23272e; -fx-border-width:5; -fx-border-color: #4a5160;");
			progressText.setStyle("-fx-text-fill: #e8eaed;");
		} else {
			splashLayout
					.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-width:5; -fx-border-color: black;");
			progressText.setStyle("");
		}
	}

	public void start(final Stage initStage) throws Exception {
		String lockFile = DirUtils.getAppDir() + File.separator + "habiTv.lock";
		if (lockInstance(lockFile)) {
			Popin.fatalError("Une instance d'habiTv est déjà en cours d'exécution.\n Si ce n'est pas le cas supprimer le fichier : \n  "
					+ lockFile);
		} else {
			initStage.getIcons().add(
					new Image(ClassLoader.getSystemResource("fixe.gif")
							.openStream()));
			updateController = new UpdateController(this);
			updateController.run(initStage);
		}
	}

	public interface InitHandler {
		void onInitDone();
	}

	public void showSplash(final Stage initStage, Task<?> task,
			final InitHandler initHandler) {
		progressText.textProperty().bind(task.messageProperty());
		loadProgress.progressProperty().bind(task.progressProperty());
		task.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(
					ObservableValue<? extends Worker.State> observableValue,
					Worker.State oldState, Worker.State newState) {
				if (newState == Worker.State.SUCCEEDED) {
					loadProgress.progressProperty().unbind();
					loadProgress.setProgress(1);
					initHandler.onInitDone();
					// mainStage.setIconified(false);
					initStage.toFront();
					FadeTransition fadeSplash = new FadeTransition(Duration
							.seconds(1.2), splashLayout);
					fadeSplash.setFromValue(1.0);
					fadeSplash.setToValue(0.0);
					fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent actionEvent) {
							initStage.hide();
						}
					});
					fadeSplash.play();
				} // todo add code to gracefully handle other task states.
			}
		});
		Scene splashScene = new Scene(splashLayout);
		final String splashTheme = resolveSplashTheme();
		applySplashChrome(splashTheme);
		UiThemeApplier.apply(splashScene, splashTheme);
		initStage.initStyle(StageStyle.UNDECORATED);
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH
				/ 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2
				- SPLASH_HEIGHT / 2);
		initStage.show();
	}

	private static String resolveSplashTheme() {
		try {
			return XMLUserConfig.initConfig().getUiTheme();
		} catch (RuntimeException e) {
			return UiThemePreference.DEFAULT;
		}
	}

	private static boolean lockInstance(final String lockFile) {
		final Logger log = Logger.getLogger(HabiTvSplashScreen.class);
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(
					file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} catch (Exception e) {
							log.error(
									"Unable to remove lock file: " + lockFile,
									e);
						}
					}
				});
				return false;
			}
		} catch (Exception e) {
			log.error("Unable to create and/or lock file: " + lockFile, e);
		}
		return true;
	}
}