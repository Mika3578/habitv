# Habitv development plan

High-level phased plan for restarting the Habitv modernization from
`master`. Each phase is delivered through one or more small PRs tied
to tracker items in `docs/dev-tracker.md`. No phase is allowed to
bundle work from a later phase.

## Phase 0 — Bootstrap governance from master

- Establish branching model, CI baseline, AI agent guidance, and the
  documentation skeleton (this PR).
- Deliverables: `.github/` templates and workflow, `.cursor` rules,
  `AGENTS.md`, `docs/` plan/tracker/risk/decision/settings/audit.
- Validation: `mvn -B -ntp -DskipTests validate` on Ubuntu + Windows
  with Temurin 8.
- Exit criteria: bootstrap PR merged to `master` and recommended
  GitHub settings applied manually.

## Phase 1 — Stabilize Maven reactor

- Make the root `pom.xml` an actual reactor parent and wire
  `fwk`, `application`, `plugins` aggregators so `mvn validate`
  walks the full project.
- Reconcile parent version mismatches (`4.1.0` vs
  `4.1.0-SNAPSHOT`, the `4.1.0-SNASPHOT` typo, etc.).
- Decide the status of `plugins/plugin-tester` (include vs
  separate harness) and `application/habiTv-linux` /
  `habiTv-windows` (excluded from the application aggregator
  today).
- Tracker: HBTV-001.

## Phase 2 — Stabilize Java 8 build/test baseline

- Make `mvn -B -ntp -DskipTests compile` succeed on Temurin 8 in CI.
- Pin `maven-compiler-plugin`, `maven-surefire-plugin`, and
  `maven-failsafe-plugin` to versions compatible with Java 8 and the
  legacy `testSourceDirectory` layout.
- Add a separate opt-in profile or workflow for `test` once the
  network-dependent tests are quarantined.
- Tracker: HBTV-002.

## Phase 3 — Remove legacy free.fr / SVN / FTP references safely

- Replace SVN/Assembla `<scm>` blocks with the current GitHub URLs.
- Remove FTP `<distributionManagement>` and `wagon-ftp` extension.
- Replace `http://dabiboo.free.fr/repository` with the planned
  GitHub Pages / static repo location (see Phase 4).
- Quarantine `STAT_URL` / `UPDATE_URL` behind a feature flag so
  development builds do not ping legacy hosts.
- Tracker: HBTV-004.

## Phase 4 — Publish artifacts/plugins/tools to `habitv-repo`

- Stand up the `habitv-repo` static repository (GitHub Pages or
  equivalent) to host artifacts, plugin drops, and update metadata.
- Define directory layout compatible with the existing runtime
  updater expectations (`FindArtifactUtils`, `UpdateManager`).
- Document publication workflow and credentials handling.
- Tracker: HBTV-005.

## Phase 5 — Replace youtube-dl with yt-dlp

- Switch the `youtube` plugin's binary expectations from
  `youtube-dl` to `yt-dlp` (executable name, command flags,
  output parsing).
- Migrate default config (`application/core/configuration.xml`).
- Provide a deprecation note for users relying on `youtube-dl`.
- Tracker: HBTV-007.

## Phase 6 — Audit/deprecate obsolete providers

- Inventory all provider plugins and verify endpoints are reachable
  and parsable (offline against captured fixtures, not live).
- Mark obsolete or renamed providers with a deprecation plan and
  dedicated removal PRs.
- Tracker: HBTV-006.

## Phase 7 — Modernize UI/runtime only after stable baseline

- Migrate JavaFX 2.x (JDK-bundled `jfxrt`) to OpenJFX with a
  modern build (jpackage, jlink, or equivalent).
- Re-evaluate `application/habiTv-linux` and `habiTv-windows`
  packaging.
- Modernize the runtime updater (HTTPS, signed metadata) once the
  static repo from Phase 4 is in place.
- Tracker: HBTV-008.
