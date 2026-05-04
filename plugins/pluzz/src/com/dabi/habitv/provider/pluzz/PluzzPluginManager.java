package com.dabi.habitv.provider.pluzz;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
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
import com.dabi.habitv.framework.plugin.utils.M3U8Utils;
import com.dabi.habitv.provider.pluzz.jpluzz.Archive;
import com.dabi.habitv.provider.pluzz.jpluzz.JsonMainParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PluzzPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private Archive cachedArchive;

	private long cachedTimeMs;

	private JsonMainParser jsonArchiveParser;

	@Override
	public String getName() {
		return PluzzConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		if (!category.getSubCategories().isEmpty()) {
			for (final CategoryDTO subCat : category.getSubCategories()) {
				episodeList.addAll(findEpisode(subCat));
			}
		}
		final Collection<EpisodeDTO> collection = getCachedArchive().getCatName2Episode().get(category.getId());
		if (collection != null) {
			for (final EpisodeDTO episode : collection) {
				final EpisodeDTO newEp = new EpisodeDTO(category, episode.getName(), episode.getId());
				newEp.setNum(episode.getNum());
				episodeList.add(newEp);
			}
		}
		return episodeList;
	}

	public JsonMainParser getJsonArchiveParser() {
		if (jsonArchiveParser == null) {
			jsonArchiveParser = new JsonMainParser(PluzzConf.MAIN_URL, getHttpProxy());
		}
		return jsonArchiveParser;
	}

	private Archive getCachedArchive() {
		final long now = System.currentTimeMillis();
		if (cachedArchive == null || (now - cachedTimeMs) > PluzzConf.MAX_CACHE_ARCHIVE_TIME_MS) {
			cachedArchive = getJsonArchiveParser().load();
			cachedTimeMs = now;
		}
		return cachedArchive;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		return new LinkedHashSet<>(getCachedArchive().getCategories());
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		String mediaId = findMediaIdInUrl(downloadParam.getDownloadInput());
		if (mediaId == null) {
			mediaId = findMediaIdInPage(downloadParam.getDownloadInput());
		}
		if (mediaId == null) {
			throw new DownloadFailedException("can't find mediaId");
		}

		String videoUrl = findVideoUrl(mediaId);

		videoUrl = M3U8Utils.keepBestQuality(videoUrl);
		return DownloadUtils.download(DownloadParamDTO.buildDownloadParam(downloadParam, videoUrl), downloaders);
	}

	@SuppressWarnings("unchecked")
	private String findVideoUrl(String mediaId) {
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> videoData;
		try {
			String urlContent = getUrlContent(String.format(PluzzConf.WS_JSON, mediaId, mediaId));
			urlContent = urlContent.substring(urlContent.indexOf("(") + 1, urlContent.lastIndexOf(")"));
			videoData = mapper.readValue(urlContent, Map.class);
		} catch (IOException e) {
			throw new DownloadFailedException(e);
		}
		// http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=104433755&catalogue=Pluzz&callback=webserviceCallback_104433755

		List<Object> videoList = (List<Object>) videoData.get("videos");
		String urlm3u8 = null;
		String urlHls = null;
		String otherUrl = null;
		for (Object object : videoList) {
			Map<String, Object> mapVideo = (Map<String, Object>) object;
			String url = (String) mapVideo.get("url");
			String format = (String) mapVideo.get("format");
			if (format.contains("m3u8")) {
				urlm3u8 = url;
			} else if (format.contains("hls_v5_os")) {
				urlHls = url;
			} else {
				otherUrl = url;
			}
		}
		return urlHls == null ? (urlm3u8 == null ? otherUrl : urlm3u8) : urlHls;
	}

	private String findMediaIdInPage(String downloadInput) {
		// http://www.france2.fr/emissions/qui-sera-le-prochain-grand-patissier
		// <a class="video" id="ftv_player_104433710"

		final org.jsoup.nodes.Document doc = Jsoup.parse(getUrlContent(downloadInput));

		Elements select = doc.select("a.video");

		for (final Element aVideoElement : select) {
			String id = aVideoElement.attr("id");
			if (id.startsWith("ftv_player_")) {
				return id.replace("ftv_player_", "");
			}
		}
		return null;
	}

	private String findMediaIdInUrl(String downloadInput) {
		String mediaId;
		try {
			// http://pluzz.francetv.fr/videos/marsupilami_saison5_ep6_,104497701.html
			mediaId = downloadInput.split(",")[1].split("\\.")[0];
			Integer.valueOf(mediaId);
		} catch (Exception e) {
			// www.france2.fr/emissions/envoye-special-la-suite/videos/104433874?origin=ftvsite_homepage
			String[] slashSplit = downloadInput.split("/");
			mediaId = slashSplit[slashSplit.length - 1].split("\\?")[0];
			Integer.valueOf(mediaId);
		}
		return mediaId;
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		if (downloadInput.contains("france2.") || downloadInput.contains("france3.") || downloadInput.contains("france4.")
		        || downloadInput.contains("france5.") || downloadInput.contains("pluzz.") || downloadInput.contains("france.tv")) {
			return DownloadableState.SPECIFIC;
		} else {
			return DownloadableState.IMPOSSIBLE;
		}
	}

}
