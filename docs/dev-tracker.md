# Habitv Living Development Tracker

_Last updated: 2026-04-27T10:25:00Z_

## Governance

### Definition of Ready (DoR)
A task is Ready when:
1. Scope is explicit (module/files touched).
2. Reproduction/validation command is defined.
3. Dependencies and risks are identified.
4. Owner role and due date are set.

### Definition of Done (DoD)
A task is Done when:
1. Code/docs merged with citations/evidence.
2. Validation commands executed and recorded.
3. Tracker + risk register + decision log updated.
4. Follow-up backlog items created for deferred work.

### Escalation path
1. Task owner raises blocker in tracker.
2. Build/Release Engineer triages within 1 business day.
3. Architect decides scope tradeoff within 2 business days.
4. If release-impacting, escalate to Engineering Manager.

## Program status
- Overall program: **In Progress**
- Current phase: **Phase 0 (Build stabilization)**
- Progress: **52%**

## Backlog (live)

| ID | Title | Priority | Status | Owner role | Dependencies | Risk | Due date | Progress | Links |
|---|---|---|---|---|---|---|---|---|---|
| HBTV-001 | Align parent versions + relativePath | P0 | Done | Build/Release Eng | None | Hidden module breakages after fix | 2026-04-30 | 100% | PR:3 / Issue:TBD |
| HBTV-002 | Add root/fwk module aggregation | P0 | Done | Build/Release Eng | HBTV-001 | Reactor exposes compile failures | 2026-05-02 | 100% | PR:3 / Issue:TBD |
| HBTV-003 | Fix `4.1.0-SNASPHOT` typo and version policy | P1 | Done | Build/Release Eng | HBTV-001 | Transitive dependency drift | 2026-05-02 | 100% | PR:3 / Issue:TBD |
| HBTV-004 | Add baseline GitHub build workflow | P0 | Done | Build/Release Eng | HBTV-001,HBTV-002 | CI noise if gates too strict initially | 2026-05-05 | 100% | PR:3 / Issue:TBD |
| HBTV-005 | Add PR template + modernization/bug/feature issue templates | P1 | Done | Tech Lead | None | Process adoption lag | 2026-05-06 | 100% | PR:4 / Issue:TBD |
| HBTV-006 | Bootstrap security/dependency scan | P1 | Done | Security Eng | HBTV-004 | Legacy deps trigger many findings and first scan may not produce report | 2026-05-10 | 100% | PR:7 / Issue:TBD |
| HBTV-007 | Establish Java 17 compatibility build profile | P1 | In Progress | Architect + Build Eng | HBTV-001,HBTV-002 | JavaFX legacy blockers + JAXB compile drift | 2026-05-20 | 60% | PR:7 / Issue:TBD |
| HBTV-008 | Split deterministic unit vs integration tests | P1 | In Progress | QA/Build Eng | HBTV-002 | Test ownership ambiguity | 2026-05-25 | 40% | PR:TBD / Issue:TBD |
| HBTV-007a | Resolve `application/trayView` JavaFX compile blocker on modern toolchains | P1 | Todo | Desktop Lead + Build Eng | HBTV-007 | JavaFX modules absent outside JDK8 | 2026-05-22 | 0% | PR:TBD / Issue:TBD |
| HBTV-007b | Add JAXB runtime provider for tests/runtime (`com.sun.xml.bind.v2.ContextFactory`) | P1 | Done | Build/Release Eng | HBTV-007 | Runtime JAXB provider mismatch causes test failures | 2026-05-22 | 100% | PR:13 / Issue:TBD |
| HBTV-007c | Isolate remaining network-dependent `TestListHttp` from default install lifecycle | P1 | Done | QA/Build Eng | HBTV-008a | Network-dependent test remains non-deterministic | 2026-05-25 | 100% | PR:13 / Issue:TBD |
| HBTV-008a | URL modernization support task (HTTP/provider audit + network smoke test isolation) | P1 | In Progress | QA/Build Eng | HBTV-008 | URL-only updates may not restore provider compatibility | 2026-05-25 | 25% | PR:TBD / Issue:TBD |
| HBTV-008b | Stabilize plugin snapshot dependency resolution and isolate live provider plugin tests | P1 | In Progress | Build/Release Eng + QA/Build Eng | HBTV-008,HBTV-008a | Plugin snapshot drift or live provider tests can break deterministic installs | 2026-05-03 | 60% | PR:13 / Issue:TBD |
| HBTV-011 | Audit plugin provider inventory and classify module status (docs only) | P1 | Done | QA/Build Eng | HBTV-008b | Misclassification could hide runtime/plugin maintenance debt | 2026-04-27 | 100% | PR:TBD / Issue:TBD |
| HBTV-009 | Migrate packaging away from JDK7 JavaFX paths | P2 | Todo | Desktop Lead | HBTV-007 | Packaging regression on Windows/Linux | 2026-06-15 | 0% | PR:TBD / Issue:TBD |
| HBTV-010 | Add CODEOWNERS + release/build policy docs | P2 | Todo | Eng Manager + Release Mgr | HBTV-005 | Policy drift | 2026-06-20 | 0% | PR:TBD / Issue:TBD |

