# Habitv master baseline audit

Initial audit of the repository at the `master` tip from which the
restart-from-master modernization begins. Captured during the
HBTV-000 bootstrap PR.

## Repository layout

Top-level entries on `master`:

- `pom.xml` — root parent POM (no reactor `<modules>`).
- `fwk/` — `fwk/api`, `fwk/framework`, plus `fwk/pom.xml`
  (no aggregator `<modules>`).
- `application/` — `application/pom.xml` aggregator listing
  `core`, `consoleView`, `trayView`, `habiTv`; plus
  `application/habiTv-linux` and `application/habiTv-windows`
  on disk **but not in the aggregator**.
- `plugins/` — `plugins/pom.xml` aggregator listing 22 modules
  (`6play`, `adobeHDS`, `aria2`, `arte`, `beinsport`, `canalPlus`,
  `clubic`, `cmd`, `curl`, `email`, `ffmpeg`, `file`, `footyroom`,
  `globalnews`, `lequipe`, `mlssoccer`, `pluzz`, `RSS`, `rtmpDump`,
  `sfr`, `youtube`, `wat`); plus `plugins/plugin-tester` on disk
  **but not in the aggregator**.
- `README.md`, `.gitignore`, plus a few legacy text files
  (`habitv4TODO.txt`, `newHabiTv.txt`, `last.php`).
- Local IDE folders (`.idea`, `.metadata`, `.vscode`) exist on the
  working machine but are not in scope for this PR.

## Maven modules and parent topology

- Root `pom.xml` declares `groupId=com.dabi.habitv`,
  `artifactId=parent`, `version=4.1.0-SNAPSHOT`, `packaging=pom`.
  Provides `dependencyManagement` / `pluginManagement` only.
- `fwk/pom.xml` is a `packaging=pom` placeholder with no
  `<modules>`; `fwk/api` and `fwk/framework` are not built via
  this aggregator.
- `application/pom.xml` lists four modules but parent reference is
  `version=4.1.0` while its own version is `4.1.0-SNAPSHOT`
  (version mismatch).
- `application/habiTv-windows/pom.xml` uses
  `<parent>...<version>4.1.0-SNAPSHOT</version></parent>` and
  references the root `parent` directly, bypassing the
  `application` aggregator.
- `fwk/framework/pom.xml` declares
  `<version>4.1.0-SNASPHOT</version>` (typo).
- `plugins/pom.xml` lists 22 modules; `plugin-tester` lives in
  `plugins/plugin-tester` but is intentionally a separate harness
  project depended on by some plugin tests.

Net effect: a plain `mvn install` at the repository root only
installs the root parent POM. The reactor must be wired in
HBTV-001 before broader Maven goals are useful.

## Known legacy infrastructure references

- SCM still on Subversion / Assembla in the root `pom.xml`:
  `scm:svn:http://subversion.assembla.com/svn/habitv/trunk`.
  The same pattern is repeated in roughly 22 module POMs under
  `plugins/`, `fwk/`, and `application/habiTv-*`.
- `plugins/email/pom.xml` SCM points at `plugins/RSS` (copy/paste
  artifact).
- Maven `<repository>` in the root `pom.xml`:
  `<url>http://dabiboo.free.fr/repository</url>` (plain HTTP,
  third-party hosting). Duplicated in
  `application/habiTv-linux/pom.xml` and
  `application/habiTv-windows/pom.xml`.
- `<distributionManagement>` in the root `pom.xml`:
  `<url>ftp://ftpperso.free.fr/repository</url>` with build
  `<extension>` `wagon-ftp` `1.0-beta-6`.
- Runtime URLs:
  - `fwk/framework/src/com/dabi/habitv/framework/FrameworkConf.java`
    declares `UPDATE_URL = "http://dabiboo.free.fr/repository"`.
  - `application/core/src/com/dabi/habitv/core/config/HabitTvConf.java`
    declares `STAT_URL = "http://dabiboo.free.fr/cpt.php"`.
  - `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java`
    and
    `fwk/framework/src/com/dabi/habitv/framework/plugin/utils/update/FindArtifactUtils.java`
    use these URLs at runtime.
  - `application/core/src/com/dabi/habitv/core/mgr/CoreManager.java`
    pings `STAT_URL` for telemetry.
- `application/consoleView/config.xml` contains an FTP `curl`
  upload sample with embedded credentials targeting
  `ftp://...@hd1.freebox.fr/...`. Not used by code but illustrative
  of the legacy operational model.

## Current build assumptions

