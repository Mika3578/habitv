# build: stabilize JAXB generation for core

## Summary
This PR stabilizes JAXB generation usage in `application/core` and removes the core JAXB generation/accessor mismatch that blocked compile validation in the target Java 8 CI lanes.

## Root cause
`application/core` relied on generated JAXB types/accessors that drifted from the current generated model surface, producing compile-time symbol and API mismatch failures during reactor builds.

## What changed
- Updated JAXB/core stabilization changes in PR #10 (code branch: `codex/fix-jaxb-generation-compile-blocker`).
- Folded in documentation updates originally drafted in PR #11 so build status, tracker state, and risk reporting stay in one linear PR history.
- Synchronized tracker/risk wording to keep HBTV-007 **In Progress** and to explicitly track post-core follow-up blockers.

## Validation results
- GitHub Actions Java 8 Ubuntu/Windows: success.
- `mvn -B -ntp -DskipTests validate`: success.
- `mvn -B -ntp -DskipTests compile`: core JAXB blocker fixed; next unrelated blocker is `application/trayView` JavaFX.
- `mvn -B -ntp install`: still blocked by JAXB runtime provider and `TestListHttp` network dependency.

## Known remaining blockers
application/core JAXB generation/accessor mismatch is fixed by PR #10. The next compile blocker is application/trayView because JavaFX classes are missing from the current toolchain.

For `mvn install`, install is still blocked by JAXB runtime provider tests and remaining network-dependent TestListHttp.

## Follow-up PRs
- fix JAXB runtime provider for tests/runtime
- isolate remaining TestListHttp network test
- fix application/trayView JavaFX compile path
- rebase and finalize PR #8
- bootstrap GitHub Pages update repository support with habitv-repo
