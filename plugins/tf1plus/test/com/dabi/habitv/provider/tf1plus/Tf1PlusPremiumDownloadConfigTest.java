package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class Tf1PlusPremiumDownloadConfigTest {

	private String previousEmail;

	private String previousPassword;

	private String previousDevice;

	private String previousDownloader;

	@Before
	public void setUp() {
		assumeNoTf1EnvironmentVariables();
		previousEmail = System.getProperty(Tf1PlusConf.PROPERTY_EMAIL);
		previousPassword = System.getProperty(Tf1PlusConf.PROPERTY_PASSWORD);
		previousDevice = System.getProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH);
		previousDownloader = System.getProperty(Tf1PlusConf.PROPERTY_N_M3U8DL_RE);
	}

	@After
	public void tearDown() {
		restoreProperty(Tf1PlusConf.PROPERTY_EMAIL, previousEmail);
		restoreProperty(Tf1PlusConf.PROPERTY_PASSWORD, previousPassword);
		restoreProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH, previousDevice);
		restoreProperty(Tf1PlusConf.PROPERTY_N_M3U8DL_RE, previousDownloader);
	}

	@Test
	public void shouldLoadCredentialsFromConfigurationXmlSystemProperties() throws Exception {
		final File device = File.createTempFile("tf1plus-test", ".bin");
		final File downloader = File.createTempFile("tf1plus-test", ".exe");

		System.setProperty(Tf1PlusConf.PROPERTY_EMAIL, "user@example.com");
		System.setProperty(Tf1PlusConf.PROPERTY_PASSWORD, "secret");
		System.setProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH, device.getAbsolutePath());
		System.setProperty(Tf1PlusConf.PROPERTY_N_M3U8DL_RE, downloader.getAbsolutePath());

		assertTrue(Tf1PlusPremiumDownloadConfig.isConfigured());
		assertTrue(Tf1PlusPremiumDownloadConfig.configurationStatusForLog()
				.contains("tf1-email-source=configuration-xml"));
		assertEquals("user@example.com",
				Tf1PlusPremiumDownloadConfig.buildProcessEnvironmentOverrides().get(Tf1PlusConf.ENV_TF1_EMAIL));
		assertEquals(device.getAbsolutePath(),
				Tf1PlusPremiumDownloadConfig.buildProcessEnvironmentOverrides().get(Tf1PlusConf.ENV_DEVICE_PATH));

		device.delete();
		downloader.delete();
	}

	@Test
	public void shouldReportNotConfiguredWhenSystemPropertiesAreMissing() {
		clearProperty(Tf1PlusConf.PROPERTY_EMAIL, previousEmail);
		clearProperty(Tf1PlusConf.PROPERTY_PASSWORD, previousPassword);
		clearProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH, previousDevice);
		clearProperty(Tf1PlusConf.PROPERTY_N_M3U8DL_RE, previousDownloader);
		assertFalse(Tf1PlusPremiumDownloadConfig.isConfigured());
	}

	private static void assumeNoTf1EnvironmentVariables() {
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_TF1_EMAIL)));
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_TF1_PASSWORD)));
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_DEVICE_PATH)));
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_WVD_PATH)));
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_N_M3U8DL_RE)));
	}

	private static boolean isBlank(final String value) {
		return value == null || value.trim().isEmpty();
	}

	private static void restoreProperty(final String name, final String previous) {
		if (previous == null) {
			System.clearProperty(name);
		} else {
			System.setProperty(name, previous);
		}
	}

	private static void clearProperty(final String name, final String previous) {
		if (previous == null) {
			System.clearProperty(name);
		}
	}
}
