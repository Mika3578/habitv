# 🚀 Habitv Development Tracker

> 📌 Single source of truth for the modernization restart.
> Mirrored 1:1 in [`dev-tracker.json`](dev-tracker.json) — update both
> in the same commit. Detailed phase order lives in
> [`dev-plan.md`](dev-plan.md); architectural decisions in
> [`decision-log.md`](decision-log.md); risks in
> [`risk-register.md`](risk-register.md).

> 📝 **Identifier convention** — every work item uses a descriptive
> kebab-case slug. The opaque legacy codes (`HBTV-XXX`) are preserved
> on each entry for compatibility with existing PRs and commits. See
> the `descriptive-slug-ids` ADR for the rationale and full mapping.

**Last refresh:** 2026-05-18 · **Active branch:** `develop`

---

## 📊 Overall progress

```
████████████████░░░░░░░░  62%
```

| Category | Count |
|---------|------:|
| ✅ Delivered | **8** |
| 🟡 In progress | **0** |
| 🔵 Proposed | **5** |
| ⬜ Deferred | **0** |
| ⛔ Blocked | **0** |
| **Total work items** | **13** |

---

## 🗂️ At-a-glance summary

| Item | Status | Priority | Progress |
|---|:--:|:--:|---|
| 🏗️ `gov-bootstrap` — Governance bootstrap from master | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| ⚙️ `maven-reactor` — Maven reactor stabilization | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| ☕ `java8-baseline` — Java 8 compile baseline | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| 🛡️ `branch-protection` — GitHub branch protection rules | 🔵 Proposed | 🟠 P1 | `░░░░░░░░░░░░░░░░░░░░` 0% |
| 🔗 `legacy-url-migration` — Legacy URL migration (free.fr / SVN / FTP) | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| 📦 `static-repo-publish` — Static artifact repository publication | 🔵 Proposed | 🟠 P1 | `██░░░░░░░░░░░░░░░░░░` 10% |
| 🔌 `provider-inventory` — Provider plugin inventory & cleanup | 🔵 Proposed | 🟡 P2 | `░░░░░░░░░░░░░░░░░░░░` 0% |
| 🎬 `ytdlp-migration` — `youtube-dl` → `yt-dlp` migration | 🔵 Proposed | 🟡 P2 | `████░░░░░░░░░░░░░░░░` 20% |
| 🖼️ `javafx-modernization` — JavaFX & runtime packaging modernization | 🔵 Proposed | 🟡 P2 | `█░░░░░░░░░░░░░░░░░░░` 5% |
| 🧪 `plugin-tester-align` — `plugin-tester` reactor alignment | ✅ Done | 🟠 P1 | `████████████████████` 100% |
| ▶️ `console-runnable` — Runnable console baseline | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| 🔗 `own-version-deps-align` — Own-version plugin dependency alignment | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| 🔑 `youtube-apikey` — YouTube Data API key externalization | ✅ Done | 🟠 P1 | `████████████████████` 100% |

---

## 🏷️ Legend

| Symbol | Meaning |
|:---:|---|
| ✅ | **Done** — acceptance criteria met and merged on `develop` |
| 🟡 | **In progress** — PR exists or work actively underway |
| 🔵 | **Proposed** — accepted scope, not started |
| ⬜ | **Deferred** — accepted but postponed |
| ⛔ | **Blocked** — waiting on external dependency |
| 🔴 | **P0** critical · 🟠 **P1** high · 🟡 **P2** normal · 🟢 **P3** low |

---

# 🔍 Detailed work items

## 🏗️ `gov-bootstrap` — Governance bootstrap from master

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-000 |

**Scope** — Establish branching model, CI validate baseline, AI agent
guidance, and the documentation skeleton.

**Acceptance criteria**
- ✅ `.github/pull_request_template.md`, issue templates, and
  `.github/workflows/build.yml` present and valid
- ✅ `AGENTS.md`, `.cursor/rules/habitv-master.mdc`, and
  `.github/copilot-instructions.md` present
