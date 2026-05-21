/**
 * 
 */
package com.dabi.habitv.core.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * @author bidou
 * 
 */
public class DownloadedDAOTest {

	private DownloadedDAO dao;
	private CategoryDTO category;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		initDAO();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private void initDAO() {
		category = new CategoryDTO("channel", "tvshow", "channel", "mp4");
		dao = new DownloadedDAO(category, ".");
	}

	@Test
	public final void canAddDownloadedFilesAnReadIt() {
		final EpisodeDTO[] toAdd = new EpisodeDTO[] {
				new EpisodeDTO(category, "test1", "test1"),
				new EpisodeDTO(category, "test2", "test2") };
		dao.initIndex();
		initDAO();
		assertTrue(!dao.isIndexCreated());
		dao.addDownloadedFiles(false, toAdd);
		initDAO();
		assertTrue(dao.isIndexCreated());
		final Set<String> toTest = dao.findDownloadedFiles();
		assertTrue(DownloadedDAO.containsEpisode(toTest, toAdd[0]));
		assertTrue(DownloadedDAO.containsEpisode(toTest, toAdd[1]));
	}

	@Test
	public final void findDownloadedFilesReturnEmptyIfNoIndex() {
		dao.initIndex();
		final Set<String> toTest = dao.findDownloadedFiles();
		assertTrue(toTest.isEmpty());
	}

	@Test
	public final void doesNotShareIndexBetweenSameNamedCategories() {
		final CategoryDTO france2Category = new CategoryDTO("francetv",
				"La France en vrai", "france2-id", "mp4");
		final CategoryDTO france3Category = new CategoryDTO("francetv",
				"La France en vrai", "france3-id", "mp4");
		final String france2Index = DownloadedDAO.getFileIndex(".",
				france2Category);
		final String france3Index = DownloadedDAO.getFileIndex(".",
				france3Category);
		assertFalse(france2Index.equals(france3Index));
		final DownloadedDAO france2Dao = new DownloadedDAO(france2Category, ".");
		final DownloadedDAO france3Dao = new DownloadedDAO(france3Category, ".");
		france2Dao.initIndex();
		france3Dao.initIndex();
		france2Dao.initManualIndex();
		france3Dao.initManualIndex();
		final EpisodeDTO france2Episode = new EpisodeDTO(france2Category,
				"episode-france2", "url");
		france2Dao.addDownloadedFiles(false, france2Episode);
		assertTrue(DownloadedDAO.containsEpisode(france2Dao.findDownloadedFiles(),
				france2Episode));
		assertTrue(france3Dao.findDownloadedFiles().isEmpty());
	}

}
