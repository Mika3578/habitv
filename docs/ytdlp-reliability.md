# yt-dlp reliability (maintainers)

HabiTV uses the `youtube` Maven module as the **yt-dlp** tool plugin. Plugin id
stays `youtube`; the binary contract is **yt-dlp** (`yt-dlp.exe` on Windows,
`yt-dlp` on Unix). User-facing defaults and legal constraints are in
[`providers.md`](providers.md#yt-dlp).

## Default commands

Runtime placeholders (`#VIDEO_URL#`, `#FILE_DEST#`) are resolved by the framework.
Authoritative flag strings live in `YoutubeConf` (`DUMP_CMD`, `DUMP_CMD_MP3`).
The [`providers.md`](providers.md#yt-dlp) section summarizes video vs MP3
defaults and notes that routine video downloads do **not** enable subtitle
write/embed unless configuration extends the command.

If `configuration.xml` still references `youtube-dl`, point the `<youtube>` tool
entry at yt-dlp instead.

## Offline test strategy

- **Default CI:** Surefire excludes live `*PluginManagerTest` and network-heavy
  cases. See [`development.md`](development.md#commands).
- **Unit tests:** Progress and command parsing are covered without invoking the
  binary (for example `YtDlpProgressParser` tests under `plugins/youtube`).
- **Provider proof:** Prefer fixtures under
  `plugins/<module>/test/resources/fixtures/<module>/`. Provider modules that
  delegate retrieval to yt-dlp should keep catalogue tests offline; exercise the
  tool plugin only where a fixture or stub is enough.
- **Live checks:** Opt in with `-Plive-provider-tests` only when deliberately
  validating against the network. Live success is not the default merge bar.

Follow [provider diagnostics](../.agents/skills/provider-diagnostics/SKILL.md)
for classification (parsing vs tool vs access limits).

## Windows binary path diagnostics

Symptoms: download never starts, `[PYI-` / `_MEI` in logs, or extract errors
before yt-dlp prints progress.

| Check | Action |
|-------|--------|
| Binary path | Confirm `<youtube>` in `configuration.xml` points at `yt-dlp.exe` (or a compatible build). Logs record executable path, existence, and size at preflight. |
| Preflight | HabiTV runs `yt-dlp.exe --version` before downloads and redirects `TEMP`/`TMP` under the tool home (`…/tmp/yt-dlp`) to reduce PyInstaller bootstrap failures. |
| Environment | Stop HabiTV, clear stale `_MEI*` folders under the user TEMP directory, replace the binary, run `--version` outside HabiTV, then retry. |
| User guide | [`troubleshooting.md`](troubleshooting.md#run) (yt-dlp row). |

Implementation reference: `YtDlpRuntimeDiagnostics` in `plugins/youtube`.

## Auth and flags in git

- HabiTV does **not** pass cookies, browser profiles, or login flags by default.
- Do not commit credentials, session material, or user-specific auth recipes.
- Optional API keys for YouTube Data API belong in local config or environment
  variables (`habitv.youtube.apiKey` / `HABITV_YOUTUBE_API_KEY`), not in the
  repository.
- Keep public git text generic; see [`AGENTS.md`](../AGENTS.md) (**Public git
  text**).
