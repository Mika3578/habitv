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

			if (!trayMode) {
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

					@Override
					public void handle(WindowEvent t) {
						Platform.exit();
					}

				});
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

		} catch (Throwable e) {
			LOG.error("", e);
			Popin.fatalError();
		}
	}
}
