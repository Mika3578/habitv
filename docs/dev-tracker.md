# 🚀 Habitv Development Tracker

> 📌 Single source of truth for the modernization restart.
> Mirrored 1:1 in [`dev-tracker.json`](dev-tracker.json) — update both
> in the same commit. Detailed phase order lives in
> [`dev-plan.md`](dev-plan.md); architectural decisions in
> [`decision-log.md`](decision-log.md); risks in
> [`risk-register.md`](risk-register.md).

**Last refresh:** 2026-05-18 · **Active branch:** `develop`

---

## 📊 Overall progress

```
████████████░░░░░░░░░░░░  50%
```

| Category | Count |
|---------|------:|
| ✅ Delivered | **6** |
| 🟡 In progress | **2** |
| 🔵 Proposed | **5** |
| ⬜ Deferred | **0** |
| ⛔ Blocked | **0** |
| **Total work items** | **13** |

---

## 🗂️ At-a-glance summary

| ID | Title | Status | Priority | Progress |
|---|---|:--:|:--:|---|
| `HBTV-000` | 🏗️ Governance bootstrap from master | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| `HBTV-001` | ⚙️ Maven reactor stabilization | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| `HBTV-002` | ☕ Java 8 compile baseline | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| `HBTV-003` | 🛡️ GitHub branch protection rules | 🔵 Proposed | 🟠 P1 | `░░░░░░░░░░░░░░░░░░░░` 0% |
| `HBTV-004` | 🔗 Legacy URL migration (free.fr / SVN / FTP) | 🟡 In progress | 🟠 P1 | `███████░░░░░░░░░░░░░` 35% |
| `HBTV-005` | 📦 Static artifact repository publication | 🔵 Proposed | 🟠 P1 | `██░░░░░░░░░░░░░░░░░░` 10% |
| `HBTV-006` | 🔌 Provider plugin inventory & cleanup | 🔵 Proposed | 🟡 P2 | `░░░░░░░░░░░░░░░░░░░░` 0% |
| `HBTV-007` | 🎬 `youtube-dl` → `yt-dlp` migration | 🔵 Proposed | 🟡 P2 | `████░░░░░░░░░░░░░░░░` 20% |
| `HBTV-008` | 🖼️ JavaFX & runtime packaging modernization | 🔵 Proposed | 🟡 P2 | `█░░░░░░░░░░░░░░░░░░░` 5% |
| `HBTV-010` | 🧪 `plugin-tester` reactor alignment | ✅ Done | 🟠 P1 | `████████████████████` 100% |
| `HBTV-011` | ▶️ Runnable console baseline | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| `HBTV-012` | 🔗 Own-version plugin dependency alignment | ✅ Done | 🔴 P0 | `████████████████████` 100% |
| `HBTV-013` | 🔑 YouTube Data API key externalization | 🟡 In progress | 🟠 P1 | `█████████████████░░░` 85% |

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

## 🏗️ Governance bootstrap from master

> `HBTV-000` · ✅ **Done** · 🔴 **P0** · `████████████████████` **100%**

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
GitHub UI-level settings application moved to its own item (HBTV-003).

---

## ⚙️ Maven reactor stabilization

> `HBTV-001` · ✅ **Done** · 🔴 **P0** · `████████████████████` **100%**

**Scope** — Wire the multi-module reactor so `mvn validate` walks the
full project from the root and parent resolution is deterministic
across `fwk`, `application`, and `plugins`. Topology only.

