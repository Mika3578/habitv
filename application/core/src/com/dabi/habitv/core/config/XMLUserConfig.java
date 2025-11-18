package com.dabi.habitv.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import com.dabi.habitv.api.plugin.dto.ExportDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO;
import com.dabi.habitv.api.plugin.dto.ProxyDTO.ProtocolEnum;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.config.entities.Config;
import com.dabi.habitv.config.entities.Downloader;
import com.dabi.habitv.config.entities.Exporter;
import com.dabi.habitv.config.entities.Proxy;
import com.dabi.habitv.config.entities.TaskDefinition;
import com.dabi.habitv.configuration.entities.ConditionType;
import com.dabi.habitv.configuration.entities.Configuration;
import com.dabi.habitv.configuration.entities.Configuration.DownloadConfig;
import com.dabi.habitv.configuration.entities.Configuration.DownloadConfig.Downloaders;
import com.dabi.habitv.configuration.entities.Configuration.ExportConfig;
import com.dabi.habitv.configuration.entities.Configuration.ExportConfig.Exporters;
import com.dabi.habitv.configuration.entities.Configuration.OsConfig;
import com.dabi.habitv.configuration.entities.Configuration.Proxies;
import com.dabi.habitv.configuration.entities.Configuration.UpdateConfig;
import com.dabi.habitv.configuration.entities.Exporter.Subexporters;
import com.dabi.habitv.configuration.entities.ObjectFactory;
import com.dabi.habitv.configuration.entities.PluginSupport;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.OSUtils;
import com.dabi.habitv.utils.DirUtils;
import com.dabi.habitv.utils.FileUtils;
import com.dabi.habitv.utils.XMLUtils;

public class XMLUserConfig implements UserConfig {

	private static final int DEFAULT_MAX_ATTEMPTS = 5;

	private static final int DEFAULT_CHECK_TIME = 1800;

	private static final int DEFAULT_CUT_SIZE = 40;

	private static final String DEFAULT_DL_OUTPUT = FrameworkConf.USER_HOME
			+ "/" + "Downloads/#TVSHOW_NAME#-#EPISODE_NAME_CUT#.#EXTENSION#";

	private static final String PLUGIN_DIR = "plugins";
	private static final String BIN_DIR = "bin";
	private static final String INDEX_DIR = "index";
	private final Configuration config;

	private static final String OLD_CONF_XSD = "config.xsd";

	public static final String CONF_XSD = "configuration.xsd";

	private static final boolean DEFAULT_UPDATE_ON_STARTUP = true;

	private static final Boolean DEFAULT_AUTORISE_SNAPSHOT = false;

	private XMLUserConfig(final Configuration config) {
		super();
		this.config = config;
	}

	public static UserConfig initConfig() {
		Configuration config;
		try {
			final File oldConfFile = new File(DirUtils.getOldConfFile());
			final File confFile = new File(DirUtils.getConfFile());
			if (oldConfFile.exists()) {
				Config oldConfig = readOldConfig(oldConfFile);
				oldConfFile.delete();
				config = convertOldConfig(oldConfig);
				saveConfig(confFile, config);
			} else {
				if (!confFile.exists()) {
					config = buildConfigFile(confFile);
					saveConfig(confFile, config);
				} else {
					config = readConfig(confFile);
				}
			}

		} catch (JAXBException | UnsupportedEncodingException
				| FileNotFoundException e) {
			throw new TechnicalException(e);
		}
		return new XMLUserConfig(config);
	}

	public static void saveConfig(UserConfig userConfig) throws JAXBException,
			PropertyException {
		saveConfig(new File(DirUtils.getConfFile()),
				((XMLUserConfig) userConfig).config);
	}

	private static Configuration convertOldConfig(Config oldConfig) {
		final Configuration config = buildDefaultConfig();

		buildDownloadConfig(oldConfig, config);
		buildExportConfig(oldConfig, config);
		buildOsConfig(oldConfig, config);
		buildProxies(oldConfig, config);
		buildTaskDefinition(oldConfig, config);
		buildUpdateConfig(oldConfig, config);

		return config;
	}

