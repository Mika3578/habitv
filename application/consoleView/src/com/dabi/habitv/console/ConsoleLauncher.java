package com.dabi.habitv.console;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.dabi.habitv.api.plugin.api.PluginBaseInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.config.XMLUserConfig;
import com.dabi.habitv.core.dao.GrabConfigDAO;
import com.dabi.habitv.core.mgr.CoreManager;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.dabi.habitv.framework.plugin.utils.ProcessingThreads;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.utils.DirUtils;
import com.dabi.habitv.utils.HabitvLogger;
import org.apache.log4j.Logger;

public final class ConsoleLauncher {
	private static final String OPTION_RUN_EXPORT = "x";

	private static final String OPTION_TEST_PLUGIN = "t";

	private static final String OPTION_LIST_PLUGIN = "lp";

	private static final String OPTION_LIST_CATEGORY = "lc";

	private static final String OPTION_LIST_EPISODE = "le";

	private static final String OPTION_CLEAN_GRABCONFIG = "k";

	private static final String OPTION_UPDATE_GRABCONFIG = "u";

	private static final String OPTION_CHECK_AND_DL = "h";

	private static final String OPTION_DAEMON = "d";

	private static final String OPTION_CATEGORY = "c";

//	private static final String OPTION_EPISODE = "e";

	private static final String OPTION_PLUGIN = "p";

	private static UserConfig config;

	private static GrabConfigDAO grabConfigDAO;

	private static CoreManager coreManager;

	private static final Logger LOG = HabitvLogger.getLogger(ConsoleLauncher.class);

	private ConsoleLauncher() {

	}

