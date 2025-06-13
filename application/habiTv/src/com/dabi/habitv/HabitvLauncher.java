package com.dabi.habitv;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.LogUtils;

public class HabitvLauncher {

	public static void main(final String[] args) throws Exception {
		LogUtils.updateLog4jConfiguration();
		
		if (args == null || args.length == 0) {
			HabiTvViewRunner.main(args);
		} else {
			ConsoleLauncher.main(args);
		}
	}

}