- ✅ Tracker / risk / decision / plan / settings / audit docs present
- ✅ Restart PR merged to `master`, integration branch `develop` created

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS
git log --oneline -5                 # bootstrap commits present
```

**Related PR** · `chore: bootstrap restart workflow from master` (#21)

**Notes** — Documentation/workflow only; no runtime or provider changes.
GitHub UI-level settings application moved to its own item
(`branch-protection`).

---

## ⚙️ `maven-reactor` — Maven reactor stabilization

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-001 |

**Scope** — Wire the multi-module reactor so `mvn validate` walks the
full project from the root and parent resolution is deterministic
across `fwk`, `application`, and `plugins`. Topology only.

**Acceptance criteria**
- ✅ Root `pom.xml` aggregates `fwk`, `application`, `plugins`
- ✅ `fwk/pom.xml` aggregates `api`, `framework`
- ✅ `application/pom.xml` keeps `core`, `consoleView`, `trayView`, `habiTv`
- ✅ `plugins/pom.xml` keeps 22 plugin modules + `plugin-tester` (added in `plugin-tester-align`)
- ✅ All child POMs parent at `4.1.0-SNAPSHOT` with explicit `<relativePath>`
- ✅ `fwk/framework` own-version typo `4.1.0-SNASPHOT` → `4.1.0-SNAPSHOT` fixed

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS, 33 reactor entries
```