- Java baseline:
  - Root `maven-compiler-plugin` pins `<source>1.7` / `<target>1.7`.
  - `application/habiTv-linux/pom.xml` and `habiTv-windows/pom.xml`
    pin `maven.compiler.source/target` to `7` and reference a
    hardcoded `${jdk.home}` for JavaFX (`jfxrt.jar`,
    `com.sun.javafx.tools.ant`).
- JavaFX:
  - `application/trayView/pom.xml` uses ZenJava
    `javafx-maven-plugin 2.0`; JavaFX 2.2+ assumed.
  - `application/trayView` source imports `javafx.*` (JavaFX 2.x).
  - `habiTv-linux` / `habiTv-windows` declare `system`-scope
    `javafx:jfxrt` referencing `jfxrt.jar` under `${jdk.home}`.
- JAXB:
  - Root `pom.xml` pins `javax.xml.bind:jaxb-api:2.0`.
  - `application/core/pom.xml` runs
    `com.sun.tools.xjc.maven2:maven-jaxb-plugin` with three
    executions (`config`, `configuration`, `grabconfig`) writing
    generated sources to `generated/`.
  - `application/core` and `fwk/framework` use
    `javax.xml.bind.JAXBContext` at runtime.
  - No generated `.java` is checked in; builds depend on the
    plugin running successfully.
- Tests:
  - Root POM redirects test sources to
    `${project.basedir}/test` (non-standard layout).
  - JUnit 4.11 pinned in dependency management. No Surefire /
    Failsafe configuration anywhere.
  - No Maven `<profiles>` exist.
- CI:
  - No `.github` workflows or `Jenkinsfile` are tracked on
    `master` before this PR.

## Test risks

- Live provider scraping via `BasePluginProviderTester` and
  hard-coded remote URLs (Canal+, beIN, Dailymotion RSS, kewego,
  arte, youtube, etc.).
- Updater / repository HTTP probing
  (`UpdateManagerTest`, `TestListHttp`,
  `BasePluginUpdateTester` and its consumers in `curl`, `ffmpeg`,
  `aria2`, `rtmpDump`, `youtube`).
- External tool dependencies (`curl`, `ffmpeg`, `AdobeHDS.php`,
  `rtmpdump`) in test mains.
- `plugins/email/test/.../MessageReceiverTest.java` connects to
  live Gmail POP3 / IMAP with embedded credentials. This is a
  credential-exposure risk and a non-deterministic test.
- `plugins/cmd` has no `test/` tree.

## What this PR changes

- Adds `.github/pull_request_template.md`,
  `.github/ISSUE_TEMPLATE/bug.md`,
  `.github/ISSUE_TEMPLATE/modernization.md`, and
  `.github/workflows/build.yml` (`mvn -B -ntp -DskipTests validate`
  on Ubuntu and Windows with Temurin 8).
- Adds `AGENTS.md`, `.cursor/rules/habitv-master.mdc`, and
  `.github/copilot-instructions.md` to constrain AI assistance.
- Adds `docs/dev-plan.md`, `docs/dev-tracker.md`,
  `docs/dev-tracker.json`, `docs/risk-register.md`,
  `docs/decision-log.md`, `docs/github-repository-settings.md`,
  and this `docs/audit-master-baseline.md`.

## What this PR intentionally does not change

- No changes to any `pom.xml` (reactor, parents, versions,
  dependencies, plugins, repositories, distribution management).
- No changes to source code under `fwk/`, `application/`, or
  `plugins/`.
- No changes to legacy SVN/Assembla `<scm>` URLs.
- No changes to runtime updater URLs or telemetry endpoints.
- No removal or renaming of any plugin / provider module.
- No JavaFX, JAXB, or `youtube-dl` migration.
- No new Maven plugin (OWASP, SBOM, static analysis, formatter).

## Local validation status

Captured at PR creation time. Update if the environment changes.

- `git status --short`: clean working tree on
  `chore/bootstrap-restart-from-master` after the bootstrap files
  were staged.
- `git branch --show-current`: `chore/bootstrap-restart-from-master`.
- `git log --oneline -5`: shows the four bootstrap commits on top
  of `ed736d81 fix footyroom`.
- `mvn -B -ntp -DskipTests validate`:
  - Executed locally on Windows 11 with Apache Maven 3.9.11 and
    Azul Zulu OpenJDK 1.8.0_492. `BUILD SUCCESS` for
    `com.dabi.habitv:parent:4.1.0-SNAPSHOT (pom)` only, because
    the root POM has no `<modules>` (HBTV-001).
  - Per-module validation has **not** been run as part of this
    PR; that work belongs to HBTV-001.
  - CI (`.github/workflows/build.yml`) will re-run the same
    command on `ubuntu-latest` and `windows-latest` after merge.