	private static void buildUpdateConfig(Config oldConfig,
			final Configuration config) {
		UpdateConfig updateConfig = new UpdateConfig();
		updateConfig.setAutoriseSnapshot(DEFAULT_AUTORISE_SNAPSHOT);
		updateConfig.setUpdateOnStartup(DEFAULT_UPDATE_ON_STARTUP);
		config.setUpdateConfig(updateConfig);
	}

	private static void buildTaskDefinition(Config oldConfig,
			final Configuration config) {
		com.dabi.habitv.configuration.entities.Configuration.TaskDefinition taskDefinition = new com.dabi.habitv.configuration.entities.Configuration.TaskDefinition();
		for (TaskDefinition oldTaskDefinition : oldConfig.getTaskDefinition()) {
			taskDefinition.getAny().add(
					XMLUtils.buildAnyElement(oldTaskDefinition.getTaskName(),
							oldTaskDefinition.getSize() + ""));
		}
		config.setTaskDefinition(taskDefinition);
	}

	private static void buildProxies(Config oldConfig,
			final Configuration config) {
		Proxies proxies = new Proxies();
		for (Proxy oldProxy : oldConfig.getProxy()) {
			proxies.getProxy().add(buildProxy(oldProxy));
		}
		config.setProxies(proxies);
	}

	private static com.dabi.habitv.configuration.entities.Proxy buildProxy(
			Proxy oldProxy) {
		com.dabi.habitv.configuration.entities.Proxy proxy = new com.dabi.habitv.configuration.entities.Proxy();

		proxy.setHost(oldProxy.getHost());
		PluginSupport pluginSupport = new PluginSupport();
		for (String oldProxyPlugin : oldProxy.getPluginSupport().getPlugin()) {
			pluginSupport.getPlugin().add(oldProxyPlugin);
		}
		proxy.setPluginSupport(pluginSupport);
		proxy.setPort(oldProxy.getPort());
		proxy.setProtocol(oldProxy.getProtocol());

		return proxy;
	}

	private static void buildOsConfig(Config oldConfig,
			final Configuration config) {
		OsConfig osConfig = new OsConfig();
		osConfig.setCmdProcessor(oldConfig.getCmdProcessor());
		config.setOsConfig(osConfig);
	}

	private static void buildExportConfig(Config oldConfig,
			final Configuration config) {
		ExportConfig exportConfig = new ExportConfig();
		Exporters exporters = new Exporters();
		for (Exporter oldExporter : oldConfig.getExporter()) {
			exporters.getExporter().add(buildExporter(oldExporter));
		}
		exportConfig.setExporters(exporters);
		config.setExportConfig(exportConfig);
	}

	private static com.dabi.habitv.configuration.entities.Exporter buildExporter(
			Exporter oldExporter) {
		com.dabi.habitv.configuration.entities.Exporter exporter = new com.dabi.habitv.configuration.entities.Exporter();
		exporter.setCommand(oldExporter.getCmd());
		ConditionType conditionType = new ConditionType();
		conditionType.setPattern(oldExporter.getCondition().getPattern());
		conditionType.setReference(oldExporter.getCondition().getReference());
		exporter.setCondition(conditionType);
		exporter.setId(oldExporter.getName());
		exporter.setLabel(oldExporter.getOutput());
		Subexporters subexporters = new Subexporters();
		for (Exporter oldSubExporter : oldExporter.getExporter()) {
			subexporters.getExporter().add(buildExporter(oldSubExporter));
		}
		exporter.setSubexporters(subexporters);
		return exporter;
	}

