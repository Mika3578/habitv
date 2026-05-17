# Habitv decision log

ADR-style architecture and process decisions for the
restart-from-master modernization. Append-only; new decisions
supersede older ones rather than rewriting them in place.

---

## ADR-0001 — Restart modernization from `master`

- Status: Accepted
- Date: 2026-05-16
- Context: Earlier modernization attempts diverged from the stable
  `master` baseline and accumulated unrelated changes, making it
  hard to reason about scope and rollback. A previous bootstrap
  branch (`chore/bootstrap-restart-from-master`) had different
  filenames and conventions from the agreed spec.
- Decision: Restart modernization from the current `master` tip.
  Recreate the bootstrap branch from `origin/master` and deliver
  only governance / documentation / CI validate baseline in the
  first PR. All later modernization PRs branch from the resulting
  governance baseline (or `develop` once created).
- Consequences: Short-term throwaway of previous bootstrap branch
  content. Clear, reviewable starting point. Future ADRs can build
  on a known baseline.

## ADR-0002 — Keep Java 8 as baseline until the build is stable

- Status: Accepted
- Date: 2026-05-16
- Context: The codebase declares Java 1.7 in the root
  `maven-compiler-plugin` and Java 7 in some packaging POMs.
  JavaFX 2.x and `javax.xml.bind` 2.0 assume the legacy JDK 8
  stack. Migrating Java baselines while the reactor itself is
  broken would conflate multiple risks.
- Decision: Keep Java 8 (Temurin 8) as the baseline for all CI and
  agent guidance until HBTV-001 (reactor) and HBTV-002 (compile +
  test baseline) are stable. Any later baseline change requires a
  dedicated migration PR and a superseding ADR.
- Consequences: AI agents and contributors must reject Java 9+
  language and API suggestions. CI matrix uses Temurin 8 only in
  this phase.

## ADR-0003 — Use small PRs with linear history

- Status: Accepted
- Date: 2026-05-16
- Context: The repository's recent history mixes unrelated changes
  in single commits, making review and rollback expensive.
- Decision: One tracker item per PR. Conventional Commits with
  imperative, lowercase subjects. Linear history enforced via
  branch protection (no merge commits, squash merges preferred).
  Rebase merges allowed only when the owner is comfortable.
- Consequences: Slightly more overhead per change, materially
  better reviewability and bisectability. CI gates only on the
  validate workflow until HBTV-002 lands.

## ADR-0004 — Use `habitv-repo` as the future public static artifact repository

- Status: Proposed
- Date: 2026-05-16
- Context: Today the project relies on
  `http://dabiboo.free.fr/repository` for both Maven artifact
  resolution and runtime updates. The host is third-party,
  plain-HTTP, and unmaintained.
- Decision: Stand up a `habitv-repo` static repository (target:
  GitHub Pages or equivalent HTTPS static host) to publish Maven
  artifacts, plugin drops, and update metadata. Layout is defined
  in HBTV-005 to remain compatible with `FindArtifactUtils` and
  `UpdateManager` semantics.
- Consequences: HTTPS, version-controlled publication. Future
  ADR will confirm or supersede this once layout and publication
  workflow are validated.

## ADR-0005 — Keep provider cleanup separate from build / governance bootstrap

- Status: Accepted
- Date: 2026-05-16
- Context: Several provider plugins (Pluzz, legacy Canal+, beIN,
  others) are likely obsolete. Removing them in the bootstrap PR
  would blend documentation work with risky behavior changes and
  break the "small, scoped" rule.
- Decision: Inventory provider/plugin status in HBTV-006 without
  removing modules. Each obsolete/renamed plugin gets its own
  dedicated PR with a deprecation note. The bootstrap PR does
  not touch `plugins/*` or runtime code.
- Consequences: Plugins remain unchanged in this PR. Reviewers
  can focus on governance. Cleanup happens later in safe,
  named units.

## ADR-0006 — Establish runnable console baseline on `develop` before broader modernization

