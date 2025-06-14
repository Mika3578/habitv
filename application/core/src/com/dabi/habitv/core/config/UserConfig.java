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

	Integer getDemonCheckTime();

	boolean updateOnStartup();

	boolean autoriseSnapshot();

	String getBinDir();

	void setMaxAttempts(int parseInt);

	void setUpdateOnStartup(boolean updateOnStartup);

	void setDownloadOuput(String downloadOuput);

	void setDemonCheckTime(int demonCheckTime);

	List<CredentialConfig> getCredentials();

	void setCredentials(List<CredentialConfig> credentials);

	void addCredential(CredentialConfig credential);

	void removeCredential(String service);

	CredentialConfig getCredential(String service);

}
