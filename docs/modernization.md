# Modernization

Durable direction, current tooling inventory, and a phased roadmap. Actionable
work lives in GitHub issues and PRs; this page records the chosen order and the
reasoning behind each tooling decision. Sources consulted in November 2026 are
listed at the bottom.

## Current direction

1. Keep the existing Maven application working on the **current Java 8**
   baseline ([`development.md`](development.md)).
2. Restore providers incrementally with offline fixtures, then live checks.
3. Treat the console fat JAR as the supported baseline; JavaFX packaging
   later.
4. Finish yt-dlp as the YouTube-family tool plugin (binary publish, Windows
   diagnostics) without mixing unrelated provider rewrites.
5. Runtime move: **Java 21 + OpenJFX 21.0.7** — implemented in PR
   [#237](https://github.com/Mika3578/habitv/pull/237) (Draft): compiler
   baseline `release 21`, OpenJFX as explicit Maven dependencies, packaging
   modules back in the reactor. Required CI alignment, then merge.
   Diagnostic CI on 11/17/21/25 is not runtime support.

## Tooling already in place (do not duplicate)

| Concern | Status | Where |
|---------|--------|-------|
| Required Java 8 CI: validate, deterministic test subset, package | Present | [`ci-maven.yml`](../.github/workflows/ci-maven.yml) |
| Compatibility matrix 11/17/21/25, macOS + Windows validate | Present (diagnostic) | `ci-maven.yml`, `build.yml` |
| Maven dependency cache in CI | Present (`setup-java` `cache: maven` in the shared JDK action) | [`.github/actions/setup-build-jdk`](../.github/actions/setup-build-jdk/action.yml) |
| Static analysis | CodeQL + SonarCloud + Snyk | `codeql.yml`, SonarCloud, Snyk checks |
| Dependency review on PRs | `actions/dependency-review-action`, fails on high severity | `dependency-review.yml` |
| Dependency updates | Dependabot: Maven weekly (grouped minor/patch, Jakarta-namespace majors ignored) + grouped GitHub Actions | [`dependabot.yml`](../.github/dependabot.yml) |
| Conventional Commits + version bump consistency | CI validated | `validate-versions.yml` |
| Agent policy layout checks | `agent-policy`, `agent-policy (windows)` | `agent-policy.yml` |
| Branch protection | squash only, linear history, review-thread resolution, CodeQL and code-quality gates, no bypass actors | [`github-rulesets/`](github-rulesets/README.md) |
| Editor formatting | `.editorconfig`, `.vscode/java-formatter.xml` (Eclipse formatter) | repository config |
| Changelog | Keep a Changelog `CHANGELOG.md` with an `Unreleased` section | `CHANGELOG.md` |
| Stale triage | weekly `actions/stale`, 45 days for PRs | `stale.yml` |

Everything else below is a gap relative to current (2025–2026) practice for
legacy Java 8 Maven multi-module desktop applications.

## Phased roadmap

Order reflects risk and value: unblock review throughput first, then fix
correctness, then harden the supply chain, then add developer tooling, then
the platform move, then breadth, then shipping. One concern per task and one
module per change remain the standing rules.

### Phase a — PR backlog hygiene

Goal: a short, live backlog where every open PR is either moving, waiting on
an explicit blocker, or closable. Rules for this phase:

- Never close or force-push another contributor's branch or PR; never rename
  an open PR head (GitHub closes it).
- Merge only on the documented gate: required checks green on the current
  HEAD, threads resolved, mergeable, and squash.
- Prefer finishing small PRs over starting new ones.

Snapshot 2026-11-27 (28 open PRs). Recommendation per PR:

| PR | Topic | Recommendation |
|----|-------|----------------|
| [#284](https://github.com/Mika3578/habitv/pull/284) | grouped Maven minor/patch updates | Merged 2026-11-27 (squash `357fd91f`) |
| [#280](https://github.com/Mika3578/habitv/pull/280) | grouped Maven update (older, superseded by #284) | Leave open; Dependabot auto-closes superseded groups. If it lingers past the next Dependabot run, close manually |
| [#214–#223](https://github.com/Mika3578/habitv/pulls), [#228](https://github.com/Mika3578/habitv/pull/228) | provider drafts added 2026-09-20 | Triage one by one against the provider priority list (#261): finish with offline fixtures, or close with a pointer to a fresh issue. Do not batch-merge |
| [#224](https://github.com/Mika3578/habitv/pull/224), [#226](https://github.com/Mika3578/habitv/pull/226), [#227](https://github.com/Mika3578/habitv/pull/227), [#229](https://github.com/Mika3578/habitv/pull/229), [#230](https://github.com/Mika3578/habitv/pull/230), [#231](https://github.com/Mika3578/habitv/pull/231), [#233](https://github.com/Mika3578/habitv/pull/233) | provider diagnostic drafts | Rebase onto `develop`, convert to focused fixes mapped to the open provider issues (#254, #257, #258, #259, #168) under #175, or close when superseded |
| [#225](https://github.com/Mika3578/habitv/pull/225) | RTBF provider | Active; finish with fixtures, then live check |
| [#235](https://github.com/Mika3578/habitv/pull/235) | category-tree logos | Stale; finish or close |
| [#237](https://github.com/Mika3578/habitv/pull/237) | Java 21 migration | Keep; it is the phase (e) vehicle |
| [#253](https://github.com/Mika3578/habitv/pull/253) | parent SemVer enforcement | Stale; finish — it implements #252 |
| [#261](https://github.com/Mika3578/habitv/pull/261) | yt-dlp priority docs | Stale but small; finish |
| [#262](https://github.com/Mika3578/habitv/pull/262) | CI governance gate | Finish; pairs with phase (c)/(d) gates |
| [#263](https://github.com/Mika3578/habitv/pull/263) | sample channel ids | Stale; small finish-or-close |
| [#266](https://github.com/Mika3578/habitv/pull/266) | agent policy slimming | Not a draft; review and merge when green |
| [#267](https://github.com/Mika3578/habitv/pull/267) | shell hook hardening | Stale; finish (security-adjacent) |
| [#269](https://github.com/Mika3578/habitv/pull/269) | dark mode | Platform-created head; keep on that head, finish or close |
| [#271](https://github.com/Mika3578/habitv/pull/271) | review/merge policy split | Stale; finish |
| [#272](https://github.com/Mika3578/habitv/pull/272) | versioning workflow input quoting | Stale; finish (small CI correctness fix) |
| [#279](https://github.com/Mika3578/habitv/pull/279) | tray message escaping | Finish; small UI correctness fix |
| [#282](https://github.com/Mika3578/habitv/pull/282) | episode row actions while loading | Finish; small UI correctness fix |

### Phase b — core download correctness

Priority order (highest user impact first):

1. [#256](https://github.com/Mika3578/habitv/issues/256) — queued-download
   restart NPE. Fix shape per the issue: validate state before constructing
   `DownloadedDAO` in `EpisodeManager.restart`, and make the DAO defend its
   invariants with a controlled technical error instead of an NPE. Add
   deterministic offline tests for null, unresolved-placeholder, and valid
   destination paths. Any runtime change stays in Draft until a real
   application run is confirmed. Implementation in flight: PR
   [#286](https://github.com/Mika3578/habitv/pull/286).
2. [#167](https://github.com/Mika3578/habitv/issues/167) — pass the
   configured tool location to the external tool argument list
   (`--ffmpeg-location` equivalent). Small, testable, unblocks quality
   settings.
3. [#160](https://github.com/Mika3578/habitv/issues/160) — single-file mux
   for split video/audio downloads. Larger; design first against yt-dlp
   output templates, then implement with offline fixtures.
4. Verify-and-close sweep: confirm [#245](https://github.com/Mika3578/habitv/issues/245)
   (progress bar width) against merged PR
   [#278](https://github.com/Mika3578/habitv/pull/278) and close with
   evidence once confirmed.

### Phase c — update and execution chain security

1. [#169](https://github.com/Mika3578/habitv/issues/169) — launch external
   tools with argument lists instead of a shell command string. Prerequisite
   for #170 because argument lists remove most injection surface.
2. [#170](https://github.com/Mika3578/habitv/issues/170) — sanitize tool
   command strings in DEBUG and preflight logs.
3. [#166](https://github.com/Mika3578/habitv/issues/166) — verify update
   artifacts against the update manifest. Staged approach, current practice
   for desktop updaters:
   - Step 1: publish SHA-256 for every artifact in the update manifest and
     verify before install (fail closed, no checksum means reject).
   - Step 2: sign the manifest with an Ed25519/minisign key; ship the public
     key in the application; serve the signed bytes unchanged; verify the
     signature before parsing; keep download URLs out of the signed payload.
   - Step 3: anti-downgrade — signed version binding and a monotonic
     installed-version floor. Keep the private key in a controlled signing
     place, never in CI or the repository.
   - Practical tool: `minisign` (small, audited Ed25519) rather than cosign,
     which targets container images.
4. SBOM + open vulnerability scanning: generate a CycloneDX aggregate SBOM
   with `cyclonedx-maven-plugin:makeAggregateBom` at release and on a weekly
   schedule, then scan it with Google `osv-scanner` and upload SARIF to code
   scanning. Keep Snyk as the commercial layer; OWASP dependency-check is
   only worth its NVD-download cost if NVD-based reporting is explicitly
   required.
5. [OSSF Scorecard](https://github.com/ossf/scorecard-action) workflow:
   pinned by commit SHA, `cache-mode: none`, `publish_results: true`
   (requires `id-token: write`), weekly schedule. Improves the public
   supply-chain posture signal and feeds the Scorecard badge.
6. Land the CI governance gate (#262) so these checks become part of the
   merge baseline instead of best effort.

### Phase d — build tooling and quality gates

Adopt in this order; each item is a dedicated PR, and each stays diagnostic
first (non-required check) until it is green for a full cycle:

1. **Maven wrapper** — `mvn wrapper:wrapper` on Maven 3.9.x; commit
   `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`. Removes
   local-Maven-version drift for contributors and CI.
2. **Enforcer** — `maven-enforcer-plugin` in the parent POM:
   `requireJavaVersion`, `requireMavenVersion`,
   `reactorModuleConvergence`, `banDuplicatePomDependencyVersions`,
   `requireUpperBoundDeps` (start as warn, promote after triage).
3. **Reproducible builds** — set `project.build.outputTimestamp` in the
   parent POM; run `mvn artifact:check-buildplan` once to find non-reproducible
   plugin versions, upgrade them; verify with
   `mvn clean verify artifact:compare` in a scheduled job. This directly
   supports phase (g): identical artifacts make published checksums
   trustworthy.
4. **Formatter gate (Spotless)** — wire the existing Eclipse formatter
   configuration (`.vscode/java-formatter.xml`) into
   `spotless-maven-plugin` so IDE and build agree. Use `ratchetFrom` so only
   changed files are enforced and history stays blameable. Caveat: recent
   Spotless requires the Maven JVM to be 17+; the required Java 8 jobs must
   not run it. Run it in a dedicated CI job on JDK 21 and via a local git
   hook; make it required only when the build JVM separates from the runtime
   baseline.
5. **Static analysis tiering** — the 2026 consensus is that the tools
   overlap little and tier well:
   - Checkstyle: style enforcement, cheap, run on PRs.
   - Error Prone: runs inside compilation; requires a JDK 11+ (17+
     recommended) compiler JVM — enable in the JDK 17/21 diagnostic jobs
     first, promote after the Java 21 move.
   - SpotBugs (+ FindSecBugs): bytecode-level bugs; verify phase on PRs,
     max effort nightly; gate on new findings only, keep a baseline.
   - PMD (with CPD): source-level smells and duplication; PR job.
   SonarCloud already provides a quality-gate view; these plugins add
   build-local gates Sonar cannot enforce per-diff.
6. **JaCoCo** — `prepare-agent` + `report` in the parent POM; per-module
   BUNDLE rule (start with measurement only, then set floors per module);
   aggregate report via a small aggregator module or `report-aggregate` with
   SonarCloud as the cross-module view (JaCoCo's `check` goal is
   single-module by design; cross-module thresholds need merged `.exec`
   files).
7. **JUnit 5** — import `junit-bom`, add `junit-jupiter` +
   `junit-vintage-engine` (transitional), write all new tests on Jupiter,
   convert module by module with the OpenRewrite `JUnit4to5Migration`
   recipe, then remove Vintage and `junit:junit`. Surefire is already 3.x.
8. **Dependency updates** — keep Dependabot (config is already curated and
   the Jakarta-major ignore is correct). Renovate would add dashboards and
   automerge, but introduces a second bot and config surface; revisit only
   if update volume becomes a burden. Do not run both.
9. **Pre-commit hooks** — a `pre-commit` config running `spotless:apply`
   scope, whitespace, and large-file checks for human contributors. Agent
   hooks already exist under `.cursor/hooks/`.

### Phase e — Java 21 runtime migration

Vehicle: PR [#237](https://github.com/Mika3578/habitv/pull/237). Sequence:

1. Land phase (b)/(c) correctness fixes on the Java 8 baseline first; do not
   mix runtime migration into bugfix PRs.
2. Mechanical migration with OpenRewrite:
   `org.openrewrite.java.migrate.UpgradeToJava21` (composite — includes the
   17 step), `mvn rewrite:dryRun`, review the diff, then `mvn rewrite:run`.
   Note: `rewrite-migrate-java` is source-available; binaries come from the
   Moderne/Code Genome repository — acceptable for this project, record the
   provenance.
3. Pre-migration audit: `mvn dependency:tree`, `jdeps --jdk-internals
   --multi-release 21` to find JDK-internal API usage; prefer fixing
   libraries over sprinkling `--add-opens`.
4. Known blockers, each a dedicated single-concern task per policy:
   JAXB `javax.xml.bind` → `jakarta.xml.bind` (Dependabot already blocks
   accidental majors); JavaFX 2.x `jfxrt` → OpenJFX 21 explicit dependencies
   (#237 already drafts this); out-of-reactor `habiTv-linux` /
   `habiTv-windows` packagers (#173).
5. CI alignment: after #237 lands, flip required checks from `java8` to
   `java21` in `ci-maven.yml` and the rulesets, and retire the diagnostic
   status of the compatibility jobs for 17/21.
6. Java 25 is the next step after 21 soaks; OpenRewrite provides
   `UpgradeToJava25`.

### Phase f — provider restoration

Umbrella: [#175](https://github.com/Mika3578/habitv/issues/175) (graceful
degradation of obsolete listing providers). Per-provider work, one module
per change, offline fixtures first:

- [#254](https://github.com/Mika3578/habitv/issues/254) TF1+ (in flight: PR
  [#265](https://github.com/Mika3578/habitv/pull/265))
- [#255](https://github.com/Mika3578/habitv/issues/255) Arte
- [#259](https://github.com/Mika3578/habitv/issues/259) Clubic (draft #231)
- [#258](https://github.com/Mika3578/habitv/issues/258) SFR (draft #229)
- [#257](https://github.com/Mika3578/habitv/issues/257) L'Équipe (draft #230)
- [#168](https://github.com/Mika3578/habitv/issues/168) MLS Soccer (draft #226)
- [#225](https://github.com/Mika3578/habitv/pull/225) RTBF Auvio
- Remaining 2026-09 provider drafts (#214–#223, #228) as capacity allows,
  ordered by the yt-dlp/provider priority guide (#261).

### Phase g — release automation and update-site republish

1. **Release automation** — adopt
   [release-please](https://github.com/googleapis/release-please-action)
   with `release-type: maven`: it parses Conventional Commits (already
   CI-enforced), maintains `CHANGELOG.md`, opens a Release PR, bumps all
   `pom.xml` versions recursively, tags, and creates the GitHub Release —
   closing the gap since 4.1.0 (2017). Configure `target-branch: develop`.
   Caveat: a PR created with the default `GITHUB_TOKEN` does not trigger
   other workflows; use a PAT secret so the Release PR runs required checks.
   The existing `validate-versions.yml` bump-consistency job must be aligned
   with (or replaced by) release-please's version PRs.
2. **Packaging** — after the runtime move: `jpackage` native installers per
   OS (Windows MSI, macOS DMG 11+, Linux DEB/RPM on GTK3) built on their own
   runners, `jlink` runtime image with platform JavaFX jmods, Docker image
   for the console fat JAR. Supersedes the JavaFX 2.x self-contained
   packaging (#173).
3. **Artifacts** — attach SHA-256 checksums (and later the signed manifest
   from phase (c)) and the CycloneDX SBOM to each GitHub Release.
4. **Update-site republish** (`Mika3578/habitv-repo`, GitHub Pages; last
   published 2026-05-30). Maintainer-only; this repository cannot be pushed
   to from CI. Needed:
   - Build the current plugin set and publish the full repository with the
     `static-repo-deploy` / `static-repo-publish` profiles into a local
     `habitv-repo` checkout (`scripts/static-repo/`; PowerShell wrappers use
     `HABITV_REPO_DIR`).
   - Regenerate the update manifest with
     `publish-repository-extras.ps1` so every active plugin appears — the
     published manifest (2026-05-19) is missing later plugin additions
     (for example `novo19`) and pins `youtube`, `arte`, `francetv` at older
     versions than `develop` now has.
   - Include per-artifact SHA-256 (phase (c) step 1) in the regenerated
     manifest format.
   - Commit and push to `habitv-repo` `main`; Pages picks it up.
   - Afterwards, re-verify a real client update check against the HTTPS
     update base.

## Sequencing notes

- Phases (a) and (b) can proceed in parallel; (c) items 1–2 are small and
  unblock #166; (d) items 1–3 are safe groundwork independent of the
  runtime move.
- Do not start (e) before (b) is largely done: bug fixes land on the Java 8
  baseline and must not chase a moving compiler.
- Everything in (c) and (d) is additive tooling — each piece lands as its
  own diagnostic CI job before any of it becomes required.

## Sources

- [OpenRewrite — Migrate to Java 21](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-java-21) and [rewrite-migrate-java](https://github.com/openrewrite/rewrite-migrate-java) (composite LTS recipes, dry-run workflow, source-available licensing)
- [Java 8 to 21 migration guide](https://katyella.com/blog/java-8-to-21-migration-guide/) (dependency audit, jdeps, illegal reflective access, add-opens discipline)
- [Maven — Configuring for Reproducible Builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) (`project.build.outputTimestamp`, `artifact:check-buildplan`, `artifact:compare`)
- [Maven Enforcer built-in rules](https://maven.apache.org/enforcer/enforcer-rules/) and [reactorModuleConvergence](https://maven.apache.org/enforcer/enforcer-rules/reactorModuleConvergence.html)
- [Maven Wrapper](https://maven.apache.org/tools/wrapper/)
- [Spotless Maven plugin](https://github.com/diffplug/spotless/blob/main/plugin-maven/README.md) (JVM 17+ requirement, `ratchetFrom`, step ordering)
- [Java static analysis comparison 2026](https://www.pistack.xyz/posts/2026-09-07-java-static-analysis-checkstyle-spotbugs-pmd-comparison/) (Checkstyle/PMD/SpotBugs tiering) and [static analysis integration guide](https://www.devarmor.com/blog/static-code-analysis-java) (Error Prone placement, new-findings-only gating)
- [JaCoCo report-aggregate](https://www.jacoco.org/jacoco/trunk/doc/report-aggregate-mojo.html) and [no aggregate-check limitation](https://github.com/jacoco/jacoco/issues/902)
- [OpenRewrite — JUnit 4 to Jupiter](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-from-junit-4-to-junit-5) and [JUnit 5 migration guide](https://howtodoinjava.com/junit/junit-5-vs-junit-4/) (junit-bom + Vintage transition)
- [Renovate bot comparison](https://docs.renovatebot.com/bot-comparison/) and [automerge guidance](https://docs.renovatebot.com/key-concepts/automerge/)
- [actions/setup-java caching](https://github.com/actions/setup-java/blob/main/README.md) and [GitHub Docs — Java with Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [release-please action](https://github.com/googleapis/release-please-action/) and [Maven strategy](https://github.com/googleapis/release-please/blob/HEAD/docs/java.md) (recursively bumping `pom.xml`, SNAPSHOT release PRs, PAT caveat)
- [CycloneDX Maven plugin via Eclipse security handbook](https://csi.eclipse.dev/security-handbook/sbom/howto.html), [OWASP DevSecOps SBOM guidance](https://owasp.github.io/DevSecOpsGuideline/2-Process/2-3-Build/2-3-6-Supply-Chain-Security/2-3-6-1-SBOM/), [osv-scanner SARIF workflow](https://github.com/octaviospain/lirp/blob/6fe6762d/.github/workflows/osv-scanner.yml)
- [OWASP dependency-check Maven](https://dependency-check.github.io/DependencyCheck/dependency-check-maven/) (NVD mirror caveat)
- [ossf/scorecard-action](https://github.com/ossf/scorecard-action/) (SHA pinning, `cache-mode: none`, `id-token: write`)
- [Signed desktop update manifests](https://omnihorn.com/blog/sign-desktop-app-updates) and [update trust chain](https://www.deputyos.com/docs/security/update-trust-chain/) (Ed25519/minisign, SHA-256 manifest, version binding, anti-downgrade)
