package com.dabi.habitv.provider.francetv;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.dabi.habitv.framework.plugin.utils.M3U8Utils;
import com.dabi.habitv.provider.francetv.francetv.FranceTvArchive;
import com.dabi.habitv.provider.francetv.francetv.FranceTvFetcher;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FranceTvPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {
	private FranceTvArchive cachedArchive;

	private long cachedTimeMs;

	private FranceTvFetcher franceTvFetcher;

	@Override
	public String getName() {
		return FranceTvConf.NAME;
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
	public FranceTvFetcher getFranceTvFetcher() {
		if (franceTvFetcher == null) {
			franceTvFetcher = new FranceTvFetcher(getHttpProxy());
		}
		return franceTvFetcher;
	}

	private FranceTvArchive getCachedArchive() {
		final long now = System.currentTimeMillis();
		if (cachedArchive == null || (now - cachedTimeMs) > FranceTvConf.MAX_CACHE_ARCHIVE_TIME_MS) {
			cachedArchive = getFranceTvFetcher().load();
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
			String urlContent = getUrlContent(String.format(FranceTvConf.WS_JSON, mediaId, mediaId));
			urlContent = urlContent.substring(urlContent.indexOf("(") + 1, urlContent.lastIndexOf(")"));
			videoData = mapper.readValue(urlContent, Map.class);
		} catch (IOException e) {
			throw new DownloadFailedException(e);
		}
		// http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=104433755&catalogue=FranceTv&callback=webserviceCallback_104433755

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
			}		}
		
		// Return the best available URL format
		if (urlHls != null) {
			return urlHls;
		} else if (urlm3u8 != null) {
			return urlm3u8;
		} else {
			return otherUrl;
		}
	}
	private String findMediaIdInPage(String downloadInput) {
		try {
			return getFranceTvFetcher().extractMediaIdFromPage(downloadInput);
		} catch (Exception e) {
			return null;
		}
	}

	private String findMediaIdInUrl(String downloadInput) {
		return getFranceTvFetcher().extractMediaIdFromUrl(downloadInput);
	}
	@Override
	public DownloadableState canDownload(String downloadInput) {
		if (downloadInput.contains("france.tv") || 
		    downloadInput.contains("france2.") || 
		    downloadInput.contains("france3.") || 
		    downloadInput.contains("france4.") ||
		    downloadInput.contains("france5.") || 
		    downloadInput.contains("pluzz.")) {
			return DownloadableState.SPECIFIC;
		} else {
			return DownloadableState.IMPOSSIBLE;
		}
	}

}
