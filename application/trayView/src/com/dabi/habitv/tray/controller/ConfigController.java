package com.dabi.habitv.tray.controller;

import javafx.scene.Node;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.tray.Popin;

public class ConfigController extends BaseController {

	private TextField downloadOuput;

	private TextField nbrMaxAttempts;

	private TextField daemonCheckTimeSec;

	private CheckBox autoUpdate;

	public ConfigController(TextField downloadOuput, TextField nbrMaxAttempts,
			TextField daemonCheckTimeSec, CheckBox autoUpdate) {
		super();
		this.downloadOuput = downloadOuput;
		this.nbrMaxAttempts = nbrMaxAttempts;
		this.daemonCheckTimeSec = daemonCheckTimeSec;
		this.autoUpdate = autoUpdate;
	}

	public void init() {
		loadConfig();
		addButtonActions();
		addTooltips();
	}

	private void addTooltips() {
		downloadOuput
				.setTooltip(new Tooltip(
						"Modèle de stockage des téléchargements, vous pouvez utiliser les tokens suivant : \n"
								+ "#EPISODE# : nom de l'épisode\n"
								+ "#CHANNEL# : nom du fournisseur\n"
								+ "#CATEGORY# : nom de la catégorie\n"
								+ "#EXTENSION# : extension du fichier\n"
								+ "#NUM# : le numéro d'épisode pour le fournisseur\n"
								+ "#DATE§yyyyMMdd# : la date de téléchargement de l'épisode, le paramètre après § peut être modifié suivant : Format de date"));

		nbrMaxAttempts
				.setTooltip(new Tooltip(
						"Nombre de tentatives de téléchargement d'un épisode avant d'arrêter de retenter."));
		daemonCheckTimeSec
				.setTooltip(new Tooltip(
						"Période de temps entre 2 recherches automatiques de téléchargement."));
		autoUpdate.setTooltip(new Tooltip(
				"si coché habiTv se mettra à jour automatiquement."));
	}

	private void loadConfig() {
		UserConfig userConfig = getController().loadUserConfig();
		downloadOuput.setText(userConfig.getDownloadOuput());
		nbrMaxAttempts.setText(String.valueOf(userConfig.getMaxAttempts()));
		daemonCheckTimeSec.setText(String.valueOf(userConfig
				.getDemonCheckTime()));
		autoUpdate.setSelected(userConfig.updateOnStartup());
	}

	private void addButtonActions() {

		final Runnable saveDlOupput = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				if (!userConfig.getDownloadOuput().equals(
						downloadOuput.getText())) {
					userConfig.setDownloadOuput(downloadOuput.getText());
					saveConfig(userConfig);
				}
			}

		};

		triggersave(downloadOuput, saveDlOupput);

		Runnable saveMaxAttemps = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				if (!String.valueOf(userConfig.getMaxAttempts()).equals(
						nbrMaxAttempts.getText())) {
					userConfig.setMaxAttempts(Integer.parseInt(nbrMaxAttempts
							.getText()));
					saveConfig(userConfig);
				}
			}
		};

		triggersave(nbrMaxAttempts, saveMaxAttemps);

		Runnable saveDaemonCheck = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				if (!String.valueOf(userConfig.getDemonCheckTime()).equals(
						daemonCheckTimeSec.getText())) {
					userConfig.setDemonCheckTime(Integer
							.parseInt(daemonCheckTimeSec.getText()));
					saveConfig(userConfig);
				}
			}
		};
		triggersave(daemonCheckTimeSec, saveDaemonCheck);

		autoUpdate.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				UserConfig userConfig = getController().loadUserConfig();
				userConfig.setUpdateOnStartup(autoUpdate.isSelected());
				saveConfig(userConfig);
			}
		});
	}

	private void triggersave(final Node eventTarget, final Runnable toExecute) {
		eventTarget.focusedProperty().addListener(
				new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean focus) {
						if (!focus) {
							planTaskIfNot(toExecute);
						}
					}
				});
	}

	private void saveConfig(UserConfig userConfig) {
		getController().saveConfig(userConfig);
		new Popin()
				.show("Configuration sauvegardée",
						"La configuration a été sauvegardée \n mais ne sera active qu'après un redémarrage de l'application.");
	}
}
