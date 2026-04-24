# Habitv Living Development Tracker

_Last updated: 2026-04-24T19:20:00Z_

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
- Current phase: **Phase 1 (Toolchain/dependency modernization)**
- Progress: **55%**

## Backlog (live)

| ID | Title | Priority | Status | Owner role | Dependencies | Risk | Due date | Progress | Links |
|---|---|---|---|---|---|---|---|---|---|
| HBTV-001 | Align parent versions + relativePath | P0 | Done | Build/Release Eng | None | Hidden module breakages after fix | 2026-04-30 | 100% | PR:3 / Issue:TBD |
| HBTV-002 | Add root/fwk module aggregation | P0 | Done | Build/Release Eng | HBTV-001 | Reactor exposes compile failures | 2026-05-02 | 100% | PR:3 / Issue:TBD |
| HBTV-003 | Fix `4.1.0-SNASPHOT` typo and version policy | P1 | Done | Build/Release Eng | HBTV-001 | Transitive dependency drift | 2026-05-02 | 100% | PR:3 / Issue:TBD |
| HBTV-004 | Add baseline GitHub build workflow | P0 | Done | Build/Release Eng | HBTV-001,HBTV-002 | CI noise if gates too strict initially | 2026-05-05 | 100% | PR:3 / Issue:TBD |
| HBTV-005 | Add PR template + modernization/bug/feature issue templates | P1 | Done | Tech Lead | None | Process adoption lag | 2026-05-06 | 100% | PR:4 / Issue:TBD |
| HBTV-006 | Bootstrap security/dependency scan | P1 | Done | Security Eng | HBTV-004 | Legacy deps trigger many findings | 2026-05-10 | 100% | PR:TBD / Issue:TBD |
| HBTV-007 | Establish Java 17 compatibility build profile | P1 | In Progress | Architect + Build Eng | HBTV-001,HBTV-002 | JavaFX legacy blockers + JAXB compile drift | 2026-05-20 | 60% | PR:TBD / Issue:TBD |
| HBTV-008 | Split deterministic unit vs integration tests | P1 | Todo | QA/Build Eng | HBTV-002 | Test ownership ambiguity | 2026-05-25 | 0% | PR:TBD / Issue:TBD |
| HBTV-009 | Migrate packaging away from JDK7 JavaFX paths | P2 | Todo | Desktop Lead | HBTV-007 | Packaging regression on Windows/Linux | 2026-06-15 | 0% | PR:TBD / Issue:TBD |
| HBTV-010 | Add CODEOWNERS + release/build policy docs | P2 | Todo | Eng Manager + Release Mgr | HBTV-005 | Policy drift | 2026-06-20 | 0% | PR:TBD / Issue:TBD |

## Active blockers

| Blocker ID | Description | Impacted items | Owner | Escalate by | Status |
|---|---|---|---|---|---|
| BLK-001 | Parent POM mismatch prevents module builds | HBTV-001,HBTV-002,HBTV-004 | Build/Release Eng | 2026-04-25 | Closed (2026-04-24) |
| BLK-002 | External HTTP repo returns 403 | HBTV-007 | Build/Release Eng | 2026-04-28 | Open |
| BLK-003 | Java 17 compile reveals JAXB-generated model/API mismatch in `core` | HBTV-007 | Architect + Build Eng | 2026-04-29 | Open |

## Decisions snapshot
- See `docs/decision-log.md`.

## Changelog (append-only)
- 2026-04-24T13:30:00Z — Initialized living tracker structure (DoR/DoD/escalation/backlog/blockers).
- 2026-04-24T13:35:00Z — Added initial backlog IDs, priorities, dependencies, and due dates.
- 2026-04-24T13:40:00Z — Synced tracker with modernization report v2 and marked Phase 0 active.
- 2026-04-24T16:10:00Z — Completed P0 backlog items HBTV-001/HBTV-002/HBTV-004; closed BLK-001.
- 2026-04-24T16:25:00Z — Completed HBTV-005: PR template + modernization/bug/feature issue templates added.
- 2026-04-24T18:05:00Z — Completed HBTV-006: added baseline dependency scan workflow and report artifact publication.
- 2026-04-24T19:20:00Z — Started HBTV-007: added `java17-compat` profile and Java 17 CI lane; discovered `core` compile blocker (JAXB-generated API mismatch).

## Next update trigger
Update this file after each of:
- merged PR,
- blocker status change,
- decision log entry,
- risk level change.
