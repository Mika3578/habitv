# HBTV-004 - Legacy URL migration plan

**Document status:** Preserved historical planning and reference material
imported from PR #25 (`claude/analyze-audit-jrczu`). It records the legacy
inventory and sequenced plan as understood before execution.

**Execution:** PR #36 (`chore/remove-legacy-dabiboo-repository`) implements
HBTV-004 wiring removal on `develop`. That PR changes active POM and runtime
constants; this file is not a plan-only PR description.

**Out of scope here (HBTV-005 and later):** GitHub Pages publication layout,
static `index.html` generation for updater discovery, manifest automation, and
full updater contract validation against a live host.

This document pairs with `docs/hbtv-005-publication-layout.md` (future) and
supersedes the rough notes in `docs/audit-master-baseline.md` lines 54-84 by
enumerating exact call sites and the original sequenced migration plan.

## 1. Inventory of legacy references (pre-PR #36 baseline)

The tables below describe the repository **before** PR #36. They are kept for
audit traceability, not as the current wiring state.

### 1.1 SCM blocks (SVN / Assembla) - 23 POMs

Root:

- `pom.xml:21-24` - `scm:svn:http://subversion.assembla.com/svn/habitv/trunk`

Framework:

- `fwk/api/pom.xml`
- `fwk/framework/pom.xml`

Packaging (out of reactor, HBTV-008):

- `application/habiTv-linux/pom.xml`
- `application/habiTv-windows/pom.xml`

Plugins (18 of 22 + `plugin-tester`):

- `plugins/RSS/pom.xml`
- `plugins/adobeHDS/pom.xml`
- `plugins/aria2/pom.xml`
- `plugins/arte/pom.xml`
- `plugins/beinsport/pom.xml`
- `plugins/canalPlus/pom.xml`
- `plugins/clubic/pom.xml`
- `plugins/cmd/pom.xml`
- `plugins/curl/pom.xml`
- `plugins/email/pom.xml` (also pointed at `plugins/RSS` by copy/paste -
  fixed in the same change set)
- `plugins/ffmpeg/pom.xml`
- `plugins/file/pom.xml`
- `plugins/lequipe/pom.xml`
- `plugins/pluzz/pom.xml`
- `plugins/rtmpDump/pom.xml`
- `plugins/sfr/pom.xml`
- `plugins/wat/pom.xml`
- `plugins/youtube/pom.xml`

Plugins without `<scm>` block at planning time (intentionally left as-is until
the global swap):

- `plugins/footyroom`, `plugins/globalnews`, `plugins/mlssoccer`,
  `plugins/6play`.

### 1.2 Maven `<repository>` (HTTP `dabiboo.free.fr`) - 3 POMs

- `pom.xml:33-39`
- `application/habiTv-linux/pom.xml:20`
- `application/habiTv-windows/pom.xml:32`

All three declared the same id `dabi-repo` pointing at
`http://dabiboo.free.fr/repository`. Plain HTTP, third-party hosting, blocked
by Maven 3.9+ default mirror policy.

### 1.3 `<distributionManagement>` FTP - 1 POM

- `pom.xml:26-31` - `ftp://ftpperso.free.fr/repository`
- `pom.xml:159-166` - `<extension>` `wagon-ftp:1.0-beta-6`

FTP is unencrypted, free.fr personal hosting is deprecated, and the extension
is from 2009.

### 1.4 Runtime URL constants - 2 constants

| Constant     | File:line                                                                 | Purpose       |
|--------------|---------------------------------------------------------------------------|---------------|
| `UPDATE_URL` | `fwk/framework/src/com/dabi/habitv/framework/FrameworkConf.java:21`       | Artifact resolution for the in-app updater |
| `STAT_URL`   | `application/core/src/com/dabi/habitv/core/config/HabitTvConf.java:16`   | Telemetry ping on startup |

### 1.5 Runtime call sites

- `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java:40`
  - constructor passes `FrameworkConf.UPDATE_URL` to the `UpdateManager`
  instance.
- `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java:48`
  - `RetrieverUtils.getUrlContent(site + "/plugins.txt", null)`.