## Active blockers

| Blocker ID | Description | Impacted items | Owner | Escalate by | Status |
|---|---|---|---|---|---|
| BLK-001 | Parent POM mismatch prevents module builds | HBTV-001,HBTV-002,HBTV-004 | Build/Release Eng | 2026-04-25 | Closed (2026-04-24) |
| BLK-002 | External HTTP repo returns 403 / runtime updater migration incomplete | HBTV-006,HBTV-007 | Build/Release Eng | 2026-04-28 | Open (partially mitigated for Maven CI; runtime updater migrated to jsDelivr) |
| BLK-003 | `application/trayView` compile requires JavaFX classes not present in modern/non-JDK8 toolchains | HBTV-007,HBTV-007a | Desktop Lead + Build Eng | 2026-04-29 | Open |
| BLK-004 | JAXB runtime provider `com.sun.xml.bind.v2.ContextFactory` missing in test/runtime paths | HBTV-007,HBTV-007b | Build/Release Eng | 2026-04-29 | Closed (2026-04-25) |
| BLK-005 | Remaining network-dependent `TestListHttp` keeps `install` non-deterministic | HBTV-008,HBTV-008a,HBTV-007c | QA/Build Eng | 2026-04-30 | Closed (2026-04-25) |
| BLK-006 | Plugin modules resolve stale snapshot metadata due to version drift (`4.1.1/4.1.2-SNAPSHOT`); live provider `BasePluginProviderTester` tests no longer run in default Surefire | HBTV-008b | Build/Release Eng + QA/Build Eng | 2026-04-30 | Open |

## Decisions snapshot
- See `docs/decision-log.md`.

