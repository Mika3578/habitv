# Canonical media metadata and interoperable naming

Habitv historically treated provider display strings as if they were universal
media semantics. Category labels drove `#TVSHOW_NAME#`, episode display names
drove `#EPISODE_NAME#`, and `#DATE#` used the **download/current** date rather
than broadcast air date.

This document describes the additive canonical metadata model and the
`MEDIA_SERVER` naming profile introduced for Plex / Jellyfin / Kodi-friendly
paths.

## Legacy display fields vs semantic metadata

| Concept | Legacy (`EpisodeDTO`) | Canonical (`EpisodeMetadataDTO`) |
|---------|------------------------|----------------------------------|
| Show / series | Often approximated by `category.name` via `#TVSHOW_NAME#` | `seriesTitle` — only when a provider declares it |
| Episode title | `name` / `#EPISODE_NAME#` | `episodeTitle` |
| Season / episode numbers | **Not** `num` (`#NUM#` is a legacy counter) | `seasonNumber` / `episodeNumber` |
| Air / broadcast date | `episodeDate` (when providers set it) | `airDate` via `#AIR_DATE#` |
| Download date | `#DATE#` / `#DATETIME#` | unchanged legacy tokens |

Providers supply facts. Core (`EpisodeMetadataResolver`,
`MediaServerNamingPolicy`, `TokenReplacer`) decides filenames.

**Never** assume `CategoryDTO.name == seriesTitle` globally. Arte thematic
pages are a counter-example; FranceTV program leaves and 6play program
categories are intentional show-name candidates.

## New tokens

| Token | Meaning |
|-------|---------|
| `#SERIES_NAME#` / `#SHOW_NAME#` | Canonical series/show title |
| `#EPISODE_TITLE#` | Canonical episode title |
| `#SEASON_NUMBER#` | Season number when known |
| `#EPISODE_NUMBER#` | Episode number when known |
| `#SEASON_EPISODE#` | `S01E03` only when **both** numbers exist (never `S00E00`) |
| `#AIR_DATE§pattern#` / `#EPISODE_DATE§pattern#` | Broadcast/air date (`yyyy-MM-dd` default) |
| `#MEDIA_SERVER_PATH#` | Full relative MEDIA_SERVER path including extension |

Legacy tokens (`#TVSHOW_NAME#`, `#EPISODE_NAME#`, `#DATE#`, `#NUM#`, …) keep
their historical semantics.

Semantic tokens sanitize only filesystem-unsafe characters and **preserve**
French accents. Legacy `#TVSHOW_NAME#` / `#EPISODE_NAME#` still use the older
ASCII-stripping sanitizer for compatibility.

## Naming profiles

### `LEGACY` (default for existing installs)

Configured `downloadOuput` templates are expanded exactly as before.

Example (unchanged default style):

```text
Downloads/#TVSHOW_NAME#-#EPISODE_NAME_CUT#.#EXTENSION#
```

### `MEDIA_SERVER` (opt-in)

Include `#MEDIA_SERVER_PATH#` in `downloadOuput`, for example:

```text
Downloads/#MEDIA_SERVER_PATH#
```

Rules:

1. Season + episode known:
   `Series/Season NN/Series - SNNENN - Episode title.ext`
2. Air date known (dated shows):
   `Series/yyyy/Series - yyyy-MM-dd - Episode title.ext`
3. Otherwise with series:
   `Series/Series - Episode title.ext`
4. Series missing: episode-title filename only

Missing parts collapse cleanly (no duplicated ` - `, no empty folders, no fake
`S00E00`).

## Provider audit (active plugins)

| Provider | series/show | episode title | season | episode # | air date | duration | id / URL |
|----------|-------------|---------------|--------|-----------|----------|----------|----------|
| francetv | Program category name | `episode_title` / `title` | API `season` when &gt; 0 | only if API supplies `episode_number` | `broadcast_begin_date` | API `duration` | API id + page URL |
| arte | **not** category (thematic) | teaser title/subtitle | — | — | — | — | episode URL |
| 6play | Program category | `.tile__name` | — | — | — | — | episode URL |
| youtube | — (playlist/channel ≠ series) | video title | — | — | — | — | video URL |
| canalPlus / cstar | category label (channel-like) | title | — | — | — | — | URL |
| novo19 | catalog program when mapped | tile title | — | — | — | tile duration when present | href / asset |
| RSS / file / others | — | feed/file title | — | — | — | — | link/path |

Only FranceTV, Arte, and 6play populate `EpisodeMetadataDTO` in this phase.
Other providers remain legacy-display-only until follow-up work.

## Deferred work

- yt-dlp structured enrichment (`series`, `season_number`, `episode_number`, …)
  and safe `--embed-metadata` mapping
- Optional NFO sidecars
- Technical download-result metadata (resolution, codecs, ffprobe duration)

## Configuration note

Existing user configs are **not** migrated. Opt into MEDIA_SERVER by changing
`downloadOuput` to include `#MEDIA_SERVER_PATH#`. Fresh-install default may
switch later in a dedicated change.
