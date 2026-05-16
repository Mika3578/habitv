# Habitv risk register

Initial risk register for the restart-from-master modernization.
Severity uses `Likelihood x Impact` (Low / Medium / High) and an
overall priority. Update when a risk is added, mitigated, realized,
or accepted.

| ID    | Title                                          | Likelihood | Impact | Priority |
|-------|------------------------------------------------|------------|--------|----------|
| R-001 | Legacy Maven repository / free.fr dependency   | High       | High   | P0       |
| R-002 | FTP deployment no longer viable                | High       | High   | P0       |
| R-003 | JavaFX tied to JDK 8 assumptions               | High       | High   | P1       |
| R-004 | JAXB generation / runtime mismatch             | Medium     | High   | P1       |
| R-005 | Live provider tests are non-deterministic      | High       | Medium | P1       |
| R-006 | Provider endpoints obsolete or renamed         | High       | Medium | P1       |
| R-007 | Auto-update pulls unexpected old artifacts     | Medium     | High   | P1       |
| R-008 | yt-dlp migration changes download behavior     | Medium     | Medium | P2       |
| R-009 | GitHub Pages layout mismatches updater         | Medium     | High   | P1       |

---

## R-001 — Legacy Maven repository / free.fr dependency

- Description: Root `pom.xml` and packaging POMs declare
  `<repository><url>http://dabiboo.free.fr/repository</url>` (plain
  HTTP, third-party hosting). If `dabiboo.free.fr` is down or
  removed, Maven cannot resolve project-specific artifacts. Plain
  HTTP is also blocked by newer Maven defaults.
- Mitigation: Plan migration to a controlled static repository
  (HBTV-004 / HBTV-005). Until then, document the dependency and
  do not rely on it in CI.

## R-002 — FTP deployment no longer viable

- Description: `<distributionManagement>` uses
  `ftp://ftpperso.free.fr/repository` with `wagon-ftp 1.0-beta-6`.
  FTP is unencrypted, free.fr personal pages are deprecated, and
  the password mechanism would expose credentials.
- Mitigation: Remove FTP deploy in HBTV-004 / HBTV-005 and replace
  with a documented static publication workflow.

## R-003 — JavaFX tied to JDK 8 assumptions

- Description: `application/trayView` uses `zenjava/javafx-maven-plugin 2.0`
  and JavaFX 2.x APIs; `habiTv-linux` / `habiTv-windows` declare
  `system`-scope `javafx:jfxrt` pointing at `${jdk.home}/jre/lib/ext/jfxrt.jar`
  with hardcoded `jdk.home`. JDK 11+ no longer bundles JavaFX, and
  paths break on most modern machines.
- Mitigation: Capture in HBTV-008. Do not migrate in this restart
  phase; keep Java 8 baseline first.

## R-004 — JAXB generation / runtime mismatch

- Description: `application/core` generates JAXB classes via the
  unmaintained `com.sun.tools.xjc.maven2:maven-jaxb-plugin` and
  depends on `javax.xml.bind:jaxb-api:2.0`. Generated sources are
  not checked in. On JDK 9+, `javax.xml.bind` is not on the default
  classpath; behavior depends entirely on the plugin running.
- Mitigation: Keep Java 8 baseline; revisit when migrating off
  Java 8. Document under HBTV-001 follow-up if reactor surfaces
  hidden generation failures.

## R-005 — Live provider tests are non-deterministic

- Description: Many tests reach live endpoints (Canal+, beIN, RSS,
  Dailymotion, kewego, etc.) via `BasePluginProviderTester` /
  `BasePluginUpdateTester` / hardcoded URLs in
  `fwk/framework/test/.../TestUrl.java` and
  `plugins/*/test/.../*Test.java`. They will flap based on remote
  availability and HTML/JSON changes.
- Mitigation: Keep tests excluded from the default lifecycle in
  HBTV-002; quarantine network tests behind an opt-in profile.

## R-006 — Provider endpoints obsolete or renamed

- Description: Several providers (e.g. Pluzz, beIN, Canal+ legacy
  endpoints) are likely dead or renamed. Provider plugins may
  compile but never produce results in production.
- Mitigation: Inventory in HBTV-006. Removal/rename happens in
  dedicated PRs, not in this restart bootstrap.

## R-007 — Auto-update pulls unexpected old artifacts during development

- Description: `UpdateManager` and `FindArtifactUtils` resolve
  `FrameworkConf.UPDATE_URL` (`http://dabiboo.free.fr/repository`)
  at runtime. Running a dev build can pull whatever happens to
  be on that host (or fail noisily if it is down).
- Mitigation: Plan a feature flag / env override in HBTV-005 to
  disable updates in development. Until then, document the risk.

## R-008 — yt-dlp migration can change download behavior

- Description: `yt-dlp` is not a drop-in for `youtube-dl`: option
  parsing, output templates, and post-processors differ. A naive
  swap risks regressing downloads silently.
- Mitigation: HBTV-007 plans the migration with a behavior diff
  and a deprecation note before any code change.

## R-009 — GitHub Pages / static repo layout may not match old updater expectations

- Description: The runtime updater expects a specific directory
  layout (groupId-as-path, artifactId folder, version list, latest
  marker). Hosting on GitHub Pages or another static host may
  diverge subtly and break update discovery.
- Mitigation: HBTV-005 defines the layout explicitly and validates
  it against `FindArtifactUtils.findLastVersionUrl` semantics
  before cutting over.
