package com.dabi.habitv.core.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.StatusEnum;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.grabconfig.entities.Category;
import com.dabi.habitv.grabconfig.entities.CategoryType;
import com.dabi.habitv.grabconfig.entities.CategoryType.Configuration;
import com.dabi.habitv.grabconfig.entities.CategoryType.Excludes;
import com.dabi.habitv.grabconfig.entities.CategoryType.Includes;
import com.dabi.habitv.grabconfig.entities.CategoryType.Subcategories;
import com.dabi.habitv.grabconfig.entities.Channel;
import com.dabi.habitv.grabconfig.entities.GrabConfig;
import com.dabi.habitv.grabconfig.entities.GrabConfig.Plugins;
import com.dabi.habitv.grabconfig.entities.Parameter;
import com.dabi.habitv.grabconfig.entities.Plugin;
import com.dabi.habitv.grabconfig.entities.Plugin.Categories;
import com.dabi.habitv.utils.FileUtils;
import com.dabi.habitv.utils.XMLUtils;

public class GrabConfigDAO {

	private static final String GRAB_CONF_XSD = "grab-config.xsd";

	private final String grabConfigFile;

	public GrabConfigDAO(final String grabConfigFile) {
		super();
		this.grabConfigFile = grabConfigFile;
	}

	public void saveGrabConfig(Map<String, CategoryDTO> categories) {
		final GrabConfig config = new GrabConfig();
		addPlugins(categories, config);
		marshal(config);
	}

	private void addPlugins(final Map<String, CategoryDTO> categoriesDTO,
			final GrabConfig config) {
		if (config.getPlugins() == null) {
			config.setPlugins(new Plugins());
		}
		for (final CategoryDTO categoryPlugin : categoriesDTO.values()) {
			final Plugin plugin = new Plugin();
			plugin.setName(categoryPlugin.getName());
			if (categoryPlugin.isDeleted()) {
				plugin.setDeleted(categoryPlugin.isDeleted());
			}
			if (categoryPlugin.getState() == null) {
				plugin.setStatus(StatusEnum.NEW.name());
			} else {
				plugin.setStatus(categoryPlugin.getState().name());
			}
			Categories categories = new Categories();
			plugin.setCategories(categories);
			for (final CategoryDTO categoryDTO : categoryPlugin
					.getSubCategories()) {
				if (categoryDTO.check()) {
					categories.getCategory().add(buildCategory(categoryDTO));
				}
			}
			config.getPlugins().getPlugin().add(plugin);
		}
	}

	private CategoryType buildCategory(final CategoryDTO categoryDTO) {
		final CategoryType category = new CategoryType();
		category.setId(categoryDTO.getId());
		category.setName(categoryDTO.getName());
		category.setExtension(categoryDTO.getExtension());
		category.setDownload(categoryDTO.isSelected());

		if (!categoryDTO.isDownloadable()) {
			category.setDownloadable(categoryDTO.isDownloadable());
		}

		if (categoryDTO.getState() != null) {
			category.setStatus(categoryDTO.getState().name());
		}

		if (categoryDTO.isDeleted()) {
			category.setDeleted(true);
		}

		if (categoryDTO.isTemplate()) {
			category.setTemplate(categoryDTO.isTemplate());
		}

		if (!categoryDTO.getExclude().isEmpty()) {
			Excludes excludes = new Excludes();
			category.setExcludes(excludes);
			for (final String exclude : categoryDTO.getExclude()) {
				excludes.getExclude().add(exclude);
			}
		}

		if (!categoryDTO.getInclude().isEmpty()) {
			Includes includes = new Includes();
			for (final String include : categoryDTO.getInclude()) {
				includes.getInclude().add(include);
			}
			category.setIncludes(includes);
		}

		Subcategories subCategories = new Subcategories();
		category.setSubcategories(subCategories);
		for (final CategoryDTO subCategoryDTO : categoryDTO.getSubCategories()) {
			subCategories.getCategory().add(buildCategory(subCategoryDTO));
		}

		if (categoryDTO.getParameters() != null
				&& !categoryDTO.getParameters().isEmpty()) {
			Configuration configuration = new Configuration();
			category.setConfiguration(configuration);
			for (final Entry<String, String> entry : categoryDTO
					.getParameters().entrySet()) {
				configuration.getAny().add(
						XMLUtils.buildAnyElement(entry.getKey(),
								entry.getValue()));
			}
		}
		return category;
	}

