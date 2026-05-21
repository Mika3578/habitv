# Habitv

Habitv is a legacy **Java 8 / Maven** application that automatically watches
French TV replay (catch-up) sites and downloads new episodes in the background.
The project is in a **controlled modernization restart**: build and governance
are being restored first; provider endpoints and the JavaFX GUI trail the core
reactor.

| Topic | Current state |
|-------|----------------|
| Integration branch | [`develop`](https://github.com/Mika3578/habitv/tree/develop) |
| Java baseline | **Java 8** — CI uses Zulu 8 for legacy validate, Liberica 8 `jdk+fx` for Maven CI Java 8 jobs, and Temurin 8 in the `master`-only legacy build |
| Root `mvn validate` / `mvn compile` | Works on `develop` |
| Full `mvn package` (GUI) | Works with a JDK 8 that includes JavaFX; platform packaging modules use separate `jdk.home` setup |
| Artifact / plugin updates | HTTPS static repo ([`habitv-repo`](https://github.com/Mika3578/habitv-repo)) — legacy `dabiboo.free.fr` removed |
| Provider plugins | Mixed: some work offline; many replay sites changed — see [provider inventory](docs/provider-inventory.md) |

---

## What Habitv does (runtime)

- **Modular plugins**: provider plugins list categories and episodes; downloader
  and exporter plugins wrap external tools (`yt-dlp`, `curl`, `ffmpeg`, etc.).
- **Automatic category watch**: categories you mark as selected are scanned on a
  schedule; only **new** episodes (per category index) are downloaded after the
  first baseline scan. See [automatic category download](docs/automatic-category-download.md).
- **Interfaces**: tray/GUI (`application/habiTv`, `trayView`) and CLI
  (`application/consoleView`). The **console fat-jar** is the supported
  modernization baseline today; JavaFX packaging is tracked separately.

---

## Quick start (developers)

**Prerequisites:** JDK 8, Maven 3.6+, Git.

```bash
git clone https://github.com/Mika3578/habitv.git
cd habitv
git checkout develop
mvn -B -ntp -DskipTests validate
```

Default safe validation (also required in CI):

```bash
mvn -B -ntp -DskipTests validate
```

Full reactor compile (Java 8):

```bash
mvn -B -ntp -DskipTests compile
```

Runnable console package (excludes JavaFX GUI modules):

```bash
mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package
```

Runtime layout and manual download: [`docs/runtime-quickstart.md`](docs/runtime-quickstart.md).

---

## Build and test matrix

| Command | Status on `develop` | Notes |
|---------|---------------------|--------|
| `mvn -B -ntp -DskipTests validate` | Safe, default | 33 reactor modules |
| `mvn -B -ntp -DskipTests compile` | Safe | Java 8 only |
| `mvn -B -ntp -DskipTests package` | Java 8 + JavaFX | Requires a JDK 8 with JavaFX for GUI modules; use scoped builds for console-only validation |
| `mvn -B -ntp test` | Partial | Live `*PluginManagerTest` excluded by default |
| `mvn -B -ntp test -Plive-provider-tests` | Opt-in | Hits real broadcaster networks |
| `mvn -B -ntp verify` | Not safe yet | Full lifecycle still gated |

CI parity: [`docs/ci.md`](docs/ci.md).

### Known build constraints

- **Java 8 only** for this phase — no Java 9+ language features or APIs.
- **JAXB**: configuration/grabconfig types are generated under
  `target/generated-sources/jaxb` in `application/core` (see tracker
  `jaxb-launcher-recovery`).
- **JavaFX**: `application/habiTv`, `trayView`, and out-of-reactor
  `habiTv-linux` / `habiTv-windows` expect JDK 8 with `jfxrt.jar` (tracker
  `javafx-modernization`).
- **Legacy HTTP repo** (`dabiboo.free.fr`, FTP deploy, SVN SCM): removed from
  active POMs; history in [`docs/audit-master-baseline.md`](docs/audit-master-baseline.md).

---

## Runtime and updates

- Plugin JARs and tool binaries are published to
  **`https://mika3578.github.io/habitv-repo/repository/`** (see
  [`docs/static-repository-deploy.md`](docs/static-repository-deploy.md)).
- Updates are **on by default** at startup; disable with
  `-Dhabitv.update.enabled=false` or configuration.
- **SNAPSHOT / timestamped** plugin artifacts: default runtime config allows
  snapshot resolution; see [`docs/runtime-quickstart.md`](docs/runtime-quickstart.md).
- Telemetry to legacy hosts is **opt-in** (`habitv.stat.enabled`).

---

## Provider landscape (summary)

Replay sites change often. Treat README-era names as **legacy labels**:

| Legacy name | Modern meaning / module |
|-------------|-------------------------|
| Pluzz | France Télévisions / France.tv — module `plugins/francetv` (rename grab-config `pluzz` → `francetv`) |
| D8 / D17 | Canal-era channels — embedded in `plugins/canalPlus` (obsolete endpoints) |
| CNews | Canal+ group news channel — embedded `cnews` sub-provider in `plugins/canalPlus` (HOME_URL `https://www.canalplus.com/chaines/cnews`) |
| 6play | M6+ / M6 replay area — `plugins/6play` (needs rewrite) |
| NRJ12 | Historical reference only — no module in reactor |

Full table, fixture policy, and follow-up queue:
[`docs/provider-inventory.md`](docs/provider-inventory.md).

---

## Contributing

1. Branch from latest **`develop`** (`docs/…`, `fix/…`, `feat/…`, etc.).
2. One tracker item per PR — see [`docs/dev-tracker.md`](docs/dev-tracker.md).
3. [Conventional Commits](https://www.conventionalcommits.org/) in English.
4. Run validation; paste **exact command output** in the PR body.
5. Keep PRs small; linear history (no merge commits on feature branches).

Details: [`CONTRIBUTING.md`](CONTRIBUTING.md), [`AGENTS.md`](AGENTS.md),
[`docs/pull-request-style-guide.md`](docs/pull-request-style-guide.md).

---

## Documentation map

| Document | Purpose |
|----------|---------|
| [`docs/dev-plan.md`](docs/dev-plan.md) | Phased modernization roadmap |
| [`docs/dev-tracker.md`](docs/dev-tracker.md) | Work items (mirror: `dev-tracker.json`) |
| [`docs/automatic-category-download.md`](docs/automatic-category-download.md) | Category watch, index, deduplication limits |
| [`docs/runtime-quickstart.md`](docs/runtime-quickstart.md) | Console runtime, flags, yt-dlp |
| [`docs/static-repository-deploy.md`](docs/static-repository-deploy.md) | `habitv-repo` publish contract |
| [`docs/provider-inventory.md`](docs/provider-inventory.md) | Plugin modules and status |
| [`docs/ci.md`](docs/ci.md) | GitHub Actions required vs diagnostic checks |
| [`docs/repository-maintenance.md`](docs/repository-maintenance.md) | Merge hygiene, Dependabot, doc sync |
| [`docs/risk-register.md`](docs/risk-register.md) | Active risks |
| [`docs/decision-log.md`](docs/decision-log.md) | ADRs |
| [`docs/audit-master-baseline.md`](docs/audit-master-baseline.md) | **Historical** pre-restart audit snapshot |
| [`docs/ytdlp-cli-compatibility.md`](docs/ytdlp-cli-compatibility.md) | yt-dlp CLI contract |
| [`SECURITY.md`](SECURITY.md) | Vulnerability reporting |

---

## Known limitations

- Many provider plugins target **obsolete URLs or HTML layouts**; live tests may
  fail even when the build is green.
- Category deduplication uses **episode display names** in index files — title
  changes can re-download; duplicate names can false-skip (see
  [`docs/automatic-category-download.md`](docs/automatic-category-download.md)).
- GUI packaging (`habiTv`, platform installers) is not yet aligned with the
  modernized reactor baseline.
- `youtube-dl` is deprecated in favor of **yt-dlp** (`ytdlp-migration` tracker).

---

## License

No explicit license file yet — treat sources as proprietary until the
maintainer adds a license. See [`CONTRIBUTING.md`](CONTRIBUTING.md).