	@SuppressWarnings("static-access")
	public static void main(final String[] args) {
		if (args.length > 0 && DownloadUtils.isHttpUrl(args[0])) {
			init();
			downloadEpisodes(args);
		} else {

			Options options = new Options();

			options.addOption(OPTION_DAEMON, "deamon", false,
					"Lancement en mode démon avec scan automatique des épisodes à télécharger.");
			options.addOption(OPTION_CHECK_AND_DL, "checkAndDL", false,
					"Recherche des épisodes et lance les téléchargements.");
			options.addOption(OPTION_UPDATE_GRABCONFIG, "updateGrabConfig",
					false, "Met à jour le fichier des épisodes à télécharger.");
			options.addOption(OPTION_CLEAN_GRABCONFIG, "cleanGrabConfig",
					false,
					"Purge le fichier des épisodes à télécharger des catégories périmées.");
			options.addOption(OPTION_LIST_EPISODE, "listEpisode", false,
					"Met à jour le fichier des épisodes à télécharger.");
			options.addOption(OPTION_LIST_CATEGORY, "listCategory", false,
					"Recherche et liste les catégories des plugins.");
			options.addOption(OPTION_LIST_PLUGIN, "listPlugin", false,
					"Liste les plugins.");
//			options.addOption(OPTION_TEST_PLUGIN, "testPlugin", false,
//					"Teste le plugin avec un téléchargement aléatoire.");
			options.addOption(OPTION_RUN_EXPORT, "runExport", false,
					"Reprise des exports en échec.");

			options.addOption(OptionBuilder
					.withLongOpt("plugins")
					.hasArgs()
					.withValueSeparator()
					.withDescription(
							"Pour lister les plugins concernés par la commande, si vide tous les plugins le seront.")
					.create(OPTION_PLUGIN));

			options.addOption(OptionBuilder
					.withLongOpt("categories")
					.hasArgs()
					.withValueSeparator()
					.withDescription(
							"Pour lister les catégories concernées par la commande, si vide tous les catégories le seront.")
					.create(OPTION_CATEGORY));
//
//			options.addOption(OptionBuilder
//					.withLongOpt("episodes")
//					.hasArgs()
//					.withValueSeparator()
//					.withDescription(
//							"Pour lister les identifiants (URL)  d'épisodes concernés par la commande, si vide tous les épisodes le seront.")
//					.create(OPTION_EPISODE));

			// create the parser
			CommandLineParser parser = new BasicParser();
			// parse the command line arguments
			CommandLine line;
			try {
				line = parser.parse(options, args);

				List<String> pluginList = null;
				if (line.hasOption(OPTION_PLUGIN)) {
					pluginList = Arrays.asList(line
							.getOptionValues(OPTION_PLUGIN));
				}

				List<String> categoryList = null;
				if (line.hasOption(OPTION_CATEGORY)) {
					categoryList = Arrays.asList(line
							.getOptionValues(OPTION_CATEGORY));
				}
//
//				List<String> episodeIdList = null;
//				if (line.hasOption(OPTION_EPISODE)) {
//					episodeIdList = Arrays.asList(line
//							.getOptionValues(OPTION_EPISODE));
//				}

				init();

				if (line.hasOption(OPTION_DAEMON)) {
					daemonMode();
				} else if (line.hasOption(OPTION_CHECK_AND_DL)) {
					checkAndDLMode(pluginList, categoryList);
				} else if (line.hasOption(OPTION_UPDATE_GRABCONFIG)) {
					updateGrabConfig(pluginList);
				} else if (line.hasOption(OPTION_CLEAN_GRABCONFIG)) {
					updateGrabConfig(pluginList);
				} else if (line.hasOption(OPTION_LIST_EPISODE)) {
					listEpisode(pluginList, categoryList);
				} else if (line.hasOption(OPTION_LIST_CATEGORY)) {
					listCategory(pluginList);
				} else if (line.hasOption(OPTION_LIST_PLUGIN)) {
					listPlugin();
				} else if (line.hasOption(OPTION_TEST_PLUGIN)) {
					testPlugin(pluginList);
				} else if (line.hasOption(OPTION_RUN_EXPORT)) {
					runExport(pluginList);
				} else {
					usage(options);
				}

			} catch (ParseException e) {
				usage(options);
			} catch (final Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private static void init() {
		config = XMLUserConfig.initConfig();

		grabConfigDAO = new GrabConfigDAO(DirUtils.getGrabConfigPath());
		coreManager = new CoreManager(config);
		if (!grabConfigDAO.exist()) {
			info("Génération des catégories à télécharger");
			grabConfigDAO.saveGrabConfig(coreManager.findCategory());
		}
		if (config.updateOnStartup()) {
			coreManager.update();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				info("Interrupted, closing all treatments");
				coreManager.forceEnd();
				ProcessingThreads.killAllProcessing();
			}

		});
	}

	private static void testPlugin(List<String> pluginList) {
		info("testPlugin : " + pluginList);
	}

	private static void runExport(List<String> pluginList) {
		info("runExport : " + pluginList);
		coreManager.reTryExport(pluginList);
	}

	private static void listPlugin() {
		info("Plugin provider : ");
		for (PluginBaseInterface pluginBaseInterface : coreManager
				.getPluginManager().getProvidersHolder().getPlugins()) {
			info(pluginBaseInterface.getName());
		}
		info("");
		info("Plugin downloader : ");
		for (PluginBaseInterface pluginBaseInterface : coreManager
				.getPluginManager().getDownloadersHolder().getPlugins()) {
			info(pluginBaseInterface.getName());
		}
		info("");
		info("Plugin exporter : ");
		for (PluginBaseInterface pluginBaseInterface : coreManager
				.getPluginManager().getExportersHolder().getPlugins()) {
			info(pluginBaseInterface.getName());
		}
	}

	private static void listCategory(List<String> pluginList) {
		info("listCategory : " + pluginList);
		Map<String, CategoryDTO> plugins2Categories = coreManager
				.findCategory(pluginList);
		for (Entry<String, CategoryDTO> plugin2Categories : plugins2Categories
				.entrySet()) {
			info("Plugin : " + plugin2Categories.getKey());
			showCategories(plugin2Categories.getValue().getSubCategories(), 0);
		}
	}

	private static void showCategories(Collection<CategoryDTO> categories,
			int decalage) {
		for (CategoryDTO category : categories) {
			info(decalageSpace(decalage) + category.getId() + " - "
					+ category.getName());
			showCategories(category.getSubCategories(), decalage + 1);
		}
	}

	private static String decalageSpace(int decalage) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < decalage; i++) {
			str.append(" ");
		}
		return str.toString();
	}

	private static void listEpisode(List<String> pluginList,
			List<String> categoryList) {
		info("listEpisode : " + pluginList + " / " + categoryList);
		Map<String, CategoryDTO> plugins2Categories = coreManager
				.findCategory(pluginList);
		for (Entry<String, CategoryDTO> plugin2Categories : plugins2Categories
				.entrySet()) {
			Set<CategoryDTO> categories = plugin2Categories.getValue()
					.getSubCategories();
			int decalage = 0;
			showEpisodes(categories, decalage);
		}
	}

	private static void showEpisodes(Set<CategoryDTO> categories, int decalage) {
		for (CategoryDTO category : categories) {
			info(decalageSpace(decalage) + "category : " + category.getName());
			Collection<EpisodeDTO> episodeList = coreManager
					.findEpisodeByCategory(category);
			for (EpisodeDTO episode : episodeList) {
				info(decalageSpace(decalage) + episode.getName());
			}
			showEpisodes(category.getSubCategories(), decalage + 1);
		}
	}

	private static void updateGrabConfig(List<String> pluginList) {
		info("updateGrabConfig : " + pluginList);
		grabConfigDAO.updateGrabConfig(coreManager.findCategory(pluginList),
				pluginList);
	}

	private static void checkAndDLMode(List<String> pluginList,
			List<String> categoryList) {
		info("checkAndDLMode" + pluginList + " / " + categoryList);
		Map<String, CategoryDTO> plugins2Categories = grabConfigDAO.load();
		for (Entry<String, CategoryDTO> pluginsCategories : plugins2Categories
				.entrySet()) {
			checkAndDLMode(pluginsCategories.getValue().getSubCategories(),
					categoryList);
		}

		coreManager.retreiveEpisode(plugins2Categories);
	}

	private static boolean checkAndDLMode(Set<CategoryDTO> subCategories,
			List<String> categoryList) {
		Iterator<CategoryDTO> it = subCategories.iterator();
		boolean found = false;
		while (it.hasNext()) {
			CategoryDTO categoryDTO = it.next();
			boolean subCatFound = checkAndDLMode(categoryDTO.getSubCategories(), categoryList);
			if (!(subCatFound || categoryDTO.isSelected() || (categoryList != null && categoryList
					.contains(categoryDTO.getId())))) {
				it.remove();
			} else {
				found = true;
			}
		}
		return found;
	}

	private static void downloadEpisodes(String[] episodesUrl) {
		info("downloadEpisodes" + Arrays.asList(episodesUrl));
		for (String url : episodesUrl) {
			String name = RetrieverUtils.getTitleByUrl(url);
			coreManager.restart(new EpisodeDTO(new CategoryDTO("Manuel",
					"Manuel", "Manuel", "mp4"), name, url), false);
			// FIXME comment gérer l'exntesion ?
		}
	}

	private static void daemonMode() throws InterruptedException {
		info("daemonMode");
		if (config.getDemonCheckTime() == null) {
			coreManager.retreiveEpisode(grabConfigDAO.load());
		} else {
			final long demonTime = config.getDemonCheckTime() * 1000L;
			// demon mode
			while (true) {
				coreManager.retreiveEpisode(grabConfigDAO.load());
				Thread.sleep(demonTime);
			}
		}
	}

	private static void info(String string) {
		LOG.info(string);
	}

	private static void usage(Options options) {
		// Use the inbuilt formatter class
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("habiTv", options);
	}
}
