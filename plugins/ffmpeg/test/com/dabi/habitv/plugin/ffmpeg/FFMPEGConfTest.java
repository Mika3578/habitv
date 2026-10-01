package com.dabi.habitv.plugin.ffmpeg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FFMPEGConfTest {

	@Test
	public void linuxAvconvDefaultOmitsProgressFlags() {
		assertFalse(FFMPEGConf.FFMPEG_CMD_LINUX.contains("-progress"));
		assertFalse(FFMPEGConf.supportsProgressPipe("/usr/bin/avconv"));
	}

	@Test
	public void ffmpegBinaryGetsProgressFlags() {
		assertTrue(FFMPEGConf.supportsProgressPipe("/usr/bin/ffmpeg"));
		assertTrue(FFMPEGConf.augmentRemuxCommand(FFMPEGConf.FFMPEG_CMD_LINUX, "/usr/bin/ffmpeg")
				.contains("-progress pipe:1"));
	}

}
