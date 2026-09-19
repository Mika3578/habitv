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

Keep public PR/commit text high-level (no bypass recipes).

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
| Recently worked | `francetv` (ex Pluzz), `youtube` (yt-dlp), `novo19`, `teleMb` |
| Needs rewrite / investigation | `arte`, `6play`, `lequipe`, `footyroom`, `sfr`, `globalnews`, `mlssoccer` |
| Obsolete / degraded endpoints | `canalPlus` (CStar; D8 removed), `wat`, `beinsport`, `clubic` |
| Tools / infrastructure | `curl`, `ffmpeg`, `aria2`, `cmd`, `file`, `RSS`, `rclone`, `rtmpDump`, `adobeHDS`, `email`, `plugin-tester` |
| Historical names only | Pluzz → `francetv`; NRJ12 — no module |

Labels: **working** (validated), **degraded**, **protected** (auth/geo/DRM),
**obsolete**, **removed**, **unknown**.

France.tv and NOVO19 delegate download to the youtube/yt-dlp plugin when
public URLs exist. Canal+ family and WAT/TF1+ are not treated as freely
downloadable DRM catalogues.

Full module list: `plugins/pom.xml`. Offline fixtures live under
`plugins/<name>/test/resources/fixtures/<name>/`.
