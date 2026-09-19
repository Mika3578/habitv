# Architecture

Current HabiTV layout for maintainers. This is not a redesign proposal.

Related: [`development.md`](development.md), [`providers.md`](providers.md),
[`configuration.md`](configuration.md).

## What the application does

HabiTV scans French TV replay (catch-up) catalogues through **provider
plugins**, downloads new episodes through **downloader plugins** (usually
external tools such as yt-dlp, curl, or ffmpeg), and optionally runs
**exporter** commands. Users select categories in `grabconfig.xml`; a
daemon or CLI pass downloads only episodes that are not already indexed.

Interfaces:

- **Console** (`application/consoleView`) — supported modernization baseline
- **Tray / JavaFX GUI** (`application/trayView`, `application/habiTv`) —
  still Java 8 + JavaFX 2.x; packaging is tracked separately

## Maven reactor

Root parent: `com.dabi.habitv:parent:4.1.0-SNAPSHOT` (`pom.xml`).
Default reactor: **35 modules** (`mvn -B -ntp -DskipTests validate` on
2026-09-19). Out-of-reactor packaging modules are listed below.

```
.
├── pom.xml                 # parent aggregator
├── fwk/
│   ├── api/                # plugin interfaces and DTOs
│   └── framework/          # shared plugin/runtime helpers, updater
├── application/
│   ├── core/               # grab/config, tasks, plugin manager
│   ├── consoleView/        # CLI fat JAR
│   ├── trayView/           # JavaFX tray UI
│   └── habiTv/             # GUI launcher
├── plugins/                # provider, downloader, and exporter modules
└── build/static-repo-publisher/   # only with -Pstatic-repo-deploy / -Pstatic-repo-publish
```

**Out of the default reactor** (present on disk, not in aggregator
`<modules>`):

- `application/habiTv-linux`
- `application/habiTv-windows`

Those two still assume a JavaFX-capable JDK 8 and `${jdk.home}`. Do not
remove them in a documentation cleanup. Java compiler/CI baseline:
[`development.md`](development.md).

Confirm the live module list with:

```bash
mvn -B -ntp validate
```

## Plugin system

Plugins are separate JARs loaded at runtime from a `plugins/` directory
next to the application, or resolved from the static update repository.

| Kind | Role | Examples |
|------|------|----------|
| Provider | Categories and episodes | `francetv`, `youtube`, `arte`, `novo19` |
| Downloader | Fetch media | `youtube` (yt-dlp), `curl`, `ffmpeg`, `aria2` |
| Exporter | Post-download commands | `cmd`, `rclone` |
| Mailbox input | POP3/IMAP ingest | `email` |

A provider may delegate download to another plugin (France.tv and NOVO19
delegate to the YouTube/yt-dlp plugin).

Inventory, status, and legal limits: [`providers.md`](providers.md).

## Configuration and user data

Typical files: [`configuration.md`](configuration.md). Existing user files
must stay loadable; do not change defaults silently.

Category watch and naming: [`configuration.md`](configuration.md).

## Updates

Runtime updates pull plugin JARs (and optional tool zips) from the HTTPS
static repository:

`https://mika3578.github.io/habitv-repo/repository/`

See [`development.md`](development.md). Disable with
`-Dhabitv.update.enabled=false`. The old `dabiboo.free.fr` HTTP
repository is historical only ([`history.md`](history.md)).

## Packaging today

| Artifact | How to get it | Notes |
|----------|---------------|--------|
| Console fat JAR | `mvn -B -ntp -DskipTests -pl "!application/trayView,!application/habiTv" package` | `application/consoleView/target/consoleView-4.1.0-SNAPSHOT-all.jar` |
| GUI uber JAR | Full package on a **JavaFX-capable JDK 8** | Needs `jfxrt.jar` |
| Windows / Linux installers | Out-of-reactor modules | Not part of the current console baseline |

Exact commands: [`development.md`](development.md).
