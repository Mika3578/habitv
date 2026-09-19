# Configuration

User files typically sit next to the working directory (or under a user
Habitv directory when no local config is present):

| File | Role |
|------|------|
| `configuration.xml` | Paths, tool binaries, update flags, pool sizes |
| `grabconfig.xml` | Selected categories per provider |
| `*.index` | Already-seen episode names per category |
| `plugins/` | Runtime plugin JARs |

Samples/schema: `application/core/xsd/`. Preserve existing user files;
do not change defaults silently or delete downloads/indexes.

## Category watch

Selected categories are scanned on a schedule. After the first baseline
scan, only **new** episode **display names** are downloaded. Title changes
can re-download; identical titles can skip. This is name-based, not a
canonical episode id.

## Tokens and MEDIA_SERVER naming

Legacy tokens (`#TVSHOW_NAME#`, `#EPISODE_NAME#`, `#DATE#`) remain.
`#DATE#` is the download/current date.

Canonical metadata (`EpisodeMetadataDTO`) is additive. Providers fill
facts only; they must not build filesystem paths. Global naming lives in
`application/core` (`MediaNamingService`, `EpisodeMetadataResolver`,
`MediaServerNamingPolicy`). Useful tokens include `#SERIES_NAME#`,
`#AIR_DATE#`, `#SEASON_EPISODE#`, `#MEDIA_SERVER_PATH#`.

`#AIR_DATE#` is a true broadcast date only. Publication or upload dates
must not create dated MEDIA_SERVER paths. `#DATE#` remains the
download/current date. Do not copy a provider category name into
`seriesTitle` unless the item is a verified program/series.

New configs may default `downloadOuput` to a MEDIA_SERVER path; existing
user configs stay as stored.

## Batch downloads

The tray UI can enqueue multiple listed episodes. Queue/progress behavior
is implemented in `application/core` and `trayView`; do not assume
undocumented batch flags.

## Grab-config migration

Rename `<plugin name="pluzz">` to `francetv`. Legacy `pluzz.` URLs may
still be accepted by `canDownload`.
