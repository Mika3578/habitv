package com.dabi.habitv.core.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamingProfileTest {

	@Test
	public void exactMediaServerTokenSelectsMediaServer() {
		assertEquals(NamingProfile.MEDIA_SERVER,
				NamingProfile.fromDownloadOutputTemplate(
						"{user.home}/Downloads/#MEDIA_SERVER_PATH#"));
	}

	@Test
	public void parameterizedMediaServerTokenSelectsMediaServer() {
		assertEquals(NamingProfile.MEDIA_SERVER,
				NamingProfile.fromDownloadOutputTemplate(
						"D:/media/#MEDIA_SERVER_PATH§40#"));
	}

	@Test
	public void cutMediaServerTokenSelectsMediaServer() {
		assertEquals(NamingProfile.MEDIA_SERVER,
				NamingProfile.fromDownloadOutputTemplate(
						"D:/media/#MEDIA_SERVER_PATH_CUT#"));
	}

	@Test
	public void cutWithParameterStillSelectsMediaServer() {
		assertEquals(NamingProfile.MEDIA_SERVER,
				NamingProfile.fromDownloadOutputTemplate(
						"D:/media/#MEDIA_SERVER_PATH_CUT§20#"));
	}

	@Test
	public void bareMediaServerTextWithoutTokenDelimitersStaysLegacy() {
		assertEquals(NamingProfile.LEGACY,
				NamingProfile.fromDownloadOutputTemplate(
						"D:/media/MEDIA_SERVER_PATH/show.mp4"));
		assertEquals(NamingProfile.LEGACY,
				NamingProfile.fromDownloadOutputTemplate(
						"path containing MEDIA_SERVER_PATH text"));
		assertEquals(NamingProfile.LEGACY,
				NamingProfile.fromDownloadOutputTemplate(
						"#MEDIA_SERVER_PATH"));
	}

	@Test
	public void nullTemplateIsLegacy() {
		assertEquals(NamingProfile.LEGACY, NamingProfile.fromDownloadOutputTemplate(null));
	}

	@Test
	public void legacyTemplatesStayLegacy() {
		assertEquals(NamingProfile.LEGACY,
				NamingProfile.fromDownloadOutputTemplate(
						"#TVSHOW_NAME#/#EPISODE_NAME_CUT#.#EXTENSION#"));
		assertEquals(NamingProfile.LEGACY,
				NamingProfile.fromDownloadOutputTemplate(""));
	}
}
