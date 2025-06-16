package com.dabi.habitv.plugintester;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.framework.FrameworkConf;

public class BasePluginProviderTester {

	protected static final String TEST_FILE = "testFile.txt";
	protected static final Logger LOG = Logger.getLogger(BasePluginProviderTester.class);
	private static final int MAX_ATTEMPTS = 10;
	private DownloaderPluginHolder downloaders;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public BasePluginProviderTester() {
		super();
	}

	@Before
	public void setUp() throws Exception {
		final Map<String, PluginDownloaderInterface> downloaderName2downloader = new HashMap<>();
		final MockDownloader mockDownloader = new MockDownloader();

		downloaderName2downloader.put(mockDownloader.getName(), mockDownloader);
		downloaderName2downloader.put(FrameworkConf.RTMDUMP, mockDownloader);
		downloaderName2downloader.put(FrameworkConf.CURL, mockDownloader);
		downloaderName2downloader.put(FrameworkConf.FFMPEG, mockDownloader);
		downloaderName2downloader.put(FrameworkConf.ADOBEHDS, mockDownloader);
		downloaderName2downloader.put(FrameworkConf.YOUTUBE, mockDownloader);

		final Map<String, String> downloaderName2BinPath = new HashMap<>();
		downloaders = new DownloaderPluginHolder("cmdProcessor", downloaderName2downloader, downloaderName2BinPath, "downloadOutputDir", "indexDir",
		        "binDir", "plugins");
	}

	@After
	public void tearDown() throws Exception {
	}

	protected void testPluginProvider(final PluginProviderInterface plugin, final boolean episodeOnlyOnLeaf) throws DownloadFailedException {
		LOG.error("---------------------------------------------------------------------");
		LOG.error("searching categories for " + plugin.getName());
		final Set<CategoryDTO> categories = plugin.findCategory();
		checkCategories(categories);

		showCategoriesTree(categories, 0);

		Set<EpisodeDTO> episodeList = Collections.emptySet();
		int i = 0;
		while (episodeList.isEmpty() && i < MAX_ATTEMPTS) {
			LOG.error("no ep found, searching againg categories for " + plugin.getName());
			final CategoryDTO category = findCategory(episodeOnlyOnLeaf, categories);
			if (category.isDownloadable()) {
				LOG.error("search episodes for " + plugin.getName() + "/" + category);
				episodeList = plugin.findEpisode(category);
				i++;
			}
		}
		if (i == MAX_ATTEMPTS) {
			Assert.fail("no ep found in " + MAX_ATTEMPTS + " attempts");
		}

		checkEpisodes(episodeList);

		final EpisodeDTO episode = (new ArrayList<>(episodeList)).get(getRandomIndex(episodeList));
		testEpisode(plugin, episode);
	}

	protected void showCategoriesTree(Set<CategoryDTO> categories, int i) {
		for (CategoryDTO categoryDTO : categories) {
			LOG.error(StringUtils.repeat(" ", i) + categoryDTO.getName());
			showCategoriesTree(categoryDTO.getSubCategories(), i + 1);
		}
	}

	protected void testEpisode(final PluginProviderInterface plugin, final EpisodeDTO episode) throws DownloadFailedException {
		LOG.error("episode found " + episode);
		LOG.error("episode id " + episode.getId());

		if (PluginDownloaderInterface.class.isInstance(plugin)) {
			final PluginDownloaderInterface pluginDownloader = (PluginDownloaderInterface) plugin;
			pluginDownloader.download(buildDownloadersHolder(episode), downloaders);
		}
	}

	protected void checkCategories(final Set<CategoryDTO> categories) {
		assertTrue("categorie liste vide ", !categories.isEmpty());
		for (final CategoryDTO categoryDTO : categories) {
			if (categoryDTO.getName().isEmpty() || categoryDTO.getId().isEmpty()) {
				LOG.error(categoryDTO);
				Assert.fail("category incorrect : " + categoryDTO);
			}
		}
	}

	protected void checkEpisodes(final Set<EpisodeDTO> episodeList) {
		for (final EpisodeDTO episode : episodeList) {
			if (episode.getName().isEmpty() || episode.getId().isEmpty()) {
				LOG.error("episode incorrect : " + episode);
				Assert.fail("episode incorrect");
			}
		}
	}

	protected CategoryDTO findCategory(final boolean episodeOnlyOnLeaf, final Collection<CategoryDTO> categories) {
		final CategoryDTO category = (new ArrayList<>(categories)).get(getRandomIndex(categories));
		if ((episodeOnlyOnLeaf || !category.isDownloadable()) && !category.getSubCategories().isEmpty()) {
			return findCategory(episodeOnlyOnLeaf, category.getSubCategories());
		}
		return category;
	}

	protected DownloadParamDTO buildDownloadersHolder(final EpisodeDTO episode) {
		return new DownloadParamDTO(episode.getId(), episode.getName(), episode.getCategory().getExtension());
	}

	protected int getRandomIndex(final Collection<?> collection) {
		final int min = 0;
		final int max = collection.size() - 1;
		final Random rand = new Random();
		return rand.nextInt(max - min + 1) + min;
	}

	public void testPluginProvider(final Class<? extends PluginProviderInterface> prDlPluginClass, final boolean episodeOnlyOnLeaf)
	        throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		final PluginProviderInterface plugin = prDlPluginClass.getDeclaredConstructor().newInstance();
		testPluginProvider(plugin, episodeOnlyOnLeaf);
	}
} 