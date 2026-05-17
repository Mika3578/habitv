# Habitv development tracker

Human-readable tracker for restart-from-master modernization work.
This file MUST stay in sync with `docs/dev-tracker.json`.

Status legend: `proposed`, `in-progress`, `blocked`, `done`,
`deferred`. Priority legend: `P0` (critical), `P1` (high),
`P2` (normal), `P3` (low).

---

## HBTV-000 — Restart from master governance bootstrap

- Status: in-progress
- Priority: P0
- Scope: Add governance, CI validate baseline, AI agent guidance,
  tracker/risk/decision/audit docs.
- Acceptance criteria:
  - `.github/pull_request_template.md`, issue templates, and
    `.github/workflows/build.yml` present and valid.
  - `AGENTS.md`, `.cursor/rules/habitv-master.mdc`, and
    `.github/copilot-instructions.md` present.
  - `docs/dev-plan.md`, `docs/dev-tracker.md`,
    `docs/dev-tracker.json`, `docs/risk-register.md`,
    `docs/decision-log.md`, `docs/github-repository-settings.md`,
    `docs/audit-master-baseline.md` present.
  - PR opened against `master` and reviewable.
- Validation: `git status --short`, `git branch --show-current`,
  `git log --oneline -5`, `mvn -B -ntp -DskipTests validate`.
- PR: chore: bootstrap restart workflow from master
- Notes: Documentation/workflow only. No runtime or provider changes.

## HBTV-001 — Maven reactor audit

- Status: in-progress
- Priority: P0
- Scope: Wire the Maven multi-module reactor so `mvn validate` walks
  the full project from the root and parent resolution is
  deterministic across `fwk`, `application`, and `plugins`. Topology
  only; no dependency or plugin upgrades, no source changes.
- Acceptance criteria:
  - Root `pom.xml` aggregates `fwk`, `application`, `plugins`. (done)
  - `fwk/pom.xml` aggregates `api`, `framework`. (done)
  - `application/pom.xml` keeps `core`, `consoleView`, `trayView`,
    `habiTv`. `habiTv-linux` and `habiTv-windows` intentionally
    excluded (hardcoded `${jdk.home}`, JDK-bundled JavaFX, ZenJava
    plugin). (done — see notes)
  - `plugins/pom.xml` keeps the 22 plugin modules. `plugin-tester`
    intentionally excluded (deferred to HBTV-002 test-compile
    scope). (done — see notes)
  - All child POMs parent at `4.1.0-SNAPSHOT` with explicit
    `<relativePath>`. (done)
  - `fwk/framework/pom.xml` own version typo `4.1.0-SNASPHOT`
    fixed to `4.1.0-SNAPSHOT`. (done)
- Validation:
  - `mvn -B -ntp -DskipTests validate` walks all 32 reactor
    modules and reports BUILD SUCCESS.
  - `mvn -B -ntp -DskipTests compile` reaches the next real
    blocker (intra-reactor dependency range `[4.1,4.2)` on
    `api`/`framework` in the root POM excludes SNAPSHOT siblings;
    compile FAILS at `framework`). Deferred to HBTV-002.
- PR: build: stabilize Maven reactor from master.
- Notes:
  - `habiTv-linux` and `habiTv-windows` left on disk but out of
    the reactor; status documented in
    `docs/audit-master-baseline.md`. They remain untouched and
    are tracked under HBTV-008 (JavaFX / runtime packaging audit).
  - `plugin-tester` left on disk but out of the reactor; test
    sources depend on it only when the lifecycle reaches
    `test-compile`, which HBTV-002 will address.
  - One latent inconsistency intentionally preserved:
    `application/habiTv-windows` parents to root `parent` while
    its sibling `habiTv-linux` parents to `application`. Not
    fixed because both modules are excluded from the reactor.

## HBTV-002 — Java 8 baseline CI

- Status: in-progress
- Priority: P0
- Scope: Extend the baseline workflow from `validate` to `compile`
  (and later `test`) once HBTV-001 lands. Specifically:
  - Replace intra-reactor version range `[4.1,4.2)` on
    `com.dabi.habitv:api` / `com.dabi.habitv:framework` in root
    `pom.xml` with `${project.version}` so reactor SNAPSHOTs
    resolve without the blocked HTTP repository.
  - Decide whether to wire `plugins/plugin-tester` into the
    reactor so test sources can resolve the harness once the
    workflow reaches `test-compile`.
  - Pin `maven-jaxb-plugin` to an explicit version (current
    warning: missing version) so `application/core` keeps
    generating deterministically.
- Acceptance criteria:
  - `mvn -B -ntp -DskipTests compile` passes on Ubuntu and Windows
    with Temurin 8.
  - Network/provider tests remain excluded by default.
