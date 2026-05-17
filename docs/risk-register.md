# Habitv risk register

Initial risk register for the restart-from-master modernization.
Severity uses `Likelihood x Impact` (Low / Medium / High) and an
overall priority. Update when a risk is added, mitigated, realized,
or accepted.

| ID    | Title                                                           | Likelihood | Impact | Priority |
|-------|-----------------------------------------------------------------|------------|--------|----------|
| R-001 | Legacy Maven repository / free.fr dependency                    | High       | High   | P0       |
| R-002 | FTP deployment no longer viable                                 | High       | High   | P0       |
| R-003 | JavaFX tied to JDK 8 assumptions                                | High       | High   | P1       |
| R-004 | JAXB generation / runtime mismatch                              | Medium     | High   | P1       |
| R-005 | Live provider tests are non-deterministic                       | High       | Medium | P1       |
| R-006 | Provider endpoints obsolete or renamed                          | High       | Medium | P1       |
| R-007 | Auto-update pulls unexpected old artifacts                      | Medium     | High   | P1       |
| R-008 | yt-dlp migration changes download behavior                      | Medium     | Medium | P2       |
| R-009 | GitHub Pages layout mismatches updater                          | Medium     | High   | P1       |
| R-010 | Intra-reactor version range excludes SNAPSHOTs (mitigated)      | Low        | Low    | P3       |
| R-011 | maven-jaxb-plugin missing pinned version (mitigated)            | Low        | Low    | P3       |
| R-012 | Plugin tester version mismatch blocks compile (mitigated)       | Low        | Low    | P3       |
| R-013 | Hardcoded Gmail and freebox credentials in tests / samples      | Medium     | High   | P1       |
| R-014 | GitHub Pages does not serve autoindex (updater contract break)  | High       | Medium | P1       |
| R-015 | CoreManager.stat() pings third-party host on every startup      | High       | Low    | P2       |

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
- Status update (HBTV-012): Mitigated for local reactor compilation of
  internal shared dependencies. Root dependencyManagement now maps
  `com.dabi.habitv:api` and `com.dabi.habitv:framework` to
  `${project.parent.version}`, so own-version plugin modules no longer
  request plugin-local coordinates (`4.1.1-SNAPSHOT` / `4.1.2-SNAPSHOT`)
  from blocked `http://dabiboo.free.fr/repository`.
- Residual risk: Legacy repository migration is still required for
  external publication/runtime update concerns and remains tracked under
  HBTV-004/HBTV-005; this fix is Maven dependency alignment only.

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
- Status update (HBTV-011): Partially mitigated for command wiring by
  adding an offline unit test (`YoutubePluginDownloaderCmdTest`) and a
  runnable `consoleView` runtime path. End-to-end live provider behavior
  remains intentionally out of scope until provider inventory/cleanup
  work (HBTV-006).

## R-009 — GitHub Pages / static repo layout may not match old updater expectations

- Description: The runtime updater expects a specific directory
  layout (groupId-as-path, artifactId folder, version list, latest
  marker). Hosting on GitHub Pages or another static host may
  diverge subtly and break update discovery.
- Mitigation: HBTV-005 defines the layout explicitly and validates
  it against `FindArtifactUtils.findLastVersionUrl` semantics
  before cutting over.

## R-010 — Intra-reactor version range `[4.1,4.2)` excludes SNAPSHOTs

- Description: Root `pom.xml` declares dependencyManagement entries
  for `com.dabi.habitv:api` and `com.dabi.habitv:framework` with the
  closed version range `[4.1,4.2)`. Maven does not by default
  include `4.1.0-SNAPSHOT` in that range, and the legacy
  `dabiboo.free.fr` HTTP repository (which would have served the
  matching release) is blocked by Maven 3.9+ defaults and likely
  unreachable. As a result, `mvn compile` fails at `framework`
  during dependency collection.
- Mitigation: Replace the range with `${project.version}` for
  intra-reactor coordinates in HBTV-002. No code change; POM only.
- Status: Mitigated in HBTV-002 by replacing the `api` and `framework`
  root dependencyManagement versions with `${project.version}`.