- `fwk/framework/src/com/dabi/habitv/framework/plugin/utils/update/FindArtifactUtils.java:56`
  - builds `${UPDATE_URL}/${groupIdPath}/${artifactId}` and recursively lists
  directory contents via Jsoup.
- `application/core/src/com/dabi/habitv/core/mgr/CoreManager.java:46-55`
  - `stat()` fired a background thread on construction that called
  `RetrieverUtils.getUrlContent(HabitTvConf.STAT_URL, null)`.

### 1.6 Tests that hit the legacy hosts

- `application/core/test/com/dabi/habitv/core/updater/TestListHttp.java:30-37`
  - called `FindArtifactUtils.findLastVersionUrl(...)`, which hit the live
  `UPDATE_URL`.
- `fwk/framework/test/com/dabi/habitv/framework/plugin/utils/TestUrl.java:10`
  - `RetrieverUtils.getTitleByUrl("http://www.beinsports.fr")`, out of scope
  for HBTV-004 but tracked under R-005.

### 1.7 Adjacent legacy traces (not in scope, recorded for context)

- `application/consoleView/config.xml:57,84` - sample `curl` commands with
  hardcoded freebox FTP credentials (`ftp://freebox:4688@hd1.freebox.fr/...`).
  Sample file, not executed. Tracked as new risk R-013.
- `plugins/email/test/com/dabi/habitv/plugin/email/MessageReceiverTest.java:14,19`
  and
  `plugins/email/test/com/dabi/habitv/plugin/email/EmailPluginManagerTest.java:29`
  - hardcoded Gmail credentials `testhabitv` / `HabiTV410`. Tracked as new
  risk R-013.

## 2. Updater contract (reverse-engineered from FindArtifactUtils)

The updater relies on an Apache-style `mod_autoindex` HTTP server. Any
replacement host must serve compatible HTML or the updater stops discovering
new artifacts.

Required URL shapes:

```
${UPDATE_URL}/plugins.txt
${UPDATE_URL}/${groupId-with-slashes}/${artifactId}/
${UPDATE_URL}/${groupId-with-slashes}/${artifactId}/${version}/
```

Required HTML shape (per directory):

- `<a href="X.Y.Z/">X.Y.Z</a>` for version directories.
- `<a href="artifactId-X.Y.Z.jar">...</a>` for files.
- Entries with text `Parent Directory`, `Name`, `Last modified`, `Size`,
  `Description` are filtered out (Apache autoindex headers).

Selection rules:

- `AlphanumComparator` is used to pick the highest version that starts with
  `${coreVersionMajor.Minor}` (e.g. `4.1`).
- `autoriseSnapshot` toggles whether versions containing `SNAPSHOT` are
  eligible.

This constraint is the main driver for HBTV-005 hosting choice.

## 3. Target URLs

### 3.1 SCM (executed in PR #36)

```
scm:git:https://github.com/Mika3578/habitv.git
scm:git:git@github.com:Mika3578/habitv.git (developerConnection)
```

Per-module `<scm>` blocks were removed or aligned with the parent; see PR #36
for the final POM set.

### 3.2 Maven `<repository>` (executed base URL)

