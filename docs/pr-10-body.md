# build: stabilize JAXB generation for core

## Summary

PR #10 stabilizes JAXB generation usage in `application/core` and aligns documentation so code, tracker status, and risk follow-ups land together in one linear history.

## Root cause

`application/core` consumed JAXB-generated types/accessors that drifted from the current generated model surface, causing compile-time symbol/accessor mismatches when the reactor reached core generation consumers.

## What changed

- Stabilized JAXB generation/accessor usage for `application/core` in PR #10 code.
- Folded useful documentation updates from duplicate PR #11/#12 directly into this PR branch.
- Added explicit follow-up tracker items for:
  - JavaFX/trayView compile blocker (`HBTV-007a`)
  - JAXB runtime provider gap (`HBTV-007b`)
  - Remaining network-dependent `TestListHttp` (`HBTV-007c`)
- Updated risk register entries for JAXB drift, JavaFX toolchain gap, JAXB runtime provider gap, and remaining network-dependent test risk.

## Validation results

- `mvn -B -ntp -DskipTests validate`: **Success**.
- `mvn -B -ntp -DskipTests compile`: **PR #10 fixes the application/core JAXB generation/accessor compile blocker. The next compile blocker is application/trayView because JavaFX classes are missing from the current toolchain.**
- `mvn -B -ntp install`: **mvn install remains blocked by JAXB runtime provider/test-runtime setup and remaining network-dependent TestListHttp.**

## Known remaining blockers

- `application/trayView` requires JavaFX classes not present in the current toolchain.
- JAXB runtime provider setup for tests/runtime is still incomplete.
- Remaining network-dependent `TestListHttp` is still in the default install path.

## Follow-up PRs

1. Resolve `application/trayView` JavaFX compile blocker (`HBTV-007a`).
2. Add JAXB runtime provider for tests/runtime (`HBTV-007b`).
3. Isolate `TestListHttp` from default install lifecycle (`HBTV-007c`).