## R-011 — `maven-jaxb-plugin` missing pinned version

- Description: `application/core/pom.xml` declares
  `com.sun.tools.xjc.maven2:maven-jaxb-plugin` without a `<version>`.
  Maven 3.9+ warns and may refuse to build in future versions. Build
  behavior depends on which plugin version Maven happens to resolve.
- Mitigation: Pin the plugin version (e.g. the last known-working
  `1.1.1`) in HBTV-002 so JAXB generation stays deterministic. No
  source change.
- Status: Mitigated in HBTV-002 by pinning
  `com.sun.tools.xjc.maven2:maven-jaxb-plugin` to `1.1.1` in
  `application/core/pom.xml`.

## R-012 — Plugin tester version mismatch blocks compile

- Description: Plugin modules declare test-scope dependencies on
  `com.dabi.habitv:plugin-tester:4.1.0`, while the project version is
  `4.1.0-SNAPSHOT` and the reactor artifact is
  `com.dabi.habitv:plugin-tester:4.1.0-SNAPSHOT`. During
  `mvn -B -ntp -DskipTests compile`, Maven fails at `plugins/6play`
  while resolving the `plugin-tester:4.1.0` descriptor from the blocked
  legacy HTTP repository.
- Mitigation: Dedicated POM-only follow-up to align plugin test-harness
  versions to reactor coordinates (for example `${project.version}`) and
  confirm whether `plugin-tester` should stay aggregated in
  `plugins/pom.xml`.
- Status: Mitigated in HBTV-010 by aligning plugin test-harness
  dependency versions to reactor expressions and including
  `plugins/plugin-tester` in `plugins/pom.xml`; `mvn compile` now moves
  past the former `6play`/`plugin-tester:4.1.0` descriptor blocker.
- Residual risk: Compile still hits blocked legacy repository resolution
  for `framework/api:4.1.1-SNAPSHOT` in `beinsport`, which is tracked
  under the existing legacy repository migration risk (R-001 / HBTV-004).

## R-013 — Hardcoded Gmail and freebox credentials in tests / samples

- Description: Test sources and a sample config carry plaintext
  credentials:
  - `plugins/email/test/com/dabi/habitv/plugin/email/MessageReceiverTest.java:14,19`
    and `EmailPluginManagerTest.java:29` use `testhabitv` / `HabiTV410`
    against `pop.gmail.com` / `imap.gmail.com`.
  - `application/consoleView/config.xml:57,84` carries
    `ftp://freebox:4688@hd1.freebox.fr/...` upload examples.
- Mitigation: Dedicated PR to scrub credentials (replace with
  placeholders / environment variables, mark the affected tests
  `@Ignore` until they are reworked as offline fixtures). Rotate the
  Gmail account if still active. Out of scope for HBTV-004; tracked
  here so it is not lost.

## R-014 — GitHub Pages does not serve autoindex (updater contract break)

- Description: `FindArtifactUtils.findLastVersionUrl` (lines 77-123)
  parses Apache `mod_autoindex` HTML to discover versions and files
  under `${UPDATE_URL}/${groupIdPath}/${artifactId}/`. GitHub Pages
  does not serve directory listings by default. If `habitv-repo` is
  hosted on GitHub Pages without per-directory `index.html`, the
  updater silently stops finding new versions.
- Mitigation: HBTV-005 must define a publication workflow that
  emits an `index.html` per directory with `<a href="X/">` entries
  matching the `mod_autoindex` shape. ADR-0009 captures the
  recommended approach.

## R-015 — CoreManager.stat() pings third-party host on every startup

- Description: `application/core/src/com/dabi/habitv/core/mgr/CoreManager.java:46-55`
  fires a background thread on construction that GETs
  `http://dabiboo.free.fr/cpt.php`. There is no gate, no opt-out, and
  no failure logging. Developers and CI silently send traffic to a
  third-party host on every test run that instantiates `CoreManager`.
- Mitigation: HBTV-004 PR 2 adds a `habitv.stat.enabled` system
  property (default `false`). Production packaging sets it to `true`.
  The URL itself is not changed in that step.