	private static void buildDownloadConfig(Config oldConfig,
			final Configuration config) {
		DownloadConfig downloadConfig = new DownloadConfig();
		downloadConfig
				.setDemonCheckTime(oldConfig.getDemonTime() == null ? DEFAULT_CHECK_TIME
						: oldConfig.getDemonTime());
		// downloadConfig.setDownloadOuput(DEFAULT_DL_OUTPUT);
		downloadConfig.setDownloadOuput(oldConfig.getDownloadOuput());
		downloadConfig
				.setFileNameCutSize(oldConfig.getFileNameCutSize() == null ? DEFAULT_CUT_SIZE
						: oldConfig.getFileNameCutSize());
		downloadConfig.setMaxAttempts(oldConfig.getMaxAttempts());

		Downloaders downloaders = new Downloaders();
		for (Downloader oldDownloader : oldConfig.getDownloader()) {
			downloaders.getAny().add(
					XMLUtils.buildAnyElement(oldDownloader.getName(),
							oldDownloader.getBinPath()));
		}

		downloadConfig.setDownloaders(downloaders);
		config.setDownloadConfig(downloadConfig);
	}

	@SuppressWarnings("unchecked")
	private static Config readOldConfig(final File confFile)
			throws JAXBException, UnsupportedEncodingException,
			FileNotFoundException {
		Config config;
		final JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(Config.class.getPackage()
				.getName());
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		FileUtils.setValidation(unmarshaller, OLD_CONF_XSD);
		config = ((JAXBElement<Config>) unmarshaller
				.unmarshal(new InputStreamReader(new FileInputStream(confFile),
						HabitTvConf.ENCODING))).getValue();
		return config;
	}

	private static Configuration readConfig(final File confFile)
			throws JAXBException, UnsupportedEncodingException,
			FileNotFoundException {
		Configuration config;
		final JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(Configuration.class.getPackage()
				.getName());
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		FileUtils.setValidation(unmarshaller, CONF_XSD);
		config = (Configuration) unmarshaller.unmarshal(new InputStreamReader(
				new FileInputStream(confFile), HabitTvConf.ENCODING));
		return config;
	}

	private static Configuration buildConfigFile(final File file)
			throws JAXBException {
		final Configuration config = buildDefaultConfig();
		if (!OSUtils.isWindows()) {
			OsConfig osConfig = new OsConfig();
			osConfig.setCmdProcessor("/bin/bash -c #CMD#");
			config.setOsConfig(osConfig);
		}

		return config;
	}

	private static void saveConfig(final File file, final Configuration config)
			throws JAXBException, PropertyException {
		final JAXBContext jaxbContext = JAXBContext
				.newInstance(Configuration.class.getPackage().getName());
		final Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		FileUtils.setValidation(marshaller, CONF_XSD);
		marshaller.marshal(config, file);
	}

	private static Configuration buildDefaultConfig() {
		final Configuration config = (new ObjectFactory())
				.createConfiguration();

		DownloadConfig downloadConfig = new DownloadConfig();
		config.setDownloadConfig(downloadConfig);
		downloadConfig.setDemonCheckTime(DEFAULT_CHECK_TIME);
		downloadConfig.setDownloadOuput(DEFAULT_DL_OUTPUT);
		downloadConfig.setMaxAttempts(DEFAULT_MAX_ATTEMPTS);

		UpdateConfig updateConfig = new UpdateConfig();
		updateConfig.setAutoriseSnapshot(DEFAULT_AUTORISE_SNAPSHOT);
		config.setUpdateConfig(updateConfig);
		return config;
	}

	@Override
	public String getPluginDir() {
		return getAppDir() + "/" + PLUGIN_DIR;
	}

	private String getAppDir() {
		return DirUtils.getAppDir();
	}

	@Override
	public Map<String, Integer> getTaskDefinition() {
		return buildTaskName2PoolSizeMap(config.getTaskDefinition());
	}

