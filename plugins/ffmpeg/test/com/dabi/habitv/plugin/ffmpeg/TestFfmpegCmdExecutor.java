package com.dabi.habitv.plugin.ffmpeg;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

@Ignore
public class TestFfmpegCmdExecutor {

	private class FakeCurlCmdExecutor extends FFMPEGCmdExecutor {

		int ret;

		public FakeCurlCmdExecutor(final int ret, final String cmd) {
			super(null, cmd);
			this.ret = ret;
		}

		@Override
		protected Process buildProcess() throws ExecutorFailedException {
			final String inputString = "% Total    % Received % Xferd  Average Speed   Time    Time     Time  Current"
					+ "                                 Dload  Upload   Total   Spent    Left  Speed\n"
					// +
					// "100 18.6M    12.5     0  100 18.6M      0   928k  0:00:20  0:00:20 --:--:--  952k";
					+ "  7.5 26.4M    0     0    7 2096k      0  1049k  0:00:25  0:00:01  0:00:24 1066k\n"
					+ "100 33.9M    12.5     0    1  368k      0   502k  0:01:09 --:--:--  0:01:09  535k";
			// 1 33.9M 0 0 1 368k 0 502k 0:01:09 --:--:-- 0:01:09 535k
			return new FakeFfmpegProcess(ret, inputString);
		}

	}

	@Test
	public void displayProgressionAndEndProperly()
			throws ExecutorFailedException {
		final FFMPEGCmdExecutor curlCmdExecutor = new FakeCurlCmdExecutor(0, "");
		ProcessHolder process = curlCmdExecutor;
		process.start();
		Assert.assertEquals("100", process.getProgression());
	}

	@Test
	public void displayProgressionAndEndWithError() {
		final FFMPEGCmdExecutor curlCmdExecutor = new FakeCurlCmdExecutor(1, "");
		try {
			curlCmdExecutor.start();
			Assert.fail("must be in error");
		} catch (final ExecutorFailedException e) {
		}
	}

}
