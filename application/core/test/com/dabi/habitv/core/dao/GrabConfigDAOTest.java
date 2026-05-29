package com.dabi.habitv.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.StatusEnum;
import com.dabi.habitv.core.dao.GrabConfigDAO.LoadModeEnum;

public class GrabConfigDAOTest {

	private GrabConfigDAO dao;

	private static final String XML_FILE = "testGrabConfig.xml";

	private static final String OLD_XML_FILE = "testOldGrabconfig.xml";

	private static final String STALE_FRANCETV_FIXTURE = "test/com/dabi/habitv/core/dao/fixtures/grabconfig-francetv-stale-public.xml";

	private static final String FRANCETV_PLUGIN = "francetv";

	private static final String PUBLIC_ROOT_NAME = "Pages publiques (france.tv)";

	private static final List<String> EXPECTED_PUBLIC_HUB_ORDER = Arrays.asList("Sport", "Franceinfo", "Arte",
			"TV5 Monde Plus", "France 24", "INA", "LCP", "Public Sénat", "Mieux");

	private static final List<String> STALE_PUBLIC_HUB_ORDER = Arrays.asList("Sport", "Franceinfo", "TV5 Monde Plus",
			"France 24", "Public Sénat", "Mieux", "LCP", "Arte", "INA");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		(new File(XML_FILE)).delete();
		dao = new GrabConfigDAO(XML_FILE);
	}

	@After
	public void tearDown() throws Exception {
		// (new File(XML_FILE)).delete();
	}

	@Test
	@Ignore
	public final void testLoadOld() throws IOException {
		File file = new File(XML_FILE);
		Files.copy(new File(OLD_XML_FILE).toPath(), file.toPath());
		final Map<String, CategoryDTO> channel2CategoriesTotest = dao
				.load(LoadModeEnum.ALL);
		assertNotNull(channel2CategoriesTotest);
	}

	@Test
	public final void testSaveAndLoadGrabConfig() {
		final Map<String, CategoryDTO> channel2Categories = buildChannelMap(
				true, false);
		assertFalse(dao.exist());
		dao.saveGrabConfig(channel2Categories);
		assertTrue((new File(XML_FILE)).exists());
		final Map<String, CategoryDTO> channel2CategoriesTotest = dao
				.load(LoadModeEnum.ALL);
		assertEquals(channel2Categories, channel2CategoriesTotest);
	}

	@Test
	public final void testUpdateGrabConfig() {
		Map<String, CategoryDTO> channel2Categories = buildChannelMap(true,
				false);
		assertFalse(dao.exist());
		dao.saveGrabConfig(channel2Categories);
		assertTrue((new File(XML_FILE)).exists());
		channel2Categories = buildChannelMap(false, true);
		dao.updateGrabConfig(channel2Categories);
		final Map<String, CategoryDTO> channel2CategoriesTotest = dao
				.load(LoadModeEnum.ALL);
		final CategoryDTO category = channel2CategoriesTotest.get("channel1")
				.getSubCategories().iterator().next();
		assertEquals(category.getExclude().iterator().next(), "exc1");
		assertEquals(category.getInclude().iterator().next(), "inc1");
		assertTrue(category.getSubCategories().iterator().next().getName().equals("sub"));
	}

	@Test
	public final void updateGrabConfigReordersPublicHubsAndSyncsPartnerChildren() throws IOException {
		Files.copy(new File(STALE_FRANCETV_FIXTURE).toPath(), new File(XML_FILE).toPath(),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		assertTrue(dao.exist());

		final Map<String, CategoryDTO> loadedBefore = dao.load(LoadModeEnum.ALL);
		final CategoryDTO stalePublicRoot = findChild(loadedBefore.get(FRANCETV_PLUGIN), PUBLIC_ROOT_NAME);
		assertNotNull(stalePublicRoot);
		assertEquals(STALE_PUBLIC_HUB_ORDER, categoryNames(stalePublicRoot.getSubCategories()));
		assertTrue(findChild(stalePublicRoot, "INA").getSubCategories().isEmpty());

		dao.updateGrabConfig(buildFreshFrancetvPluginTree());

		final Map<String, CategoryDTO> loadedAfter = dao.load(LoadModeEnum.ALL);
		final CategoryDTO mergedPublicRoot = findChild(loadedAfter.get(FRANCETV_PLUGIN), PUBLIC_ROOT_NAME);
		assertNotNull(mergedPublicRoot);
		assertEquals(EXPECTED_PUBLIC_HUB_ORDER, categoryNames(mergedPublicRoot.getSubCategories()));

		assertEquals("Roland-Garros", findChild(findChild(mergedPublicRoot, "Sport"), "Roland-Garros").getName());
		assertEquals("L'info s'éclaire",
				findChild(findChild(mergedPublicRoot, "Franceinfo"), "L'info s'éclaire").getName());
		assertEquals("Arte Journal", findChild(findChild(mergedPublicRoot, "Arte"), "Arte Journal").getName());
		assertEquals("Le journal de TV5MONDE",
				findChild(findChild(mergedPublicRoot, "TV5 Monde Plus"), "Le journal de TV5MONDE").getName());
		assertEquals("L'invité de l'économie",
				findChild(findChild(mergedPublicRoot, "France 24"), "L'invité de l'économie").getName());
		assertEquals("L'INA éclaire l'actu",
				findChild(findChild(mergedPublicRoot, "INA"), "L'INA éclaire l'actu").getName());
		assertEquals("Questions au Gouvernement",
				findChild(findChild(mergedPublicRoot, "LCP"), "Questions au Gouvernement").getName());
		assertEquals("Public Sénat L'invité",
				findChild(findChild(mergedPublicRoot, "Public Sénat"), "Public Sénat L'invité").getName());
		assertEquals("Mieux consommer",
				findChild(findChild(mergedPublicRoot, "Mieux"), "Mieux consommer").getName());

		final CategoryDTO inaHub = findChild(mergedPublicRoot, "INA");
		assertFalse(inaHub.isDownloadable());
		assertFalse(inaHub.getSubCategories().isEmpty());
		assertEquals("5", inaHub.getParameter(GrabConfigDAO.CATEGORY_ORDER_PARAMETER));

		dao.load(LoadModeEnum.ALL);
	}

	@Test
	public final void updateGrabConfigSkipsCategoriesWithoutIdAndRemainsSchemaValid() throws IOException {
		Files.copy(new File(STALE_FRANCETV_FIXTURE).toPath(), new File(XML_FILE).toPath(),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING);

		final Map<String, CategoryDTO> pluginTree = buildFreshFrancetvPluginTree();
		final CategoryDTO sportHub = findChild(
				findChild(pluginTree.get(FRANCETV_PLUGIN), PUBLIC_ROOT_NAME), "Sport");
		sportHub.addSubCategory(new CategoryDTO(FRANCETV_PLUGIN, "En immersion", "", "mp4"));

		dao.updateGrabConfig(pluginTree);
		dao.load(LoadModeEnum.ALL);

		final Map<String, CategoryDTO> loadedAfter = dao.load(LoadModeEnum.ALL);
		final CategoryDTO sportAfter = findChild(
				findChild(loadedAfter.get(FRANCETV_PLUGIN), PUBLIC_ROOT_NAME), "Sport");
		for (final CategoryDTO child : sportAfter.getSubCategories()) {
			assertTrue(child.getId() != null && !child.getId().isEmpty());
		}
	}

	private Map<String, CategoryDTO> buildFreshFrancetvPluginTree() {
		final Map<String, CategoryDTO> channel2Categories = new HashMap<>();
		final Set<CategoryDTO> pluginCategories = new LinkedHashSet<>();
		pluginCategories.add(buildFreshPublicRootCategory());
		channel2Categories.put(FRANCETV_PLUGIN, new CategoryDTO(FRANCETV_PLUGIN, pluginCategories));
		return channel2Categories;
	}

	private CategoryDTO buildFreshPublicRootCategory() {
		final CategoryDTO publicRoot = new CategoryDTO(FRANCETV_PLUGIN, PUBLIC_ROOT_NAME,
				"https://www.france.tv/", "mp4");
		publicRoot.setDownloadable(false);
		int order = 0;
		publicRoot.addSubCategory(buildPublicHub("Sport", "https://www.france.tv/sport/", order++,
				downloadableChild("Roland-Garros", "https://www.france.tv/sport/tennis/roland-garros/")));
		publicRoot.addSubCategory(buildPublicHub("Franceinfo", "https://www.france.tv/franceinfo/", order++,
				downloadableChild("L'info s'éclaire", "https://www.france.tv/franceinfo/l-info-s-eclaire/")));
		publicRoot.addSubCategory(buildPublicHub("Arte", "https://www.france.tv/arte/", order++,
				downloadableChild("Arte Journal", "https://www.france.tv/arte/arte-journal/")));
		publicRoot.addSubCategory(buildPublicHub("TV5 Monde Plus", "https://www.france.tv/tv5-monde/", order++,
				downloadableChild("Le journal de TV5MONDE", "https://www.france.tv/tv5-monde/le-journal-de-tv5monde/")));
		publicRoot.addSubCategory(buildPublicHub("France 24", "https://www.france.tv/france-24/", order++,
				downloadableChild("L'invité de l'économie", "https://www.france.tv/france-24/l-invite-de-l-economie/")));
		publicRoot.addSubCategory(buildPublicHub("INA", "https://www.france.tv/ina/", order++,
				downloadableChild("L'INA éclaire l'actu", "https://www.france.tv/ina/l-ina-eclaire-l-actu/")));
		publicRoot.addSubCategory(buildPublicHub("LCP", "https://www.france.tv/lcp/", order++,
				downloadableChild("Questions au Gouvernement", "https://www.france.tv/lcp/questions-au-gouvernement/")));
		publicRoot.addSubCategory(buildPublicHub("Public Sénat", "https://www.france.tv/public-senat/", order++,
				downloadableChild("Public Sénat L'invité", "https://www.france.tv/public-senat/public-senat-l-invite/")));
		publicRoot.addSubCategory(buildPublicHub("Mieux", "https://www.france.tv/mieux/", order++,
				downloadableChild("Mieux consommer", "https://www.france.tv/mieux/mieux-consommer/")));
		return publicRoot;
	}

	private static CategoryDTO buildPublicHub(final String name, final String url, final int order,
			final CategoryDTO... children) {
		final CategoryDTO hub = new CategoryDTO(FRANCETV_PLUGIN, name, url, "mp4");
		hub.setDownloadable(false);
		hub.addParameter(GrabConfigDAO.CATEGORY_ORDER_PARAMETER, String.valueOf(order));
		for (final CategoryDTO child : children) {
			hub.addSubCategory(child);
		}
		return hub;
	}

	private static CategoryDTO downloadableChild(final String name, final String url) {
		final CategoryDTO child = new CategoryDTO(FRANCETV_PLUGIN, name, url, "mp4");
		child.setDownloadable(true);
		return child;
	}

	private static CategoryDTO findChild(final CategoryDTO parent, final String name) {
		for (final CategoryDTO child : parent.getSubCategories()) {
			if (name.equals(child.getName())) {
				return child;
			}
		}
		throw new AssertionError("Missing child category: " + name);
	}

	private static List<String> categoryNames(final Set<CategoryDTO> categories) {
		final List<String> names = new java.util.ArrayList<>();
		for (final CategoryDTO category : categories) {
			names.add(category.getName());
		}
		return names;
	}

	private Map<String, CategoryDTO> buildChannelMap(final boolean inc,
			final boolean sup) {
		final Map<String, CategoryDTO> channel2Categories = new HashMap<>();
		Set<CategoryDTO> categories = new LinkedHashSet<>();
		List<String> includeList = null;
		List<String> excludeList = null;
		if (inc) {
			includeList = Arrays.asList(new String[] { "inc1", "inc2" });
			excludeList = Arrays.asList(new String[] { "exc1", "exc2" });
		}
		CategoryDTO category = new CategoryDTO("channel1", "cat1", "cat1I",
				includeList, excludeList, "ext");
		category.setState(StatusEnum.EXIST);
		CategoryDTO subCategory = new CategoryDTO("sub", "sub", "sub", "sub");
		subCategory.setState(StatusEnum.EXIST);
		category.addSubCategory(subCategory);
		categories.add(category);
		category = new CategoryDTO("channel1", "cat2", "cat2I", includeList,
				excludeList, "ext2");
		if (sup) {
			category.addSubCategory(new CategoryDTO("sub2", "sub2", "sub2",
					"sub2"));
		}
		categories.add(category);
		channel2Categories.put("channel1", new CategoryDTO("channel1",
				categories));
		categories = new LinkedHashSet<>();
		category = new CategoryDTO("channel2", "cat3", "cat3I", includeList,
				excludeList, "ext");
		categories.add(category);
		channel2Categories.put("channel2", new CategoryDTO("channel2",
				categories));
		return channel2Categories;
	}
}
