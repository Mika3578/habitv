package com.dabi.habitv.plugin.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.tpl.TemplateIdBuilder;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

/**
 * YouTube plugin manager using yt-dlp as an external tool.
 *
 * yt-dlp must be present in the following location:
 *   - Windows: C:\Users\<username>\habitv\bin\yt-dlp.exe
 *   - Linux/macOS: ~/.habitv/bin/yt-dlp
 *
 * If yt-dlp is not found in this location, the plugin will log a warning and provide a download link.
 */
public class YoutubePluginManager extends BasePluginWithProxy implements PluginProviderInterface {
	private static final Logger LOG = Logger.getLogger(YoutubePluginManager.class);
	
	private static final String PLAYLIST_ID = "playlistId";
	private static final String QUERY = "q";
	private static final String MAX_RESULTS = "maxResults";
	private static final String TOP = "Top";
	private static final String PLAYLIST = "Playlist";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private String binDir;

	public void setBinDir(String binDir) {
		this.binDir = binDir;
	}

	/**
	 * Returns the directory where yt-dlp is expected.
	 *
	 * Windows: C:\Users\<username>\habitv\bin
	 * Linux/macOS: ~/.habitv/bin
	 */
	private String getUserAppBinDir() {
		String userHome = System.getProperty("user.home");
		String appDir;
		if (isWindows()) {
			// Always use C:\Users\<user>\habitv\bin
			appDir = userHome + "\\habitv";
		} else {
			appDir = userHome + "/.habitv";
		}
		return appDir + (isWindows() ? "\\bin" : "/bin");
	}

	@Override
	public String getName() {
		return YoutubeConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(CategoryDTO category) {
		// Always use userapp bin dir
		this.binDir = getUserAppBinDir();
		ensureYtDlpAvailable();
		Map<String, String> params = TemplateUtils.getParamValues(category.getId());
		if (PLAYLIST.equals(category.getFatherCategory().getName())) {
			return findEpisodePlaylist(category, params);
		} else if (TOP.equals(category.getFatherCategory().getName())) {
			return findEpisodeSearch(category, params);
		} else {
			throw new TechnicalException(category.getFatherCategory().getId() + " unknown");
		}
	}

	private void ensureYtDlpAvailable() {
		String ytDlpPath = getYtDlpBinary();
		File ytDlpFile = new File(ytDlpPath);
		if (!ytDlpFile.exists()) {
			LOG.warn("yt-dlp not found at: " + ytDlpPath);
			LOG.info("Please download yt-dlp from: https://github.com/yt-dlp/yt-dlp/releases/latest and place it in: " + getUserAppBinDir());
		} else if (!ytDlpFile.canExecute()) {
			LOG.warn("yt-dlp found but not executable: " + ytDlpPath);
			LOG.info("Please ensure yt-dlp has execute permissions");
		} else {
			LOG.debug("yt-dlp is available at: " + ytDlpPath);
		}
	}

	private Set<EpisodeDTO> findEpisodePlaylist(CategoryDTO category, Map<String, String> params) {
		String playlistId = params.get(PLAYLIST_ID);
		if (playlistId == null || playlistId.isEmpty()) {
			throw new TechnicalException("Missing playlistId parameter");
		}
		String playlistUrl = YoutubeConf.BASE_URL + "/playlist?list=" + playlistId;
		String cmd = "--flat-playlist --dump-json \"" + playlistUrl + "\"";
		return runYtDlpAndParseEpisodes(category, cmd);
	}

	private Set<EpisodeDTO> findEpisodeSearch(CategoryDTO category, Map<String, String> params) {
		String query = params.get(QUERY);
		if (query == null || query.isEmpty()) {
			throw new TechnicalException("Missing search query parameter");
		}
		String maxResults = params.getOrDefault(MAX_RESULTS, String.valueOf(YoutubeConf.DEFAULT_MAX_RESULTS));
		String cmd = "ytsearch" + maxResults + ":" + query + " --dump-json";
		return runYtDlpAndParseEpisodes(category, cmd);
	}

	private Set<EpisodeDTO> runYtDlpAndParseEpisodes(CategoryDTO category, String ytDlpArgs) {
		Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		String ytDlpBinary = getYtDlpBinary();
		String[] ytDlpArgsArr = ytDlpArgs.split(" ");
		String[] commandArr = new String[1 + ytDlpArgsArr.length];
		commandArr[0] = ytDlpBinary;
		System.arraycopy(ytDlpArgsArr, 0, commandArr, 1, ytDlpArgsArr.length);
		ProcessBuilder pb = new ProcessBuilder(commandArr);
		pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					JsonNode json = objectMapper.readTree(line);
					String id = json.has("id") ? json.get("id").asText() : null;
					String title = json.has("title") ? json.get("title").asText() : null;
					if (id != null && title != null) {
						String href = YoutubeConf.BASE_URL + "/watch?v=" + id;
						episodeList.add(new EpisodeDTO(category, title, href));
					}
				} catch (Exception e) {
					LOG.warn("Failed to parse yt-dlp output: " + line);
				}
			}
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new TechnicalException("yt-dlp exited with code " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			LOG.error("Failed to run yt-dlp", e);
			throw new TechnicalException("Failed to run yt-dlp: " + e.getMessage());
		}
		return episodeList;
	}

	private String getYtDlpBinary() {
		String exe = isWindows() ? "yt-dlp.exe" : "yt-dlp";
		String binDir = getUserAppBinDir();
		File f = new File(binDir, exe);
		if (f.exists() && f.canExecute()) {
			return f.getAbsolutePath();
		}
		return exe; // fallback to PATH
	}

	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryList = new LinkedHashSet<>();
		
		CategoryDTO playListCat = TemplateUtils.buildCategoryTemplate(getName(), PLAYLIST, buildPlaylistTemplateID());
		playListCat.addSubCategory(buildFrance24LiveCat());
		categoryList.add(playListCat);
		
		CategoryDTO topTemplate = TemplateUtils.buildCategoryTemplate(getName(), TOP, buildSearchTemplateID());
		topTemplate.addSubCategory(buildSampleSearchCat());
		categoryList.add(topTemplate);
		
		return categoryList;
	}

	private String buildSearchTemplateID() {
		TemplateIdBuilder templateIdBuilder = new TemplateIdBuilder()
				.addTemplateParam(MAX_RESULTS, "Taille", String.valueOf(YoutubeConf.DEFAULT_MAX_RESULTS))
				.addTemplateParam(QUERY, "Mots clés", "")
				.addComment("Saisissez les mots clés pour la recherche YouTube");
		return templateIdBuilder.buildID();
	}

	private String buildPlaylistTemplateID() {
		TemplateIdBuilder templateIdBuilder = new TemplateIdBuilder()
				.addTemplateParam(PLAYLIST_ID, "Identifiant", null)
				.addComment("Saisissez l'identifiant de la playlist YouTube");
		return templateIdBuilder.buildID();
	}

	private CategoryDTO buildSampleSearchCat() {
		return TemplateUtils.buildSampleCat(getName(), "Recherche: France24", ImmutableMap.of(QUERY, "France24", MAX_RESULTS, "5"));
	}

	private CategoryDTO buildFrance24LiveCat() {
		return TemplateUtils.buildSampleCat(getName(), "France24 Live EN", ImmutableMap.of(PLAYLIST_ID, "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));
	}

}
