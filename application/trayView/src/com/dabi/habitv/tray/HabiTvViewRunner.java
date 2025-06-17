package com.dabi.habitv.tray;

import org.apache.log4j.PropertyConfigurator;

import com.dabi.habitv.api.logging.HabitvLogger;

import javafx.application.Application;
import javafx.stage.Stage;

public class HabiTvViewRunner extends Application {

	private HabiTvSplashScreen habiTvSplashScreen;

	public HabiTvViewRunner() {
		this.habiTvSplashScreen = new HabiTvSplashScreen();
	}

	@Override
	public void start(final Stage initStage) throws Exception {
		habiTvSplashScreen.start(initStage);		
	}

	public static void main(String[] args) {
		// Initialize unified logging system early
		// This will load log4j.properties from the classpath
		HabitvLogger.initialize();

		launch(args);
	}	

}