- Status: Accepted
- Date: 2026-05-17
- Context: `develop` is the active modernization line. PR #27 introduces
  a useful runnable console/yt-dlp path but also includes
  `application/core` source edits that fail scoped compile/package.
  Concurrent PR #25 and PR #26 are currently targeted at `master`,
  which would mix branch lines if merged directly.
- Decision: Create a dedicated stabilization branch from `develop` and
  supersede PR #27 with scoped linear commits that keep only the
  buildable subset (consoleView fat JAR path, yt-dlp runtime/test
  wiring, and required build POM updates). Exclude failing unrelated
  source edits and keep HBTV-004/HBTV-005, JavaFX modernization, and
  provider cleanup out of scope.
- Consequences: `develop` gains a factual runnable console baseline with
  offline yt-dlp command coverage while known legacy repository blockers
  remain explicit. PR #25 and PR #26 must be retargeted/rebased to
  `develop` (or recreated as scoped follow-ups) after this baseline
  lands.

## ADR-0008 — Quarantine network pings at startup in development builds

- Status: Proposed
- Date: 2026-05-18
- Context: `CoreManager.stat()` and `UpdateManager.process()` both
  resolve URLs from `HabitTvConf.STAT_URL` /
  `FrameworkConf.UPDATE_URL` (`http://dabiboo.free.fr/...`) at
  runtime. Today every dev or CI run that constructs `CoreManager`
  or invokes the updater silently pings the legacy third-party host.
  This leaks traffic, slows tests, and prevents the URL migration
  in HBTV-004 / HBTV-005 from being verifiable on a clean machine.
- Decision: Introduce two opt-in system properties (or environment
  variables) gating the network calls without modifying the URL
  constants themselves:
  - `habitv.stat.enabled` gates `CoreManager.stat()`.
  - `habitv.update.enabled` gates `UpdateManager.process()`.
  Both default to `false`. Production packaging scripts and end-user
  releases set them to `true`. This decouples "should we ping" from
  "what URL do we ping" so HBTV-004 PRs 2-3 can land before HBTV-005
  picks the new host.
- Consequences: Slight increase in code surface (two property
  lookups) in exchange for predictable, network-free dev/CI runs.
  Telemetry data may decrease until packaging scripts are updated;
  acceptable trade-off given the third-party host status.

## ADR-0009 — Publish via GitHub Pages with generated `index.html`

- Status: Proposed
- Date: 2026-05-18
- Context: `FindArtifactUtils.findLastVersionUrl` discovers
  artifacts by parsing Apache `mod_autoindex` HTML at
  `${UPDATE_URL}/${groupIdPath}/${artifactId}/[version/]`. GitHub
  Pages does not serve directory listings, and other static-host
  options either require authentication (GitHub Packages) or break
  the URL shape (GitHub Releases). Rewriting the updater
  contract is out of scope for the restart phase. The companion
  `docs/static-repository-deploy.md` introduced on `develop`
  already documents the side-by-side workspace layout for
  publication scripts; this ADR adds the contract for the served
  HTTP shape.
- Decision: Adopt GitHub Pages on a dedicated `Mika3578/habitv-repo`
  repository. The publication workflow (GitHub Action triggered
  on release tags) generates a per-directory `index.html` whose
  `<a href="...">` entries match the `mod_autoindex` shape that
  `FindArtifactUtils` already parses (version directories end with
  `/`, files do not, excluded headers match the existing filter
  list). Sidecar `.sha256` files are emitted next to each artifact
  for integrity verification. HTTPS is provided by GitHub Pages.
  Signing (PGP) is deferred to a later ADR.
- Consequences: Updater code stays untouched. Publication is
  reproducible, version-controlled, and free of third-party
  dependencies. Until the action and the `habitv-repo` repository
  exist, no releases can be published; this is acceptable because
  no automated publication runs today either. ADR-0004 becomes
  `Accepted` once the workflow is validated end-to-end against a
  test artifact.
