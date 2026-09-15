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
import com.dabi.habitv.core.config.YoutubeApiKeyConfig;
import com.dabi.habitv.tray.Popin;

public class ConfigController extends BaseController {

	private TextField downloadOuput;

	private TextField nbrMaxAttempts;

	private TextField daemonCheckTimeSec;

	private CheckBox autoUpdate;

	private TextField youtubeApiKey;

	private TextField maxConcurrentDownloads;

	private CheckBox embedSubtitles;

	public ConfigController(TextField downloadOuput, TextField nbrMaxAttempts,
			TextField daemonCheckTimeSec, CheckBox autoUpdate,
			TextField youtubeApiKey, TextField maxConcurrentDownloads,
			CheckBox embedSubtitles) {
		super();
		this.downloadOuput = downloadOuput;
		this.nbrMaxAttempts = nbrMaxAttempts;
		this.daemonCheckTimeSec = daemonCheckTimeSec;
		this.autoUpdate = autoUpdate;
		this.youtubeApiKey = youtubeApiKey;
		this.maxConcurrentDownloads = maxConcurrentDownloads;
		this.embedSubtitles = embedSubtitles;
	}

	public void init() {
		loadConfig();
		addButtonActions();
		addTooltips();
	}

	private void addTooltips() {
		final Tooltip downloadOutputTooltip = new Tooltip(buildDownloadOutputTokenHelp());
		downloadOutputTooltip.setMaxWidth(520);
		downloadOuput.setTooltip(downloadOutputTooltip);

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
		embedSubtitles.setTooltip(new Tooltip(
				"Intègre les sous-titres dans la vidéo téléchargée lorsqu'ils sont disponibles. Nécessite ffmpeg via le post-traitement yt-dlp."));
	}

	/**
	 * Help text for {@code downloadOuput} tokens (legacy + semantic + MEDIA_SERVER).
	 */
	static String buildDownloadOutputTokenHelp() {
		return "Modèle de stockage des téléchargements. Tokens supportés :\n"
				+ "\n"
				+ "Profil MEDIA_SERVER (recommandé Plex/Jellyfin/Kodi) :\n"
				+ "#MEDIA_SERVER_PATH# : chemin relatif complet (dossiers + fichier)\n"
				+ "  Exemple : C:/media/#MEDIA_SERVER_PATH#\n"
				+ "\n"
				+ "Métadonnées sémantiques (si le fournisseur les fournit) :\n"
				+ "#SERIES_NAME# / #SHOW_NAME# : titre de série / émission\n"
				+ "#EPISODE_TITLE# : titre d'épisode (normalisé)\n"
				+ "#SEASON_NUMBER# : numéro de saison\n"
				+ "#EPISODE_NUMBER# : numéro d'épisode TV\n"
				+ "#SEASON_EPISODE# : S01E03 (seulement si saison ET épisode connus)\n"
				+ "#AIR_DATE# / #EPISODE_DATE# : date de diffusion (défaut yyyy-MM-dd)\n"
				+ "#AIR_DATE§yyyyMMdd# : même date avec format personnalisé après §\n"
				+ "\n"
				+ "Tokens historiques (compatibilité) :\n"
				+ "#EPISODE# / #EPISODE_NAME# : nom d'épisode (affichage fournisseur)\n"
				+ "#EPISODE_UNTOUCHED# : nom d'épisode sans sanitization\n"
				+ "#CHANNEL# / #PLUGIN# / #PROVIDER# : nom du fournisseur\n"
				+ "#CATEGORY# / #TVSHOW_NAME# : nom de la catégorie Habitv\n"
				+ "#EXTENSION# : extension du fichier\n"
				+ "#NUM# : compteur legacy (pas le numéro d'épisode TV)\n"
				+ "#DATE# / #DATETIME# : date de téléchargement (pas la diffusion)\n"
				+ "#DATE§yyyyMMdd# : date de téléchargement avec format après §\n"
				+ "\n"
				+ "Options communes :\n"
				+ "Tout token peut utiliser §longueur (ex. #EPISODE§40#) pour tronquer.\n"
				+ "Suffixe _CUT (ex. #EPISODE_NAME_CUT#) : tronque à fileNameCutSize.";
	}

	private void loadConfig() {
		UserConfig userConfig = getController().loadUserConfig();
		downloadOuput.setText(userConfig.getDownloadOuput());
		nbrMaxAttempts.setText(String.valueOf(userConfig.getMaxAttempts()));
		daemonCheckTimeSec.setText(String.valueOf(userConfig
				.getDemonCheckTime()));
		autoUpdate.setSelected(userConfig.updateOnStartup());
		youtubeApiKey.setText(userConfig.getYoutubeApiKey());
		maxConcurrentDownloads.setText(String.valueOf(Math.max(1,
				userConfig.getMaxConcurrentDownloads())));
		embedSubtitles.setSelected(userConfig.getEmbedSubtitles());
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
				nbrMaxAttempts.setText(String.valueOf(maxAttempts.intValue()));
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
				daemonCheckTimeSec.setText(String.valueOf(demonCheckTime.intValue()));
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
				String newValue = YoutubeApiKeyConfig.sanitizePlainConfigValue(normalize(youtubeApiKey.getText()));
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
				maxConcurrentDownloads.setText(String.valueOf(newValue.intValue()));
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

		embedSubtitles.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				UserConfig userConfig = getController().loadUserConfig();
				userConfig.setEmbedSubtitles(embedSubtitles.isSelected());
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
