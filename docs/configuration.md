# Configuration

User files typically sit next to the working directory (or under a user
Habitv directory when no local config is present):

| File | Role |
|------|------|
| `configuration.xml` | Paths, tool binaries, update flags, pool sizes |
| `grabconfig.xml` | Selected categories per provider |
| `*.index` | Downloaded episode keys (legacy files may be display names) |
| `plugins/` | Runtime plugin JARs |

Samples/schema: `application/core/xsd/`. Preserve existing user files;
do not change defaults silently or delete downloads/indexes.

## Category watch

Selected categories are scanned on a schedule. After the first baseline
scan, only episodes whose **index key** is new are downloaded. Current
indexes store composite keys (plugin, category id, episode id, and name);
legacy files may contain display names only. Title-only matching is
therefore not guaranteed.

## Tokens and MEDIA_SERVER naming

Legacy tokens (`#TVSHOW_NAME#`, `#EPISODE_NAME#`) remain.
The download/current date token needs a format: `#DATE§yyyy-MM-dd#`
(not bare `#DATE#`).

Canonical metadata (`EpisodeMetadataDTO`) is additive. Providers fill
facts only; they must not build filesystem paths. Global naming lives in
`application/core` (`MediaNamingService`, `EpisodeMetadataResolver`,
`MediaServerNamingPolicy`). Useful tokens include `#SERIES_NAME#`,
`#AIR_DATE#`, `#SEASON_EPISODE#`, `#MEDIA_SERVER_PATH#`.

`#AIR_DATE#` is a true broadcast date only. Publication or upload dates
must not create dated MEDIA_SERVER paths. `#DATE§pattern#` is the
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

Rename `<plugin name="wat">` to `tf1plus`. A runtime alias still maps
`wat` to the `tf1plus` plugin so existing grab-config files keep
loading. Optional TF1+ account fields in `configuration.xml` (and
matching environment variables) stay unused until the user fills them
in; detailed helper setup stays outside git (`local/`).
