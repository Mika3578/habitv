# HBTV-004 — Legacy URL migration plan

Plan-only document for migrating Habitv away from legacy
SVN/Assembla, plain-HTTP `dabiboo.free.fr`, FTP `ftpperso.free.fr`,
and embedded telemetry/update URLs. No code or POM is changed in
this PR. Implementation will be split across follow-up PRs listed
in section 5.

This document pairs with `docs/hbtv-005-publication-layout.md`
(future) and supersedes the rough notes in
`docs/audit-master-baseline.md` lines 54-84 by enumerating exact
call sites and a sequenced migration plan.

## 1. Inventory of legacy references

### 1.1 SCM blocks (SVN / Assembla) — 23 POMs

Root:

- `pom.xml:21-24` — `scm:svn:http://subversion.assembla.com/svn/habitv/trunk`

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
- `plugins/email/pom.xml` (also points at `plugins/RSS` by
  copy/paste — fix in the same change set)
- `plugins/ffmpeg/pom.xml`
- `plugins/file/pom.xml`
- `plugins/lequipe/pom.xml`
- `plugins/pluzz/pom.xml`
- `plugins/rtmpDump/pom.xml`
- `plugins/sfr/pom.xml`
- `plugins/wat/pom.xml`
- `plugins/youtube/pom.xml`

Plugins without `<scm>` block today (intentionally left as-is until
the global swap):

- `plugins/footyroom`, `plugins/globalnews`, `plugins/mlssoccer`,
  `plugins/6play`.

### 1.2 Maven `<repository>` (HTTP `dabiboo.free.fr`) — 3 POMs

- `pom.xml:33-39`
- `application/habiTv-linux/pom.xml:20`
- `application/habiTv-windows/pom.xml:32`

All three declare the same id `dabi-repo` pointing at
`http://dabiboo.free.fr/repository`. Plain HTTP, third-party
hosting, blocked by Maven 3.9+ default mirror policy.

### 1.3 `<distributionManagement>` FTP — 1 POM

- `pom.xml:26-31` — `ftp://ftpperso.free.fr/repository`
- `pom.xml:159-166` — `<extension>` `wagon-ftp:1.0-beta-6`

FTP is unencrypted, free.fr personal hosting is deprecated, and the
extension is from 2009.

### 1.4 Runtime URL constants — 2 constants

| Constant     | File:line                                                                 | Purpose       |
|--------------|---------------------------------------------------------------------------|---------------|
| `UPDATE_URL` | `fwk/framework/src/com/dabi/habitv/framework/FrameworkConf.java:21`       | Artifact resolution for the in-app updater |
| `STAT_URL`   | `application/core/src/com/dabi/habitv/core/config/HabitTvConf.java:16`   | Telemetry ping on startup |

### 1.5 Runtime call sites

- `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java:40`
  — constructor passes `FrameworkConf.UPDATE_URL` to the
  `UpdateManager` instance.
- `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java:48`
  — `RetrieverUtils.getUrlContent(site + "/plugins.txt", null)`.
- `fwk/framework/src/com/dabi/habitv/framework/plugin/utils/update/FindArtifactUtils.java:56`
  — builds `${UPDATE_URL}/${groupIdPath}/${artifactId}` and
  recursively lists directory contents via Jsoup.
- `application/core/src/com/dabi/habitv/core/mgr/CoreManager.java:46-55`
  — `stat()` fires a background thread on construction that calls
  `RetrieverUtils.getUrlContent(HabitTvConf.STAT_URL, null)`.

### 1.6 Tests that hit the legacy hosts

- `application/core/test/com/dabi/habitv/core/updater/TestListHttp.java:30-37`
  — calls `FindArtifactUtils.findLastVersionUrl(...)`, which hits
  the live `UPDATE_URL`.
- `fwk/framework/test/com/dabi/habitv/framework/plugin/utils/TestUrl.java:10`
  — `RetrieverUtils.getTitleByUrl("http://www.beinsports.fr")`,
  out of scope for HBTV-004 but tracked under R-005.

### 1.7 Adjacent legacy traces (not in scope, recorded for context)

- `application/consoleView/config.xml:57,84` — sample `curl`
  commands with hardcoded freebox FTP credentials
  (`ftp://freebox:4688@hd1.freebox.fr/...`). Sample file, not
  executed. Tracked as new risk R-013.
- `plugins/email/test/com/dabi/habitv/plugin/email/MessageReceiverTest.java:14,19`
  and
  `plugins/email/test/com/dabi/habitv/plugin/email/EmailPluginManagerTest.java:29`
  — hardcoded Gmail credentials `testhabitv` / `HabiTV410`.
  Tracked as new risk R-013.

## 2. Updater contract (reverse-engineered from FindArtifactUtils)

The current updater relies on an Apache-style `mod_autoindex` HTTP
server. Any replacement host must serve compatible HTML or the
updater stops discovering new artifacts.

Required URL shapes:

```
${UPDATE_URL}/plugins.txt
${UPDATE_URL}/${groupId-with-slashes}/${artifactId}/
${UPDATE_URL}/${groupId-with-slashes}/${artifactId}/${version}/
```

Required HTML shape (per directory):

- `<a href="X.Y.Z/">X.Y.Z</a>` for version directories.
- `<a href="artifactId-X.Y.Z.jar">...</a>` for files.
- Entries with text `Parent Directory`, `Name`, `Last modified`,
  `Size`, `Description` are filtered out (Apache autoindex
  headers).

Selection rules:

- `AlphanumComparator` is used to pick the highest version that
  starts with `${coreVersionMajor.Minor}` (e.g. `4.1`).
- `autoriseSnapshot` toggles whether versions containing
  `SNAPSHOT` are eligible.