	private Map<String, Integer> buildTaskName2PoolSizeMap(
			final com.dabi.habitv.configuration.entities.Configuration.TaskDefinition taskDefinition) {
		final Map<String, Integer> taskName2PoolSizeMap = new HashMap<>();
		if (taskDefinition != null) {
			List<Object> taskAny = taskDefinition.getAny();
			if (taskAny != null) {
				for (final Object taskDef : taskAny) {
					taskName2PoolSizeMap.put(XMLUtils.getTagName(taskDef),
							Integer.parseInt(XMLUtils.getTagValue(taskDef)));
				}
			}
		}
		return taskName2PoolSizeMap;
	}

	@Override
	public Integer getFileNameCutSize() {
		if (config.getDownloadConfig() == null
				|| config.getDownloadConfig().getFileNameCutSize() == null) {
			return DEFAULT_CUT_SIZE;
		} else {
			return config.getDownloadConfig().getFileNameCutSize();
		}
	}

	@Override
	public Map<String, Map<ProtocolEnum, ProxyDTO>> getProxy() {
		return buildProxyMap(config.getProxies());
	}

	private Map<String, Map<ProtocolEnum, ProxyDTO>> buildProxyMap(
			final Proxies proxies) {
		final Map<String, Map<ProxyDTO.ProtocolEnum, ProxyDTO>> plugin2protocol2proxy = new HashMap<>();
		if (proxies != null && !proxies.getProxy().isEmpty()) {
			for (final com.dabi.habitv.configuration.entities.Proxy proxy : proxies
					.getProxy()) {
				if (proxy.getPluginSupport() != null) {
					for (final String plugin : proxy.getPluginSupport()
							.getPlugin()) {
						setProxyByPluginName(proxy, plugin,
								plugin2protocol2proxy);
					}
				} else {
					// default proxy
					setProxyByPluginName(proxy, null, plugin2protocol2proxy);
				}
			}
		}
		return plugin2protocol2proxy;
	}

	private void setProxyByPluginName(
			final com.dabi.habitv.configuration.entities.Proxy proxy,
			final String plugin,
			final Map<String, Map<ProtocolEnum, ProxyDTO>> plugin2protocol2proxy) {
		Map<ProxyDTO.ProtocolEnum, ProxyDTO> protocol2Proxy;
		protocol2Proxy = plugin2protocol2proxy.get(plugin);
		if (protocol2Proxy == null) {
			protocol2Proxy = new HashMap<>();
			plugin2protocol2proxy.put(plugin, protocol2Proxy);
		}
		protocol2Proxy.put(ProxyDTO.ProtocolEnum.valueOf(proxy.getProtocol()),
				new ProxyDTO(proxy.getHost(), proxy.getPort()));
	}

	@Override
	public String getCmdProcessor() {
		return config.getOsConfig() == null ? null : config.getOsConfig()
				.getCmdProcessor();
	}

	@Override
	public Map<String, String> getDownloader() {
		return buildDownloadersBinPath(config.getDownloadConfig()
				.getDownloaders());
	}

	private Map<String, String> buildDownloadersBinPath(
			final Downloaders downloaders) {
		final Map<String, String> downloaderName2BinPath = new HashMap<>();
		if (downloaders != null) {
			for (final Object downloader : downloaders.getAny()) {
				String binPath = XMLUtils.getTagValue(downloader);
				if (!binPath.isEmpty() && !binPath.startsWith("/")
						&& !binPath.matches("[a-zA-Z]:.*")) {
					binPath = getAppDir() + "/" + binPath;
				}
				binPath = binPath.replace("\\", "/");
				downloaderName2BinPath.put(XMLUtils.getTagName(downloader),
						binPath);
			}
		}
		return downloaderName2BinPath;
	}

	@Override
	public String getIndexDir() {
		return getAppDir() + "/" + INDEX_DIR;
	}

	@Override
	public String getDownloadOuput() {
		return config.getDownloadConfig() == null
				|| config.getDownloadConfig().getDownloadOuput() == null ? DEFAULT_DL_OUTPUT
				: config.getDownloadConfig().getDownloadOuput();
	}

