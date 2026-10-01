package com.dabi.habitv.plugin.ffmpeg;

import com.dabi.habitv.framework.FrameworkConf;

public final class FFMPEGConf {

	private FFMPEGConf() {

	}

	public static final String NAME = "ffmpeg";

	public static final long MAX_HUNG_TIME = 100000L;

	/** Machine-readable progress on stdout ({@code -progress pipe:1 -nostats}). */
	public static final String PROGRESS_FLAGS = " -progress pipe:1 -nostats ";

	public static final String FFMPEG_CMD_LINUX = PROGRESS_FLAGS + " -i \""+FrameworkConf.DOWNLOAD_INPUT+"\" -c copy -y -f "+FrameworkConf.EXTENSION+" \"" + FrameworkConf.DOWNLOAD_DESTINATION + "\" ";

	public static final String FFMPEG_CMD_WINDOWS_COR_OLD = PROGRESS_FLAGS + " -i \""+FrameworkConf.DOWNLOAD_INPUT+"\" -c copy -aprofile aac_low -acodec libvo_aacenc -vbsf aac_adtstoasc -y -f "+FrameworkConf.EXTENSION+" \"" + FrameworkConf.DOWNLOAD_DESTINATION + "\" ";

	public static final String FFMPEG_CMD_WINDOWS_COR = PROGRESS_FLAGS + " -i \""+FrameworkConf.DOWNLOAD_INPUT+"\" -c copy -bsf:a aac_adtstoasc -y -f "+FrameworkConf.EXTENSION+" \"" + FrameworkConf.DOWNLOAD_DESTINATION + "\" ";

	public static final String DEFAULT_LINUX_BIN_PATH = "avconv";

}
