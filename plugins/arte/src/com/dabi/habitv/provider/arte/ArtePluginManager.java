package com.dabi.habitv.provider.arte;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class ArtePluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

	@Override
	public Set<EpisodeDTO> findEpisode(CategoryDTO category) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			String categoryUrl = category.getId();
			String language = extractLanguageFromUrl(categoryUrl);
			String categoryCode = extractCategoryFromUrl(categoryUrl);
			
			// Try multiple URL patterns for different Arte sections
			String[] urlsToTry = buildUrlsToTry(language, categoryCode);
			
			for (String url : urlsToTry) {
				getLog().info("Trying to fetch episodes from: " + url);
				
				try {
					// Get the page content
					Document doc = Jsoup.connect(url)
							.userAgent(USER_AGENT)
							.timeout(30000)
							.get();
					
					// Try multiple methods to extract video data
					Set<EpisodeDTO> foundEpisodes = extractEpisodesFromJsonData(doc, language);
					
					// Fallback: try to extract from HTML structure
					if (foundEpisodes.isEmpty()) {
						foundEpisodes = extractEpisodesFromHtml(doc, language);
					}
					
					// Fallback: try to extract from API endpoints
					if (foundEpisodes.isEmpty()) {
						foundEpisodes = extractEpisodesFromApi(language, categoryCode);
					}
					
					episodes.addAll(foundEpisodes);
					
					if (!foundEpisodes.isEmpty()) {
						getLog().info("Found " + foundEpisodes.size() + " episodes from " + url);
						break; // Found episodes, no need to try other URLs
					}
					
				} catch (Exception e) {
					getLog().warn("Failed to fetch from " + url + ": " + e.getMessage());
				}
			}
			
			getLog().info("Total episodes found for category " + category.getName() + ": " + episodes.size());
			
		} catch (Exception e) {
			getLog().error("Error parsing Arte category " + category.getName() + ": " + e.getMessage(), e);
		}
		
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		Set<CategoryDTO> categories = new LinkedHashSet<>();
		
		for (String language : ArteConf.LANGUAGES) {
			// Add main categories
			for (String categoryCode : ArteConf.CATEGORIES) {
				try {
					String categoryName = getCategoryName(categoryCode, language);
					String categoryId = String.format("%s/%s", language, categoryCode);
					
					CategoryDTO category = new CategoryDTO(ArteConf.NAME, categoryName, categoryId, ArteConf.EXTENSION);
					categories.add(category);
					
				} catch (Exception e) {
					getLog().error("Error creating Arte category " + language + "/" + categoryCode + ": " + e.getMessage());
				}
			}
			
			// Add replay/replay categories
			try {
				CategoryDTO replayCategory = new CategoryDTO(ArteConf.NAME, "Replay", String.format("%s/REPLAY", language), ArteConf.EXTENSION);
				categories.add(replayCategory);
			} catch (Exception e) {
				getLog().error("Error creating Arte replay category for " + language + ": " + e.getMessage());
			}
		}
		
		return categories;
	}

	@Override
	public ProcessHolder download(DownloadParamDTO downloadParam, DownloaderPluginHolder downloaders) throws DownloadFailedException {
		// Use yt-dlp as it has good support for Arte videos
		// This is more reliable than implementing custom download logic
		return DownloadUtils.download(downloadParam, downloaders, "youtube");
	}

	@Override
	public String getName() {
		return ArteConf.NAME;
	}

	private String[] buildUrlsToTry(String language, String categoryCode) {
		if ("REPLAY".equals(categoryCode)) {
			// For replay videos, try multiple URL patterns
			return new String[] {
				String.format("https://www.arte.tv/%s/videos/replay/", language),
				String.format("https://www.arte.tv/%s/replay/", language),
				String.format("https://www.arte.tv/%s/videos/", language),
				String.format("https://www.arte.tv/%s/", language)
			};
		} else {
			// For regular categories
			return new String[] {
				String.format("https://www.arte.tv/%s/videos/%s/", language, getCategorySlug(categoryCode)),
				String.format("https://www.arte.tv/%s/%s/", language, getCategorySlug(categoryCode)),
				String.format("https://www.arte.tv/%s/videos/", language)
			};
		}
	}

	private Set<EpisodeDTO> extractEpisodesFromApi(String language, String categoryCode) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			// Try Arte's API endpoints for video data
			String apiUrl;
			if ("REPLAY".equals(categoryCode)) {
				apiUrl = String.format("https://api.arte.tv/api/emac/v4/%s/videos/replay", language);
			} else {
				apiUrl = String.format("https://api.arte.tv/api/emac/v4/%s/videos/%s", language, getCategorySlug(categoryCode));
			}
			
			getLog().info("Trying API endpoint: " + apiUrl);
			
			String jsonResponse = getUrlContent(apiUrl);
			if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
				JSONObject json = new JSONObject(jsonResponse);
				episodes.addAll(parseEpisodesFromApiJson(json, language));
			}
			
		} catch (Exception e) {
			getLog().warn("Failed to fetch from API: " + e.getMessage());
		}
		
		return episodes;
	}

	private Set<EpisodeDTO> extractEpisodesFromJsonData(Document doc, String language) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			// Look for the embedded JSON data in script tags
			Elements scripts = doc.select("script");
			for (Element script : scripts) {
				String scriptContent = script.html();
				
				// Try multiple JSON patterns
				String[] patterns = {
					"window\\.__INITIAL_STATE__\\s*=\\s*({.*?});",
					"window\\.__APOLLO_STATE__\\s*=\\s*({.*?});",
					"window\\.__NEXT_DATA__\\s*=\\s*({.*?});",
					"\"data\":\\s*({.*?})\\s*};"
				};
				
				for (String patternStr : patterns) {
					Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
					Matcher matcher = pattern.matcher(scriptContent);
					if (matcher.find()) {
						String jsonData = matcher.group(1);
						try {
							JSONObject json = new JSONObject(jsonData);
							episodes.addAll(parseEpisodesFromJson(json, language));
							if (!episodes.isEmpty()) {
								break; // Found episodes, no need to try other patterns
							}
						} catch (Exception e) {
							getLog().debug("Failed to parse JSON pattern: " + patternStr);
						}
					}
				}
			}
		} catch (Exception e) {
			getLog().error("Error extracting episodes from JSON data: " + e.getMessage(), e);
		}
		
		return episodes;
	}

	private Set<EpisodeDTO> extractEpisodesFromHtml(Document doc, String language) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			// Look for video links in the HTML structure - multiple selectors
			String[] selectors = {
				"a[href*='/videos/']",
				"a[href*='/replay/']",
				".video-card a",
				".episode-card a",
				".program-card a",
				"[data-video-id] a",
				".video-item a"
			};
			
			for (String selector : selectors) {
				Elements videoLinks = doc.select(selector);
				for (Element link : videoLinks) {
					String href = link.attr("href");
					String title = link.text().trim();
					
					// Also try to get title from title attribute or data attributes
					if (StringUtils.isEmpty(title)) {
						title = link.attr("title");
					}
					if (StringUtils.isEmpty(title)) {
						title = link.attr("data-title");
					}
					
					if (!StringUtils.isEmpty(href) && !StringUtils.isEmpty(title) && 
						(href.contains("/videos/") || href.contains("/replay/") || href.contains("arte.tv"))) {
						
						// Build the full URL
						String fullUrl = href.startsWith("http") ? href : "https://www.arte.tv" + href;
						
						// Create episode
						EpisodeDTO episode = new EpisodeDTO(null, title, fullUrl);
						episodes.add(episode);
					}
				}
				
				if (!episodes.isEmpty()) {
					break; // Found episodes, no need to try other selectors
				}
			}
		} catch (Exception e) {
			getLog().error("Error extracting episodes from HTML: " + e.getMessage(), e);
		}
		
		return episodes;
	}

	private Set<EpisodeDTO> parseEpisodesFromJson(JSONObject json, String language) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			// Try multiple JSON structures
			episodes.addAll(parseEpisodesFromJsonStructure(json, language, "data.zones"));
			episodes.addAll(parseEpisodesFromJsonStructure(json, language, "data.videos"));
			episodes.addAll(parseEpisodesFromJsonStructure(json, language, "videos"));
			episodes.addAll(parseEpisodesFromJsonStructure(json, language, "data"));
			
		} catch (Exception e) {
			getLog().error("Error parsing episodes from JSON: " + e.getMessage(), e);
		}
		
		return episodes;
	}

	private Set<EpisodeDTO> parseEpisodesFromJsonStructure(JSONObject json, String language, String path) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			String[] pathParts = path.split("\\.");
			Object current = json;
			
			// Navigate through the path
			for (String part : pathParts) {
				if (current instanceof JSONObject) {
					JSONObject obj = (JSONObject) current;
					if (obj.has(part)) {
						current = obj.get(part);
					} else {
						return episodes; // Path not found
					}
				} else {
					return episodes; // Path not found
				}
			}
			
			// Now extract episodes from the current object
			if (current instanceof JSONArray) {
				JSONArray array = (JSONArray) current;
				for (int i = 0; i < array.length(); i++) {
					JSONObject video = array.getJSONObject(i);
					EpisodeDTO episode = createEpisodeFromVideo(video, language);
					if (episode != null) {
						episodes.add(episode);
					}
				}
			} else if (current instanceof JSONObject) {
				JSONObject obj = (JSONObject) current;
				if (obj.has("data") && obj.get("data") instanceof JSONArray) {
					JSONArray array = obj.getJSONArray("data");
					for (int i = 0; i < array.length(); i++) {
						JSONObject video = array.getJSONObject(i);
						EpisodeDTO episode = createEpisodeFromVideo(video, language);
						if (episode != null) {
							episodes.add(episode);
						}
					}
				}
			}
			
		} catch (Exception e) {
			getLog().debug("Failed to parse JSON structure: " + path + " - " + e.getMessage());
		}
		
		return episodes;
	}

	private Set<EpisodeDTO> parseEpisodesFromApiJson(JSONObject json, String language) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			if (json.has("data")) {
				JSONArray data = json.getJSONArray("data");
				for (int i = 0; i < data.length(); i++) {
					JSONObject video = data.getJSONObject(i);
					EpisodeDTO episode = createEpisodeFromVideo(video, language);
					if (episode != null) {
						episodes.add(episode);
					}
				}
			}
		} catch (Exception e) {
			getLog().error("Error parsing episodes from API JSON: " + e.getMessage(), e);
		}
		
		return episodes;
	}

	private EpisodeDTO createEpisodeFromVideo(JSONObject video, String language) {
		try {
			String title = "";
			String url = "";
			
			// Try multiple field names for title
			if (video.has("title")) {
				title = video.getString("title");
			} else if (video.has("name")) {
				title = video.getString("name");
			} else if (video.has("label")) {
				title = video.getString("label");
			}
			
			// Try multiple field names for URL
			if (video.has("url")) {
				url = video.getString("url");
			} else if (video.has("href")) {
				url = video.getString("href");
			} else if (video.has("link")) {
				url = video.getString("link");
			} else if (video.has("programId")) {
				// Build URL from program ID
				String programId = video.getString("programId");
				url = String.format("/%s/videos/%s/", language, programId);
			}
			
			if (StringUtils.isEmpty(title) || StringUtils.isEmpty(url)) {
				return null;
			}
			
			// Build the full URL
			String fullUrl = url.startsWith("http") ? url : "https://www.arte.tv" + url;
			
			// Create episode name
			String episodeName = title;
			if (video.has("durationLabel")) {
				episodeName += " (" + video.getString("durationLabel") + ")";
			} else if (video.has("duration")) {
				episodeName += " (" + video.getString("duration") + ")";
			}
			
			return new EpisodeDTO(null, episodeName, fullUrl);
			
		} catch (Exception e) {
			getLog().error("Error creating episode from video data: " + e.getMessage());
			return null;
		}
	}

	private String extractLanguageFromUrl(String url) {
		if (url.startsWith("en/")) return "en";
		if (url.startsWith("fr/")) return "fr";
		if (url.startsWith("de/")) return "de";
		if (url.startsWith("es/")) return "es";
		if (url.startsWith("pl/")) return "pl";
		if (url.startsWith("it/")) return "it";
		return "en"; // default
	}

	private String extractCategoryFromUrl(String url) {
		String[] parts = url.split("/");
		if (parts.length > 1) {
			return parts[1];
		}
		return "CIN"; // default
	}

	private String getCategorySlug(String categoryCode) {
		switch (categoryCode) {
			case "CIN": return "cinema";
			case "DOR": return "documentaries-and-reportage";
			case "CRE": return "creative";
			case "POL": return "politics-and-society";
			case "SCI": return "science";
			case "CON": return "concerts";
			case "JEU": return "youth";
			case "SPO": return "sports";
			case "REPLAY": return "replay";
			default: return "cinema";
		}
	}

	private String getCategoryName(String categoryCode, String language) {
		switch (categoryCode) {
			case "CIN": return "Cinema";
			case "DOR": return "Documentaries and Reportage";
			case "CRE": return "Creative";
			case "POL": return "Politics and Society";
			case "SCI": return "Science";
			case "CON": return "Concerts";
			case "JEU": return "Youth";
			case "SPO": return "Sports";
			case "REPLAY": return "Replay";
			default: return "Cinema";
		}
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		return downloadInput.contains("arte") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
