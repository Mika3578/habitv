package com.dabi.habitv.tray.logo;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.image.Image;

/**
 * Loads and reuses JavaFX {@link Image} instances for classpath logo resources.
 * Failed loads are remembered so list-cell rendering does not retry noisily.
 */
public final class LogoImageCache {

	private static final Logger LOG = Logger.getLogger(LogoImageCache.class.getName());

	private static final Object ABSENT = new Object();

	private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

	public Image getOrLoad(final String classpathResource) {
		if (classpathResource == null || classpathResource.isEmpty()) {
			return null;
		}
		final Object cached = cache.get(classpathResource);
		if (cached == ABSENT) {
			return null;
		}
		if (cached instanceof Image) {
			return (Image) cached;
		}
		final Image loaded = load(classpathResource);
		final Object toStore = loaded == null ? ABSENT : loaded;
		final Object raced = cache.putIfAbsent(classpathResource, toStore);
		if (raced == ABSENT) {
			return null;
		}
		if (raced instanceof Image) {
			return (Image) raced;
		}
		return loaded;
	}

	private static Image load(final String classpathResource) {
		InputStream stream = null;
		try {
			stream = ClassLoader.getSystemResourceAsStream(classpathResource);
			if (stream == null) {
				return null;
			}
			final Image image = new Image(stream);
			if (image.isError()) {
				LOG.log(Level.FINE, "Logo resource failed to decode: {0}", classpathResource);
				return null;
			}
			return image;
		} catch (Exception e) {
			LOG.log(Level.FINE, "Logo resource could not be loaded: " + classpathResource, e);
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ignored) {
					// ignore close failures after load attempt
				}
			}
		}
	}
}