**Acceptance criteria**
- ✅ Root `pom.xml` aggregates `fwk`, `application`, `plugins`
- ✅ `fwk/pom.xml` aggregates `api`, `framework`
- ✅ `application/pom.xml` keeps `core`, `consoleView`, `trayView`, `habiTv`
- ✅ `plugins/pom.xml` keeps 22 plugin modules + `plugin-tester` (added in HBTV-010)
- ✅ All child POMs parent at `4.1.0-SNAPSHOT` with explicit `<relativePath>`
- ✅ `fwk/framework` own-version typo `4.1.0-SNASPHOT` → `4.1.0-SNAPSHOT` fixed

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS, 33 reactor entries
```

**Related PR** · `build: stabilize Maven reactor from master` (#22)

**Notes** — `habiTv-linux` / `habiTv-windows` stay out of reactor
(JavaFX 2.x + hardcoded `${jdk.home}`), tracked under HBTV-008.

---

## ☕ Java 8 compile baseline

> `HBTV-002` · ✅ **Done** · 🔴 **P0** · `████████████████████` **100%**

**Scope** — Make `mvn compile` succeed on Temurin 8 from the root,
without resolving artifacts from the blocked legacy HTTP repository.

**Acceptance criteria**
- ✅ Intra-reactor `[4.1,4.2)` ranges replaced with `${project.version}`
- ✅ `maven-jaxb-plugin` pinned (1.1.1) in `application/core`
- ✅ `mvn compile` BUILD SUCCESS on Ubuntu and Windows with Temurin 8
- ✅ Network/provider tests excluded from default lifecycle

**Validation**
```bash
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS
mvn -B -ntp -DskipTests compile      # BUILD SUCCESS (after HBTV-010 + HBTV-012)
```

**Related PR** · `build: stabilize Java 8 compile baseline` (#23)

**Notes** — Original blocker chain: `plugin-tester:4.1.0`
(HBTV-010) → `framework/api:4.1.1-SNAPSHOT` (HBTV-012). Both cleared.
CI workflow now triggers on `master` **and** `develop` so every
modernization PR is validated. Risks **R-010**, **R-011**, **R-012**
are mitigated.

---

## 🛡️ GitHub branch protection rules

> `HBTV-003` · 🔵 **Proposed** · 🟠 **P1** · `░░░░░░░░░░░░░░░░░░░░` **0%**

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

## 🔗 Legacy URL migration — free.fr / SVN / FTP

> `HBTV-004` · 🟡 **In progress** · 🟠 **P1** · `███████░░░░░░░░░░░░░` **35%**

**Scope** — Remove or replace every active reference to:
- `<scm>scm:svn:http://subversion.assembla.com/svn/habitv/trunk` (~22 POMs)
- `<repository>http://dabiboo.free.fr/repository` (3 POMs)
- `<distributionManagement>ftp://ftpperso.free.fr/repository` + `wagon-ftp`
- Runtime constants `UPDATE_URL` and `STAT_URL`

**Acceptance criteria**
- 🟡 Migration plan document published — *PR #25 (draft)*
- ⬜ Runtime quarantine flags `habitv.stat.enabled` and
  `habitv.update.enabled` (default `false`) implemented and tested
- 🟡 POMs swapped to GitHub URLs — *PR #36 (open)*
- ⬜ `wagon-ftp` extension removed
- ⬜ `<scm>` blocks point to the current GitHub URL
- ⬜ Plain-HTTP `dabiboo.free.fr` repository fully removed

**Validation**
```bash
git grep -nIE "dabiboo|free\.fr|ftpperso|subversion\.assembla|scm:svn" -- .
# expected: no active matches outside docs/history sections
mvn -B -ntp -DskipTests validate     # BUILD SUCCESS
mvn -B -ntp -DskipTests compile      # BUILD SUCCESS
```

**Related PRs** · #25 (draft, plan) · #36 (open, execution)

**Notes** — Sequenced: (1) merge the plan, (2) ship the quarantine
flags, (3) flip POMs, (4) remove FTP, (5) point updater to the static
repo prepared by HBTV-005. Risks **R-001**, **R-002**, **R-007**,
**R-013**, **R-014**, **R-015** all converge here.

---

## 📦 Static artifact repository publication

> `HBTV-005` · 🔵 **Proposed** · 🟠 **P1** · `██░░░░░░░░░░░░░░░░░░` **10%**

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

**Notes** — Pairs with HBTV-004 phase 5. Risks **R-009**, **R-014**.

---

