package com.dabi.habitv.core.dao;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class ExportDAOTest {

	ExportDAO dao = new ExportDAO(".");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	//	@Test
	//	public final void testA() {
	//		Collection<EpisodeExportState> exportStep = dao.loadExportStep();
	//		assertTrue(exportStep.isEmpty());
	//	}

	@Test
	public final void testSaveAndLoadExportStep() {
		final CategoryDTO category = new CategoryDTO("channel", "name", "id", "ext");
		final EpisodeDTO episode1 = new EpisodeDTO(category, "episode1", "url1");
		final EpisodeDTO episode2 = new EpisodeDTO(category, "episode2", "url2");
		dao.init();
		final EpisodeExportState episodeExportState1 = new EpisodeExportState(episode1, 3);
		dao.addExportStep(episodeExportState1);
		final EpisodeExportState episodeExportState2 = new EpisodeExportState(episode2, 5);
		dao.addExportStep(episodeExportState2);
		final Collection<EpisodeExportState> exportStep = dao.loadExportStep();
		assertTrue(exportStep.contains(episodeExportState1));
		assertTrue(exportStep.contains(episodeExportState2));
	}
}
