# Canonical media metadata and interoperable naming

Habitv historically treated provider display strings as if they were universal
media semantics. Category labels drove `#TVSHOW_NAME#`, episode display names
drove `#EPISODE_NAME#`, and `#DATE#` used the **download/current** date rather
than broadcast air date.

This document describes the additive canonical metadata model and the
**global** `MEDIA_SERVER` naming profile introduced for Plex / Jellyfin /
Kodi-friendly paths.

## Architecture (global naming, provider facts)

```text
┌─────────────────────┐     facts only      ┌──────────────────────────┐
│ Provider plugins    │ ──────────────────► │ EpisodeMetadataDTO       │
│ (francetv, arte, …) │  series/title/S/E/… │ (API, optional on DTO)   │
└─────────────────────┘                     └────────────┬─────────────┘
                                                         │
                                                         ▼
                                            ┌──────────────────────────┐
                                            │ MediaNamingService       │
                                            │  (single entry point)    │
                                            │  ├ EpisodeMetadataResolver
                                            │  ├ EpisodeTitleNormalizer│
                                            │  ├ MediaServerNamingPolicy
                                            │  └ TokenReplacer         │
                                            └────────────┬─────────────┘
                                                         │
                                                         ▼
                                            download path / tokens
```

**Rules for maintainability:**

1. **Providers never build paths.** They only fill `EpisodeMetadataDTO` (and
   legacy `EpisodeDTO` fields for compatibility).
2. **One global naming policy** lives under `application/core/.../metadata/`.
   Do not add `plugins/*/…Naming…` helpers for filenames.
3. **Adaptations are global rules**, not provider forks — for example stripping a
   leading `S8 E2013 -` from the title when `seasonNumber`/`episodeNumber` are
   already set (FranceTV often embeds those markers in `episode_title`).
4. Future provider quirks should either map into the shared DTO or, if truly
   needed, register a small **fact** normalizer — not a separate path builder.

Public entry point: `MediaNamingService.resolveOutputPath(template, episode)`
(used by `DownloadTask`).

## Legacy display fields vs semantic metadata

| Concept | Legacy (`EpisodeDTO`) | Canonical (`EpisodeMetadataDTO`) |
|---------|------------------------|----------------------------------|
| Show / series | Often approximated by `category.name` via `#TVSHOW_NAME#` | `seriesTitle` — only when a provider declares it |
| Episode title | `name` / `#EPISODE_NAME#` | `episodeTitle` (filename uses normalized form) |
| Season / episode numbers | **Not** `num` (`#NUM#` is a legacy counter) | `seasonNumber` / `episodeNumber` |
| Air / broadcast date | `episodeDate` (legacy fallback only) | `airDate` via `#AIR_DATE#` — **true broadcast only** |
| Publication / upload | often stuffed into `episodeDate` historically | `publicationDate` — never used as MEDIA_SERVER air date |
| Duration (editorial) | `durationSeconds` | `durationSeconds` (not verified file duration) |
| Description | — | `description` (future tags/NFO) |
| Thumbnail URL | — | `thumbnailUrl` (URL only; no download in this phase) |
| Language | — | `contentLanguage` when known |
| Provider id / URL | `id` | `providerEpisodeId` / `sourceUrl` (canonical public URL) |
| Download date | `#DATE#` / `#DATETIME#` | unchanged legacy tokens |

**Date rules (mandatory):**

- FranceTV `broadcast_begin_date` / `broadcasted_at` → `airDate`
- FranceTV HTML card text « Diffusé le … » → treated as broadcast → `airDate`
- TF1+ GraphQL `date` → `airDate`; `published` → `publicationDate` (**not** `airDate`)
- BFMTV `begin_date` → `airDate`
- NOVO19 `publishedAt` → `publicationDate` (**not** `airDate`)
- Arte `rights.begin` → neither (availability/rights, deferred)
- yt-dlp `upload_date` → neither as `airDate`

If `publicationDate` is set, core does **not** promote legacy `episodeDate` to
`airDate` (avoids `Series/yyyy/…` folders based on upload time).

Providers supply facts. Core (`MediaNamingService` and helpers) decides
filenames.

