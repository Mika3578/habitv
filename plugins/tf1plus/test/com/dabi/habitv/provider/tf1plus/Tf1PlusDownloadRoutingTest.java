package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.framework.plugin.utils.DownloadFailureDiagnostics;

public class Tf1PlusDownloadRoutingTest {

	@Test
	public void canDownloadUsesParsedHostOnly() {
		final Tf1PlusPluginManager plugin = new Tf1PlusPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://www.tf1.fr/tf1/replay"));
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload("https://tf1.fr/tmc/replay"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://example.com/video"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://evil.com/www.tf1.fr/tf1/replay"));
	}

	@Test
	public void configuredFragmentFailsAsProtectedContent() {
		final Tf1PlusPluginManager plugin = new Tf1PlusPluginManager();
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.tf1.fr/tf1/automoto/videos/automoto-du-31-mai-2026.html#habitvTf1=14510494,premium",
				"out.mp4", Tf1PlusConf.EXTENSION);
		try {
			plugin.download(param, null);
			fail("expected protected-content failure");
		} catch (final DownloadFailedException e) {
			assertEquals(DownloadFailureDiagnostics.CLASSIFICATION_PROTECTED_CONTENT,
					DownloadFailureDiagnostics.getClassificationKey(e));
			assertEquals(Tf1PlusConf.USER_MESSAGE_PROTECTED_CONTENT, e.getCause().getMessage());
		}
	}
}