	public void marshal(final GrabConfig config) {
		final JAXBContext jaxbContext;
		FileOutputStream outputFile = null;
		try {
			jaxbContext = JAXBContext.newInstance(GrabConfig.class.getPackage()
					.getName());
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,
					HabitTvConf.ENCODING);
			outputFile = new FileOutputStream(grabConfigFile);
			marshaller.marshal(config, outputFile);
		} catch (JAXBException | FileNotFoundException e) {
			throw new TechnicalException(e);
		} finally {
			if (outputFile != null) {
				try {
					outputFile.close();
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			}
		}
	}

	private static Set<CategoryDTO> buildCategoryListDTO(
			final LoadModeEnum loadMode, final String channelName,
			final List<CategoryType> categories) {
		final Set<CategoryDTO> categoryDTOs = new LinkedHashSet<CategoryDTO>();
		CategoryDTO categoryDTO;
		for (final CategoryType category : categories) {
			final Set<CategoryDTO> subCategoriesDTO;
			if (category.getSubcategories() != null) {
				subCategoriesDTO = buildCategoryListDTO(loadMode, channelName,
						category.getSubcategories().getCategory());
			} else {
				subCategoriesDTO = Collections.emptySet();
			}

			if (category.isDownload() == null || category.isDownload()
					|| !subCategoriesDTO.isEmpty()
					|| loadMode.equals(LoadModeEnum.ALL)) {
				categoryDTO = buildCategoryDTO(channelName, category,
						subCategoriesDTO);
				categoryDTOs.add(categoryDTO);
			}
		}
		return categoryDTOs;
	}

	private static CategoryDTO buildCategoryDTO(final String channelName,
			final CategoryType category, final Set<CategoryDTO> subCategoriesDTO) {
		CategoryDTO categoryDTO;
		categoryDTO = new CategoryDTO(channelName, category.getName(),
				category.getId(), getInclude(category), getExclude(category),
				category.getExtension());

		categoryDTO.setSelected(category.isDownload() != null
				&& category.isDownload());

		categoryDTO.setTemplate(category.isTemplate() != null
				&& category.isTemplate());

		categoryDTO.setDownloadable(category.isDownloadable() == null
				|| category.isDownloadable());

		categoryDTO.setDeleted(category.isDeleted() != null
				&& category.isDeleted());

		categoryDTO.setState(category.getStatus() == null ? StatusEnum.EXIST
				: StatusEnum.valueOf(category.getStatus()));

		categoryDTO.addSubCategories(subCategoriesDTO);
		if (category.getConfiguration() != null
				&& !category.getConfiguration().getAny().isEmpty()) {
			for (final Object parameter : category.getConfiguration().getAny()) {
				categoryDTO.addParameter(XMLUtils.getTagName(parameter),
						XMLUtils.getTagValue(parameter));
			}
		}
		return categoryDTO;
	}

	private static List<String> getExclude(final CategoryType category) {
		if (category.getExcludes() == null) {
			return new LinkedList<>();
		} else {
			return category.getExcludes().getExclude();
		}
	}

	private static List<String> getInclude(final CategoryType category) {
		if (category.getIncludes() == null) {
			return new LinkedList<>();
		} else {
			return category.getIncludes().getInclude();
		}
	}

	public GrabConfig unmarshal() {
		GrabConfig grabConfig = null;
		try {
			final JAXBContext jaxbContext = JAXBContext
					.newInstance(GrabConfig.class.getPackage().getName());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			FileUtils.setValidation(unmarshaller, GRAB_CONF_XSD);
			grabConfig = ((GrabConfig) unmarshaller
					.unmarshal(new InputStreamReader(new FileInputStream(
							grabConfigFile), HabitTvConf.ENCODING)));
			if (!grabConfig.getChannel().isEmpty()) {
				grabConfig = convertOldGrabconfig(grabConfig);
			}
		} catch (final JAXBException e) {
			throw new TechnicalException(e);
		} catch (final UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		} catch (final FileNotFoundException e) {
			throw new TechnicalException(e);
		}
		return grabConfig;
	}