## Recommended next PR after merge

`build: stabilize Maven reactor from master` (HBTV-001):

- Wire root `pom.xml` to aggregate `fwk`, `application`, `plugins`.
- Wire `fwk/pom.xml` to aggregate `fwk/api`, `fwk/framework`.
- Decide and document the status of
  `application/habiTv-linux`, `application/habiTv-windows`, and
  `plugins/plugin-tester` in their respective aggregators.
- Resolve parent version mismatches and the
  `4.1.0-SNASPHOT` typo in `fwk/framework/pom.xml`.
- Keep the change scope to POM topology only; do not upgrade
  plugins or dependencies in that PR.

## Maven reactor stabilization findings (HBTV-001)

Captured during the `build: stabilize Maven reactor from master`
PR. Branch: `build/stabilize-maven-reactor-from-master` based on
`develop` (which was created from `origin/master` at the bootstrap
merge commit). Local environment: Windows 11, Apache Maven 3.9.11,
Azul Zulu OpenJDK 1.8.0_492.

### Modules wired

Reactor build order observed via `mvn -B -ntp -DskipTests validate`
walks 32 entries:

- Root: `com.dabi.habitv:parent` (pom).
- `fwk`: `api`, `framework`, plus the `fwk` aggregator itself.
- `application`: `core`, `consoleView`, `trayView`, `habiTv`, plus
  the `application` aggregator itself.
- `plugins`: 22 plugin modules (`6play`, `adobeHDS`, `aria2`,
  `arte`, `beinsport`, `canalPlus`, `clubic`, `cmd`, `curl`,
  `email`, `ffmpeg`, `file`, `footyroom`, `globalnews`, `lequipe`,
  `mlssoccer`, `pluzz`, `RSS`, `rtmpDump`, `sfr`, `youtube`,
  `wat`) plus the `plugins` aggregator itself.

### Modules intentionally not wired

- `application/habiTv-linux` and `application/habiTv-windows`.
  Both declare hardcoded `${jdk.home}` properties pointing at
  Linux/Windows JDK 7 install paths, use `system`-scope
  `javafx:jfxrt` referencing `jfxrt.jar` under those paths, depend
  on `com.zenjava:javafx-maven-plugin:2.0` (unmaintained), and use
  `com.sun.javafx.tools.ant` packaging tasks. They cannot build
  on a clean machine. Tracked under HBTV-008 (JavaFX / runtime
  packaging audit). Their POMs were not modified in this PR.
- `plugins/plugin-tester`. It is the shared harness referenced by
  most plugin tests at `<scope>test</scope>`. `validate` and
  `compile` do not exercise test sources, so excluding it from
  the reactor does not block the current CI baseline. Wiring it
  in will be revisited when HBTV-002 extends the workflow to
  `test-compile` (or `test`).

### Cross-cutting POM changes applied

- Root `pom.xml`: added `<modules>fwk, application, plugins</modules>`.
- `fwk/pom.xml`: added `<modules>api, framework</modules>`.
- All in-reactor child POMs now declare their `<parent>` at
  version `4.1.0-SNAPSHOT` with an explicit `<relativePath>` to
  the correct parent file. This eliminates the previous Maven
  warning "`parent.relativePath` ... points at ... instead of ...".
- `fwk/framework/pom.xml`: own `<version>` fixed from
  `4.1.0-SNASPHOT` to `4.1.0-SNAPSHOT`.
- `application/habiTv-windows` was deliberately not edited.
  It still parents to root `parent` (its sibling `habiTv-linux`
  parents to `application`); both are out of the reactor.

### Cross-cutting non-changes (preserved as-is)

- Dependency versions are unchanged. Plugin own versions
  `4.1.1-SNAPSHOT` (`pluzz`, `footyroom`, `beinsport`) and
  `4.1.2-SNAPSHOT` (`ffmpeg`) are intentional (not typos).
- Test-scope `plugin-tester` references at version `4.1.0`
  inside plugin POMs are unchanged. They fail to resolve when
  `test-compile` runs, but `validate` and `compile` do not
  trigger them. HBTV-002 will reconcile.
- The intra-reactor version range `[4.1,4.2)` on `api` and
  `framework` inside the root `pom.xml` dependencyManagement is
  unchanged. It is the next real blocker (see below); fixing it
  belongs to HBTV-002 per scope rules.
- `maven-jaxb-plugin` (`application/core`) still has no pinned
  `<version>`. Warning persists; no change in this PR.
- Legacy SVN `<scm>`, HTTP `dabiboo.free.fr` `<repository>`, and
  FTP `<distributionManagement>` are untouched (HBTV-004).

