package com.dabi.habitv.core.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.utils.FileUtils;

public class DownloadedDAO {

	private static final Logger LOG = Logger.getLogger(DownloadedDAO.class);

	private final String indexDir;

	private boolean indexExist;

	private boolean manualIndexExist;

	private CategoryDTO category;

	public DownloadedDAO(final CategoryDTO category, final String indexDir) {
		super();
		this.indexDir = indexDir;
		this.category = category;
		final File indexDirectory = new File(indexDir);
		if (indexDirectory.exists()) {
			indexExist = new File(getFileIndex()).exists();
			manualIndexExist = new File(getManualFileIndex()).exists();
		} else {
			indexExist = false;
			manualIndexExist = false;
			if (!indexDirectory.mkdir()) {
				throw new TechnicalException("Folder can't be created"
						+ indexDirectory.getAbsolutePath());
			}
		}
	}

	private String getFileIndex() {
		return getFileIndex(indexDir, category);
	}

	public static String getFileIndex(String indexDir, CategoryDTO category) {
		return (indexDir + "/" + FileUtils.sanitizeFilename(category
				.getPlugin() + "_" + category.getName() + ".index"));
	}

	private String getManualFileIndex() {
		return (indexDir + "/" + FileUtils.sanitizeFilename(category
				.getPlugin() + "_" + category.getName() + "_manual.index"));
	}

	public Set<String> findDownloadedFiles() {
		Set<String> dlFiles = readFile(getFileIndex());
		dlFiles.addAll(readFile(getManualFileIndex()));
		return dlFiles;
	}

	private Set<String> readFile(String file) {
		BufferedReader lecteurAvecBuffer = null;
		String ligne;

		final Set<String> fileList = new LinkedHashSet<>();
		try {

			lecteurAvecBuffer = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), HabitTvConf.ENCODING));
			while ((ligne = lecteurAvecBuffer.readLine()) != null) {
				fileList.add(ligne);
			}
		} catch (final FileNotFoundException exc) {
			// will return empty
			LOG.debug("", exc);
		} catch (final IOException e) {
			throw new TechnicalException(e);
		} finally {
			if (lecteurAvecBuffer != null) {
				try {
					lecteurAvecBuffer.close();
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			}
		}
		return fileList;
	}

	public synchronized void addDownloadedFiles(boolean manual,
			final EpisodeDTO... episodes) {
		String file;
		if (manual) {
			if (isIndexCreated()) {
				file = getFileIndex();
			} else {
				file = getManualFileIndex();
			}
		} else {
			if (!isIndexCreated() && isManualIndexCreated()) {
				addToFile(getFileIndex(), readFile(getManualFileIndex()));
				initManualIndex();
			}
			file = getFileIndex();
		}
		addToFile(file, episodes);
	}

	private void addToFile(String file, final EpisodeDTO... episodes) {
		Collection<String> toAddList = convertEpisodesToNameList(episodes);
		addToFile(file, toAddList);
	}

	private void addToFile(String file, Collection<String> toAddList) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), HabitTvConf.ENCODING));
			for (final String toAdd : toAddList) {
				writer.println(toAdd);
			}
			writer.close();
		} catch (final IOException e) {
			throw new TechnicalException(e);
		}
	}

	private Collection<String> convertEpisodesToNameList(
			final EpisodeDTO... episodes) {
		Collection<String> toAddList = new ArrayList<>(episodes.length);
		for (EpisodeDTO episodeDTO : episodes) {
			toAddList.add(episodeDTO.getName());
		}
		return toAddList;
	}

	public boolean isIndexCreated() {
		return indexExist;
	}

	public boolean isManualIndexCreated() {
		return manualIndexExist;
	}

	void initIndex() {
		final String fileIndex = getFileIndex();
		(new File(fileIndex)).delete();
		LOG.info("réinitialisation de l'index " + fileIndex);
		indexExist = false;
	}

	void initManualIndex() {
		final String fileIndex = getManualFileIndex();
		(new File(fileIndex)).delete();
		LOG.info("réinitialisation de l'index " + fileIndex);
		manualIndexExist = false;
	}
}
