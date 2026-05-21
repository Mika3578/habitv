package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class CategoryTreeNormalizerTest {

	@Test
	public void padsChannelToEmissionWithProgramsLevel() {
		final CategoryDTO channel = new CategoryDTO("sixplay", "M6", "http://m6", "mp4");
		final CategoryDTO show = new CategoryDTO("sixplay", "Show", "http://show", "mp4");
		show.setDownloadable(true);
		channel.addSubCategory(show);

		final Set<CategoryDTO> normalized = CategoryTreeNormalizer.normalize("sixplay",
				new LinkedHashSet<>(Arrays.asList(channel)));

		assertEquals(1, normalized.size());
		final CategoryDTO m6 = normalized.iterator().next();
		assertEquals("M6", m6.getName());
		assertFalse(m6.isDownloadable());
		final CategoryDTO programs = m6.getSubCategories().iterator().next();
		assertEquals(CategoryTreeNormalizer.DEFAULT_CATEGORY_NAME, programs.getName());
		final CategoryDTO showNode = programs.getSubCategories().iterator().next();
		assertEquals("Show", showNode.getName());
		assertTrue(showNode.isDownloadable());
		assertEquals(3, CategoryTreeNormalizer.structureDepth(m6));
	}

	@Test
	public void leavesFourLevelTreeUnchanged() {
		final CategoryDTO channel = new CategoryDTO("francetv", "France 2", "http://f2", "mp4");
		channel.setDownloadable(false);
		final CategoryDTO rubrique = new CategoryDTO("francetv", "Info", "http://f2/info", "mp4");
		rubrique.setDownloadable(false);
		final CategoryDTO program = new CategoryDTO("francetv", "JT", "http://jt", "mp4");
		program.setDownloadable(true);
		rubrique.addSubCategory(program);
		channel.addSubCategory(rubrique);

		final Set<CategoryDTO> normalized = CategoryTreeNormalizer.normalize("francetv",
				new LinkedHashSet<>(Arrays.asList(channel)));

		assertEquals(1, normalized.size());
		assertEquals(3, CategoryTreeNormalizer.structureDepth(normalized.iterator().next()));
		assertEquals("Info", normalized.iterator().next().getSubCategories().iterator().next().getName());
	}

	@Test
	public void wrapsSingleDownloadableRootInPrincipalAndPrograms() {
		final CategoryDTO leaf = new CategoryDTO("mlssoccer", "Highlights", "http://highlights", "mp4");
		leaf.setDownloadable(true);

		final Set<CategoryDTO> normalized = CategoryTreeNormalizer.normalize("mlssoccer",
				new LinkedHashSet<>(Arrays.asList(leaf)));

		assertEquals(1, normalized.size());
		final CategoryDTO channel = normalized.iterator().next();
		assertEquals(CategoryTreeNormalizer.DEFAULT_CHANNEL_NAME, channel.getName());
		final Iterator<CategoryDTO> it = channel.getSubCategories().iterator();
		final CategoryDTO category = it.next();
		assertEquals(CategoryTreeNormalizer.DEFAULT_CATEGORY_NAME, category.getName());
		assertEquals("Highlights", category.getSubCategories().iterator().next().getName());
	}
}