	private GrabConfig convertOldGrabconfig(GrabConfig grabConfig) {
		Plugins Plugins = new Plugins();
		grabConfig.setPlugins(Plugins);
		Iterator<Channel> it = grabConfig.getChannel().iterator();
		while (it.hasNext()) {
			Channel oldPlugin = it.next();
			Plugins.getPlugin().add(buildPlugin(oldPlugin));
			it.remove();
		}
		marshal(grabConfig);
		return grabConfig;
	}

	private Plugin buildPlugin(Channel oldPlugin) {
		Plugin channelType = new Plugin();
		channelType.setName(oldPlugin.getName());
		channelType.setStatus(channelType.getStatus());
		Categories categories = new Categories();
		channelType.setCategories(categories);
		for (Category oldCategory : oldPlugin.getCategory()) {
			categories.getCategory().add(buildCategoryType(oldCategory));
		}
		return channelType;
	}

	private CategoryType buildCategoryType(Category oldCategory) {
		CategoryType categoryType = new CategoryType();

		buildCategoryConfiguration(oldCategory, categoryType);

		categoryType.setDownload(oldCategory.isToDownload());

		Excludes excludes = new Excludes();
		for (String oldExclude : oldCategory.getExclude()) {
			excludes.getExclude().add(oldExclude);
		}
		categoryType.setExcludes(excludes);

		categoryType.setExtension(oldCategory.getExtension());
		categoryType.setId(oldCategory.getId());
		Includes includes = new Includes();
		for (String oldInclude : oldCategory.getInclude()) {
			includes.getInclude().add(oldInclude);
		}
		categoryType.setIncludes(includes);

		categoryType.setName(oldCategory.getName());
		if (oldCategory.getStatus() != null) {
			categoryType.setStatus(StatusEnum.values()[oldCategory.getStatus()
					.intValue()].name());
		}
		Subcategories subCategories = new Subcategories();
		for (Category oldSuCategory : oldCategory.getCategory()) {
			subCategories.getCategory().add(buildCategoryType(oldSuCategory));
		}
		categoryType.setSubcategories(subCategories);

		return categoryType;
	}

	private void buildCategoryConfiguration(Category oldCategory,
			CategoryType categoryType) {
		Configuration configuration = new Configuration();
		for (Parameter oldParameter : oldCategory.getParameter()) {
			configuration.getAny().add(
					XMLUtils.buildAnyElement(oldParameter.getKey(),
							oldParameter.getValue()));
		}
		categoryType.setConfiguration(configuration);
	}

	private Map<String, CategoryDTO> buildCategoryDTO(
			final GrabConfig grabConfig, final LoadModeEnum loadMode) {
		final Map<String, CategoryDTO> channel2Category = new HashMap<>();
		if (grabConfig.getPlugins() != null) {
			for (final Plugin plugin : grabConfig.getPlugins().getPlugin()) {
				final Set<CategoryDTO> buildCategoryListDTO;
				if (plugin.getCategories() == null) {
					buildCategoryListDTO = new LinkedHashSet<>();
				} else {
					buildCategoryListDTO = buildCategoryListDTO(loadMode,
							plugin.getName(), plugin.getCategories()
									.getCategory());
				}
				if (!buildCategoryListDTO.isEmpty()) {
					CategoryDTO categoryPlugin = new CategoryDTO(
							plugin.getName(), buildCategoryListDTO);
					categoryPlugin.setDownloadable(false);
					categoryPlugin
							.setDeleted(plugin.isDeleted() == null ? false
									: plugin.isDeleted());
					if (plugin.getStatus() != null) {
						categoryPlugin.setState(StatusEnum.valueOf(plugin
								.getStatus()));
					}
					channel2Category.put(plugin.getName(), categoryPlugin);
				}
			}
		}
		return channel2Category;
	}

	public enum LoadModeEnum {
		ALL, TO_DOWNLOAD_ONLY;
	}

	public Map<String, CategoryDTO> load(final LoadModeEnum loadMode) {
		return buildCategoryDTO(unmarshal(), loadMode);
	}

