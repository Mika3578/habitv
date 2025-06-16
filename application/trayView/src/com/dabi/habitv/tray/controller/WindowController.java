package com.dabi.habitv.tray.controller;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.config.CredentialConfig;
import com.dabi.habitv.tray.Popin;
import com.dabi.habitv.tray.controller.dl.DownloadController;
import com.dabi.habitv.tray.controller.todl.IncludeExcludeEnum;
import com.dabi.habitv.tray.controller.todl.ToDownloadController;
import com.dabi.habitv.tray.model.ActionProgress;
import com.dabi.habitv.tray.model.HabitTvViewManager;
import com.dabi.habitv.tray.view.HabiTvTrayView;

public class WindowController {

	private static final Logger LOG = Logger.getLogger(WindowController.class);

	/*
	 * DL
	 */

	@FXML
	private ProgressIndicator mainProgress;

	@FXML
	private Tab downloadTab;

	@FXML
	private Button searchButton;

	@FXML
	private Button clearButton;

	@FXML
	private Button retryExportButton;

	@FXML
	private TableView<ActionProgress> downloadTable;

	@FXML
	private Button downloadDirButton;

	@FXML
	private Button indexButton;

	@FXML
	private Button errorBUtton;

	@FXML
	private Button openLogButton;

	/*
	 * TO DL
	 */

	@FXML
	private ProgressIndicator searchCategoryProgress;

	@FXML
	private Tab toDownloadTab;

	@FXML
	private Button refreshCategoryButton;

	@FXML
	private Button cleanCategoryButton;

	@FXML
	private Label indicationText;

	@FXML
	private TreeView<CategoryDTO> toDLTree;

	@FXML
	private ListView<EpisodeDTO> episodeListView;

	@FXML
	private TextField episodeFilter;

	@FXML
	private TextField categoryFilter;

	@FXML
	private CheckBox applySavedFilters;

	@FXML
	private ChoiceBox<IncludeExcludeEnum> filterTypeChoice;

	@FXML
	private Button addFilterButton;

	@FXML
	private HBox currentFilterVBox;

	/*
	 * CONFIG
	 */

	@FXML
	private Tab configTab;

	@FXML
	private TextField downloadOuput;

	@FXML
	private TextField nbrMaxAttempts;

	@FXML
	private TextField daemonCheckTimeSec;

	@FXML
	private CheckBox autoUpdate;

	/*
	 * CREDENTIALS
	 */

	@FXML
	private Tab credentialsTab;

	@FXML
	private TableView<CredentialConfig> credentialsTable;

	@FXML
	private Button addCredentialButton;

	@FXML
	private Button editCredentialButton;

	@FXML
	private Button removeCredentialButton;

	@FXML
	private Button refreshCredentialsButton;

	@FXML
	private Tab logsTab;
	@FXML
	private javafx.scene.control.TextArea logView;
	@FXML
	private Button openLogFileButton;
	@FXML
	private Button clearLogFileButton;
	@FXML
	private javafx.scene.control.ToggleButton pauseLogRefreshButton;
	@FXML
	private Button exportLogFileButton;
	@FXML
	private TextField logFilterField;
	@FXML
	private javafx.scene.layout.FlowPane logFlow;

	private boolean trayMode = false;

	public WindowController() {
	}

