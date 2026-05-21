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

	private TextField youtubeApiKey;

	private TextField maxConcurrentDownloads;

	public ConfigController(TextField downloadOuput, TextField nbrMaxAttempts,
			TextField daemonCheckTimeSec, CheckBox autoUpdate,
			TextField youtubeApiKey, TextField maxConcurrentDownloads) {
		super();
		this.downloadOuput = downloadOuput;
		this.nbrMaxAttempts = nbrMaxAttempts;
		this.daemonCheckTimeSec = daemonCheckTimeSec;
		this.autoUpdate = autoUpdate;
		this.youtubeApiKey = youtubeApiKey;
		this.maxConcurrentDownloads = maxConcurrentDownloads;
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
		youtubeApiKey.setTooltip(new Tooltip(
				"Clé API YouTube Data v3. Laissez vide pour utiliser la variable d'environnement ou l'option Java."));
		maxConcurrentDownloads.setTooltip(new Tooltip(
				"Nombre maximum de téléchargements d'épisodes exécutés en même temps (minimum 1)."));
	}

	private void loadConfig() {
		UserConfig userConfig = getController().loadUserConfig();
		downloadOuput.setText(userConfig.getDownloadOuput());
		nbrMaxAttempts.setText(String.valueOf(userConfig.getMaxAttempts()));
		daemonCheckTimeSec.setText(String.valueOf(userConfig
				.getDemonCheckTime()));
		autoUpdate.setSelected(userConfig.updateOnStartup());
		youtubeApiKey.setText(userConfig.getYoutubeApiKey());
		maxConcurrentDownloads.setText(String.valueOf(userConfig
				.getMaxConcurrentDownloads()));
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
				final Integer currentValue = userConfig.getMaxAttempts();
				final Integer maxAttempts = parsePositiveInteger(nbrMaxAttempts.getText());
				if (maxAttempts == null) {
					nbrMaxAttempts.setText(String.valueOf(currentValue));
					new Popin().show("Configuration invalide",
							"Le nombre maximum de tentatives doit être un entier supérieur ou égal à 1.");
					return;
				}
				if (!currentValue.equals(maxAttempts)) {
					userConfig.setMaxAttempts(maxAttempts);
					saveConfig(userConfig);
				}
			}
		};

		triggersave(nbrMaxAttempts, saveMaxAttemps);

		Runnable saveDaemonCheck = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				final Integer currentValue = userConfig.getDemonCheckTime();
				final Integer demonCheckTime = parsePositiveInteger(daemonCheckTimeSec.getText());
				if (demonCheckTime == null) {
					daemonCheckTimeSec.setText(String.valueOf(currentValue));
					new Popin().show("Configuration invalide",
							"La période entre deux recherches doit être un entier supérieur ou égal à 1.");
					return;
				}
				if (!currentValue.equals(demonCheckTime)) {
					userConfig.setDemonCheckTime(demonCheckTime);
					saveConfig(userConfig);
				}
			}
		};
		triggersave(daemonCheckTimeSec, saveDaemonCheck);

		Runnable saveYoutubeApiKey = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				String currentValue = userConfig.getYoutubeApiKey();
				String newValue = normalize(youtubeApiKey.getText());
				if (currentValue == null ? newValue != null
						: !currentValue.equals(newValue)) {
					userConfig.setYoutubeApiKey(newValue);
					saveConfig(userConfig);
				}
			}
		};
		triggersave(youtubeApiKey, saveYoutubeApiKey);

		Runnable saveMaxConcurrent = new Runnable() {

			@Override
			public void run() {
				UserConfig userConfig = getController().loadUserConfig();
				final int currentValue = Math.max(1, userConfig.getMaxConcurrentDownloads());
				final Integer newValue = parsePositiveInteger(maxConcurrentDownloads.getText());
				if (newValue == null) {
					maxConcurrentDownloads.setText(String.valueOf(currentValue));
					new Popin().show("Configuration invalide",
							"Le nombre maximum de téléchargements simultanés doit être un entier supérieur ou égal à 1.");
					return;
				}
				if (userConfig.getMaxConcurrentDownloads() != newValue.intValue()) {
					userConfig.setMaxConcurrentDownloads(newValue);
					saveConfig(userConfig);
				}
			}
		};
		triggersave(maxConcurrentDownloads, saveMaxConcurrent);

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

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Integer parsePositiveInteger(String value) {
		try {
			final int parsed = Integer.parseInt(value == null ? "" : value.trim());
			return parsed < 1 ? Integer.valueOf(1) : Integer.valueOf(parsed);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
