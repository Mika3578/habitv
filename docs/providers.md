# Providers

HabiTV loads provider, downloader, and exporter JARs at runtime. A compiling
module is **not** a claim that live download works.

## Legal

Only public catalogues and user-authorized, legally accessible workflows.

The project does **not** bypass DRM, encryption, paywalls, or license
checks, and does **not** extract credentials, cookies, or browser sessions
into git. User login material, if ever used, stays in local config or
environment variables. Optional auth paths stay disabled until the user
configures them.

Keep public git text short and generic. See [`AGENTS.md`](../AGENTS.md)
(**Public git text**).

## yt-dlp

Plugin Maven id remains `youtube`. The binary contract is **yt-dlp**
(`yt-dlp.exe` on Windows, `yt-dlp` on Unix), artifact id `yt-dlp`.

Default video command: `-o <dest> -f "bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b" --merge-output-format mp4 --newline --no-check-certificate`.
MP3: `--extract-audio --audio-format mp3 --newline --no-check-certificate`.
Default video does **not** pass `--write-sub` / `--write-auto-sub`.
Habitv does not pass auth/browser flags by default. If `configuration.xml`
still points at `youtube-dl`, switch it to yt-dlp.

## Status (documentation pass; not a live re-test)

| Group | Modules |
|-------|---------|
| Recently worked | `francetv` (ex Pluzz), `youtube` (yt-dlp), `novo19`, `tvCom` |
| Needs rewrite / investigation | `arte`, `6play`, `lequipe`, `footyroom`, `sfr`, `globalnews`, `mlssoccer` |
| Obsolete / degraded endpoints | `canalPlus` (CStar; D8 removed), `wat`, `beinsport`, `clubic` |
| Tools / infrastructure | `curl`, `ffmpeg`, `aria2`, `cmd`, `file`, `RSS`, `rclone`, `rtmpDump`, `adobeHDS`, `email`, `plugin-tester` |
| Historical names only | Pluzz → `francetv`; NRJ12 — no module |

Labels: **working** (validated), **degraded**, **protected** (auth/geo/DRM),
**obsolete**, **removed**, **unknown**.

France.tv and NOVO19 delegate download to the youtube/yt-dlp plugin when
public URLs exist. Canal+ family and WAT/TF1+ are not treated as freely
downloadable DRM catalogues.

## Arte catalogue discovery

HabiTV owns catalogue discovery and hierarchy; **yt-dlp** owns stream
extraction (player API, HLS, languages, subtitles, geo messages).

Discovery uses the public EMAC API (`https://api.arte.tv/api/emac/v4`) only
(no copied tokens, no custom HLS parsing in the Arte plugin):

- Languages from EMAC `HOME` `alternativeLanguages`, plus Romanian when the
  `ro` HOME page is reachable (Romanian is a distinct web edition; some
  areas such as `ro/DOR` may 404 while others work).
- Catalogue **pages** from a merged walk of `web` and `tv` HOME payloads,
  `genres_HOME`, and best-effort hub HTML `arte://emac/` hints. Small
  fallback codes (`DEC`, `ACT`) apply only when HOME omits them.
- UI tree: `language → page → zone/listing → collection (RC-*) → episodes`.
  Category ids: `z/{lang}/{page}/{zoneId}`, `c/{lang}/{collectionId}`, with
  legacy `lang:page` merge retained for older configs.
- Zones are independent listings; collections load
  `/web/collections/{id}`; pagination prefers `pagination.links.next`.
- Playable leaves are `SHOW` items with classic `/videos/NNNNNN-NNN-A/…`
  URLs. `RC-*` entries are collection navigation, not flattened episodes.
- Downloads stay `ArtePluginManager.download` → youtube/yt-dlp on the
  public Arte video URL.

Offline fixtures: `plugins/arte/test/resources/fixtures/arte/`. Live checks
are manual, not CI.

Full module list: `plugins/pom.xml`. Offline fixtures live under
`plugins/<name>/test/resources/fixtures/<name>/`.