- Validation:
  - Baseline (before fixes):
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS` (32 modules)
      with warning: `maven-jaxb-plugin` missing version.
    - `mvn -B -ntp -DskipTests compile` -> `BUILD FAILURE` at
      `framework`: `No versions available for ... api:jar:[4.1,4.2)`.
  - After replacing root intra-reactor ranges with `${project.version}`:
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS`.
    - `mvn -B -ntp -DskipTests compile` moves past `framework` and fails
      later at `6play` on `plugin-tester:4.1.0` descriptor resolution.
  - After pinning `maven-jaxb-plugin` to `1.1.1` in `application/core`:
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS` with no
      missing-plugin-version warning.
    - `mvn -B -ntp -DskipTests compile` still fails at `6play` due to
      `plugin-tester:4.1.0` resolution via blocked legacy repository.
  - Evaluation: adding `<module>plugin-tester</module>` to
    `plugins/pom.xml` does not remove the compile blocker because plugin
    modules still request `plugin-tester:4.1.0` while reactor builds
    `4.1.0-SNAPSHOT`. Change was not kept in this PR.
- PR: build: stabilize Java 8 compile baseline.
- Notes: Depends on HBTV-001. R-010 and R-011 are mitigated; the next
  blocker is plugin test-harness version alignment (`plugin-tester`
  `4.1.0` vs reactor `4.1.0-SNAPSHOT`).

## HBTV-003 — Repository branch / rules setup

- Status: proposed
- Priority: P1
- Scope: Apply the GitHub settings documented in
  `docs/github-repository-settings.md` (branch model, protection,
  merge strategy, required checks).
- Acceptance criteria:
  - `master` protected, linear history required.
  - `develop` created from `master` after bootstrap merge.
  - Squash merge enabled, merge commits disabled.
- Validation: Manual confirmation in GitHub UI.
- PR: N/A (settings change, not code).
- Notes: Done by repo owner; tracked here for visibility.

## HBTV-004 — Legacy repository URL migration plan

- Status: proposed
- Priority: P1
- Scope: Plan migration of `<scm>` (SVN/Assembla), Maven
  `<repository>` (`http://dabiboo.free.fr/repository`),
  `<distributionManagement>` (`ftp://ftpperso.free.fr/repository`),
  and runtime telemetry/update URLs.
- Acceptance criteria:
  - Document target URLs and transition steps.
  - Identify code call sites in
    `fwk/framework/.../FrameworkConf.java`,
    `application/core/.../HabitTvConf.java`,
    `application/core/.../UpdateManager.java`,
    `fwk/framework/.../FindArtifactUtils.java`,
    `application/core/.../CoreManager.java`.
- Validation: Plan reviewed in PR; no code changes in this item.
- PR: TBD.
- Notes: Pairs with HBTV-005.

## HBTV-005 — Runtime updater publication plan

- Status: proposed
- Priority: P1
- Scope: Define the `habitv-repo` static publication layout
  (GitHub Pages or equivalent) compatible with the existing
  updater expectations.
- Acceptance criteria:
  - Directory layout documented.
  - Migration path for `UPDATE_URL` documented.
  - Plan for signing / integrity (HTTPS, checksums) documented.
- Validation:
  - `mvn -B -ntp -DskipTests validate`
  - `mvn -B -ntp -DskipTests deploy` with temporary `file://` target
- PR: build/standard-cross-os-static-repo-layout (pending).
- Notes: Pairs with HBTV-004 and Phase 4 of `docs/dev-plan.md`; adds
  cross-OS deploy scripts and standard side-by-side workspace layout
  under `$HOME/dev`.

## HBTV-006 — Provider / plugin inventory

- Status: proposed
- Priority: P2
- Scope: Inventory every plugin in `plugins/` (22 in the
  aggregator + `plugin-tester`); record current status (working,
  obsolete endpoint, renamed, broken parser) without removing
  modules in this item.
- Acceptance criteria:
  - Inventory table per plugin with last-known status.
  - For each obsolete/renamed plugin, a recommended dedicated
    removal/rename PR is named.
- Validation: Inventory reviewed in PR. No code removal.
- PR: TBD.
- Notes: Feeds Phase 6 of `docs/dev-plan.md`.

## HBTV-007 — yt-dlp replacement plan

- Status: proposed
- Priority: P2
- Scope: Plan migration of the `youtube` plugin from `youtube-dl`
  to `yt-dlp` (executable, flags, parsing, config defaults).
- Acceptance criteria:
  - Differences in CLI between `youtube-dl` and `yt-dlp` captured.
  - Migration steps for code and default config listed.
  - Backward-compatibility / deprecation note drafted.
- Validation: Plan reviewed in PR; no behavior change in this item.
- PR: TBD.
- Notes: Implementation deferred to a follow-up PR after the
  reactor is stable.

## HBTV-008 — JavaFX / runtime packaging audit

- Status: proposed
- Priority: P2
- Scope: Audit JavaFX 2.x usage (`application/trayView`,
  `application/habiTv-linux`, `application/habiTv-windows`),
  `zenjava/javafx-maven-plugin 2.0`, and `jfxrt`/`jdk.home`
  assumptions.
- Acceptance criteria:
  - Inventory of JavaFX surface area documented.
  - Migration options for OpenJFX + modern packaging captured
    (jpackage, jlink, jdeploy, or fat-jar fallback).
