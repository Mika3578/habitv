package com.dabi.habitv.tray.controller.dl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.tray.controller.BaseController;
import com.dabi.habitv.tray.model.ActionProgress;
import com.dabi.habitv.tray.subscriber.CoreSubscriber;

public class DownloadController extends BaseController implements
		CoreSubscriber {

	private Button searchButton;

	private Button clearButton;

	private Button retryExportButton;

	private TableView<ActionProgress> downloadTable;

	private Button downloadDirButton;

	private Button indexButton;

	private Button errorBUtton;

	private ProgressIndicator mainProgress;

	private Button openLogButton;

	private EtatColumn etatColumn;

	@SuppressWarnings("unchecked")
	public DownloadController(ProgressIndicator mainProgress,
			Button searchButton, Button clearButton, Button retryExportButton,
			final TableView<ActionProgress> downloadTable,
			Button downloadDirButton, Button indexButton, Button errorBUtton,
			Button openLogButton) {
		super();
		this.mainProgress = mainProgress;
		this.searchButton = searchButton;
		this.clearButton = clearButton;
		this.retryExportButton = retryExportButton;
		this.downloadTable = downloadTable;
		this.downloadDirButton = downloadDirButton;
		this.indexButton = indexButton;
		this.errorBUtton = errorBUtton;
		this.openLogButton = openLogButton;
		runCheckProgressThread();

		etatColumn = new EtatColumn(getController());

		this.downloadTable.getColumns().clear();

		this.downloadTable.setOnContextMenuRequested(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				buildMenuItem(downloadTable.getSelectionModel()
						.getSelectedItems());
			}
		});
		this.downloadTable.getColumns().addAll(new PluginColumn(),
				new CategoryColumn(), new EpisodeColumn(), etatColumn);

		this.downloadTable.setContextMenu(new ContextMenu());
	}

	void buildMenuItem(final ObservableList<ActionProgress> actionProgressList) {
		ContextMenu contextMenu = this.downloadTable.getContextMenu();
		contextMenu.getItems().clear();
		if (ActionProgress.isInProgress(actionProgressList)) {
			MenuItem menuItemStop = new MenuItem("Arrêter");
			menuItemStop.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					for (ActionProgress actionProgress : actionProgressList) {
						if (actionProgress.getProcessHolder() != null) {
							actionProgress.getProcessHolder().stop();
						} else {
							getController().cancel(
									actionProgress.getEpisode());
						}
					}
				}
			});
			contextMenu.getItems().add(menuItemStop);
			MenuItem menuItemStopAndSetAsDL = new MenuItem(
					"Arrêter et marquer comme téléchargé");
			menuItemStopAndSetAsDL.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					for (ActionProgress actionProgress : actionProgressList) {
						if (actionProgress.getProcessHolder() != null) {
							actionProgress.getProcessHolder().stop();
						} else {
							getController().cancel(
									actionProgress.getEpisode());
						}
						getController().setDownloaded(
								actionProgress.getEpisode());
					}
				}
			});
			contextMenu.getItems().add(menuItemStopAndSetAsDL);
		}
		MenuItem menuItemOpenIndex = new MenuItem("Ouvrir l'index");
		menuItemOpenIndex.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				for (ActionProgress actionProgress : actionProgressList) {
					getController().openIndex(
							actionProgress.getEpisode().getCategory());
				}
			}
		});
		contextMenu.getItems().add(menuItemOpenIndex);

		if (ActionProgress.hasFailed(actionProgressList)) {
			MenuItem menuItemReStart = new MenuItem("Relancer");
			menuItemReStart.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					for (ActionProgress actionProgress : actionProgressList) {
						getController().restart(actionProgress.getEpisode(),
								actionProgress.getState().isExport());
					}
				}
			});
			contextMenu.getItems().add(menuItemReStart);
		}
		downloadTable.setContextMenu(contextMenu);
	}

	public void init() {
		downloadTable.getItems().clear();
		addTooltip();
		updateDownloadPanel();
		addDownloadActions();
		addExportActions();
		addFilesAndFoldersActions();
	}

	private void runCheckProgressThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								updateDownloadPanel();
							}
						});
					} catch (final InterruptedException e) {
						throw new TechnicalException(e);
					}
				}
			}
		}).start();
	}

	private void addTooltip() {
		this.searchButton.setTooltip(new Tooltip(
				"Rechercher des épisodes à télécharger."));
		this.clearButton.setTooltip(new Tooltip(
				"Vider la liste des téléchargements terminés."));
		this.retryExportButton.setTooltip(new Tooltip(
				"Retenter les exports précédemment échoués."));
		this.downloadDirButton.setTooltip(new Tooltip(
				"Ouvrir le répertoire de téléchargement."));
		this.indexButton
				.setTooltip(new Tooltip(
						"Ouvrir le répertoire contenant les index des épisodes marqués comme déjà téléchargés."));
		this.errorBUtton
				.setTooltip(new Tooltip(
						"Ouvrir le fichier contenant les téléchargements notés en erreur qui ne seront pas retentés."));
	}

	private void addFilesAndFoldersActions() {
		downloadDirButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().openDownloadDir();
			}
		});

		indexButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().openIndexDir();
			}
		});

		errorBUtton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().openErrorFile();
			}
		});
	}

	private void addExportActions() {
		retryExportButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().reDoExport();
			}
		});
		retryExportButton.setVisible(getController().hasExportToResume());

		// clearExportButton.setOnAction(new EventHandler<ActionEvent>() {
		//
		// @Override
		// public void handle(ActionEvent event) {
		// getController().clearExport();
		// }
		// });
		// clearExportButton.setVisible(false);
	}

	private void addDownloadActions() {
		searchButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						getController().start();
					}

				}).run();
			}
		});

		clearButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().clear();
				updateDownloadPanel();
			}
		});

		openLogButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().openLogFile();
			}
		});
	}

	private void updateDownloadPanel() {
		Set<String> existingEpId = new HashSet<>();
		for (ActionProgress actionProgress : getModel().getProgressionModel()
				.getEpisodeName2ActionProgress()) {
			if (!downloadTable.getItems().contains(actionProgress)) {
				downloadTable.getItems().add(actionProgress);
			} else {
				etatColumn.buildOrUpdateDownloadBox(actionProgress);
			}
			existingEpId.add(actionProgress.getEpisode().getId());
		}

		Iterator<ActionProgress> it = downloadTable.getItems().iterator();
		while (it.hasNext()) {
			ActionProgress actionProgress = it.next();
			if (!existingEpId.contains(actionProgress.getEpisode().getId())) {
				it.remove();
			}
		}
	}

	@Override
	public void update(final RetreiveEvent event) {
		getController().update(event);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (event.getState() == EpisodeStateEnum.EXPORT_FAILED) {
					retryExportButton.setVisible(true);
				}
				updateDownloadPanel();
			}

		});
	}

	@Override
	public void update(UpdatePluginEvent event) {

	}

	@Override
	public void update(UpdatablePluginEvent event) {

	}

	private int searchCount;

	private int searchSize;

	@Override
	public void update(final SearchEvent event) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				switch (event.getState()) {
				case STARTING:
					searchButton.setDisable(true);
					searchCount = 0;
					searchSize = Integer.parseInt(event.getInfo());
					mainProgress.setProgress((double) searchCount / searchSize);
					break;
				case DONE:
					searchCount++;
					mainProgress.setProgress((double) searchCount / searchSize);
					break;
				case ALL_SEARCH_DONE:
					searchButton.setDisable(false);
					mainProgress.setProgress(1);
					break;
				default:
					break;
				}
			}

		});
	}

	@Override
	public void update(SearchCategoryEvent event) {

	}
}
