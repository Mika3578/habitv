package com.dabi.habitv.tray;

import com.dabi.habitv.utils.LogUtils;

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
		LogUtils.updateLog4jConfiguration();
		launch(args);
	}	

}