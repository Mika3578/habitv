package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class FranceTvCategoryGroupingTest {

	@Test
	public void sectionPageUrlBuildsChannelCategoryPath() {
		assertEquals("https://www.france.tv/france-2/documentaires/",
				FranceTvUrls.sectionPageUrl("france-2", "documentaires"));
	}

	@Test
	public void programsGroupUnderApiCategoryLabels() {
		final CategoryDTO france2 = new CategoryDTO("francetv", "France 2",
				"https://www.france.tv/france-2/", "mp4");
		france2.addSubCategories(FranceTvProgramCatalog.groupProgramsBySection("france-2",
				Arrays.asList(programItem("france-2_show-a", "Show A", "documentaires", "Documentaires"),
						programItem("france-2_show-b", "Show B", "info", "Info"),
						programItem("france-2_show-c", "Show C", "cinema", "Cinéma"))));

		assertEquals(3, france2.getSubCategories().size());
		final java.util.Iterator<CategoryDTO> sectionIt = france2.getSubCategories().iterator();
		assertEquals("Cinéma", sectionIt.next().getName());
		assertEquals("Info", sectionIt.next().getName());
		assertEquals("Documentaires", sectionIt.next().getName());
		final CategoryDTO docu = findSub(france2, "Documentaires");
		assertFalse(docu.isDownloadable());
		assertEquals("Show A", docu.getSubCategories().iterator().next().getName());
		assertEquals("Info", findSub(france2, "Info").getName());
		assertEquals("Cinéma", findSub(france2, "Cinéma").getName());
	}

	private static CategoryDTO findSub(final CategoryDTO parent, final String name) {
		for (final CategoryDTO sub : parent.getSubCategories()) {
			if (name.equals(sub.getName())) {
				return sub;
			}
		}
		throw new AssertionError("missing subcategory " + name);
	}

	private static Map<String, Object> programItem(final String programPath, final String label,
			final String urlComplete, final String categoryLabel) {
		final Map<String, Object> category = new LinkedHashMap<>();
		category.put("label", categoryLabel);
		category.put("url_complete", urlComplete);
		category.put("type", "categorie");

		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("program_path", programPath);
		item.put("label", label);
		item.put("category", category);
		return item;
	}
}
