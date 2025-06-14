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
			
			// Build the category URL
			String url = String.format("https://www.arte.tv/%s/videos/%s/", language, getCategorySlug(categoryCode));
			
			// Get the page content
			Document doc = Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.timeout(30000)
					.get();
			
			// Extract embedded JSON data
			String jsonData = extractJsonData(doc);
			if (jsonData != null) {
				JSONObject json = new JSONObject(jsonData);
				episodes.addAll(parseEpisodesFromJson(json, language));
			}
			
		} catch (Exception e) {
			getLog().error("Error parsing Arte category " + category.getName() + ": " + e.getMessage(), e);
		}
		
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		Set<CategoryDTO> categories = new LinkedHashSet<>();
		
		for (String language : ArteConf.LANGUAGES) {
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
		}
		
		return categories;
	}

	@Override
	public ProcessHolder download(DownloadParamDTO downloadParam, DownloaderPluginHolder downloaders) throws DownloadFailedException {
		// This would need to be implemented to actually download the video
		// For now, we'll use youtube-dl as a fallback
		return DownloadUtils.download(downloadParam, downloaders, "youtube");
	}

	@Override
	public String getName() {
		return ArteConf.NAME;
	}

	private String extractJsonData(Document doc) {
		// Look for the embedded JSON data in script tags
		Elements scripts = doc.select("script");
		for (Element script : scripts) {
			String scriptContent = script.html();
			if (scriptContent.contains("window.__INITIAL_STATE__")) {
				// Extract the JSON data from the script
				Pattern pattern = Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*({.*?});", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(scriptContent);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		return null;
	}

	private Set<EpisodeDTO> parseEpisodesFromJson(JSONObject json, String language) {
		Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		
		try {
			// Navigate through the JSON structure to find video data
			if (json.has("data")) {
				JSONObject data = json.getJSONObject("data");
				if (data.has("zones")) {
					JSONArray zones = data.getJSONArray("zones");
					for (int i = 0; i < zones.length(); i++) {
						JSONObject zone = zones.getJSONObject(i);
						if (zone.has("content") && zone.getJSONObject("content").has("data")) {
							JSONArray contentData = zone.getJSONObject("content").getJSONArray("data");
							for (int j = 0; j < contentData.length(); j++) {
								JSONObject video = contentData.getJSONObject(j);
								EpisodeDTO episode = createEpisodeFromVideo(video, language);
								if (episode != null) {
									episodes.add(episode);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			getLog().error("Error parsing episodes from JSON: " + e.getMessage(), e);
		}
		
		return episodes;
	}

	private EpisodeDTO createEpisodeFromVideo(JSONObject video, String language) {
		try {
			String programId = video.getString("programId");
			String title = video.getString("title");
			String url = video.getString("url");
			
			// Build the full URL
			String fullUrl = "https://www.arte.tv" + url;
			
			// Create episode name
			String episodeName = title;
			if (video.has("durationLabel")) {
				episodeName += " (" + video.getString("durationLabel") + ")";
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
			default: return "Cinema";
		}
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		return downloadInput.contains("arte") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
