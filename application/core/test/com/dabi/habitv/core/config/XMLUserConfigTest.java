package com.dabi.habitv.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.configuration.entities.Configuration;

public class XMLUserConfigTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void embedSubtitlesDefaultsToFalseForNewConfig() throws Exception {
		final XMLUserConfig userConfig = newConfigInstance();
		assertFalse(userConfig.getEmbedSubtitles());
	}

	@Test
	public void maxConcurrentDownloadsDefaultsToOneWhenMissing() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), minimalConfigWithoutMaxConcurrent().getBytes(
				StandardCharsets.UTF_8));
		assertEquals(1, XMLUserConfig.readConfigForTest(file).getMaxConcurrentDownloads());
	}

	@Test
	public void maxConcurrentDownloadsReadsConfiguredValue() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:configuration xmlns:ns2=\"http://www.dabi.com/habitv/configuration/entities\">\n"
				+ "    <proxies/>\n"
				+ "    <osConfig/>\n"
				+ "    <downloadConfig>\n"
				+ "        <maxConcurrentDownloads>3</maxConcurrentDownloads>\n"
				+ "        <downloadOuput>/tmp/#EPISODE#.mp4</downloadOuput>\n"
				+ "    </downloadConfig>\n"
				+ "</ns2:configuration>\n").getBytes(StandardCharsets.UTF_8));
		assertEquals(3, XMLUserConfig.readConfigForTest(file).getMaxConcurrentDownloads());
	}

	@Test
	public void maxConcurrentDownloadsFallsBackWhenInvalid() {
		assertEquals(1, XMLUserConfig.resolveMaxConcurrentDownloads(Integer.valueOf(0)));
		assertEquals(1, XMLUserConfig.resolveMaxConcurrentDownloads(null));
	}

	@Test
	public void tf1PlusPremiumReplaySettingsAreReadFromDownloaders() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:configuration xmlns:ns2=\"http://www.dabi.com/habitv/configuration/entities\">\n"
				+ "    <downloadConfig>\n"
				+ "        <downloaders>\n"
				+ "            <tf1plusEmail>tf1@example.com</tf1plusEmail>\n"
				+ "            <tf1plusPassword>secret</tf1plusPassword>\n"
				+ "            <tf1plusDevicePath>C:/Tools/tf1plus/device.bin</tf1plusDevicePath>\n"
				+ "            <tf1plusNM3u8dlRe>C:/Tools/N_m3u8DL-RE.exe</tf1plusNM3u8dlRe>\n"
				+ "        </downloaders>\n"
				+ "        <downloadOuput>/tmp/#EPISODE#.mp4</downloadOuput>\n"
				+ "    </downloadConfig>\n"
				+ "</ns2:configuration>\n").getBytes(StandardCharsets.UTF_8));
		final Tf1PlusPremiumReplaySettings settings = XMLUserConfig.readConfigForTest(file)
				.getTf1PlusPremiumReplaySettings();
		assertEquals("tf1@example.com", settings.getEmail());
		assertEquals("secret", settings.getPassword());
		assertEquals("C:/Tools/tf1plus/device.bin", settings.getDevicePath());
		assertEquals("C:/Tools/N_m3u8DL-RE.exe", settings.getNM3u8DlRePath());
	}

	@Test
	public void tf1PlusCredentialsRoundTripThroughSetters() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), minimalConfigWithoutMaxConcurrent().getBytes(StandardCharsets.UTF_8));
		final XMLUserConfig config = XMLUserConfig.readConfigForTest(file);
		config.setTf1PlusEmail("user@tf1.example");
		config.setTf1PlusPassword("secret-pass");
		XMLUserConfig.saveConfig(file, config);
		final XMLUserConfig reloaded = XMLUserConfig.readConfigForTest(file);
		assertEquals("user@tf1.example", reloaded.getTf1PlusEmail());
		assertEquals("secret-pass", reloaded.getTf1PlusPassword());
	}

	@Test
	public void tf1PlusDevicePathAndDownloaderRoundTripThroughSetters() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), minimalConfigWithoutMaxConcurrent().getBytes(StandardCharsets.UTF_8));
		final XMLUserConfig config = XMLUserConfig.readConfigForTest(file);
		config.setTf1PlusDevicePath("C:/Tools/tf1plus/device.bin");
		config.setTf1PlusNM3u8dlRe("C:/Tools/N_m3u8DL-RE.exe");
		XMLUserConfig.saveConfig(file, config);
		final XMLUserConfig reloaded = XMLUserConfig.readConfigForTest(file);
		assertEquals("C:/Tools/tf1plus/device.bin", reloaded.getTf1PlusDevicePath());
		assertEquals("C:/Tools/N_m3u8DL-RE.exe", reloaded.getTf1PlusNM3u8dlRe());
	}

	@Test
	public void youtubeApiKeyIsNotPrefixedWithAppDirectory() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		final String fakeKey = ConfigTestValues.YOUTUBE_API_KEY_PLAIN;
		Files.write(file.toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:configuration xmlns:ns2=\"http://www.dabi.com/habitv/configuration/entities\">\n"
				+ "    <proxies/>\n"
				+ "    <osConfig/>\n"
				+ "    <downloadConfig>\n"
				+ "        <downloaders>\n"
				+ "            <youtubeApiKey>" + fakeKey + "</youtubeApiKey>\n"
				+ "        </downloaders>\n"
				+ "        <downloadOuput>/tmp/#EPISODE#.mp4</downloadOuput>\n"
				+ "    </downloadConfig>\n"
				+ "</ns2:configuration>\n").getBytes(StandardCharsets.UTF_8));
		assertEquals(fakeKey, XMLUserConfig.readConfigForTest(file).getYoutubeApiKey());
	}

	@Test
	public void maxConcurrentDownloadsRoundTrip() throws Exception {
		final File file = File.createTempFile("habitv-config-", ".xml");
		file.deleteOnExit();
		Files.write(file.toPath(), minimalConfigWithoutMaxConcurrent().getBytes(
				StandardCharsets.UTF_8));
		final XMLUserConfig config = XMLUserConfig.readConfigForTest(file);
		config.setMaxConcurrentDownloads(2);
		XMLUserConfig.saveConfig(file, config);
		assertEquals(2, XMLUserConfig.readConfigForTest(file).getMaxConcurrentDownloads());
	}

	private static XMLUserConfig newConfigInstance() throws Exception {
		final Method buildDefaultConfig = XMLUserConfig.class.getDeclaredMethod(
				"buildDefaultConfig");
		buildDefaultConfig.setAccessible(true);
		final Configuration configuration = (Configuration) buildDefaultConfig
				.invoke(null);
		final Constructor<XMLUserConfig> constructor = XMLUserConfig.class
				.getDeclaredConstructor(Configuration.class);
		constructor.setAccessible(true);
		return constructor.newInstance(configuration);
	}

	private static String minimalConfigWithoutMaxConcurrent() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:configuration xmlns:ns2=\"http://www.dabi.com/habitv/configuration/entities\">\n"
				+ "    <proxies/>\n"
				+ "    <osConfig/>\n"
				+ "    <downloadConfig>\n"
				+ "        <downloadOuput>/tmp/#EPISODE#.mp4</downloadOuput>\n"
				+ "    </downloadConfig>\n"
				+ "</ns2:configuration>\n";
	}

	@Test
	@Ignore
	public void testInitConfig() throws IOException {
		File configFile = new File("config.xml");
		configFile.delete();
		Files.copy(new File("testOldConfig.xml").toPath(), configFile.toPath());
		UserConfig config = XMLUserConfig.initConfig();
		assertNotNull(config);
	}

}
