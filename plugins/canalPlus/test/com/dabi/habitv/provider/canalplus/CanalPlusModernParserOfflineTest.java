package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.provider.canalplus.CanalPlusHodorParser.CanalPlusUnitMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CanalPlusModernParserOfflineTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void parsesDetailPageUrlFromReactQueryHtmlFixture() throws IOException {
		final String html = readFixture("page-detail-react-query.html");
		final String detailUrl = CanalPlusPageDataParser.extractDetailPageUrl(html);
		assertNotNull(detailUrl);
		assertTrue(detailUrl.contains("hodor.canalplus.pro"));
		assertTrue(detailUrl.contains("31338503_50017"));
	}

	@Test
	public void parsesHodorUnitMetadataFixture() throws IOException {
		@SuppressWarnings("unchecked")
		final Map<String, Object> root = MAPPER.readValue(readFixture("hodor-detail-unit.json"), Map.class);
		final CanalPlusUnitMetadata metadata = CanalPlusHodorParser.parseUnitDetail(root);
		assertNotNull(metadata);
		assertEquals("31338503_50017", metadata.getContentId());
		assertEquals("Les 10 hôtels les plus incroyables de France", metadata.getDisplayName());
	}

	@Test
	public void selectsPlayreadyDownloadItemFromPlaysetFixture() throws IOException {
		@SuppressWarnings("unchecked")
		final Map<String, Object> root = MAPPER.readValue(readFixture("playset-unit-hd.json"), Map.class);
		final CanalPlusPlaysetParser.CanalPlusPlaysetItem item = CanalPlusPlaysetParser.selectDownloadItem(root);
		assertNotNull(item);
		assertEquals("31338503_50017", item.getContentId());
		assertEquals("DRM_MKPC_PLAYREADY_DASH_DOWNLOAD", item.getDrmType());
		assertEquals("HD", item.getQuality());
		assertEquals("d17_1287846_1_D17", item.getIdKey());

		final Map<String, Object> viewBody = CanalPlusPlaysetParser.buildViewRequestBody(item);
		assertEquals("CATCHUP_NOLIMIT", viewBody.get("comMode"));
		assertEquals("DOWNLOAD", viewBody.get("distTechnology"));
	}

	private String readFixture(final String name) throws IOException {
		final String path = "test/resources/fixtures/canalplus/" + name;
		assertTrue("missing fixture " + path, new File(path).exists());
		try (InputStream input = new FileInputStream(path)) {
			return readUtf8(input);
		}
	}

	private String readUtf8(InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}

}
