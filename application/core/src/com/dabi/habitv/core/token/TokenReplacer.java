package com.dabi.habitv.core.token;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.utils.FileUtils;

public final class TokenReplacer {

	private static final Logger LOG = Logger.getLogger(TokenReplacer.class);

	private static final Map<String, Replacer> REF2REPLACER = new HashMap<>();

	static {
		// EPISODE
		final Replacer episodeReplacer = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return cut(ensure(episode.getName()), params);
			}
		};
		REF2REPLACER.put("#EPISODE_NAME#", episodeReplacer);
		REF2REPLACER.put("#EPISODE#", episodeReplacer);

		final Replacer episodeInitReplacer = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return cut(episode.getName(), params);
			}
		};
		REF2REPLACER.put("#EPISODE_UNTOUCHED#", episodeInitReplacer);

		// CHANNEL
		final Replacer channelReplacer = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return cut(ensure(episode.getCategory().getPlugin()),params);
			}
		};
		REF2REPLACER.put("#CHANNEL_NAME#", channelReplacer);
		REF2REPLACER.put("#CHANNEL#", channelReplacer);
		REF2REPLACER.put("#PLUGIN#", channelReplacer);
		REF2REPLACER.put("#PROVIDER#", channelReplacer);
		REF2REPLACER.put("#PROVIDER_NAME#", channelReplacer);

		// TV SHOW
		final Replacer categoryProvider = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return cut(ensure(episode.getCategory().getName()),params);
			}
		};
		REF2REPLACER.put("#TVSHOW_NAME#", categoryProvider);
		REF2REPLACER.put("#CATEGORY_NAME#", categoryProvider);
		REF2REPLACER.put("#CATEGORY#", categoryProvider);

		// TV SHOW
		REF2REPLACER.put("#EXTENSION#", new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return ensure(episode.getCategory().getExtension());
			}
		});
		// DATE
		final Replacer dateTimeReplacer = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return new SimpleDateFormat(params.get(0)).format(new Date());
			}
		};
		REF2REPLACER.put("#DATE#", dateTimeReplacer);
		REF2REPLACER.put("#DATETIME#", dateTimeReplacer);

		// NUM
		final Replacer numReplacer = new Replacer() {

			@Override
			public String replace(final EpisodeDTO episode, final List<String> params) {
				return String.valueOf(episode.getNum());
			}
		};
		REF2REPLACER.put("#NUM#", numReplacer);
	}

	private static String ensure(final String input) {
		return FileUtils.sanitizeFilename(input);
	}

	private static String cut(final String toCut, final List<String> params) {
		if (params.size() > 0) {
			final int size = Integer.valueOf(params.get(0));
			return cut(size, toCut);
		}
		return toCut;
	}

	private static String cut(final int size, final String toCut) {
		return toCut.substring(0, Math.min(size, toCut.length()));
	}

	/**
	 * Compatibility bridge for stale inner classes compiled by other compilers
	 * expecting synthetic accessors with the access$N naming.
	 */
	static String access$0(final String input) {
		return ensure(input);
	}

	static String access$1(final String toCut, final List<String> params) {
		return cut(toCut, params);
	}

	static String access$2(final int size, final String toCut) {
		return cut(size, toCut);
	}

	public static void setCutSize(final Integer cutSize) {

		for (final Entry<String, Replacer> ref2Replacer : new ArrayList<>(REF2REPLACER.entrySet())) {
			String key = ref2Replacer.getKey();
			// remove last #
			key = key.substring(0, key.length() - 1) + "_CUT#";
			REF2REPLACER.put(key, new Replacer() {

				@Override
				public String replace(final EpisodeDTO episode, final List<String> params) {
					final String replaced = ref2Replacer.getValue().replace(episode, params);
					return cut(cutSize, replaced);
				}

			});
		}

	}

	public static String replaceAll(final String input, final EpisodeDTO episode) {
		LOG.debug(input);
		final StringBuilder ret = new StringBuilder(input);
		for (final Entry<String, Replacer> toReplace : REF2REPLACER.entrySet()) {
			LOG.debug(toReplace.getKey());
			final Pattern compile = Pattern.compile(toReplace.getKey().substring(0, toReplace.getKey().length() - 1) + "(§([^#]*))?#");
			Matcher matcher = compile.matcher(input);
			final List<String> paramList = new LinkedList<>();
			while (matcher.find()) {
				final String group = matcher.group(2);
				if (group != null) {
					paramList.add(group);
				}
			}
			matcher = compile.matcher(ret);
			int i = 0;
			while (matcher.find()) {
				final List<String> params;
				if (paramList.size() > i) {
					params = Arrays.asList(paramList.get(i).split("/"));
				} else {
					params = new ArrayList<>();
				}
				final String replaced = toReplace.getValue().replace(episode, params);
				LOG.debug("replacing : " + matcher.start() + " " + matcher.end() + " " + replaced);
				ret.replace(matcher.start(), matcher.end(), replaced);
				matcher = compile.matcher(ret);
				i++;
			}
		}
		return ret.toString();
	}

}