	@Override
	public List<ExportDTO> getExporter() {
		return buildExporterListDTO(config.getExportConfig());
	}

	private List<ExportDTO> buildExporterListDTO(final ExportConfig exportConfig) {
		if (exportConfig != null && exportConfig.getExporters() != null) {
			return buildExporterListDTO(exportConfig.getExporters()
					.getExporter());
		} else {
			return Collections.emptyList();
		}
	}

	private List<ExportDTO> buildExporterListDTO(
			final List<com.dabi.habitv.configuration.entities.Exporter> exporters) {
		final List<ExportDTO> exportDTOList = new ArrayList<>();
		ExportDTO exportDTO;
		String reference;
		String pattern;
		for (final com.dabi.habitv.configuration.entities.Exporter exporter : exporters) {

			if (exporter.getCondition() != null) {
				reference = exporter.getCondition().getReference();
				pattern = exporter.getCondition().getPattern();
			} else {
				reference = null;
				pattern = null;
			}
			exportDTO = new ExportDTO(reference, pattern, exporter.getId(),
					exporter.getLabel(), getCmdProcessor(),
					exporter.getCommand(), buildExporterListDTO(exporter
							.getSubexporters().getExporter()));
			exportDTOList.add(exportDTO);
		}
		return exportDTOList;
	}

	@Override
	public Integer getMaxAttempts() {
		return config.getDownloadConfig() == null ? null : config
				.getDownloadConfig().getMaxAttempts();
	}

	@Override
	public Integer getDemonCheckTime() {
		if (config.getDownloadConfig() == null
				|| config.getDownloadConfig().getDemonCheckTime() == null) {
			return DEFAULT_CHECK_TIME;
		} else {
			return config.getDownloadConfig().getDemonCheckTime();
		}
	}

	@Override
	public boolean updateOnStartup() {
		return config.getUpdateConfig() == null
				|| config.getUpdateConfig().isUpdateOnStartup() == null ? true
				: config.getUpdateConfig().isUpdateOnStartup();
	}

	@Override
	public boolean autoriseSnapshot() {
		return config.getUpdateConfig() == null
				|| config.getUpdateConfig().isAutoriseSnapshot() == null ? false
				: config.getUpdateConfig().isAutoriseSnapshot();
	}

	@Override
	public String getBinDir() {
		return getAppDir() + "/" + BIN_DIR;
	}

	@Override
	public void setMaxAttempts(int maxAttemps) {
		DownloadConfig downloadConfig = loadDownloadConfig();
		downloadConfig.setMaxAttempts(maxAttemps);
	}

	private DownloadConfig loadDownloadConfig() {
		DownloadConfig downloadConfig = config.getDownloadConfig();
		if (downloadConfig == null) {
			downloadConfig = new DownloadConfig();
			config.setDownloadConfig(downloadConfig);
		}
		return downloadConfig;
	}

	private UpdateConfig loadUpdateConfig() {
		UpdateConfig updateConfig = config.getUpdateConfig();
		if (updateConfig == null) {
			updateConfig = new UpdateConfig();
			config.setUpdateConfig(updateConfig);
		}
		return updateConfig;
	}

	@Override
	public void setUpdateOnStartup(boolean updateOnStartup) {
		UpdateConfig updateConfig = loadUpdateConfig();
		updateConfig.setUpdateOnStartup(updateOnStartup);
	}

	@Override
	public void setDownloadOuput(String downloadOuput) {
		DownloadConfig downloadConfig = loadDownloadConfig();
		downloadConfig.setDownloadOuput(downloadOuput);
	}

	@Override
	public void setDemonCheckTime(int demonCheckTime) {
		DownloadConfig downloadConfig = loadDownloadConfig();
		downloadConfig.setDemonCheckTime(demonCheckTime);
	}

}
