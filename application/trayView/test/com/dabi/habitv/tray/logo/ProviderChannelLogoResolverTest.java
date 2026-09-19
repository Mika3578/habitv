package com.dabi.habitv.tray.logo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class ProviderChannelLogoResolverTest {

	private static final class RecordingProbe implements ClasspathResourceProbe {
		private final Set<String> existing = new HashSet<>();

		void add(final String path) {
			existing.add(path);
		}

		@Override
		public boolean exists(final String classpathResource) {
			return existing.contains(classpathResource);
		}
	}

	@Test
	public void resolvesProviderLogoForRootNode() {
		final RecordingProbe probe = new RecordingProbe();
		probe.add("icons/providers/francetv.png");
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(probe);
		final CategoryDTO root = new CategoryDTO("francetv", new HashSet<CategoryDTO>());

		final Optional<String> path = resolver.resolveClasspathResource(root);

		assertTrue(path.isPresent());
		assertEquals("icons/providers/francetv.png", path.get());
	}

	@Test
	public void resolvesFranceTvChannelFromUrlSlug() {
		final RecordingProbe probe = new RecordingProbe();
		probe.add("icons/channels/francetv/france-2.png");
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(probe);
		final CategoryDTO root = new CategoryDTO("francetv", new HashSet<CategoryDTO>());
		final CategoryDTO channel = new CategoryDTO("francetv", "France 2",
				"https://www.france.tv/france-2/", "mp4");
		root.addSubCategory(channel);

		final Optional<String> path = resolver.resolveClasspathResource(channel);

		assertTrue(path.isPresent());
		assertEquals("icons/channels/francetv/france-2.png", path.get());
	}

	@Test
	public void resolvesSixPlayChannelFromDisplayName() {
		final RecordingProbe probe = new RecordingProbe();
		probe.add("icons/channels/6play/m6.png");
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(probe);
		final CategoryDTO root = new CategoryDTO("6play", new HashSet<CategoryDTO>());
		final CategoryDTO channel = new CategoryDTO("6play", "M6", "http://m6", "mp4");
		root.addSubCategory(channel);

		final Optional<String> path = resolver.resolveClasspathResource(channel);

		assertTrue(path.isPresent());
		assertEquals("icons/channels/6play/m6.png", path.get());
	}

	@Test
	public void returnsEmptyWhenResourceMissing() {
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(
				new RecordingProbe());
		final CategoryDTO root = new CategoryDTO("arte", new HashSet<CategoryDTO>());

		assertFalse(resolver.resolveClasspathResource(root).isPresent());
	}

	@Test
	public void returnsEmptyForNullCategory() {
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(
				new RecordingProbe());
		assertFalse(resolver.resolveClasspathResource(null).isPresent());
	}

	@Test
	public void doesNotResolveDeeperThanChannelLevel() {
		final RecordingProbe probe = new RecordingProbe();
		probe.add("icons/channels/francetv/documentaires.png");
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(probe);
		final CategoryDTO root = new CategoryDTO("francetv", new HashSet<CategoryDTO>());
		final CategoryDTO channel = new CategoryDTO("francetv", "France 2",
				"https://www.france.tv/france-2/", "mp4");
		final CategoryDTO section = new CategoryDTO("francetv", "Documentaires", "doc", "mp4");
		root.addSubCategory(channel);
		channel.addSubCategory(section);

		assertFalse(resolver.resolveClasspathResource(section).isPresent());
	}

	@Test
	public void sanitizeKeyStripsUnsafeCharacters() {
		assertEquals("france-2", ProviderChannelLogoResolver.sanitizeKey("France 2"));
		assertEquals("6ter", ProviderChannelLogoResolver.sanitizeKey("6ter"));
	}

	@Test
	public void preservesCanalPlusProviderIdCasing() {
		final RecordingProbe probe = new RecordingProbe();
		probe.add("icons/providers/canalPlus.png");
		final ProviderChannelLogoResolver resolver = new ProviderChannelLogoResolver(probe);
		final CategoryDTO root = new CategoryDTO("canalPlus", new HashSet<CategoryDTO>());

		final Optional<String> path = resolver.resolveClasspathResource(root);

		assertTrue(path.isPresent());
		assertEquals("icons/providers/canalPlus.png", path.get());
	}
}