### Validation results

- `git status --short`: 32 modified POMs plus the four docs
  updates listed in the PR body.
- `git branch --show-current`:
  `build/stabilize-maven-reactor-from-master`.
- `mvn -B -ntp -DskipTests validate` from the repository root:
  `BUILD SUCCESS`, 32 reactor entries built (`pom` aggregators
  plus `jar` modules). Only remaining warning is
  `maven-jaxb-plugin` missing version (R-011).
- `mvn -B -ntp -DskipTests compile` from the repository root:
  `BUILD FAILURE` at `com.dabi.habitv:framework`. Cause:
  `No versions available for com.dabi.habitv:api:jar:[4.1,4.2)
  within specified range`. Maven cannot satisfy the closed range
  because the only available `api` artifact is the reactor's
  `4.1.0-SNAPSHOT`, which is not within `[4.1,4.2)` by default,
  and the legacy `http://dabiboo.free.fr/repository` is blocked
  by Maven 3.9 default mirror policy. Tracked as R-010 and
  scoped to HBTV-002.

### Next recommended PR after HBTV-002

`build: stabilize Java 8 compile baseline` (HBTV-002):

- In root `pom.xml`, replace the intra-reactor dependencyManagement
  ranges `[4.1,4.2)` on `com.dabi.habitv:api` and
  `com.dabi.habitv:framework` with `${project.version}`.
- Pin `com.sun.tools.xjc.maven2:maven-jaxb-plugin` to its last
  known-working version in `application/core/pom.xml`.
- Decide on `plugins/plugin-tester` wiring (likely include it as
  a module so test-scope reactor coordinates resolve).
- Keep scope to POM topology / version pins. No source changes,
  no plugin upgrades, no provider rewrites, no JavaFX work, no
  FTP/HTTP repo migration.

## Java 8 compile baseline findings (HBTV-002)

Captured during the `build: stabilize Java 8 compile baseline` PR on
branch `build/stabilize-java8-compile-baseline` from `develop`.
Environment: Windows 11, Apache Maven 3.9.11, Java 8.

### HBTV-010 scope applied

- Root `pom.xml`: replaced dependencyManagement ranges `[4.1,4.2)` for
  intra-reactor `com.dabi.habitv:api` and `com.dabi.habitv:framework`
  with `${project.version}`.
- `application/core/pom.xml`: pinned
  `com.sun.tools.xjc.maven2:maven-jaxb-plugin` to `1.1.1`.
- `plugins/pom.xml`: temporary evaluation adding
  `<module>plugin-tester</module>` was tested and reverted because it
  did not resolve compile.

### Validation progression

- Baseline `mvn -B -ntp -DskipTests validate`: `BUILD SUCCESS` across
  32 modules, with warning about missing `maven-jaxb-plugin` version.
- Baseline `mvn -B -ntp -DskipTests compile`: `BUILD FAILURE` at
  `framework` due to `api:jar:[4.1,4.2)` resolution.
- After root version fix: `compile` moved past `framework` and reached
  `plugins/6play`.
- After JAXB plugin pin: warning removed from `validate`; `compile`
  still failed at `plugins/6play` on
  `com.dabi.habitv:plugin-tester:4.1.0` descriptor resolution via the
  blocked legacy repository.
- With temporary `plugin-tester` reactor inclusion: `compile` still
  failed at the same place because module POMs request `4.1.0` while
  reactor builds `4.1.0-SNAPSHOT`.

### Risk status impact

- R-010 mitigated by replacing intra-reactor ranges with
  `${project.version}`.
- R-011 mitigated by pinning JAXB plugin version to `1.1.1`.
- New blocker logged as R-012 (plugin tester version mismatch).

### Next recommended PR

Follow up HBTV-002 with a POM-only change set to align plugin
test-harness dependencies from `plugin-tester:4.1.0` to reactor-aligned
coordinates (for example `${project.version}`), then re-run
`mvn -B -ntp -DskipTests compile`.

## Plugin tester alignment findings (HBTV-010)

Captured during the `build: align plugin tester reactor dependency` PR on
branch `build/align-plugin-tester-reactor-dependency` from `develop`.
Environment: Windows 11, Apache Maven 3.9.11, Java 8.

### Scope applied

- Plugin module POMs that pinned
  `com.dabi.habitv:plugin-tester:4.1.0` now use reactor-aligned version
  expressions:
  - `${project.version}` for modules on the parent line.
  - `${project.parent.version}` for modules with independent own
    versions (`beinsport`, `footyroom`, `pluzz`, `ffmpeg`).