## 🔌 Provider plugin inventory & cleanup

> `HBTV-006` · 🔵 **Proposed** · 🟡 **P2** · `░░░░░░░░░░░░░░░░░░░░` **0%**

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
RSS samples. Risks **R-005**, **R-006**.

---

## 🎬 `youtube-dl` → `yt-dlp` migration

> `HBTV-007` · 🔵 **Proposed** · 🟡 **P2** · `████░░░░░░░░░░░░░░░░` **20%**

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

**Notes** — Risk **R-008**. End-to-end live behavior remains out of
scope until HBTV-006 inventory cleanup.

---

## 🖼️ JavaFX & runtime packaging modernization

> `HBTV-008` · 🔵 **Proposed** · 🟡 **P2** · `█░░░░░░░░░░░░░░░░░░░` **5%**

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

**Notes** — Risk **R-003**. Largest single piece of remaining work
once HBTV-004 + HBTV-005 + HBTV-006 are clear.

---

## 🧪 `plugin-tester` reactor alignment

> `HBTV-010` · ✅ **Done** · 🟠 **P1** · `████████████████████` **100%**

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

**Notes** — Risk **R-012** mitigated. Residual `framework/api` blocker
moved to HBTV-012 (and resolved there).

---

## ▶️ Runnable console baseline

> `HBTV-011` · ✅ **Done** · 🔴 **P0** · `████████████████████` **100%**

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

**Notes** — Superseded PR #27 with a scoped subset. JavaFX modernization
(HBTV-008), tray/GUI packaging, and scraper rewrites remain out of scope.

---

## 🔗 Own-version plugin dependency alignment

> `HBTV-012` · ✅ **Done** · 🔴 **P0** · `████████████████████` **100%**

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
internal dependencyManagement coordinates. Risk **R-001** mitigated for
local reactor compilation (external publication still pending HBTV-004).

---

## 🔑 YouTube Data API key externalization

> `HBTV-013` · 🟡 **In progress** · 🟠 **P1** · `█████████████████░░░` **85%**

**Scope** — Remove the hardcoded YouTube Data API key from
`plugins/youtube` and resolve it from runtime configuration so revoked
or restricted keys do not require a code change.

**Acceptance criteria**
- ✅ `YoutubeConf` no longer embeds a concrete API key value
- ✅ Runtime lookup: Java property `habitv.youtube.apiKey`, then env
  `HABITV_YOUTUBE_API_KEY`
- ✅ Tray configuration exposes a user-editable field persisted in
  user config
- ✅ Playlist API request URL only includes supported parameters
- ✅ Error messages include sanitized request context (no key leak)
- 🟡 PR merged onto `develop`

**Validation**
```bash
mvn -B -ntp -DskipTests -pl plugins/youtube -am validate                       # BUILD SUCCESS
mvn -B -ntp -DskipTests -pl application/trayView,plugins/youtube -am compile   # BUILD SUCCESS
mvn -B -ntp -pl plugins/youtube -am -Dtest=YoutubeConfTest test                # BUILD SUCCESS
```

**Related PR** · `fix(youtube): externalize data api key and mask api errors` (#29)

**Notes** — PR #29 is blocked on a documentation merge conflict only
(this very item collided with `HBTV-012`'s id on `develop`); renumbered
here to `HBTV-013`. Risk **R-013** mitigated by this work.

---

# 📈 What ships next

Recommended merge / start order (see `dev-plan.md` for phase reasoning):

1. 🟡 **Resolve PR #29** → close `HBTV-013` *(doc-only conflict)*
2. 🔵 **Apply branch protection** → close `HBTV-003`
3. 🟡 **Land the URL migration plan PR #25** → unblock `HBTV-004` execution
4. 🟡 **Land PR #36** + write quarantine flags → halve `HBTV-004` scope
5. 🔵 **Merge `habitv-repo` PR #1** → start `HBTV-005`
6. 🔵 Then in any order: `HBTV-006`, `HBTV-007`, `HBTV-008`
