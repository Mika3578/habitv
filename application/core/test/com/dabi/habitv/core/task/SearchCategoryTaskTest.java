package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchCategoryStateEnum;

public class SearchCategoryTaskTest {

	private SearchCategoryTask task;

	private boolean done;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		done = false;
	}

	@After
	public void tearDown() throws Exception {
	}

	public void init(final boolean toFail) {
		final PluginProviderDownloaderInterface provider = new PluginProviderDownloaderInterface() {

			@Override
			public String getName() {
				return "provider";
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				return null;
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				if (toFail) {
					throw new TechnicalException("string");
				}
				final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<>();
				categoryDTOs.add(new CategoryDTO("channel", "name",
						"identifier", "extension"));
				return categoryDTOs;
			}

			@Override
			public ProcessHolder download(final DownloadParamDTO downloadParam,
					final DownloaderPluginHolder downloaders)
					throws DownloadFailedException {
				return ProcessHolder.EMPTY_PROCESS_HOLDER;
			}

			@Override
			public DownloadableState canDownload(final String downloadInput) {
				return DownloadableState.IMPOSSIBLE;
			}
		};

		final Publisher<SearchCategoryEvent> publisher = new Publisher<>();
		final Subscriber<SearchCategoryEvent> subscriber = new Subscriber<SearchCategoryEvent>() {

			private int i = 0;

			@Override
			public void update(final SearchCategoryEvent event) {
				switch (i) {
				case 0:
					assertEquals(
							new SearchCategoryEvent(
									"channel",
									SearchCategoryStateEnum.CHANNEL_CATEGORIES_TO_BUILD),
							event);
					break;
				case 1:
					assertEquals(new SearchCategoryEvent("channel",
							SearchCategoryStateEnum.BUILDING_CATEGORIES), event);
					break;
				case 2:
					if (toFail) {
						assertEquals(new SearchCategoryEvent("channel",
								SearchCategoryStateEnum.ERROR), event);
					} else {
						assertEquals(new SearchCategoryEvent("channel",
								SearchCategoryStateEnum.DONE), event);
					}
					done = true;
					break;
				default:
					fail("unexpected event" + event);
					break;
				}
				i++;
			}
		};
		publisher.attach(subscriber);

		task = new SearchCategoryTask("channel", provider, publisher);
	}

	@Test
	public final void testSearchCategoryTaskSuccess() {
		init(false);
		task.adding();
		task.addedTo("retreive", null);
		task.call();
		assertTrue(done);
	}

	@Test
	public void testSearchCategoryTaskNormalizesShallowTree() {
		final PluginProviderDownloaderInterface provider = new PluginProviderDownloaderInterface() {

			@Override
			public String getName() {
				return "provider";
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				return null;
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				final Set<CategoryDTO> roots = new LinkedHashSet<>();
				roots.add(new CategoryDTO("provider", "Emission A", "emission-a", "mp4"));
				return roots;
			}

			@Override
			public ProcessHolder download(final DownloadParamDTO downloadParam,
					final DownloaderPluginHolder downloaders) throws DownloadFailedException {
				return ProcessHolder.EMPTY_PROCESS_HOLDER;
			}

			@Override
			public DownloadableState canDownload(final String downloadInput) {
				return DownloadableState.IMPOSSIBLE;
			}
		};

		final SearchCategoryTask localTask = new SearchCategoryTask("provider", provider,
				new Publisher<SearchCategoryEvent>());
		final SearchCategoryResult result = localTask.call();
		assertTrue(result.isSuccess());
		assertEquals(1, result.getCategoryList().size());

		final CategoryDTO channel = result.getCategoryList().iterator().next();
		assertEquals("Principal", channel.getName());
		assertEquals(1, channel.getSubCategories().size());

		final CategoryDTO category = channel.getSubCategories().iterator().next();
		assertEquals("Programmes", category.getName());
		assertEquals(1, category.getSubCategories().size());

		final CategoryDTO emission = category.getSubCategories().iterator().next();
		assertEquals("Emission A", emission.getName());
	}

	public final void testSearchCategoryTaskFailed() {
		init(true);
		task.adding();
		task.addedTo("retreive", null);
		SearchCategoryResult result = task.call();
		assertFalse(result.isSuccess());
	}

}
