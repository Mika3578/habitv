# yt-dlp CLI compatibility (YouTube plugin)

Habitv's `plugins/youtube` module keeps the Maven/plugin id **`youtube`** for
compatibility. The external downloader binary contract now targets **yt-dlp**
instead of **youtube-dl**.

## Binary names

| Platform | Default path / name |
|----------|---------------------|
| Windows | `yt-dlp.exe` (under configured `bin` dir when not overridden) |
| Linux / Unix | `yt-dlp` on `PATH`, or an absolute path in user config |

Runtime auto-update looks up artifact id **`yt-dlp`** (formerly `youtube-dl`).

## Default command templates

Resolved at download time (`#VIDEO_URL#` → URL, `#FILE_DEST#` → output path):

**Video (default)**

```text
"<url>" -o "<dest>" --write-sub --write-auto-sub --no-check-certificate
```

**MP3 (`youtube-mp3` downloader)**

```text
"<url>" -o "<dest>" --extract-audio --audio-format mp3 --no-check-certificate
```

These flags are the subset Habitv relied on with youtube-dl; yt-dlp accepts them
for the supported sites. Habitv does **not** pass DRM bypass, cookie, browser
profile, or authentication flags.

## Progress output

`YtDlpCmdExecutor` parses percentage lines matching yt-dlp / legacy youtube-dl
style output, e.g. `[download]  45.2% of ...`.

## User migration (deprecation)

If you configured a manual path to `youtube-dl` or `youtube-dl.exe`, install
[yt-dlp](https://github.com/yt-dlp/yt-dlp) and point `<youtube>` in
`configuration.xml` to that binary (see `application/core/configuration.xml`
sample: `bin\yt-dlp.exe` on Windows).

No provider rewrite or live endpoint validation is included in the binary
migration PR; non-YouTube URL domains accepted by `YoutubePluginDownloader`
are unchanged.

## Out of scope for this contract

- Provider discovery / `canDownload()` domain list changes
- YouTube Data API key handling (unchanged)
- Runtime updater default URL enablement
- Publishing binaries to `habitv-repo` (separate follow-up)
