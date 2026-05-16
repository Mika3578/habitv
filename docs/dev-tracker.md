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

- Status: proposed
- Priority: P0
- Scope: Investigate and document why the root `pom.xml`, `fwk/pom.xml`
  do not list `<modules>`; propose a corrected reactor that includes
  `fwk`, `application`, `plugins` (and decides on
  `plugins/plugin-tester`, `application/habiTv-linux`,
  `application/habiTv-windows`).
- Acceptance criteria:
  - Reactor wiring proposal documented with reasoning.
  - Parent version mismatches identified
    (`4.1.0` vs `4.1.0-SNAPSHOT`, the `4.1.0-SNASPHOT` typo).
  - Pilot branch demonstrates `mvn -N validate` succeeds at root.
- Validation: `mvn -B -ntp -N -DskipTests validate` plus a per-module
  smoke walk.
- PR: build: stabilize Maven reactor from master (planned).
- Notes: Audit + minimal wiring only; no plugin/version upgrades.

## HBTV-002 — Java 8 baseline CI

- Status: proposed
- Priority: P0
- Scope: Extend the baseline workflow from `validate` to `compile`
  (and later `test`) once HBTV-001 lands.
- Acceptance criteria:
  - `mvn -B -ntp -DskipTests compile` passes on Ubuntu and Windows
    with Temurin 8.
  - Network/provider tests remain excluded by default.
- Validation: CI matrix runs green on Ubuntu and Windows.
- PR: TBD.
- Notes: Depends on HBTV-001.

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
- Validation: Plan reviewed in PR.
- PR: TBD.
- Notes: Pairs with HBTV-004 and Phase 4 of `docs/dev-plan.md`.

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
