---
name: investigate-bug
description: >-
  Diagnose a specific HabiTV bug with minimal context. Use when the user
  reports a symptom, stack trace, or failing behavior to root-cause and fix.
disable-model-invocation: true
---

# Investigate bug

One bug, minimal context. Do not turn this into a repo-wide audit.

## Workflow

1. Reproduce or clearly restate the symptom (error, module, steps).
2. Search for the relevant symbols, modules, and call sites only.
3. Read only the files/sections needed for that path.
4. Identify the root cause before editing.
5. Apply the smallest coherent fix in scope.
6. Run targeted validation (`mvn -B -ntp -pl <module> -am test` or narrower).
7. Broaden search or tests only if the targeted run fails or the cause is
   still unclear.
8. Stop when the reported bug is fixed and validated. Report unrelated
   issues separately.

## Notes

- Stay on the Java 8 baseline unless the bug is inside an explicit migration.
- Prefer offline fixtures for provider issues; see
  [provider-diagnostics](../provider-diagnostics/SKILL.md) when listing or
  retrieval is involved.
- Policy: [`AGENTS.md`](../../../AGENTS.md).
