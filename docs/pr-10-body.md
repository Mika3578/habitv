# PR #10 — build: stabilize JAXB generation for core

## Scope
- Stabilize JAXB generation determinism for `application/core`.
- Keep generated-source output path explicit and stable.
- Align accessor usage with generated model updates in core.
- Document validation state and follow-up blockers.

## Out of scope for PR #10
- `application/trayView` JavaFX/toolchain modernization.
- JAXB runtime-provider fixes for tests/runtime (`com.sun.xml.bind.v2.ContextFactory`).
- Full isolation of the remaining network-dependent `TestListHttp` (tracked follow-up).
- Any work from PR #8.

## Final validation
- GitHub Actions build: ✅ Success on Java 8 Ubuntu and Windows.
- `mvn -B -ntp -DskipTests validate`: ✅ Success.
- `mvn -B -ntp -DskipTests compile`: ✅ Confirms `application/core` JAXB compile blocker is fixed in PR context; next blocker (outside this PR) is `application/trayView` missing JavaFX classes on modern/non-JDK8 toolchains.
- `mvn -B -ntp install`: ⚠️ Still blocked by known follow-ups: JAXB runtime provider in tests/runtime and the remaining network-dependent `TestListHttp`.

## Follow-up PRs
1. JavaFX/trayView compile blocker on modern/non-JDK8 toolchains.
2. JAXB runtime provider for tests/runtime.
3. Remaining network-dependent `TestListHttp` isolation from default install lifecycle.
