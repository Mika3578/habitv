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
