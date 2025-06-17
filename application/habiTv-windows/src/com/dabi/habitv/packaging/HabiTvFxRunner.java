package com.dabi.habitv.packaging;

import java.awt.SystemTray;

import javafx.application.Application;
import javafx.stage.Stage;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvSplashScreen;
import com.dabi.habitv.api.logging.LogUtils;

public class HabiTvFxRunner extends Application {

	private HabiTvSplashScreen habiTvSplashScreen;

	public HabiTvFxRunner() {
		this.habiTvSplashScreen = new HabiTvSplashScreen();
	}

	@Override
	public void start(final Stage initStage) throws Exception {
		if (SystemTray.isSupported() && (getParameters().getRaw().isEmpty())) {
			habiTvSplashScreen.start(initStage);
		} else {
			ConsoleLauncher.main(getParameters().getRaw().toArray(new String[getParameters().getRaw().size()]));
		}

	}

	public static void main(String[] args) {
		LogUtils.updateLog4jConfiguration();
		launch(args);
	}	

}