## Changelog (append-only)
- 2026-04-24T13:30:00Z — Initialized living tracker structure (DoR/DoD/escalation/backlog/blockers).
- 2026-04-24T13:35:00Z — Added initial backlog IDs, priorities, dependencies, and due dates.
- 2026-04-24T13:40:00Z — Synced tracker with modernization report v2 and marked Phase 0 active.
- 2026-04-24T16:10:00Z — Completed P0 backlog items HBTV-001/HBTV-002/HBTV-004; closed BLK-001.
- 2026-04-24T16:25:00Z — Completed HBTV-005: PR template + modernization/bug/feature issue templates added.
- 2026-04-24T18:05:00Z — Completed HBTV-006: added baseline dependency scan workflow and report artifact publication.
- 2026-04-24T18:05:00Z — Started URL modernization by auditing obsolete HTTP/provider URLs and isolating network smoke tests (support task for HBTV-008).
- 2026-04-24T19:20:00Z — Started HBTV-007: added `java17-compat` profile and Java 17 CI lane; discovered `core` compile blocker (JAXB-generated API mismatch).
- 2026-04-24T20:05:00Z — Mitigated CI dependency resolution failures by removing custom `dabi-repo` repository declarations and aligning plugin versions to `4.1.0-SNAPSHOT`.
- 2026-04-24T21:00:00Z — Hardened security workflow (Java 17, pinned dependency-check aggregate, cache, timeout, optional `NVD_API_KEY`, explicit missing-report warnings) and corrected tracker status for PR #7.
- 2026-04-24T21:30:00Z — Hardened dependency-check robustness by avoiding cached `~/.dependency-check` H2 DB reuse, clearing local DB before run/retries, and adding retry logic for transient feed/update failures.
- 2026-04-25T12:00:00Z — Linked JAXB stabilization progress to PR:10, kept HBTV-007 In Progress, and added follow-up backlog items for JavaFX trayView, JAXB runtime provider, and TestListHttp isolation.
- 2026-04-25T12:05:00Z — Added JAXB runtime provider wiring and tracked the remaining network-dependent HTTP test for follow-up isolation from the default install lifecycle.
- 2026-04-25T12:10:00Z — Validation found an unrelated blocker at module `application/core`: compile fails on generated accessor mismatches (`isUpdateOnStartup`, `isAutoriseSnapshot`, `isDownload`/`isDeleted` family not found).
- 2026-04-25T12:55:00Z — Completed HBTV-007b/HBTV-007c in PR #13 by pinning compatible JAXB API/RI versions and isolating `ListHttpNetworkIT` to profile-gated Failsafe execution.
- 2026-04-25T13:05:00Z — Re-ran Java 8 local validation on PR #13 branch and recorded exact current outcomes (`validate` pass; `compile`/`install` stop at `application/core` accessor mismatch; `-Pnetwork-tests verify` fails in framework network IT with remote 403).
- 2026-04-25T13:14:00Z — Aligned JAXB accessor calls with generated getter API and replaced provider-specific network smoke URL with stable example.com test URL.
- 2026-04-25T13:30:00Z — Added HBTV-008b to track plugin snapshot dependency stabilization and 6play live-test isolation from default lifecycle; HBTV-007 and HBTV-007a remain unchanged.
- 2026-04-27T10:25:00Z — Isolated live provider `BasePluginProviderTester` tests to `*IT` + Failsafe (`network-tests`); updated `docs/testing.md`, url inventory, risk register, and tracker.
- 2026-04-27T08:00:00Z — Migrated runtime updater base URL from `dabiboo.free.fr` to `cdn.jsdelivr.net/gh/Mika3578/habitv-repo@main` (jsDelivr CDN backed by habitv-repo on GitHub); runtime startup no longer depends on the legacy repository host. jsDelivr supports browsable directory listings, satisfying the updater's HTML anchor discovery requirement.
- 2026-04-27T08:00:00Z — Recorded follow-up to publish current plugin/tool artifacts and `plugins.txt` metadata to `habitv-repo` for full startup update success.
- 2026-04-27T08:27:51Z — Completed provider/plugin audit documentation (`docs/plugin-provider-inventory.md`) and synchronized risk/testing/url inventories with provider status classifications, URL redirect observations, and test isolation recommendations.

## Live provider test isolation — local validation (Java 8, 2026-04-27)
- `mvn -B -ntp clean -DskipTests compile`: **Success** (full reactor).
- `mvn -B -ntp clean install`: **Failure** at `plugins/email` (`EmailPluginManagerTest`, `MessageReceiverTest`: JavaMail SSL/protocol and NPE). Live provider modules through `plugins/curl` **Success**; provider ITs no longer run in default Surefire.
- `mvn -B -ntp clean deploy -DskipTests` with `-Dhabitv.static.repo.dir` (quoted path): **Success**; artifacts deployed to local static repo dir.
- `mvn -B -ntp clean -Pnetwork-tests verify`: **Failure** at `plugins/6play` Failsafe `SixPlayPluginManagerIT` (`categorie liste vide`) — expected opt-in provider volatility; remaining plugin ITs not reached in this run.
## Next update trigger
Update this file after each of:
- merged PR,
- blocker status change,
- decision log entry,
- risk level change.
