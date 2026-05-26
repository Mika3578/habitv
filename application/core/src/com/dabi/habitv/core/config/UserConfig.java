package com.dabi.habitv.core.config;

import java.util.List;
import java.util.Map;

import com.dabi.habitv.api.plugin.dto.ExportDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;

public interface UserConfig {

	String getPluginDir();

	Map<String, Integer> getTaskDefinition();

	Integer getFileNameCutSize();

	Map<String, Map<ProtocolEnum, ProxyDTO>> getProxy();

	String getCmdProcessor();

	Map<String, String> getDownloader();

	String getIndexDir();

	String getDownloadOuput();

	List<ExportDTO> getExporter();

	Integer getMaxAttempts();

	int getMaxConcurrentDownloads();

	Integer getDemonCheckTime();

	boolean updateOnStartup();

	boolean autoriseSnapshot();

	String getBinDir();

	String getYoutubeApiKey();

	boolean getEmbedSubtitles();

	void setMaxAttempts(int parseInt);

	void setMaxConcurrentDownloads(int maxConcurrentDownloads);

	void setUpdateOnStartup(boolean updateOnStartup);

	void setDownloadOuput(String downloadOuput);

	void setDemonCheckTime(int demonCheckTime);

	void setYoutubeApiKey(String youtubeApiKey);

	void setEmbedSubtitles(boolean embedSubtitles);

}