This constraint is the main driver for HBTV-005 hosting choice.

## 3. Target URLs (proposal)

### 3.1 SCM

```
scm:git:https://github.com/Mika3578/habitv.git
scm:git:git@github.com:Mika3578/habitv.git (developerConnection)
```

For per-module POMs, append the module path segment as a `<tag>`
or leave only the root coordinates. Recommend dropping the
per-module `<scm>` blocks entirely (Maven inherits from parent);
keep only the root and the two `habiTv-linux/-windows` packaging
modules that currently parent outside the reactor.

### 3.2 Maven `<repository>`

Replace `http://dabiboo.free.fr/repository` with the static repo
defined in HBTV-005. Working assumption:

```
https://mika3578.github.io/habitv-repo/maven/
```

Until HBTV-005 stands up the host, this repository declaration can
be removed entirely (no current dependency is resolved through it
once R-010 was fixed in HBTV-002).

### 3.3 `<distributionManagement>`

Drop the FTP block and the `wagon-ftp` extension. Replace with a
documented publication workflow under HBTV-005 (GitHub Actions
pushing to the static repo).

### 3.4 Runtime `UPDATE_URL`

Switch to the same HTTPS static repo:

```
https://mika3578.github.io/habitv-repo/
```

### 3.5 Runtime `STAT_URL`

Two options, decision deferred to ADR-0008:

- Remove telemetry entirely. Simplest, no privacy surface.
- Keep it behind an opt-in flag (default off) and migrate to an
  HTTPS endpoint under Habitv control.

## 4. Feature flag plan (quarantine before swap)

Goal: stop network pings to the legacy hosts in dev/CI builds
before any URL is changed. This avoids leaking dev traffic to a
third-party host during the migration.

Proposed properties (default `false`, opt-in via system property
or environment):

| Property                 | Effect                                       |
|--------------------------|----------------------------------------------|
| `habitv.stat.enabled`    | Gate `CoreManager.stat()` background thread. |
| `habitv.update.enabled`  | Gate `UpdateManager.process()` entirely.     |

Packaging POMs and release scripts should set both to `true` for
production builds. Local development and CI leave them at `false`.

`TestListHttp.java` should be tagged `@Ignore` with a comment
pointing at R-005 (live network) once the gates are added; a
follow-up under HBTV-002 quarantines live tests behind an opt-in
profile.

## 5. PR sequencing

All PRs below are scoped to one tracker item each.

| #  | Branch                                          | Tracker | Scope summary                                                                                          |
|----|-------------------------------------------------|---------|--------------------------------------------------------------------------------------------------------|
| 1  | `claude/analyze-audit-jrczu`                    | HBTV-004 | This document + tracker / risk / decision updates. Plan only, no code or POM.                          |
| 2  | `runtime/quarantine-stat-url`                   | HBTV-004 | Add `habitv.stat.enabled` gate in `CoreManager.stat()`. URL constant unchanged.                        |
| 3  | `runtime/quarantine-update-url`                 | HBTV-004 | Add `habitv.update.enabled` gate in `UpdateManager.process()`. URL constant unchanged.                 |
| 4  | `build/replace-scm-urls`                        | HBTV-004 | Swap 23 `<scm>` SVN blocks to GitHub. Fix `plugins/email` copy/paste pointing at `plugins/RSS`.       |
| 5  | `build/remove-ftp-distribution-management`      | HBTV-004 | Drop `<distributionManagement>` FTP and `wagon-ftp` extension from `pom.xml`.                          |
| 6  | `build/replace-maven-repository-url`            | HBTV-005 | Swap 3 `<repository>` HTTP blocks to HTTPS target. Requires HBTV-005 host live.                        |
| 7  | `runtime/swap-update-stat-urls`                 | HBTV-005 | Update `FrameworkConf.UPDATE_URL` and `HabitTvConf.STAT_URL` to the HTTPS targets.                     |

Validation per PR: `mvn -B -ntp -DskipTests validate` plus the
narrower commands the touched files imply (compile for code PRs,
tests opt-in only).

Order rationale:

- PRs 2 and 3 must land first; they cut the silent traffic so the
  later POM swaps cannot regress dev behavior.
- PR 4 (SCM) is fully orthogonal and can land any time.
- PR 5 (FTP) is safe because no one publishes through it today.
- PRs 6 and 7 depend on HBTV-005 (publication host live + layout
  validated against `FindArtifactUtils` semantics).

## 6. Risks introduced or surfaced

See `docs/risk-register.md`:

- R-013 — Hardcoded Gmail and freebox credentials in tests and
  sample config.
- R-014 — GitHub Pages does not serve autoindex natively; updater
  contract breaks unless `index.html` files are generated.
- R-015 — `CoreManager.stat()` pings a third-party host on every
  startup in development builds.

## 7. Decisions referenced

See `docs/decision-log.md`:

- ADR-0004 (Proposed) — `habitv-repo` as future static artifact
  repository. To be confirmed or superseded once HBTV-005 lands.
- ADR-0008 (Proposed) — Quarantine network pings at startup in
  development builds via opt-in properties.
- ADR-0009 (Proposed) — Publish via GitHub Pages with generated
  `index.html` to remain compatible with `FindArtifactUtils`.

## 8. Out of scope for this document

- HBTV-005 layout details (directory naming, signature plan,
  publication automation). Covered by
  `docs/hbtv-005-publication-layout.md` (future PR).
- HBTV-008 packaging / JavaFX cleanup of `habiTv-linux`,
  `habiTv-windows`.
- HBTV-006 provider inventory.
- HBTV-007 yt-dlp migration.
- R-005 / R-013 remediation (live-network tests, credential
  cleanup) — recorded but executed in dedicated PRs.
