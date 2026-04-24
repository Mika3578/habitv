# Habitv Decision Log

| Date (UTC) | Decision ID | Decision | Rationale | Impact | Status |
|---|---|---|---|---|---|
| 2026-04-24 | ADR-001 | Stabilize Maven topology before dependency/toolchain upgrades | Current hard blockers prevent reliable baseline measurements | Enables reproducible build foundation | Accepted |
| 2026-04-24 | ADR-002 | Use lightweight docs-first governance (tracker + risks + decisions) before adding heavy process | Team currently has no CI/rules templates; minimal process needed immediately | Improves execution traceability with low overhead | Accepted |
| 2026-04-24 | ADR-003 | Prefer incremental modernization over rewrite | Legacy architecture spans CLI/UI/packaging; rewrite risk unjustified | Reduces migration risk and delivery disruption | Accepted |
| 2026-04-24 | ADR-004 | Complete P0 foundation as Maven topology + reactor + baseline CI | Finishing blocking infrastructure first unlocks measurable modernization flow | P0 build governance and CI baseline are now codified | Accepted |
| 2026-04-24 | ADR-005 | Introduce non-blocking baseline dependency scanning in CI | Immediate visibility into dependency risk is needed, but legacy findings are expected | Security telemetry begins now without destabilizing PR velocity | Accepted |
| 2026-04-24 | ADR-006 | Validate reactor on Java 17 via dedicated Maven compatibility profile | Runtime/toolchain modernization needs continuous signal before full packaging migration | Adds incremental Java 17 confidence while isolating JavaFX packaging blockers | Accepted |

## Update rule
Add one row whenever scope, sequencing, or standards change materially.
