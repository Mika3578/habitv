# Automatic category watch and download

This document describes how Habitv decides which replay episodes to download
when categories are selected for automatic watching. It reflects the current
implementation in `application/core` (`SearchTask`, `DownloadedDAO`).

---

## Behavior overview

1. **Selected categories** — In the grab configuration, categories marked
   selected (`category.isSelected()`) and downloadable are scanned on the search
   task schedule (daemon interval from `configuration.xml`, e.g.
   `demonCheckTime`).
2. **Provider listing** — Each scan calls the provider plugin to list episodes
   for the category (`provider.findEpisode(category)`).
3. **Index file** — Per category, Habitv maintains a text index under the
   downloader index directory:
   - `{plugin}_{categoryName}.index` — automatic downloads
   - `{plugin}_{categoryName}_manual.index` — manual download path when used
4. **First scan (no index yet)** — If the index file does not exist, the scan
   **does not enqueue downloads** for episodes found on that run. It only
   records episode **names** into the index (baseline). This avoids backfilling
   the entire catalogue history on first enable.
5. **Later scans (index exists)** — Episodes whose names are **not** already in
   the index (and pass include/exclude filters, and are not in the error list)
   are queued for download. Successfully processed names stay in the index.

Relevant logic: `SearchTask.findEpisodesByCategory` and `DownloadedDAO`.

---

## Deduplication model

| Aspect | Current behavior |
|--------|------------------|
| Key used | Episode **display name** (`EpisodeDTO.getName()`), one line per name in the index file |
| Not used | Stable provider episode IDs, canonical URLs, or content hashes |
| Error list | Separate `DlErrorDAO` tracks failed downloads by a derived full name |

---

## Known limitations

- **Title changes** — If a broadcaster renames an episode, Habitv treats it as
  a new episode and may download again.
- **Duplicate names** — Two distinct episodes with the same display name in one
  category can cause a false skip (second never downloaded).
- **Deleting an index** — Removing `{plugin}_{category}.index` resets behavior to
  “first scan” mode: the next successful scan rebuilds the baseline without
  downloading existing catalogue entries on that pass.
- **Manual vs automatic index** — Manual downloads can use a separate
  `_manual.index`; merging behavior is handled when both exist (`DownloadedDAO`).

**Planned improvement (not implemented):** deduplicate by provider episode id
and/or canonical URL in addition to display name.

---

## Related configuration

- Search/retrieve/download thread counts: `taskDefinition` in
  `configuration.xml`.
- Daemon check interval: `downloadConfig/demonCheckTime`.
- Runtime quickstart (CLI daemon): [`runtime-quickstart.md`](runtime-quickstart.md).