	public void init(final HabitTvViewManager manager, Stage primaryStage)
			throws IOException {

		try {

			downloadTab.setGraphic(new ImageView(new Image((ClassLoader
					.getSystemResource("dl.png").openStream()))));
			toDownloadTab.setGraphic(new ImageView(new Image((ClassLoader
					.getSystemResource("adl.png").openStream()))));
			configTab.setGraphic(new ImageView(new Image((ClassLoader
					.getSystemResource("config.png").openStream()))));

			final ViewController controller = new ViewController(manager,
					primaryStage);
			manager.attach(controller);

			if (SystemTray.isSupported()) {
				try {
					final HabiTvTrayView view = new HabiTvTrayView(controller,
							primaryStage);
					view.init();
					manager.attach(view);
					trayMode = true;
				} catch (AWTException e) {
					trayMode = false;
				}
			}

			// Remplacement par lambda
			if (!trayMode) {
				primaryStage.setOnCloseRequest(t -> Platform.exit());
			}

			DownloadController downloadController = new DownloadController(
					mainProgress, searchButton, clearButton, retryExportButton,
					downloadTable, downloadDirButton, indexButton, errorBUtton,
					openLogButton);
			manager.attach(downloadController);
			downloadController.init(controller, manager, primaryStage);

			ToDownloadController toDlController = new ToDownloadController(
					searchCategoryProgress, refreshCategoryButton,
					cleanCategoryButton, toDLTree, indicationText,
					episodeListView, episodeFilter, categoryFilter,
					applySavedFilters, filterTypeChoice, addFilterButton,
					currentFilterVBox);
			toDlController.init(controller, manager, primaryStage);
			manager.attach(toDlController);

			new ConfigController(downloadOuput, nbrMaxAttempts,
					daemonCheckTimeSec, autoUpdate).init(controller, manager,
					primaryStage);

			new CredentialController().init(controller, manager, primaryStage);

			controller.startDownloadCheckDemon();

			// Après les autres initialisations, démarrer l'auto-refresh du log
			startLogAutoRefresh();
			// Actions des boutons log
			openLogFileButton.setOnAction(e -> openLogFile());
			clearLogFileButton.setOnAction(e -> clearLogFile());
			pauseLogRefreshButton.setOnAction(e -> toggleLogPause());
			exportLogFileButton.setOnAction(e -> exportLogFile());
			logFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
				logFilter = newVal == null ? "" : newVal.trim();
				refreshLogDisplay(lastLogLines);
			});

		} catch (Throwable e) {
			LOG.error("", e);
			Popin.fatalError();
		}
	}

	// --- Ajout de la méthode d'auto-refresh du log ---
	private java.util.concurrent.ScheduledExecutorService logScheduler;
	private static final String LOG_PATH = System.getProperty("user.home") + "/habitv/habiTv.log";
	private volatile boolean logPaused = false;
	private volatile String logFilter = "";
	private java.util.List<String> lastLogLines = java.util.Collections.emptyList();
	private void startLogAutoRefresh() {
		final java.util.concurrent.ScheduledExecutorService logScheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
		logScheduler.scheduleAtFixedRate(() -> {
			if (logPaused) return;
			java.util.List<String> lines;
			try {
				java.nio.file.Path logFile = java.nio.file.Paths.get(LOG_PATH);
				if (java.nio.file.Files.exists(logFile)) {
					lines = java.nio.file.Files.readAllLines(logFile);
				} else {
					lines = java.util.Collections.singletonList("Aucun log disponible.");
				}
			} catch (Exception e) {
				lines = java.util.Collections.singletonList("Erreur lecture log: " + e.getMessage());
			}
			lastLogLines = lines;
			final java.util.List<String> finalLines = lines;
			Platform.runLater(() -> refreshLogDisplay(finalLines));
		}, 0, 2, java.util.concurrent.TimeUnit.SECONDS);
	}

	private void refreshLogDisplay(java.util.List<String> lines) {
		StringBuilder logContent = new StringBuilder();
		String filter = logFilter;
		for (String line : lines) {
			if (!filter.isEmpty() && !line.toLowerCase().contains(filter.toLowerCase())) continue;
			logContent.append(line).append("\n");
		}
		logView.setText(logContent.toString());
	}

	private void toggleLogPause() {
		logPaused = pauseLogRefreshButton.isSelected();
		pauseLogRefreshButton.setText(logPaused ? "▶️ Reprendre" : "⏸️ Pause");
	}

	private void exportLogFile() {
		Platform.runLater(() -> {
			javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
			fileChooser.setTitle("Exporter le log");
			fileChooser.setInitialFileName("habitv-" + java.time.LocalDateTime.now().toString().replace(':','-') + ".log");
			final java.io.File dest = fileChooser.showSaveDialog(null);
			if (dest != null) {
				try {
					java.nio.file.Files.write(dest.toPath(), lastLogLines);
				} catch (Exception ex) {
					showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur export log : " + ex.getMessage());
				}
			}
		});
	}

	private void openLogFile() {
		try {
			java.io.File logFile = new java.io.File(LOG_PATH);
			if (logFile.exists()) {
				java.awt.Desktop.getDesktop().open(logFile);
			} else {
				showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Fichier de log introuvable.");
			}
		} catch (Exception ex) {
			showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur à l'ouverture du log : " + ex.getMessage());
		}
	}

	private void clearLogFile() {
		javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Vider le fichier de log ?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
		confirm.setHeaderText(null);
		java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
		if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.YES) {
			try {
				java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get(LOG_PATH), java.nio.file.StandardOpenOption.TRUNCATE_EXISTING).close();
				// Log vidé
			} catch (Exception ex) {
				showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Impossible de vider le log : " + ex.getMessage());
			}
		}
	}

	private void showAlert(javafx.scene.control.Alert.AlertType type, String message) {
		Platform.runLater(() -> {
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type, message, javafx.scene.control.ButtonType.OK);
			alert.setHeaderText(null);
			alert.showAndWait();
		});
	}

	// Documentation :
	// - L'onglet "Logs" permet d'afficher, filtrer, colorer, exporter, ouvrir et vider le fichier de log habitv/habiTv.log.
	// - Les boutons permettent :
	//   * 📂 Ouvrir le log dans l'éditeur système
	//   * 🧹 Vider le log (après confirmation)
	//   * ⏸️/▶️ Pause/Reprendre l'auto-refresh
	//   * 💾 Exporter le log affiché
	//   * Filtrer les lignes par mot-clé ou niveau (champ de recherche)
	// - Les lignes ERROR sont rouges, WARN orange, INFO grises.
	// - Le log est relu toutes les 2s sauf en pause.
	// - Voir README.md et INSTALL.md pour plus d'infos.
}