**Never** assume `CategoryDTO.name == seriesTitle` globally. Arte thematic
pages are a counter-example; FranceTV program leaves and 6play program
categories are intentional show-name candidates.

## Artwork

PR 1 stores at most one preferred `thumbnailUrl`. Habitv does **not** download,
embed, or write sidecars yet. Multi-image models (poster / backdrop / logo)
are deferred to a later `MediaArtworkDTO` if needed.

## Technical post-download metadata (out of scope here)

Resolution, codecs, bitrate, file size, and **verified** media duration come
from yt-dlp / ffprobe after download — not from provider editorial duration.
Container tagging (`--embed-metadata`, `--embed-thumbnail`, NFO) is deferred.

## New tokens

| Token | Meaning |
|-------|---------|
| `#SERIES_NAME#` / `#SHOW_NAME#` | Canonical series/show title |
| `#EPISODE_TITLE#` | Canonical episode title after global filename normalization |
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

### `MEDIA_SERVER` (default for new configs)

Fresh installs and missing `downloadOuput` use:

```text
{user.home}/Downloads/#MEDIA_SERVER_PATH#
```

Include `#MEDIA_SERVER_PATH#` in `downloadOuput` to keep this profile.

Rules:

1. Season + episode known:
   `Series/Season NN/Series - SNNENN - Episode title.ext`
   (redundant `Sx Ey` / `SxxExx` prefixes matching those numbers are stripped
   from the title)
2. Air date known (dated shows):
   `Series/yyyy/Series - yyyy-MM-dd - Episode title.ext`
3. Otherwise with series:
   `Series/Series - Episode title.ext`
4. Series missing: episode-title filename only

Missing parts collapse cleanly (no duplicated ` - `, no empty folders, no fake
`S00E00`). Publication dates do **not** drive rule 2.

### `LEGACY` (existing installs / custom templates)

Configured templates without `#MEDIA_SERVER_PATH#` keep historical expansion, for
example:

```text
Downloads/#TVSHOW_NAME#-#EPISODE_NAME_CUT#.#EXTENSION#
```

## Provider audit (active plugins)

| Provider | Captured now | Deferred / notes |
|----------|--------------|------------------|
| **francetv** | series (program leaf), episode title, season, episode # when API, **airDate** from broadcast, duration, id, URL; optional description/`image_url` when present in taxonomy payload | richer yt-dlp fields; spritesheets; subtitles embedding |
| **arte** | episode title (+ subtitle as description); **no** seriesTitle from thematic category | Player API: language, multi-image, chapters, rights — later enrichment PR; never use `rights.begin` as airDate |
| **6play** | program → seriesTitle, tile name → episodeTitle, channel from parent | Modern M6+ season/episode/duration/synopsis/artwork needs dedicated scraper/API — not this PR |
| **novo19** | seriesTitle (program / parent of season), episodeTitle, S/E from strict `S#E#` subtitle, duration, description, **publicationDate** from `publishedAt`, id, URL, channel=`novo19` | No thumbnail in current BFF fixtures/parser; revalidate public surface vs TF1+ before larger rewrite; never map `publishedAt` → airDate |
| **tf1plus** | seriesTitle, episode title, S/E, duration, description, thumbnail, channel, **airDate** from GraphQL `date`, **publicationDate** from `published`, id, URL | DRM/auth streams fail at yt-dlp download; never map `published` → airDate |
| **bfmtv** | seriesTitle, episode title, duration, description, thumbnail, channel, **airDate** from `begin_date`, id, URL | Full-channel RMC+ replay remains out of scope |
| **canalPlus** | legacy title/URL only | Plugin too legacy; do not modernize in naming PR |
| youtube / RSS / file | display title only | category ≠ series |

## Deferred work

- yt-dlp structured enrichment and safe `--embed-metadata` / thumbnail embed
- Optional NFO sidecars
- Technical download-result metadata (resolution, codecs, ffprobe duration)
- Arte Player API per-episode enrichment
- M6+ structured metadata modernization
- Canal+ modernization
- Multi-artwork model

## Configuration note

Existing user configs are **not** migrated: if `downloadOuput` is already set
to a LEGACY template, it stays unchanged. **New** configs (and null
`downloadOuput`) default to `{user.home}/Downloads/#MEDIA_SERVER_PATH#`.
