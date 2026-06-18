package com.dabi.habitv.provider.tf1plus;



import java.io.IOException;

import java.util.Arrays;

import java.util.Calendar;

import java.util.Date;

import java.util.HashMap;

import java.util.HashSet;

import java.util.LinkedHashMap;

import java.util.LinkedHashSet;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.Set;

import java.util.regex.Matcher;

import java.util.regex.Pattern;



import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;



import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;

import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;

import com.dabi.habitv.api.plugin.holder.ProcessHolder;

import com.dabi.habitv.framework.FrameworkConf;

import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

import com.dabi.habitv.framework.plugin.utils.DownloadUtils;



public class Tf1PlusPluginManager extends BasePluginWithProxy implements PluginProviderInterface, PluginDownloaderInterface { // NO_UCD



	private static final Logger LOG = Logger.getLogger(Tf1PlusPluginManager.class);

	private static final Pattern DURATION_HHMMSS_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{2})$");

	private static final Pattern VIDEO_PAGE_PATTERN = Pattern.compile(

			Pattern.quote(Tf1PlusConf.HOME_URL) + "/[^/]+/([^/]+)/videos/([^/?#]+)\\.html");

	private static final Pattern DURATION_TEXT_PATTERN = Pattern.compile("(\\d+)\\s*(h|mn|min|s)");

	private static final Pattern TITLE_DATE_PATTERN = Pattern.compile("\\bdu\\s+(?:[a-zéû]+\\s+)?(\\d{1,2})\\s+([a-zéû]+)\\s+(\\d{4})\\b", Pattern.CASE_INSENSITIVE);

	private static final Pattern EPISODE_CTA_PREFIX_PATTERN = Pattern.compile("^(regarder la vid[ée]o|voir la vid[ée]o|lecture|play)\\s+", Pattern.CASE_INSENSITIVE);

	private static final Set<String> EXCLUDED_EPISODE_LABELS = new HashSet<>(Arrays.asList("regarder", "voir plus", "se connecter", "mon compte", "s'abonner"));

	private static final Map<String, Integer> FRENCH_MONTHS = createFrenchMonths();

	private final Tf1PlusGraphqlClient graphqlClient = new Tf1PlusGraphqlClient(this);

	private final Tf1PlusCatalogueService catalogueService = createCatalogueService();

	protected Tf1PlusCatalogueService createCatalogueService() {
		return new Tf1PlusCatalogueService(new Tf1PlusCatalogueClient(graphqlClient), new Tf1PlusCatalogueCache(),
				new Tf1PlusTreeBuilder(), new Tf1PlusHtmlCatalogueSupplement(new Tf1PlusHtmlCatalogueSupplement.UrlContentProvider() {
					@Override
					public String getUrlContent(final String url) {
						return Tf1PlusPluginManager.this.getUrlContent(url);
					}
				}));
	}



	@Override

	public String getName() {

		return Tf1PlusConf.NAME;

	}



	@Override

	public DownloadableState canDownload(final String downloadInput) {

		if (downloadInput != null && downloadInput.contains("tf1.fr")) {

			return DownloadableState.SPECIFIC;

		}

		return DownloadableState.IMPOSSIBLE;

	}



	@Override

	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)

			throws DownloadFailedException {

		final String downloadInput = downloadParam.getDownloadInput();

		if (Tf1PlusEpisodeUrl.requiresPremiumDownload(downloadInput)) {

			LOG.info("TF1+ download route=premium-replay fragment="

					+ Tf1PlusEpisodeUrl.parsePremiumStreamId(downloadInput) + " config="

					+ Tf1PlusPremiumDownloadConfig.configurationStatusForLog());

			return Tf1PlusPremiumDownloadExecutor.download(downloadParam, downloaders);

		}

		final String streamId = resolveStreamIdForPremiumDownload(downloadInput);

		if (StringUtils.isNotEmpty(streamId)) {

			if (!isPremiumDownloadEnabled()) {

				LOG.warn("TF1+ premium replay requires local setup (streamId=" + streamId + ") config="

						+ Tf1PlusPremiumDownloadConfig.configurationStatusForLog());

				throw new DownloadFailedException(new IllegalStateException(

						Tf1PlusPremiumDownloadConfig.userFacingConfigurationMessage()));

			}

			LOG.info("TF1+ download route=premium-replay streamId=" + streamId + " config="

					+ Tf1PlusPremiumDownloadConfig.configurationStatusForLog());

			return Tf1PlusPremiumDownloadExecutor.download(

					DownloadParamDTO.buildDownloadParam(downloadParam,

							Tf1PlusEpisodeUrl.withPremiumStreamId(downloadInput, streamId)),

					downloaders);

		}

		if (isPremiumDownloadEnabled()) {

			if (!isYtDlpEligibleEpisodeUrl(downloadInput)) {

				LOG.warn("TF1+ premium replay is configured but no stream id was resolved for "

						+ Tf1PlusEpisodeUrl.pageUrlWithoutFragment(downloadInput));

				throw new DownloadFailedException(new IllegalStateException(

						Tf1PlusConf.USER_MESSAGE_PREMIUM_REPLAY));

			}

		}

		LOG.info("TF1+ download route=yt-dlp config="

				+ Tf1PlusPremiumDownloadConfig.configurationStatusForLog() + " url="

				+ Tf1PlusEpisodeUrl.pageUrlWithoutFragment(downloadInput));

		return DownloadUtils.download(

				DownloadParamDTO.buildDownloadParam(downloadParam,

						Tf1PlusEpisodeUrl.pageUrlWithoutFragment(downloadInput)),

				downloaders,

				FrameworkConf.YOUTUBE);

	}



	private String resolveStreamIdForPremiumDownload(final String episodeUrl) {

		final Matcher matcher = VIDEO_PAGE_PATTERN.matcher(Tf1PlusEpisodeUrl.pageUrlWithoutFragment(episodeUrl));

		if (!matcher.find()) {

			return null;

		}

		final String programSlug = matcher.group(1);

		final String videoSlug = matcher.group(2);

		try {

			final Map<String, Object> videoBySlug = graphqlClient.fetchVideoByVideoSlug(programSlug, videoSlug);

			if (!videoBySlug.isEmpty()) {

				if (isYtDlpEligible(videoBySlug)) {

					return null;

				}

				final String premiumDeliveryId = Tf1PlusGraphqlClient.resolvePremiumDeliveryId(videoBySlug);

				if (StringUtils.isNotEmpty(premiumDeliveryId)) {

					return premiumDeliveryId;

				}

				return Tf1PlusGraphqlClient.stringValue(videoBySlug.get("streamId"));

			}

			final List<Map<String, Object>> videos = graphqlClient.fetchVideosByProgram(programSlug);

			for (final Map<String, Object> video : videos) {

				if (!videoSlug.equals(Tf1PlusGraphqlClient.stringValue(video.get("slug")))) {

					continue;

				}

				if (isYtDlpEligible(video)) {

					return null;

				}

				final String premiumDeliveryId = Tf1PlusGraphqlClient.resolvePremiumDeliveryId(video);

				if (StringUtils.isNotEmpty(premiumDeliveryId)) {

					return premiumDeliveryId;

				}

				return Tf1PlusGraphqlClient.stringValue(video.get("streamId"));

			}

			return null;

		} catch (IOException e) {

			LOG.warn("Unable to resolve TF1+ stream id for premium replay download: " + e.getMessage());

			return null;

		}

	}



	protected boolean isPremiumDownloadEnabled() {

		return Tf1PlusPremiumDownloadConfig.isConfigured();

	}



	@Override

	public Set<CategoryDTO> findCategory() {

		return catalogueService.buildCategoryTree();

	}



	@Override

	public Set<EpisodeDTO> findEpisode(CategoryDTO category) {

		final String channelSlug = extractChannelSlugFromProgramUrl(category.getId());

		final String programSlug = extractProgramSlug(category.getId(), channelSlug);

		if (StringUtils.isNotEmpty(programSlug)) {

			try {

				final Set<EpisodeDTO> graphqlEpisodes = findEpisodesFromGraphql(category, channelSlug, programSlug);

				if (!graphqlEpisodes.isEmpty()) {

					return graphqlEpisodes;

				}

			} catch (IOException e) {

				LOG.debug("TF1+ GraphQL episode discovery failed for " + programSlug + ": " + e.getMessage());

			}

		}

		return findEpisodesFromHtml(category);

	}



	private Set<EpisodeDTO> findEpisodesFromGraphql(final CategoryDTO category, final String channelSlug,

			final String programSlug) throws IOException {

		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();

		final Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics(category.getName(), Tf1PlusDiagnostics.STRATEGY_GRAPHQL);

		final Map<String, Object> variables = buildVideoVariablesForDiagnostics(programSlug);

		diagnostics.setSourceUrl(graphqlClient.graphqlSourceUrl(Tf1PlusConf.GRAPHQL_VIDEOS_BY_PROGRAM, variables));



		final List<Map<String, Object>> videos = graphqlClient.fetchVideosByProgram(programSlug);

		for (final Map<String, Object> video : videos) {

			diagnostics.incrementCandidateLinks();

			if (!hasDownloadableRights(video)) {

				diagnostics.incrementRejectedLinks();

				continue;

			}

			final String premiumDeliveryId = Tf1PlusGraphqlClient.resolvePremiumDeliveryId(video);

			final boolean premiumEpisode = shouldUsePremiumReplay(video);

			final String videoSlug = Tf1PlusGraphqlClient.stringValue(video.get("slug"));

			if (StringUtils.isEmpty(videoSlug)) {

				diagnostics.incrementRejectedLinks();

				continue;

			}

			final Map<String, Object> decoration = Tf1PlusGraphqlClient.asMap(video.get("decoration"));

			final Map<String, Object> playingInfos = Tf1PlusGraphqlClient.asMap(video.get("playingInfos"));

			String title = cleanEpisodeTitle(Tf1PlusGraphqlClient.stringValue(decoration.get("label")));

			if (StringUtils.isEmpty(title)) {

				title = cleanEpisodeTitle(Tf1PlusGraphqlClient.stringValue(video.get("slug")));

			}

			if (!isValidEpisodeLabel(title)) {

				diagnostics.incrementRejectedLinks();

				continue;

			}

			String url = buildVideoUrl(channelSlug, programSlug, videoSlug);

			if (premiumEpisode && StringUtils.isNotEmpty(premiumDeliveryId)) {

				url = Tf1PlusEpisodeUrl.withPremiumStreamId(url, premiumDeliveryId);

			}

			final EpisodeDTO episode = new EpisodeDTO(category, title, url);

			final Date episodeDate = parseEpisodeDate(Tf1PlusGraphqlClient.stringValue(video.get("date")));

			if (episodeDate != null) {

				episode.setEpisodeDate(episodeDate);

			} else {

				final Date titleDate = parseEpisodeDateFromTitle(title);

				if (titleDate != null) {

					episode.setEpisodeDate(titleDate);

				}

			}

			final Long durationSeconds = parseDurationSeconds(Tf1PlusGraphqlClient.stringValue(playingInfos.get("duration")));

			if (durationSeconds != null) {

				episode.setDurationSeconds(durationSeconds);

			}

			episodes.add(episode);

		}

		diagnostics.setCreatedEpisodes(episodes.size());

		if (episodes.isEmpty()) {

			diagnostics.setRootCauseSummary(isPremiumDownloadEnabled() ? "no-downloadable-rights-episodes-found"

					: "no-basic-rights-episodes-found");

			LOG.warn(Tf1PlusConf.USER_MESSAGE_UNAVAILABLE);

		}

		LOG.info(diagnostics.formatLogLine());

		return episodes;

	}



	private Set<EpisodeDTO> findEpisodesFromHtml(final CategoryDTO category) {

		Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();

		Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics(category.getName(), Tf1PlusDiagnostics.STRATEGY_HTML);

		diagnostics.setSourceUrl(category.getId());

		try {

			String content = getUrlContent(category.getId());

			if (StringUtils.isEmpty(content)) {

				diagnostics.setRootCauseSummary("empty-replay-payload");

				LOG.warn(Tf1PlusConf.USER_MESSAGE_UNAVAILABLE);

				LOG.info(diagnostics.formatLogLine());

				return episodes;

			}

			Document doc = Jsoup.parse(content, category.getId());

			Elements anchors = doc.select("a[href*=/videos/]");

			for (Element anchor : anchors) {

				diagnostics.incrementAnchorsScanned();

				String url = normalizeEpisodeUrl(anchor.absUrl("href"));

				if (StringUtils.isEmpty(url)) {

					diagnostics.incrementRejectedLinks();

					continue;

				}

				diagnostics.incrementCandidateLinks();

				Element entry = findEpisodeContainer(anchor);

				String title = extractEpisodeLabel(anchor);

				if (StringUtils.isEmpty(title)) {

					title = extractEpisodeLabel(entry);

				}

				title = cleanEpisodeTitle(title);

				if (!isValidEpisodeLabel(title)) {

					diagnostics.incrementRejectedLinks();

					continue;

				}

				if (!isHtmlEpisodeDownloadable(url)) {

					diagnostics.incrementRejectedLinks();

					continue;

				}

				String date = extractEpisodeDateValue(entry);

				String duration = extractEpisodeDurationValue(entry);

				EpisodeDTO episode = new EpisodeDTO(category, title, url);

				Date episodeDate = parseEpisodeDate(date);

				if (episodeDate == null) {

					episodeDate = parseEpisodeDateFromTitle(title);

				}

				if (episodeDate != null) {

					episode.setEpisodeDate(episodeDate);

				}

				Long durationSeconds = parseDurationSeconds(duration);

				if (durationSeconds != null) {

					episode.setDurationSeconds(durationSeconds);

				}

				episodes.add(episode);

			}

			if (episodes.isEmpty()) {

				Elements entries = doc.select("article, li, div[class*=card]");

				for (Element entry : entries) {

					diagnostics.incrementAnchorsScanned();

					Element link = entry.selectFirst("a[href*=/videos/]");

					String url = link == null ? "" : normalizeEpisodeUrl(link.absUrl("href"));

					String title = cleanEpisodeTitle(extractEpisodeLabel(entry));

					if (StringUtils.isEmpty(url) || !isValidEpisodeLabel(title)) {

						diagnostics.incrementRejectedLinks();

						continue;

					}

					if (!isHtmlEpisodeDownloadable(url)) {

						diagnostics.incrementRejectedLinks();

						continue;

					}

					diagnostics.incrementCandidateLinks();

					EpisodeDTO episode = new EpisodeDTO(category, title, url);

					Date episodeDate = parseEpisodeDate(extractEpisodeDateValue(entry));

					if (episodeDate == null) {

						episodeDate = parseEpisodeDateFromTitle(title);

					}

					if (episodeDate != null) {

						episode.setEpisodeDate(episodeDate);

					}

					Long durationSeconds = parseDurationSeconds(extractEpisodeDurationValue(entry));

					if (durationSeconds != null) {

						episode.setDurationSeconds(durationSeconds);

					}

					episodes.add(episode);

				}

			}

			diagnostics.setCreatedEpisodes(episodes.size());

			if (episodes.isEmpty()) {

				diagnostics.setRootCauseSummary("no-episodes-found");

				LOG.warn(Tf1PlusConf.USER_MESSAGE_UNAVAILABLE);

			}

			LOG.info(diagnostics.formatLogLine());

		} catch (RuntimeException e) {

			diagnostics.setRootCauseSummary("replay-parsing-failure");

			LOG.warn(Tf1PlusConf.USER_MESSAGE_UNAVAILABLE);

			LOG.info(diagnostics.formatLogLine() + " exception=" + e.getClass().getSimpleName());

		}

		return episodes;

	}



	private boolean hasDownloadableRights(final Map<String, Object> video) {

		return Tf1PlusRights.hasDownloadableRights(video, isPremiumDownloadEnabled());

	}

	private boolean shouldUsePremiumReplay(final Map<String, Object> video) {

		return Tf1PlusRights.shouldUsePremiumReplay(video, isPremiumDownloadEnabled());

	}

	private boolean isYtDlpEligible(final Map<String, Object> video) {

		return Tf1PlusRights.isYtDlpEligible(video);

	}

	private boolean isYtDlpEligibleEpisodeUrl(final String episodeUrl) {

		final Matcher matcher = VIDEO_PAGE_PATTERN.matcher(Tf1PlusEpisodeUrl.pageUrlWithoutFragment(episodeUrl));

		if (!matcher.find()) {

			return false;

		}

		try {

			final Map<String, Object> video = graphqlClient.fetchVideoByVideoSlug(matcher.group(1), matcher.group(2));

			return !video.isEmpty() && isYtDlpEligible(video);

		} catch (IOException e) {

			return false;

		}

	}

	private boolean isHtmlEpisodeDownloadable(final String episodeUrl) {

		final Matcher matcher = VIDEO_PAGE_PATTERN.matcher(Tf1PlusEpisodeUrl.pageUrlWithoutFragment(episodeUrl));

		if (!matcher.find()) {

			return false;

		}

		try {

			final Map<String, Object> video = graphqlClient.fetchVideoByVideoSlug(matcher.group(1), matcher.group(2));

			return !video.isEmpty() && hasDownloadableRights(video);

		} catch (IOException e) {

			LOG.debug("TF1+ HTML episode rights check failed for " + episodeUrl + ": " + e.getMessage());

			return false;

		}

	}



	private Map<String, Object> buildVideoVariablesForDiagnostics(final String programSlug) {

		final Map<String, Object> variables = new LinkedHashMap<String, Object>();

		variables.put("programSlug", Tf1PlusProgramSlug.resolveForGraphql(programSlug));

		variables.put("offset", Integer.valueOf(0));

		variables.put("limit", Integer.valueOf(500));

		final Map<String, Object> sort = new LinkedHashMap<String, Object>();

		sort.put("type", "DATE");

		sort.put("order", "DESC");

		variables.put("sort", sort);

		variables.put("types", Arrays.asList("REPLAY"));

		return variables;

	}



	static String buildProgramUrl(final String channelSlug, final String programSlug) {
		return Tf1PlusCatalogueClient.buildProgramUrl(channelSlug, programSlug);
	}



	static String buildVideoUrl(final String channelSlug, final String programSlug, final String videoSlug) {

		return buildProgramUrl(channelSlug, programSlug) + "/videos/" + videoSlug + ".html";

	}



	private String extractChannelSlugFromProgramUrl(final String programUrl) {

		if (StringUtils.isEmpty(programUrl) || !programUrl.startsWith(Tf1PlusConf.HOME_URL + "/")) {

			return extractChannelSlug(programUrl);

		}

		String normalized = programUrl.substring(Tf1PlusConf.HOME_URL.length() + 1);

		int slash = normalized.indexOf('/');

		if (slash <= 0) {

			return normalized.split("#")[0];

		}

		return normalized.substring(0, slash);

	}



	private String extractProgramSlug(final String programUrl, final String channelSlug) {

		if (StringUtils.isEmpty(programUrl) || StringUtils.isEmpty(channelSlug)) {

			return "";

		}

		final String prefix = Tf1PlusConf.HOME_URL + "/" + channelSlug + "/";

		if (!programUrl.startsWith(prefix)) {

			return "";

		}

		String path = programUrl.substring(prefix.length());

		if (path.contains("/") || path.contains("#")) {

			path = path.split("/")[0].split("#")[0];

		}

		return path.toLowerCase(Locale.ROOT).equals("replay") ? "" : path;

	}



	private String extractChannelSlug(String channelUrl) {

		if (StringUtils.isEmpty(channelUrl)) {

			return "";

		}

		String normalized = channelUrl.toLowerCase(Locale.ROOT);

		if (normalized.startsWith(Tf1PlusConf.HOME_URL)) {

			normalized = normalized.substring(Tf1PlusConf.HOME_URL.length());

		}

		String[] parts = normalized.split("/");

		for (String part : parts) {

			if (StringUtils.isNotEmpty(part) && !part.contains("#")) {

				return part;

			}

		}

		return "";

	}



	private String normalizeLabel(String value) {

		if (StringUtils.isEmpty(value)) {

			return "";

		}

		String normalized = value.replaceAll("\\s+", " ").trim();

		return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;

	}



	private String normalizeEpisodeUrl(String href) {

		if (StringUtils.isEmpty(href) || !href.startsWith(Tf1PlusConf.HOME_URL) || !href.contains("/videos/")) {

			return "";

		}

		String clean = href.split("\\?")[0].split("#")[0];

		String lower = clean.toLowerCase(Locale.ROOT);

		if (lower.contains("/compte/") || lower.contains("/recherche") || lower.endsWith("/videos")) {

			return "";

		}

		return clean.endsWith("/") ? clean.substring(0, clean.length() - 1) : clean;

	}



	private Element findEpisodeContainer(Element anchor) {

		Element container = anchor.closest("article, li, div[class*=card]");

		return container == null ? anchor : container;

	}



	private String extractEpisodeDateValue(Element entry) {

		String date = entry.select("time[datetime]").attr("datetime");

		if (StringUtils.isNotEmpty(date)) {

			return date;

		}

		return entry.select("[data-date], [datetime]").attr("data-date");

	}



	private String extractEpisodeDurationValue(Element entry) {

		String duration = entry.select("[class*=duration], time[aria-label*=dur]").text();

		if (StringUtils.isNotEmpty(duration)) {

			return duration;

		}

		return entry.select("[data-duration]").attr("data-duration");

	}



	private boolean isValidEpisodeLabel(String title) {

		if (StringUtils.isEmpty(title)) {

			return false;

		}

		String normalized = title.toLowerCase(Locale.ROOT).trim();

		return !EXCLUDED_EPISODE_LABELS.contains(normalized);

	}



	private String cleanEpisodeTitle(String rawTitle) {

		if (StringUtils.isEmpty(rawTitle)) {

			return "";

		}

		String cleaned = normalizeLabel(rawTitle);

		Matcher matcher = EPISODE_CTA_PREFIX_PATTERN.matcher(cleaned);

		while (matcher.find()) {

			cleaned = cleaned.substring(matcher.end()).trim();

			matcher = EPISODE_CTA_PREFIX_PATTERN.matcher(cleaned);

		}

		return normalizeLabel(cleaned);

	}



	private Date parseEpisodeDate(String rawDate) {

		if (StringUtils.isEmpty(rawDate)) {

			return null;

		}

		String normalized = rawDate.trim();

		if (normalized.length() >= 10) {

			normalized = normalized.substring(0, 10);

		}

		String[] parts = normalized.split("-");

		if (parts.length != 3) {

			return null;

		}

		try {

			int year = Integer.parseInt(parts[0]);

			int month = Integer.parseInt(parts[1]);

			int day = Integer.parseInt(parts[2]);

			Calendar calendar = Calendar.getInstance();

			calendar.setLenient(false);

			calendar.set(Calendar.YEAR, year);

			calendar.set(Calendar.MONTH, month - 1);

			calendar.set(Calendar.DAY_OF_MONTH, day);

			calendar.set(Calendar.HOUR_OF_DAY, 0);

			calendar.set(Calendar.MINUTE, 0);

			calendar.set(Calendar.SECOND, 0);

			calendar.set(Calendar.MILLISECOND, 0);

			return calendar.getTime();

		} catch (RuntimeException e) {

			LOG.debug("TF1+ unable to parse episode date: " + rawDate);

			return null;

		}

	}



	private Date parseEpisodeDateFromTitle(String title) {

		if (StringUtils.isEmpty(title)) {

			return null;

		}

		Matcher matcher = TITLE_DATE_PATTERN.matcher(title.toLowerCase(Locale.ROOT));

		if (!matcher.find()) {

			return null;

		}

		try {

			int day = Integer.parseInt(matcher.group(1));

			Integer month = FRENCH_MONTHS.get(matcher.group(2));

			int year = Integer.parseInt(matcher.group(3));

			if (month == null) {

				return null;

			}

			Calendar calendar = Calendar.getInstance();

			calendar.setLenient(false);

			calendar.set(Calendar.YEAR, year);

			calendar.set(Calendar.MONTH, month.intValue() - 1);

			calendar.set(Calendar.DAY_OF_MONTH, day);

			calendar.set(Calendar.HOUR_OF_DAY, 0);

			calendar.set(Calendar.MINUTE, 0);

			calendar.set(Calendar.SECOND, 0);

			calendar.set(Calendar.MILLISECOND, 0);

			return calendar.getTime();

		} catch (RuntimeException e) {

			LOG.debug("TF1+ unable to parse date from title: " + title);

			return null;

		}

	}



	private Long parseDurationSeconds(String rawDuration) {

		if (StringUtils.isEmpty(rawDuration)) {

			return null;

		}

		String normalized = rawDuration.trim().toLowerCase(Locale.ROOT);

		Matcher hhmmssMatcher = DURATION_HHMMSS_PATTERN.matcher(normalized);

		if (hhmmssMatcher.matches()) {

			long hours = Long.parseLong(hhmmssMatcher.group(1));

			long minutes = Long.parseLong(hhmmssMatcher.group(2));

			long seconds = Long.parseLong(hhmmssMatcher.group(3));

			return Long.valueOf(hours * 3600L + minutes * 60L + seconds);

		}

		if (normalized.matches("^\\d+$")) {

			return Long.valueOf(Long.parseLong(normalized));

		}

		Matcher textMatcher = DURATION_TEXT_PATTERN.matcher(normalized);

		long total = 0L;

		boolean found = false;

		while (textMatcher.find()) {

			found = true;

			long value = Long.parseLong(textMatcher.group(1));

			String unit = textMatcher.group(2);

			if ("h".equals(unit)) {

				total += value * 3600L;

			} else if ("mn".equals(unit) || "min".equals(unit)) {

				total += value * 60L;

			} else if ("s".equals(unit)) {

				total += value;

			}

		}

		return found ? Long.valueOf(total) : null;

	}



	private String extractEpisodeLabel(Element node) {

		String title = node.select("h1, h2, h3, h4, [class*=title], [data-testid*=title]").text();

		if (StringUtils.isEmpty(title)) {

			title = node.attr("title");

		}

		if (StringUtils.isEmpty(title)) {

			title = node.attr("aria-label");

		}

		if (StringUtils.isEmpty(title)) {

			title = node.ownText();

		}

		return normalizeLabel(title);

	}



	private static Map<String, Integer> createFrenchMonths() {

		Map<String, Integer> months = new HashMap<String, Integer>();

		months.put("janvier", Integer.valueOf(1));

		months.put("fevrier", Integer.valueOf(2));

		months.put("février", Integer.valueOf(2));

		months.put("mars", Integer.valueOf(3));

		months.put("avril", Integer.valueOf(4));

		months.put("mai", Integer.valueOf(5));

		months.put("juin", Integer.valueOf(6));

		months.put("juillet", Integer.valueOf(7));

		months.put("aout", Integer.valueOf(8));

		months.put("août", Integer.valueOf(8));

		months.put("septembre", Integer.valueOf(9));

		months.put("octobre", Integer.valueOf(10));

		months.put("novembre", Integer.valueOf(11));

		months.put("decembre", Integer.valueOf(12));

		months.put("décembre", Integer.valueOf(12));

		return months;

	}

}