**Related PR** · `build: stabilize Maven reactor from master` (#22)

**Notes** — `habiTv-linux` / `habiTv-windows` stay out of reactor
(JavaFX 2.x + hardcoded `${jdk.home}`), tracked under
`javafx-modernization`.

---

## ☕ `java8-baseline` — Java 8 compile baseline

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-002 |

**Scope** — Make `mvn compile` succeed on Temurin 8 from the root,
without resolving artifacts from the blocked legacy HTTP repository.

**Acceptance criteria**
- ✅ Intra-reactor `[4.1,4.2)` ranges replaced with `${project.version}`
- ✅ `maven-jaxb-plugin` pinned (1.1.1) in `application/core`
- ✅ `mvn compile` BUILD SUCCESS on Ubuntu and Windows with Temurin 8
- ✅ Network/provider tests excluded from default lifecycle
- ✅ CI workflow triggers on both `master` and `develop`

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS
mvn -B -ntp -DskipTests compile      # BUILD SUCCESS (after plugin-tester-align + own-version-deps-align)
```

**Related PR** · `build: stabilize Java 8 compile baseline` (#23)

**Notes** — Original blocker chain: `plugin-tester:4.1.0`
(`plugin-tester-align`) → `framework/api:4.1.1-SNAPSHOT`
(`own-version-deps-align`). Both cleared. Risks
`reactor-version-range`, `jaxb-plugin-unpinned`,
`plugin-tester-mismatch` are mitigated.

---

## 🛡️ `branch-protection` — GitHub branch protection rules

| | |
|---|---|
| **Status** | 🔵 Proposed |
| **Priority** | 🟠 P1 |
| **Progress** | `░░░░░░░░░░░░░░░░░░░░` 0% |
| **Legacy code** | HBTV-003 |

**Scope** — Apply the GitHub settings documented in
[`github-repository-settings.md`](github-repository-settings.md):
branch model, protection, merge strategy, required checks.

**Acceptance criteria**
- ⬜ `master` protected; linear history required
- ⬜ `develop` protected; required CI: `build` workflow on Ubuntu + Windows
- ⬜ Squash merge enabled, merge commits disabled
- ⬜ Force-push disabled on protected branches

**Validation** — Manual confirmation in the GitHub UI by the repo owner.

**Notes** — Owner-only task; no code change. Currently blocking
nothing technical but everyone has push access to `develop`.

---

## 🔗 `legacy-url-migration` — Legacy URL migration (free.fr / SVN / FTP)

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-004 |

**Scope** — Remove active legacy DabiBoo/free.fr/SVN/Assembla wiring from
Maven POMs and runtime paths; replace SCM metadata with GitHub; disable
startup telemetry and plugin update checks by default; point Maven
`<repository>` at the public `habitv-repo` GitHub Pages base.

**Acceptance criteria**
- ✅ Active POM/runtime references to `dabiboo.free.fr`,
  `subversion.assembla.com`, `scm:svn`, and `cpt.php` are removed
- ✅ Root and packaging POM repository wiring uses
  `https://mika3578.github.io/habitv-repo/repository`
- ✅ Runtime telemetry ping is opt-in only (`habitv.stat.enabled=true`)
  and requires explicit URL configuration (`habitv.stat.url`)
- ✅ Runtime plugin updates are opt-in only (`habitv.update.enabled=true`)
  with optional `habitv.update.url`

**Validation**
```bash
git grep -nIE "dabiboo|free\.fr|ftpperso|subversion\.assembla|scm:svn" -- .
# -> matches only documentation/history, no active code/POM endpoints
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS (33 modules)
mvn -B -ntp -DskipTests compile      # BUILD SUCCESS (33 modules)
```

**Related PRs** · #25 (plan, merged) · #36 (execution, merged)

**Notes** — Execution supersedes the documentation-only plan in PR #25.
Functional publication cutover remains blocked under `static-repo-publish`
until `habitv-repo` serves `/repository` with Apache-style directory
listings. Do not enable `habitv.update.enabled` until static `index.html`
files or an equivalent manifest layout are verified.

---

## 📦 `static-repo-publish` — Static artifact repository publication

| | |
|---|---|
| **Status** | 🔵 Proposed |
| **Priority** | 🟠 P1 |
| **Progress** | `██░░░░░░░░░░░░░░░░░░` 10% |
| **Legacy code** | HBTV-005 |

**Scope** — Stand up the `habitv-repo` static repository (GitHub Pages
or equivalent HTTPS host) compatible with the existing
`FindArtifactUtils` / `UpdateManager` semantics.

**Acceptance criteria**
- 🟡 Cross-OS deploy scripts present in `scripts/static-repo/` *(in develop)*
- ⬜ `habitv-repo` PR [#1](https://github.com/Mika3578/habitv-repo/pull/1) merged
- ⬜ `repository/com/dabi/habitv/` layout published with real artifacts
- ⬜ `index.html` generation for GitHub Pages (no autoindex by default)
- ⬜ `plugins.txt` format validated against `FindArtifactUtils`
- ⬜ HTTPS + checksum strategy documented
- ⬜ Migration path for `UPDATE_URL` documented

**Validation**
```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -DskipTests deploy -DaltDeploymentRepository=local::default::file:///tmp/repo
```

**Related PR** · habitv-repo #1 (draft, since 2026-04-25)

**Notes** — Pairs with `legacy-url-migration` phase 5. Risks
`pages-layout-mismatch`, `pages-autoindex-gap`.

---

## 🔌 `provider-inventory` — Provider plugin inventory & cleanup

| | |
|---|---|
| **Status** | 🔵 Proposed |
| **Priority** | 🟡 P2 |
| **Progress** | `░░░░░░░░░░░░░░░░░░░░` 0% |
| **Legacy code** | HBTV-006 |

**Scope** — Inventory every plugin in `plugins/` (22 in the aggregator
+ `plugin-tester`); record current status (working, obsolete endpoint,
renamed, broken parser). No code removal in this item.

**Acceptance criteria**
- ⬜ Inventory table per plugin with last-known status
- ⬜ For each obsolete/renamed plugin, a recommended dedicated
  removal/rename PR is named
- ⬜ Offline fixtures captured where feasible

**Validation** — Inventory reviewed in a doc-only PR; no behavior change.

**Notes** — Visible candidates: `pluzz` (already renamed `francetv` on
`habitv-repo`), `canalPlus`, `beinsport`, `D8`/`D17`/`nrj12` (README
mentions but no module exists), `wat`, `sfr`, `clubic`, `kewego`-derived
RSS samples. Risks `live-tests-flaky`, `provider-endpoints-dead`.

---

## 🎬 `ytdlp-migration` — `youtube-dl` → `yt-dlp` migration

| | |
|---|---|
| **Status** | 🔵 Proposed |
| **Priority** | 🟡 P2 |
| **Progress** | `████░░░░░░░░░░░░░░░░` 20% |
| **Legacy code** | HBTV-007 |

**Scope** — Plan and execute migration of the `youtube` plugin's binary
contract from `youtube-dl` to `yt-dlp` (executable name, command flags,
output parsing, post-processors).

**Acceptance criteria**
- 🟡 Runtime path packaged in `consoleView` fat-jar *(done)*
- 🟡 Offline command-wiring test `YoutubePluginDownloaderCmdTest` passes *(done)*
- ⬜ CLI flag diff documented (`--format`, output template, post-processors)
- ⬜ `application/core/configuration.xml` sample updated to `yt-dlp`
- ⬜ Backward-compatibility / deprecation note for users on `youtube-dl`
- ⬜ Provider behavior validated against captured fixtures

**Validation**
```bash
mvn -B -ntp -pl plugins/youtube -am test     # Tests run: 2, Failures: 0
```

**Notes** — Risk `ytdlp-behavior-diff`. End-to-end live behavior
remains out of scope until `provider-inventory` cleanup.

---

## 🖼️ `javafx-modernization` — JavaFX & runtime packaging modernization

| | |
|---|---|
| **Status** | 🔵 Proposed |
| **Priority** | 🟡 P2 |
| **Progress** | `█░░░░░░░░░░░░░░░░░░░` 5% |
| **Legacy code** | HBTV-008 |

**Scope** — Migrate JavaFX 2.x usage and `${jdk.home}` packaging
assumptions:
- `application/trayView` — JavaFX 2.x imports
- `application/habiTv-linux` and `habiTv-windows` — out of reactor
- `zenjava/javafx-maven-plugin 2.0` — unmaintained
- `system`-scope `javafx:jfxrt` referencing `${jdk.home}/jre/lib/ext/jfxrt.jar`

**Acceptance criteria**
- 🟡 Surface inventory captured in audit doc *(done in audit-master-baseline)*
- ⬜ OpenJFX migration options compared (jpackage, jlink, fat-jar)
- ⬜ Packaging blueprint accepted via dedicated ADR
- ⬜ `habiTv-linux` + `habiTv-windows` re-enterable to the reactor

**Validation** — Audit reviewed in PR; no code changes in this item.

**Notes** — Risk `javafx-jdk8`. Largest single piece of remaining
work once `legacy-url-migration` + `static-repo-publish` +
`provider-inventory` are clear.

---

## 🧪 `plugin-tester-align` — `plugin-tester` reactor alignment

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🟠 P1 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-010 |

**Scope** — Align plugin module test-harness dependencies so
`com.dabi.habitv:plugin-tester` resolves from the local reactor instead
of the blocked legacy HTTP repository; include `plugins/plugin-tester`
in the aggregator.

**Acceptance criteria**
- ✅ Plugin modules using `${project.version}` or `${project.parent.version}`
- ✅ `plugins/pom.xml` aggregates `plugin-tester`
- ✅ Compile passes the former `6play` / `plugin-tester:4.1.0` blocker

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS (33 modules)
mvn -B -ntp -DskipTests compile      # past the previous blocker
```

**Related PR** · `build: align plugin tester reactor dependency` (#24)

**Notes** — Risk `plugin-tester-mismatch` mitigated. Residual
`framework/api` blocker moved to `own-version-deps-align` (and
resolved there).

---

## ▶️ `console-runnable` — Runnable console baseline

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-011 |

**Scope** — Establish a factual runnable baseline on `develop`:
`consoleView` packages a runnable fat-jar, the YouTube command wiring
is unit-tested offline, and tracker/audit docs reflect exact outcomes.

**Acceptance criteria**
- ✅ `application/consoleView` packages a runnable fat JAR in scoped builds
- ✅ Offline `YoutubePluginDownloaderCmdTest` passes (2/2)
- ✅ Tracker/audit reflect exact validation results

**Validation**
```bash
mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' validate    # BUILD SUCCESS
mvn -B -ntp -pl plugins/youtube -am test                                            # 2/2 pass
```

**Related PR** · `feat(console): restore runnable baseline with yt-dlp provider` (#28)

**Notes** — Superseded PR #27 with a scoped subset.
`javafx-modernization`, tray/GUI packaging, and scraper rewrites
remain out of scope.

---

## 🔗 `own-version-deps-align` — Own-version plugin dependency alignment

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🔴 P0 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-012 |

**Scope** — Fix own-version plugin module dependency resolution so
shared internal reactor dependencies (`com.dabi.habitv:api`,
`com.dabi.habitv:framework`) resolve to the parent/reactor version
instead of plugin-local artifact versions.

**Acceptance criteria**
- ✅ Root `dependencyManagement` does not force non-reactor coordinates
- ✅ `mvn validate` and `mvn compile` succeed from the root
- ✅ `maven-default-http-blocker` no longer triggered for
  `framework/api:4.1.1-SNAPSHOT`

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS (33 modules)
mvn -B -ntp -DskipTests compile      # BUILD SUCCESS (33 modules)
```

**Related PR** · `build: align own-version plugin internal dependencies` (#33)

**Notes** — Own-version plugin modules (`beinsport`, `footyroom`,
`pluzz`, `ffmpeg`) now use `${project.parent.version}` for shared
internal dependencyManagement coordinates. Risk `legacy-maven-repo`
mitigated for local reactor compilation (external publication still
pending `legacy-url-migration`).

---

## 🔑 `youtube-apikey` — YouTube Data API key externalization

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🟠 P1 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-013 |

**Scope** — Remove the hardcoded YouTube Data API key from
`plugins/youtube` and resolve it from runtime configuration so revoked
or restricted keys do not require a code change.

**Acceptance criteria**
- ✅ `YoutubeConf` no longer embeds a concrete API key value
- ✅ `YoutubePluginManager` resolves the key from runtime configuration
- ✅ Tray configuration exposes a user-editable field persisted in
  user config
- ✅ Playlist API request URL only includes supported parameters
- ✅ Error messages include sanitized request context (no key leak)
- ✅ PR merged onto `develop`

**Validation**
```bash
mvn -B -ntp -DskipTests -pl plugins/youtube -am validate                       # BUILD SUCCESS
mvn -B -ntp -DskipTests -pl application/trayView,plugins/youtube -am compile   # BUILD SUCCESS
mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubeConfTest -Dsurefire.failIfNoSpecifiedTests=false test  # BUILD SUCCESS
```

**Related PR** · `fix(youtube): externalize data api key and mask api errors` (#29)

**Notes** — Risk `youtube-key-hardcoded` mitigated. End users can set
the key from the tray configuration tab. Runtime key lookup order:
Java property `habitv.youtube.apiKey`, then environment variable
`HABITV_YOUTUBE_API_KEY`.

> **Audit trail note** — PR #29 (`fix(youtube): externalize data api key and mask api errors`,
> <https://github.com/Mika3578/habitv/pull/29>) references `HBTV-012` under
> the legacy numbering scheme because the tracker at the time of
> authorship had not yet assigned HBTV-013 to this work. This refresh
> assigns `HBTV-013` / slug `youtube-apikey` to the YouTube API key
> work and `HBTV-012` / slug `own-version-deps-align` to the dependency
> alignment work (PR #33). The slug-based IDs introduced by the
> `descriptive-slug-ids` ADR make such numbering collisions impossible
> going forward.

---


## 🧩 `jaxb-launcher-recovery` — JAXB generated sources & launcher classpath recovery

| | |
|---|---|
| **Status** | ✅ Done |
| **Priority** | 🟠 P1 |
| **Progress** | `████████████████████` 100% |
| **Legacy code** | HBTV-014 |

**Scope** — Restore Maven/IDE visibility for JAXB-generated packages in
`application/core` and fix `HabitvLauncher` classpath setup without
broad dependency remediation or provider modernization.

**Acceptance criteria**
- ✅ JAXB outputs land under `target/generated-sources/jaxb` and are
  registered as compile source roots for `config`, `configuration`, and
  `grabconfig`
- ✅ `HabitvLauncher` compiles on Java 8 without
  `Utils4J.addToClasspath`
- ✅ `application/core/generated` remains gitignored for legacy local
  trees while fresh builds use `target/generated-sources/jaxb`

**Validation**
```bash
mvn -B -ntp -DskipTests generate-sources   # BUILD SUCCESS
mvn -B -ntp -DskipTests validate           # BUILD SUCCESS
mvn -B -ntp -DskipTests clean compile      # BUILD SUCCESS (33 modules)
git diff --check                           # clean on committed files
```

**Related PR** · `fix: restore generated sources and launcher classpath` (#44)

**Notes** — `application/habiTv` now uses a small Java 8-compatible
local `URLClassLoader` helper instead of the unavailable
`Utils4J.addToClasspath` API. Incompatible `utils4j` Java 17 bytecode
and broader transitive dependency vulnerability cleanup remain out of
scope for a dedicated security PR.

---

# 📈 What ships next

Recommended merge / start order (see `dev-plan.md` for phase reasoning):

1. 🔵 **Apply branch protection** → close `branch-protection`
2. 🔵 **Merge `habitv-repo` PR #1** → start `static-repo-publish`
3. 🔵 Then in any order: `provider-inventory`, `ytdlp-migration`, `javafx-modernization`

---

# 🗂️ Legacy code index

For incoming references in PR descriptions, commits, and external
issue trackers:

| Legacy code | Slug |
|---|---|
| HBTV-000 | `gov-bootstrap` |
| HBTV-001 | `maven-reactor` |
| HBTV-002 | `java8-baseline` |
| HBTV-003 | `branch-protection` |
| HBTV-004 | `legacy-url-migration` |
| HBTV-005 | `static-repo-publish` |
| HBTV-006 | `provider-inventory` |
| HBTV-007 | `ytdlp-migration` |
| HBTV-008 | `javafx-modernization` |
| HBTV-010 | `plugin-tester-align` |
| HBTV-011 | `console-runnable` |
| HBTV-012 | `own-version-deps-align` |
| HBTV-013 | `youtube-apikey` |
| HBTV-014 | `jaxb-launcher-recovery` |
