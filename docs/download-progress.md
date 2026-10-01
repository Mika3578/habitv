# Download progress

HabiTV exposes download and post-processing work through
`DownloadProgressSnapshot` and `DownloadStage` (see `fwk/api`). The UI reads
snapshots from `ProcessHolder.getProgressSnapshot()`; legacy percentage strings
remain for older downloaders.

There is **no weighted global percentage** across phases. Each stage keeps its
own ratio or stays indeterminate until observables exist.

## Lifecycle

Typical yt-dlp replay flow:

1. **QUEUED** — task registered, process not started.
2. **PREPARING** — extractor / metadata work without byte progress.
3. **DOWNLOADING** — one or more transfers (for example separate video and
   audio). Detail may label **Vidéo** / **Audio**. Progress ratio comes from
   bytes or yt-dlp percentage for the **current** transfer only.
4. **MERGING** — separate streams combined (yt-dlp Merger).
5. **REMUXING** — ffmpeg remux or conversion (yt-dlp post-processors or the
   ffmpeg plugin).
6. **SUBTITLES** — subtitle download or embedding.
7. **METADATA** — metadata or thumbnail embedding.
8. **POST_PROCESSING** — generic post-process when no finer stage is known.
9. **FINALIZING** — move / rename / timestamp fixups.
10. **COMPLETED** / **FAILED** / **CANCELLED** — terminal states.

When a new phase starts, numeric progress from the previous phase is not
carried over (for example download at 100% then merger is indeterminate).

## yt-dlp (youtube plugin)

Built-in default downloader commands (`YoutubeConf.DUMP_CMD` / `DUMP_CMD_MP3`)
append `--progress-template` only when preflight detects a yt-dlp build that
supports the flag (not youtube-dl). Lines are prefixed with `habitv-progress:`
followed by JSON.

- `phase=download` — numeric `pct`, optional `total`, `speed`, `eta`, `dest`.
- `phase=postprocess` — `pp` post-processor name mapped to `DownloadStage`.

`YtDlpProgressParser` consumes these lines and still accepts legacy
`[download] …` and `[Merger]` / `[ffmpeg]` style stdout when templates are
unavailable.

## ffmpeg plugin

Built-in ffmpeg downloader constants (`FFMPEGConf`) append `-progress pipe:1 -nostats`.
User-configured export scripts are unchanged. `FfmpegProgressParser`
reads `duration=` and `out_time_*` keys on stdout. A ratio is published only
when duration is known; otherwise the stage stays indeterminate **REMUXING**.
Stderr `Duration:` / `time=` lines remain a fallback for older invocations.

## UI

`DownloadProgressFormatter` and `DownloadBox` map stages to labels and choose
determinate vs indeterminate bars via `DownloadStage.isIndeterminate()`.

See also [`architecture.md`](architecture.md).
