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
for the supported sites. Habitv does **not** pass protected-content, browser
profile, or authentication flags by default (user may configure yt-dlp locally
when an extractor supports login — see
[`docs/provider-policy.md`](provider-policy.md#site-authentication-for-download)).

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

## Controlled temp directory (Windows)

PyInstaller onefile builds of `yt-dlp.exe` extract runtime files into `TEMP`/`TMP`.
Habitv sets both variables to `<habitv-home>/tmp/yt-dlp` for yt-dlp child processes
so extraction does not depend on a crowded or permission-broken system temp folder.

Before each download, Habitv runs `yt-dlp.exe --version` as a preflight check. If
stderr contains PyInstaller signatures (`[PYI-`, `Failed to extract`, `Cryptodome`,
`_MEI`), the failure is reported as a **yt-dlp bootstrap error**, not a provider
URL parsing failure.

## Troubleshooting: yt-dlp PyInstaller extraction failure on Windows

Symptoms in logs:

```text
[PYI-17924:ERROR] Failed to extract Cryptodome\PublicKey\_ec_ws.pyd: decompression resulted in return code -1!
[PYI-17924:ERROR] Failed to extract entry: Cryptodome\PublicKey\_ec_ws.pyd.
```

This happens **before** any site-specific extraction (for example France.tv). The
first manual test is always `yt-dlp.exe --version`, then a direct URL download.

### Recovery steps

1. Stop Habitv.
2. Remove stale PyInstaller temp folders:

```powershell
Get-ChildItem "$env:TEMP" -Directory -Filter "_MEI*" -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
```

3. Rename the current binary:

```powershell
Rename-Item "$env:USERPROFILE\habitv\bin\yt-dlp.exe" "yt-dlp.exe.broken" -ErrorAction SilentlyContinue
```

4. Download a fresh official Windows `yt-dlp.exe` from:
   https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe
5. Install it as `%USERPROFILE%\habitv\bin\yt-dlp.exe` (adjust paths to your
   Habitv home directory).
6. Test outside Habitv:

```powershell
& "$env:USERPROFILE\habitv\bin\yt-dlp.exe" --version
& "$env:USERPROFILE\habitv\bin\yt-dlp.exe" "https://www.france.tv/france-3/nouvelle-aquitaine_la-france-en-vrai-aquitaine/8456007-oleron-la-vie-continue.html" -o "$env:USERPROFILE\habitv\Downloads\manual-francetv-test.%(ext)s" --write-sub --write-auto-sub --no-check-certificate
```

7. Retry the same URL through Habitv.

Tracker: `ytdlp-runtime-diagnostics`.

## Out of scope for this contract

- Provider discovery / `canDownload()` domain list changes
- YouTube Data API key handling (unchanged)
- Runtime updater default URL enablement
- Publishing binaries to `habitv-repo` (separate follow-up)