- `plugins/pom.xml` now includes `<module>plugin-tester</module>` before
  the provider plugin modules, so the test harness is built in-reactor.

### HBTV-010 validation results

- Baseline `mvn -B -ntp -DskipTests validate`: `BUILD SUCCESS`
  (32 modules).
- Baseline `mvn -B -ntp -DskipTests compile`: `BUILD FAILURE` at
  `6play` due to `com.dabi.habitv:plugin-tester:4.1.0` descriptor
  resolution via blocked `http://dabiboo.free.fr/repository`.
- After alignment `mvn -B -ntp -DskipTests validate`: `BUILD SUCCESS`
  (33 modules, including `plugin-tester`).
- After alignment `mvn -B -ntp -DskipTests compile`: `BUILD FAILURE`
  later at `beinsport` while resolving
  `com.dabi.habitv:framework:4.1.1-SNAPSHOT` and
  `com.dabi.habitv:api:4.1.1-SNAPSHOT` from the blocked legacy
  repository.

### Risk and tracker impact

- R-012 is mitigated: compile now moves past the plugin-tester
  descriptor blocker.
- Remaining blocker class is legacy external repository resolution,
  mapped to HBTV-004.

### HBTV-010 next recommended PR

`build: align plugin module framework/api reactor versions` (HBTV-004):

- Replace non-reactor `4.1.1-SNAPSHOT` / `4.1.2-SNAPSHOT` intra-project
  dependency coordinates in plugin POMs with parent-reactor-aligned
  expressions where valid.
- Keep scope POM-only; do not change Java source, provider behavior,
  JavaFX modules, runtime updater, or repository publication settings.

## Runnable console baseline findings on `develop` (HBTV-011)

Captured during branch `dev/modernization-status-and-next-step` from
`develop`. Environment: Windows 11, Apache Maven 3.9.11, Java 8.

### Baseline before branch changes (`develop`)

- `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS`.
- `mvn -B -ntp -DskipTests compile` -> `BUILD FAILURE` at `beinsport`
  when resolving `com.dabi.habitv:framework:4.1.1-SNAPSHOT` and
  `com.dabi.habitv:api:4.1.1-SNAPSHOT` via blocked
  `http://dabiboo.free.fr/repository`.

### PR #27 inspection outcome

- Local review branch: `review/pr-27-yt-dlp-provider`.
- GitHub mergeability metadata (Mika3578/habitv): `mergeable=MERGEABLE`,
  `mergeStateStatus=CLEAN` at inspection time.
- Scoped command results:
  - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' validate`
    -> `BUILD SUCCESS`.
  - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' compile`
    -> `BUILD FAILURE` at `application/core` with 17 compile errors
    caused by boolean accessor changes in
    `XMLUserConfig` / `GrabConfigDAO`.
  - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package`
    -> `BUILD FAILURE` at `application/core` (same errors).
  - `mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubePluginDownloaderCmdTest -Dsurefire.failIfNoSpecifiedTests=false test`
    -> `BUILD SUCCESS` (`Tests run: 2, Failures: 0, Errors: 0`).

Conclusion: PR #27 was not merged as-is; it was superseded with a scoped
subset.

### Scoped changes kept in HBTV-011 branch

- Build/POM subset from PR #27 kept (Java 8+ compiler/JAXB dependency
  updates) without the failing `application/core` Java source edits.
- `application/consoleView` now packages a runnable fat JAR path and has
  sample runtime configuration.
- `plugins/youtube` has offline yt-dlp command wiring coverage
  (`YoutubePluginDownloaderCmdTest`).

### Final scoped validation on HBTV-011 branch

- `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' validate`
  -> `BUILD SUCCESS`.
- `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' compile`
  -> `BUILD FAILURE` at `beinsport` on blocked legacy repository
  resolution for `framework/api:4.1.1-SNAPSHOT`.
- `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package`
  -> `BUILD FAILURE` at `beinsport` on the same blocked legacy
  repository resolution.
- `mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubePluginDownloaderCmdTest -Dsurefire.failIfNoSpecifiedTests=false test`
  -> `BUILD SUCCESS` (`Tests run: 2, Failures: 0, Errors: 0`).

### Explicit out-of-scope reminders

- JavaFX modernization and GUI runtime packaging (HBTV-008) remain out
  of scope for this baseline.
- Provider scraper cleanup/rewrite and obsolete plugin removal (HBTV-006)
  remain out of scope.
- PR #25 and PR #26 currently target `master`; they must be
  retargeted/rebased onto `develop` later and are not merged in this
  branch.
- HBTV-004/HBTV-005 legacy repository and update URL migration stay
  separate from this runnable baseline.
