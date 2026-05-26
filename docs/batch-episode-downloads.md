# Batch episode downloads and queue behavior

This document describes the JavaFX tray UI batch download flow, duplicate
prevention, download concurrency, optional episode metadata, and the
plugin/category tree mapping.

## Batch selection

On the downloads tab (labeled **A télécharger** in the UI), episodes are
listed in a multi-select table.
Users can:

- Select multiple rows with mouse or keyboard (Ctrl/Cmd, Shift where supported).
- Use **Download selected** to enqueue all selected episodes at once.
- Use the context menu to download a single episode (unchanged).

If nothing is selected, **Download selected** stays disabled. If every
selection is skipped, the UI shows an error pop-in.

## Duplicate prevention

Before each episode is queued, the core checks:

| Condition | Skip reason |
|-----------|-------------|
| Episode name already in category index | `ALREADY_DOWNLOADED` |
| Retrieve task already queued/running | `ALREADY_QUEUED` |
| Download task queued/running | `ALREADY_DOWNLOADING` |
| Same episode twice in one batch | `DUPLICATE_IN_BATCH` |

Identity uses plugin, category, episode id/name, and existing `EpisodeDTO`
equality. Logs include `Episode queued`, `Duplicate skipped`, and download
lifecycle messages from `DownloadTask` / `TaskMgr`.

## `maxConcurrentDownloads`

Configuration element under `downloadConfig` in `configuration.xml`:

```xml
<maxConcurrentDownloads>1</maxConcurrentDownloads>
```

- Default: **1** (same as legacy single-download behavior).
- Minimum: **1**; invalid or missing values fall back to 1.
- Editable in the Configuration tab (requires application restart to apply).
- Enforced globally on the download `TaskMgr` pool (not per-plugin pools).

When the limit is reached, additional downloads wait in the executor queue.
`TaskMgr` logs `Queue waiting ... because concurrency limit was reached`.

## Episode metadata (best-effort)

Optional fields on `EpisodeDTO` (providers may leave them null):

| Field | UI column |
|-------|-----------|
| `episodeDate` | Date |
| `durationSeconds` | Duration |
| `sizeBytes` | Size |
| `id` (HTTP URL or id) | Source |

Missing values display **Unknown**. The UI never fails when metadata is absent.

## Plugin / category / show tree

The tree uses existing `CategoryDTO` nesting:

1. **Plugin** — top-level plugin holder (display name; no separate channel model).
2. **Category** — first subcategory level.
3. **Show** — deeper subcategory.
4. **Group** — further subcategory / episode groups.

Template categories keep their context menu and checkbox behavior.

## Known limitations

- No distinct “channel” entity in the plugin API; the plugin name stands in.
- Metadata is not populated by most legacy providers in this PR.
- Queue state in the episode table follows retrieve/download events; a restart clears in-memory queue hints.
- `maxConcurrentDownloads` applies after restart; `taskDefinition/download` is no longer used for per-plugin download pools.

## Follow-up

- Provider-specific metadata enrichment.
- Queue pause/resume and persistent queue recovery.
- Richer channel/show taxonomy when plugins are modernized.