**Original planning note (PR #25):** assumed a future path such as
`https://mika3578.github.io/habitv-repo/maven/`.

**Executed in PR #36:** active `<repository>` URLs and runtime discovery use the
GitHub Pages base path:

```
https://mika3578.github.io/habitv-repo/repository
```

HBTV-005 may later split Maven, tools, and manifests under separate prefixes
(see `docs/static-repository-deploy.md`). That layout work is not part of PR
#36.

### 3.3 `<distributionManagement>` (executed in PR #36)

FTP `distributionManagement` and the `wagon-ftp` extension were removed.
Publication automation remains under HBTV-005.

### 3.4 Runtime `UPDATE_URL` (executed in PR #36)

```
https://mika3578.github.io/habitv-repo/repository
```

Runtime plugin updates remain **disabled by default** in PR #36; enabling them
for production builds is a separate packaging/release decision.

### 3.5 Runtime `STAT_URL`

Two options, decision deferred to ADR-0008:

- Remove telemetry entirely. Simplest, no privacy surface.
- Keep it behind an opt-in flag (default off) and migrate to an HTTPS endpoint
  under Habitv control.

PR #36 disables stat/update network calls by default; see `CoreManager` and
`UpdateManager` gating in that PR.

## 4. Feature flag plan (quarantine before swap)

Goal: stop network pings to legacy hosts in dev/CI builds before any URL is
changed. PR #36 implements default-off gating for stat and update processing.

Proposed properties (default `false`, opt-in via system property or
environment):

| Property                 | Effect                                       |
|--------------------------|----------------------------------------------|
| `habitv.stat.enabled`    | Gate `CoreManager.stat()` background thread. |
| `habitv.update.enabled`  | Gate `UpdateManager.process()` entirely.     |

Packaging POMs and release scripts may set both to `true` for production
builds. Local development and CI leave them at `false`.

`TestListHttp.java` should be tagged `@Ignore` with a comment pointing at R-005
(live network) once the gates are added; a follow-up under HBTV-002
quarantines live tests behind an opt-in profile.

## 5. PR sequencing (original plan vs execution)

The table below is the **original** split from PR #25. PR #36 consolidates
most build/runtime wiring removal; remaining rows are follow-ups.

| #  | Branch / PR                                      | Tracker | Status / scope summary                                                                                |
|----|--------------------------------------------------|---------|-------------------------------------------------------------------------------------------------------|
| 1  | PR #25 (`claude/analyze-audit-jrczu`)            | HBTV-004 | **Done (plan only).** This document + tracker/risk/decision updates.                                  |
| -  | PR #36 (`chore/remove-legacy-dabiboo-repository`)| HBTV-004 | **Execution PR.** SCM swap, remove FTP/dabiboo wiring, HTTPS repo URL, default-off stat/update.       |
| 2  | `runtime/quarantine-stat-url`                    | HBTV-004 | Largely covered by PR #36 default-off stat gate.                                                      |
| 3  | `runtime/quarantine-update-url`                  | HBTV-004 | Largely covered by PR #36 default-off update gate.                                                    |
| 4  | `build/replace-scm-urls`                         | HBTV-004 | Covered by PR #36.                                                                                    |
| 5  | `build/remove-ftp-distribution-management`         | HBTV-004 | Covered by PR #36.                                                                                    |
| 6  | `build/replace-maven-repository-url`             | HBTV-005 | Host layout and publication; `/repository` base set in PR #36.                                        |
| 7  | `runtime/swap-update-stat-urls`                  | HBTV-005 | HTTPS constants set in PR #36; live updater validation depends on HBTV-005 index/manifest work.       |

Validation per execution PR: `mvn -B -ntp -DskipTests validate` and `compile`
plus narrower commands implied by touched files (tests opt-in only).

Order rationale (historical):

- Quarantine gates before or with URL swaps to avoid dev traffic to legacy hosts.
- SCM and FTP removal are orthogonal to hosting layout.
- HBTV-005 depends on publication host live and layout compatible with
  `FindArtifactUtils` semantics.

## 6. Risks introduced or surfaced

See `docs/risk-register.md`:

- R-013 - Hardcoded Gmail and freebox credentials in tests and sample config.
- R-014 - GitHub Pages does not serve autoindex natively; updater contract
  breaks unless `index.html` files are generated.
- R-015 - `CoreManager.stat()` pinged a third-party host on every startup in
  development builds (mitigated by PR #36 default-off gate).

## 7. Decisions referenced

See `docs/decision-log.md`:

- ADR-0004 (Proposed) - `habitv-repo` as future static artifact repository.
  Confirmed base path `/repository` for PR #36; full layout under HBTV-005.
- ADR-0008 (Proposed) - Quarantine network pings at startup in development
  builds via opt-in properties (implemented default-off in PR #36).
- ADR-0009 (Proposed) - Publish via GitHub Pages with generated `index.html`
  to remain compatible with `FindArtifactUtils`.

## 8. Out of scope for this document

- HBTV-005 layout details (directory naming, signature plan, publication
  automation). Covered by `docs/hbtv-005-publication-layout.md` (future PR).
- HBTV-008 packaging / JavaFX cleanup of `habiTv-linux`, `habiTv-windows`.
- HBTV-006 provider inventory.
- HBTV-007 yt-dlp migration.
- R-005 / R-013 remediation (live-network tests, credential cleanup) - recorded
  but executed in dedicated PRs.
