package com.dabi.habitv.tray.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent.UpdatablePluginStateEnum;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.config.XMLUserConfig;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.tray.HabiTvSplashScreen;
import com.dabi.habitv.tray.HabiTvSplashScreen.InitHandler;
import com.dabi.habitv.tray.HabitvViewMain;
import com.dabi.habitv.tray.Popin;
import com.dabi.habitv.tray.model.HabitTvViewManager;
import com.dabi.habitv.tray.subscriber.CoreSubscriber;
import com.dabi.habitv.tray.subscriber.UpdateSubscriber;

public class UpdateController {

	private HabiTvSplashScreen taskBasedSplash;

	private Stage mainStage;

	private HabitvViewMain habitvViewMain;
	
	private static final Logger LOG = Logger.getLogger(UpdateController.class);

	public UpdateController(HabiTvSplashScreen taskBasedSplash) {
		this.taskBasedSplash = taskBasedSplash;
		this.mainStage = new Stage(StageStyle.DECORATED);
	}

	public void run(final Stage initStage) {
		mainStage.setIconified(true);
		RunHabitvTask runTask = new RunHabitvTask(mainStage);
		taskBasedSplash.showSplash(initStage, runTask, new InitHandler() {

			@Override
			public void onInitDone() {
				mainStage.setIconified(false);
			}
		});
		new Thread(runTask).start();
	}

	class RunHabitvTask extends Task<Void> implements CoreSubscriber {

		private Stage stage;
		private int updateSize;
		private int updateCount;

		public RunHabitvTask(Stage stage) {
			this.stage = stage;
		}

		@Override
		protected Void call() throws Exception {
			try {
				UserConfig userConfig = XMLUserConfig.initConfig();
				final HabitTvViewManager model = new HabitTvViewManager(
						userConfig);
				if (userConfig.updateOnStartup()) {
					model.attach((UpdateSubscriber) this);
					model.update();
				}
				habitvViewMain = new HabitvViewMain(model);
				model.attach((CoreSubscriber) this);
				model.buildGrabConfigIfNeeded();

				initHabiTv();

				return null;
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
				System.exit(1);
				throw e;
			}
		}

		private void initHabiTv() {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					try {
						habitvViewMain.run(stage);
					} catch (Throwable e) {
						LOG.error("", e);
						Popin.fatalError();
					}
				}

			});
		}

		@Override
		public void update(UpdatablePluginEvent event) {
			UpdatablePluginStateEnum state = event.getState();
			switch (state) {
			case STARTING_ALL:
				updateSize = Integer.parseInt(event.getInfo());
				updateCount = 0;
				updateProgress(updateCount, updateSize);
				updateMessage("Les plugins se mettent à jour...");
				break;
			case CHECKING:
				break;
			case DOWNLOADING:
				updateMessage("Le plugin " + event.getPlugin()
						+ " set met à jour en version " + event.getVersion()
						+ "...");
				break;
			case ERROR:
				break;
			case DONE:
				updateCount++;
				updateProgress(updateCount, updateSize);
				break;
			case ALL_DONE:
				updateMessage("Intialisation de l'application...");
				break;
			default:
				break;
			}
		}

		@Override
		public void update(UpdatePluginEvent event) {
			switch (event.getState()) {
			case STARTING_ALL:
				updateSize = Integer.parseInt(event.getInfo());
				updateCount = 0;
				updateProgress(updateCount, updateSize);
				updateMessage("Mise à jour des plugins...");
				break;
			case CHECKING:
				break;
			case DOWNLOADING:
				updateMessage("Mise à jour du plugin " + event.getPlugin()
						+ " en version " + event.getVersion() + "...");
				break;
			case ERROR:
				break;
			case DONE:
				updateCount++;
				updateProgress(updateCount, updateSize);
				// updateMessage(Messages.getString(
				// "HabiTvTrayView.majpluginfini", event.getPlugin(),
				// event.getVersion()));
				break;
			case ALL_DONE:
				break;
			default:
				break;
			}
		}

		@Override
		public void update(SearchEvent event) {
		}

		@Override
		public void update(RetreiveEvent event) {
		}

		@Override
		public void update(SearchCategoryEvent event) {
			switch (event.getState()) {
			case STARTING:
				updateSize = Integer.parseInt(event.getInfo());
				updateCount = 0;
				updateProgress(updateCount, updateSize);
				updateMessage("Chargement des catégories...");
				break;
			case BUILDING_CATEGORIES:
				break;
			case CATEGORIES_BUILT:
				updateCount++;
				updateProgress(updateCount, updateSize);
				updateMessage("Catégories du plugin " + event.getPlugin()
						+ " chargées");
				break;
			case ERROR:
				break;
			case ALL_DONE:
				updateMessage("Sauvegarde du fichier grabconfig contenant les catégories");
				break;
			default:
				break;
			}
		}
	}

}
