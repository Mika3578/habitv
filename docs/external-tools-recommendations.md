# 🧰 External tools — recommendations and obsolescence analysis

**Tracker item**: `external-tools-recommendations`
**Last refresh:** 2026-05-31 (tracker sync; content unchanged since initial analysis PR #104)

**Scope in this PR**: documentation, classification, and recommended
follow-up PR queue. **No** plugin module is added or removed, **no**
`configuration.xml` change, **no** `tool-sources.properties` change,
**no** runtime updater behavior change.

---

## 1. Purpose

Habitv ships five binary integrations (`yt-dlp`/`youtube-dl`, `ffmpeg`,
`curl`, `aria2c`, `rtmpdump`) and several internal plugins without a
bundled binary: `cmd` (exporter), `file` (provider and downloader), and
`email` (mailbox-input provider). This document records:

- which **additional** external tools would deliver the most user value
  if introduced as new exporter/notifier/downloader plugins,
- which **existing** binary integrations and provider plugins should be
  considered for deprecation,
- the cross-references to existing tracker items that already own those
  concerns.

Every concrete plugin addition or module removal will land in a
**separate** follow-up PR with its own tracker entry. This document is
strictly a set of proposals.

---

## 2. Inventory snapshot

For exhaustive plugin status, see
[`provider-inventory.md`](provider-inventory.md). The runtime binary
contracts currently honored by Habitv:

| Binary | Plugin module | Role | Current status |
|---|---|---|---|
| `yt-dlp` (was `youtube-dl`) | `plugins/youtube` | downloader | active, migration in progress (`ytdlp-migration`) |
| `ffmpeg` | `plugins/ffmpeg` | downloader + transcode exporter | active |
| `curl` | `plugins/curl` | downloader + exporter | active |
| `aria2c` | `plugins/aria2` | downloader | active |
| `rtmpdump` | `plugins/rtmpDump` | downloader (RTMP/Flash) | obsolete — see §5.1 |
| `AdobeHDS.php` | `plugins/adobeHDS` | downloader (Adobe HDS/Flash) | obsolete — see §5.1 |
| no external binary | `plugins/cmd` | exporter | active |
| no external binary | `plugins/file` | provider + downloader | active |
| no external binary | `plugins/email` | provider (mailbox input) | active |

---

## 3. 🎯 High-value additions

Each candidate below targets a real workflow gap. Adding any of them
follows the existing plugin pattern (`*Conf` + `*CmdExecutor` +
`*PluginDownloader` or `*PluginExporterManager`) and publishes its
Windows binary through `scripts/static-repo/tool-sources.properties`,
exactly like `yt-dlp.exe`.

| Tool | Proposed module | Role | Why it matters here |
|---|---|---|---|
| `rclone` | `plugins/rclone` | exporter | Push downloaded files to Nextcloud, Google Drive, OneDrive, S3, WebDAV, SFTP, … in one shot. Replaces ad-hoc `cmd` + `curl` FTP recipes currently buried in `configuration.xml`. Single static binary, stable CLI, MIT license. |
| `mkvmerge` (MKVToolNix) | `plugins/mkvmerge` | exporter | Mux yt-dlp's separate video + audio + subtitle outputs into one MKV, **losslessly and quickly** (no re-encode). Pairs naturally with `ytdlp-migration`. |
| `subliminal` | `plugins/subliminal` | exporter | Auto-fetch subtitles when the source episode has none. Python CLI, multiple providers (OpenSubtitles, Addic7ed, …). |
| `apprise` | `plugins/apprise` | exporter / notifier | One CLI, 90+ notification targets (Telegram, Discord, ntfy, Pushover, Slack, Matrix, Gotify, …). Modernizes the legacy SMTP-only `email` exporter for users who want push-to-phone notifications. |
| `streamlink` | `plugins/streamlink` | downloader | HLS/live stream coverage where yt-dlp lacks a working extractor, particularly for French live sports/news pages. Backup downloader, no provider rewrite required. |

### Integration constraints

- Each plugin must remain **Java 8 compatible** and reuse the existing
  `*CmdExecutor` pattern (no `ProcessBuilder` redesign).
- Windows binary publication goes through
  `scripts/static-repo/tool-sources.properties` — same mechanism as
  `yt-dlp.exe`. Linux/macOS users still install via their package
  manager.
- Each plugin needs an offline `*CmdTest` (no network) before merge,
  mirroring `YtDlpCmdExecutorTest`.
- New tool additions must not break the default
  `mvn -B -ntp -DskipTests validate` baseline.
- Each addition must consider supply-chain integrity (checksum or
  signature verification on download) — note in PR body.

---

## 4. 🟡 Medium-value additions

Useful but lower priority. Defer until at least one §3 plugin has
shipped.

| Tool | Proposed module | Role | Rationale |
|---|---|---|---|
| `mediainfo` CLI | `plugins/mediainfo` | exporter (probe) | Inspect container/codec/duration to drive conditional `<exporter>` rules in `configuration.xml` (e.g. only re-encode H.265). |
| `HandBrakeCLI` | `plugins/handbrake` | exporter (transcode) | Preset-driven transcoding (Apple TV, Roku, Plex) is much easier to configure than raw ffmpeg argument strings. |
| `filebot` CLI | `plugins/filebot` | exporter (rename) | Smart renaming/library organization for Plex/Jellyfin/Kodi consumers. Non-free but free for personal use — licensing must be confirmed before adoption. |
| generic webhook | `plugins/webhook` | exporter | Lightweight `curl` POST to Jellyfin/Plex/Kodi library-refresh endpoints after each export. May not need an extra binary if `plugins/curl` is reused. |
| `N_m3u8DL-RE` | (alternative downloader) | downloader | Modern HLS/DASH downloader, occasionally more robust than ffmpeg on fragmented streams. Single static binary, MIT license. |

---

## 5. 🔻 Obsolescence and removal candidates

This section identifies modules whose technical assumptions or upstream
endpoints no longer hold. **No module is removed in this PR.** Every
candidate below is a proposal for a dedicated future PR — under
`provider-inventory` for provider modules, or a new tracker item for
downloader-tool deprecation.

### 5.1 Runtime downloader tools (Flash-era)

| Module | Reason | Recommended action |
|---|---|---|
| `plugins/rtmpDump` | Flash Player reached end-of-life on **2020-12-31**. Adobe RTMP-streamed content is virtually nonexistent on the French replay providers Habitv targets. `tool-sources.properties` already flags `rtmpdump.skip=true` because no maintained Windows binary exists upstream. | Dedicated `deprecate-rtmpdump` PR: keep the plugin classes for grab-config backward compatibility, mark the plugin as deprecated in the sample `configuration.xml`, and stop publishing the tool. Requires its own tracker item. |
| `plugins/adobeHDS` | Adobe HDS shares Flash's EOL. `tool-sources.properties` already marks `adobeHDS.type=skip`. The bundled `AdobeHDS.php` requires a PHP runtime — an undocumented system dependency for end users. | Dedicated `deprecate-adobeHDS` PR with the same shape as `deprecate-rtmpdump`. Requires its own tracker item. |

### 5.2 Provider plugins already classified elsewhere

These are already documented as obsolete or pending rewrite in
[`provider-inventory.md`](provider-inventory.md). They are listed here
for completeness and **must not be removed outside the
`provider-inventory` tracker scope**.

| Module | Status (per `provider-inventory.md`) | Recommendation |
|---|---|---|
| `plugins/clubic` | obsolete endpoint | dedicated deprecation PR (`provider-inventory` follow-up #5) |
| `plugins/beinsport`, `plugins/wat`, `plugins/footyroom`, `plugins/lequipe` | obsolete endpoint / needs rewrite | grouped rewrite PR (`provider-inventory` follow-up #4) |
| Embedded `D8` / `D17` sub-providers in `plugins/canalPlus` | renamed channels (C8, CSTAR) | covered by canal-family rewrite (`provider-inventory` follow-up #2) |
| Historical `NRJ12`, `Kewego`, `pluzz` references | no live module / renamed | already addressed — no further action |

### 5.3 Internal plugins (non-binary)

| Module | Observation | Recommendation |
|---|---|---|
| `plugins/cmd` | Generic exporter wrapping user-defined shell commands. | **Keep**; complements dedicated exporter plugins such as `rclone`. |
| `plugins/file` | Local filesystem provider and downloader. | **Keep**; not an exporter. |
| `plugins/email` | Mailbox-input provider; some deployments also use it for SMTP notification. Apprise (§3) covers push notifications more cleanly. | **Keep** until §3 `apprise` plugin ships. Then evaluate whether to narrow SMTP/IMAP notification use in a dedicated PR. |

---

## 6. Cross-references to existing tracker items

| Tracker | Relevance |
|---|---|
| `provider-inventory` (HBTV-006) | Owns every provider-module removal/rewrite mentioned in §5.2. |
| `ytdlp-migration` (HBTV-007) | Owns the yt-dlp binary contract. New downloader plugins must not regress that contract. |
| `static-repo-publish` (HBTV-005) | Any new external tool added under §3 or §4 must extend `scripts/static-repo/tool-sources.properties` and the static repository layout. |
| `dependency-security-audit` (HBTV-016) | Adding a new external runtime is also a supply-chain decision; each addition must document integrity verification (checksum or signature). |

---

## 7. Out-of-scope confirmations

- No plugin module added or removed.
- No `configuration.xml` change.
- No `tool-sources.properties` change.
- No runtime updater behavior change.
- No provider parser or scraper change.
- No new risk surfaced (risk register unchanged).
- No architectural decision required (decision log unchanged); each
  §3/§4 addition or §5 deprecation will carry its own ADR if it
  changes user-visible defaults.

---

## 8. Suggested follow-up PR queue (safe order)

1. **`feat(rclone): add rclone exporter plugin`** — highest user value,
   no provider-side risk. New tracker item.
2. **`feat(apprise): add apprise notifier exporter plugin`** —
   modernizes notifications without touching the existing `email`
   module.
3. **`feat(mkvmerge): add mkvmerge muxer exporter plugin`** — pairs
   naturally with `ytdlp-migration` once that completes.
4. **`feat(subliminal): add subliminal subtitle fetcher`** — independent
   of providers.
5. **`feat(streamlink): add streamlink downloader plugin`** — only
   after `ytdlp-migration` fixture work is stable.
6. **`docs(deprecate-rtmpdump-adobeHDS): propose deprecation of legacy
   Flash downloaders`** — paper-only proposal first; actual code
   removal requires a separate accepted tracker item.