- Validation: Audit reviewed in PR; no code changes in this item.
- PR: TBD.
- Notes: Pairs with Phase 7 of `docs/dev-plan.md`.

## HBTV-010 — Plugin tester reactor dependency alignment

- Status: done
- Priority: P1
- Scope: Align plugin module test-harness dependencies so
  `com.dabi.habitv:plugin-tester` resolves from the local reactor line
  instead of the blocked legacy HTTP repository; aggregate
  `plugins/plugin-tester` in `plugins/pom.xml`.
- Acceptance criteria:
  - Every plugin module that pinned
    `com.dabi.habitv:plugin-tester:4.1.0` now uses the reactor version
    expression (`${project.version}` or `${project.parent.version}` for
    modules with independent own versions).
  - `plugins/pom.xml` includes `<module>plugin-tester</module>`.
  - `mvn -B -ntp -DskipTests compile` moves past the former
    `plugin-tester:4.1.0` descriptor blocker.
- Validation:
  - Baseline:
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS` (32 modules).
    - `mvn -B -ntp -DskipTests compile` -> `BUILD FAILURE` at `6play` on
      `com.dabi.habitv:plugin-tester:4.1.0` descriptor resolution from
      blocked `http://dabiboo.free.fr/repository`.
  - After alignment:
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS` (33 modules;
      includes `plugin-tester`).
    - `mvn -B -ntp -DskipTests compile` -> moves past `6play` and fails
      later at `beinsport` on `framework/api:4.1.1-SNAPSHOT` resolution
      from blocked legacy repository (HBTV-004-class blocker).
- PR: build: align plugin tester reactor dependency.
- Notes: R-012 is mitigated by this item. Remaining compile blocker is
  legacy repository resolution for non-reactor versions in selected
  plugin modules (`beinsport`, with similar risk for `footyroom`,
  `pluzz`, and `ffmpeg`).

## HBTV-011 — Runnable console baseline with yt-dlp path on `develop`

- Status: in-progress
- Priority: P0
- Scope: Establish a factual runnable baseline on `develop` by
  superseding PR #27 with scoped, linear commits: keep consoleView
  runnable fat-jar packaging and yt-dlp runtime path/testing while
  preserving HBTV-004/HBTV-005 as separate work items.
- Acceptance criteria:
  - `application/consoleView` packages a runnable fat JAR in scoped
    builds.
  - An offline YouTube downloader command wiring test exists and passes.
  - Tracker/audit/risk/decision docs reflect exact command outcomes and
    scope boundaries.
  - PR #25 and PR #26 are explicitly documented as `master`-targeted
    work to retarget/rebase later, not merged into this baseline PR.
- Validation:
  - `develop` baseline:
    - `mvn -B -ntp -DskipTests validate` -> `BUILD SUCCESS`.
    - `mvn -B -ntp -DskipTests compile` -> `BUILD FAILURE` at
      `beinsport` (`framework/api:4.1.1-SNAPSHOT` resolution blocked by
      `maven-default-http-blocker` for
      `http://dabiboo.free.fr/repository`).
  - PR #27 branch inspection (`review/pr-27-yt-dlp-provider`):
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' validate`
      -> `BUILD SUCCESS`.
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' compile`
      -> `BUILD FAILURE` at `application/core` (17 compile errors from
      boolean accessor method changes in `XMLUserConfig` /
      `GrabConfigDAO`).
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package`
      -> `BUILD FAILURE` at `application/core` (same compile errors).
    - `mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubePluginDownloaderCmdTest -Dsurefire.failIfNoSpecifiedTests=false test`
      -> `BUILD SUCCESS` (`Tests run: 2, Failures: 0, Errors: 0`).
  - Final branch (`dev/modernization-status-and-next-step`):
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' validate`
      -> `BUILD SUCCESS`.
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' compile`
      -> `BUILD FAILURE` at `beinsport` on blocked legacy repository
      resolution of `framework/api:4.1.1-SNAPSHOT`.
    - `mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package`
      -> `BUILD FAILURE` at `beinsport` on the same blocked legacy
      repository resolution.
    - `mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubePluginDownloaderCmdTest -Dsurefire.failIfNoSpecifiedTests=false test`
      -> `BUILD SUCCESS` (`Tests run: 2, Failures: 0, Errors: 0`).
- PR: feat(console): restore runnable baseline with yt-dlp provider.
- Notes:
  - PR #27 was **superseded** (scoped subset reused): POM/build,
    consoleView fat-jar/runtime docs, and YouTube offline test were
    preserved; failing `application/core` source edits were intentionally
    excluded.
  - JavaFX modernization (HBTV-008), tray/GUI packaging, provider
    cleanup (HBTV-006), and scraper rewrites remain out of scope.
  - PR #25 and PR #26 currently target `master`; they must be
    retargeted/rebased onto `develop` (or recreated in scoped PRs)
    after this baseline is merged.
  - HBTV-004/HBTV-005 legacy repository/update URL migration remains
    separate and must not be mixed into this runnable baseline PR.
