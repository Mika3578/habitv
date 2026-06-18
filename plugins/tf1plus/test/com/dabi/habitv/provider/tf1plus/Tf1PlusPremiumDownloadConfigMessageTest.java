package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class Tf1PlusPremiumDownloadConfigMessageTest {

	private String previousEmail;

	private String previousPassword;

	@Before
	public void setUp() {
		assumeNoTf1EnvironmentVariables();
		previousEmail = System.getProperty(Tf1PlusConf.PROPERTY_EMAIL);
		previousPassword = System.getProperty(Tf1PlusConf.PROPERTY_PASSWORD);
		System.clearProperty(Tf1PlusConf.PROPERTY_EMAIL);
		System.clearProperty(Tf1PlusConf.PROPERTY_PASSWORD);
	}

	@After
	public void tearDown() {
		restoreProperty(Tf1PlusConf.PROPERTY_EMAIL, previousEmail);
		restoreProperty(Tf1PlusConf.PROPERTY_PASSWORD, previousPassword);
	}

	@Test
	public void shouldAskForCredentialsWhenEmailOrPasswordMissing() {
		assertFalse(Tf1PlusPremiumDownloadConfig.hasCredentials());
		assertEquals(Tf1PlusConf.USER_MESSAGE_TF1_CREDENTIALS_REQUIRED,
				Tf1PlusPremiumDownloadConfig.userFacingConfigurationMessage());

		System.setProperty(Tf1PlusConf.PROPERTY_EMAIL, "user@example.com");
		assertFalse(Tf1PlusPremiumDownloadConfig.hasCredentials());
		assertEquals(Tf1PlusConf.USER_MESSAGE_TF1_CREDENTIALS_REQUIRED,
				Tf1PlusPremiumDownloadConfig.userFacingConfigurationMessage());
	}

	@Test
	public void shouldReportDevicePathRequiredWhenCredentialsPresentOnly() {
		System.setProperty(Tf1PlusConf.PROPERTY_EMAIL, "user@example.com");
		System.setProperty(Tf1PlusConf.PROPERTY_PASSWORD, "secret");
		assertTrue(Tf1PlusPremiumDownloadConfig.hasCredentials());
		assertFalse(Tf1PlusPremiumDownloadConfig.isConfigured());
		assertEquals(Tf1PlusConf.USER_MESSAGE_TF1_DEVICE_PATH_REQUIRED,
				Tf1PlusPremiumDownloadConfig.userFacingConfigurationMessage());
	}

	private static void assumeNoTf1EnvironmentVariables() {
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_TF1_EMAIL)));
		Assume.assumeTrue(isBlank(System.getenv(Tf1PlusConf.ENV_TF1_PASSWORD)));
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
}