	public Map<String, CategoryDTO> load() {
		return buildCategoryDTO(unmarshal(), LoadModeEnum.TO_DOWNLOAD_ONLY);
	}

	public boolean exist() {
		return (new File(grabConfigFile)).exists();
	}

	public void updateGrabConfig(final Map<String, CategoryDTO> channel2Category) {
		updateGrabConfig(channel2Category, null);
	}

	public void updateGrabConfig(Map<String, CategoryDTO> channel2Category,
			List<String> pluginList) {
		final HashMap<String, CategoryDTO> channel2CategoryTemp = new HashMap<>(
				channel2Category);
		final GrabConfig grabConfig = unmarshal();
		if (grabConfig.getPlugins() != null) {
			StatusEnum pluginstatus;
			for (final Plugin plugin : grabConfig.getPlugins().getPlugin()) {
				if (pluginList == null || pluginList.contains(plugin.getName())) {
					CategoryDTO categoryDTO = channel2CategoryTemp.get(plugin
							.getName());
					final Set<CategoryDTO> categoryPlugin = categoryDTO == null ? null
							: categoryDTO.getSubCategories();
					if (plugin.getCategories() == null) {
						plugin.setCategories(new Categories());
					}
					if (categoryPlugin != null
							&& categoryDTO.getState() != StatusEnum.FAILED) {
						updateCategory(plugin.getCategories().getCategory(),
								categoryPlugin);
						channel2CategoryTemp.remove(plugin.getName());
						pluginstatus = StatusEnum.EXIST;
					} else {
						if (categoryDTO != null
								&& categoryDTO.getState() == StatusEnum.FAILED) {
							pluginstatus = StatusEnum.FAILED;
						} else {
							pluginstatus = StatusEnum.DELETED;
						}
					}
					plugin.setStatus(pluginstatus.name());
				}
			}
		}
		addPlugins(channel2CategoryTemp, grabConfig);
		marshal(grabConfig);
	}

	private void updateCategory(final List<CategoryType> categoryList,
			final Collection<CategoryDTO> categoryDTOList) {
		final Map<String, CategoryDTO> catNameToCat = new HashMap<>();
		for (final CategoryDTO categoryDTO : categoryDTOList) {
			catNameToCat.put(categoryDTO.getName(), categoryDTO);
		}
		StatusEnum statusEnum;
		for (final CategoryType category : categoryList) {
			final CategoryDTO associatedCatDTO = catNameToCat.get(category
					.getName());
			if (associatedCatDTO != null) {
				catNameToCat.remove(category.getName());
				
				category.setId(associatedCatDTO.getId());
				category.setExtension(associatedCatDTO.getExtension());
				category.setDownloadable(associatedCatDTO.isDownloadable());
				
				if (category.getSubcategories() != null) {
					updateCategory(category.getSubcategories().getCategory(),
							associatedCatDTO.getSubCategories());
				}
				statusEnum = StatusEnum.EXIST;
				category.setDownloadable(associatedCatDTO.isDownloadable());
			} else {
				statusEnum = StatusEnum.DELETED;
			}
			if (category.getStatus() == null
					|| StatusEnum.valueOf(category.getStatus()) != StatusEnum.USER) {
				category.setStatus(statusEnum.name());
			}
		}
		for (final CategoryDTO categoryDTO : catNameToCat.values()) {
			categoryList.add(buildCategory(categoryDTO));
		}
	}

	public void clean() {
		GrabConfig grabconfig = unmarshal();
		Iterator<Plugin> it = grabconfig.getPlugins().getPlugin().iterator();
		while (it.hasNext()) {
			Plugin channel = it.next();
			if (StatusEnum.DELETED.name().equals(channel.getStatus())) {
				it.remove();
			} else {
				cleanCategories(channel.getCategories().getCategory());
			}
		}
		marshal(grabconfig);
	}

	private void cleanCategories(List<CategoryType> categories) {
		Iterator<CategoryType> it = categories.iterator();
		while (it.hasNext()) {
			CategoryType category = it.next();
			if (StatusEnum.DELETED.name().equals(category.getStatus())) {
				it.remove();
			} else {
				cleanCategories(category.getSubcategories().getCategory());
			}
		}
	}

}
