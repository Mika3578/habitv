package com.dabi.habitv.core.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.dabi.habitv.api.plugin.api.PluginBaseInterface;
import com.dabi.habitv.api.plugin.api.PluginClassLoaderInterface;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

class PluginsLoader {

	private static final String JAR_EXTENSION = ".jar";

	private static final String CLASS_EXTENSION = ".class";

	private static final int CLASS_EXTENSION_SIZE = CLASS_EXTENSION.length();

	private final List<File> files;

	private final List<Plugin> classPlugins;

	@SuppressWarnings("unchecked")
	PluginsLoader(final File[] files) {
		this.files = (List<File>) (files == null ? Collections.emptyList()
				: Arrays.asList(files));
		this.classPlugins = new LinkedList<>();
	}

	List<PluginBaseInterface> loadAllPlugins() {

		this.initializeLoader();

		final List<PluginBaseInterface> tmpPlugins = new ArrayList<>(
				this.classPlugins.size());
		for (final Plugin plugin : this.classPlugins) {
			try {
				final PluginBaseInterface pluginProviderInterface = plugin
						.getClassPluginProvider().getDeclaredConstructor().newInstance();
				if (PluginClassLoaderInterface.class
						.isInstance(pluginProviderInterface)) {
					((PluginClassLoaderInterface) pluginProviderInterface)
							.setClassLoader(plugin.getClassLoaders());
				}
				tmpPlugins.add(pluginProviderInterface);
			} catch (Exception e) {
				throw new TechnicalException(e);
			}
		}

		return tmpPlugins;
	}

	private void initializeLoader() {

		// Pour eviter le double chargement des plugins
		if (this.classPlugins.size() != 0) {
			return;
		}

		for (final File file : this.files) {
			if (!file.exists()) {
				break;
			}

			// recherche tous les plugins dans le fichier
			findAllPlugins(file);
		}

	}

	private void findAllPlugins(final File file) {
		if (file.getName().endsWith(JAR_EXTENSION)) {
			// Pour la comparaison de chaines
			String tmp = "";
			// Pour le contenu de l'archive jar
			Enumeration<JarEntry> enumeration;
			// Pour déterminer quels sont les interfaces implémentées
			Class<PluginBaseInterface> tmpClass = null;

			final URL url = getFileUrl(file);
			// On créer un nouveau URLClassLoader pour charger le jar qui se
			// trouve ne dehors du CLASSPATH
			try (URLClassLoader loader = new URLClassLoader(new URL[] { url }, this.getClass().getClassLoader())) {
				// On charge le jar en mémoire
				try (final JarFile jar = new JarFile(file.getAbsolutePath());) {

					// On récupére le contenu du jar
					enumeration = jar.entries();

					while (enumeration.hasMoreElements()) {

						final JarEntry nextElement = enumeration.nextElement();
						tmp = nextElement.toString();

						// On vérifie que le fichier courant est un .class (et
						// pas
						// un
						// fichier d'informations du jar )
						if (tmp.length() > CLASS_EXTENSION_SIZE
								&& tmp.substring(
										tmp.length() - CLASS_EXTENSION_SIZE)
										.compareTo(CLASS_EXTENSION) == 0) {
							tmpClass = getClass(
									tmp.substring(0,
											tmp.length() - CLASS_EXTENSION_SIZE)
											.replaceAll("/", "."), loader);
							final List<Class<?>> interfaces = getInterfaces(tmpClass);
							for (final Class<?> interfaceClass : interfaces) {
								// Une classe ne doit pas appartenir à deux
								// catégories
								// de plugins différents.
								// Si tel est le cas on ne la place que dans la
								// catégorie de la première interface correct
								// trouvée
								if (isPlugin(interfaceClass)) {
									this.classPlugins.add(new Plugin(tmpClass,
											loader));
								}
							}

						}
					}
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			} catch (final IOException e) {
				throw new TechnicalException(e);
			}
		}
	}

	private boolean isPlugin(final Class<?> clazz) {
		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			if (PluginBaseInterface.class.getName().equals(
					interfaceClass.getName())
					|| isPlugin(interfaceClass)) {
				return true;
			}
		}
		return false;
//		return PluginBaseInterface.class.isAssignableFrom(clazz);
	}

	private List<Class<?>> getInterfaces(
			final Class<PluginBaseInterface> tmpClass) {
		final List<Class<?>> interfaces = new LinkedList<>();
		addInterfaces(tmpClass, interfaces);
		return interfaces;
	}

	private void addInterfaces(final Class<?> tmpClass,
			final List<Class<?>> interfaces) {
		interfaces.addAll(Arrays.asList(tmpClass.getInterfaces()));
		if (tmpClass.getGenericSuperclass() != null) {
			addInterfaces(tmpClass.getSuperclass(), interfaces);
		}
	}

	private Class<PluginBaseInterface> getClass(final String tmp,
			final ClassLoader loader) {
		Class<PluginBaseInterface> tmpClass;
		try {
			@SuppressWarnings("unchecked")
			final Class<PluginBaseInterface> forName = (Class<PluginBaseInterface>) Class
					.forName(tmp, true, loader);
			tmpClass = forName;
		} catch (final ClassNotFoundException e) {
			throw new TechnicalException(e);
		}
		return tmpClass;
	}

	private static URL getFileUrl(final File file) {
		URL url;
		try {
			url = file.toURI().toURL();
		} catch (final MalformedURLException e) {
			throw new TechnicalException(e);
		}
		return url;
	}

}
