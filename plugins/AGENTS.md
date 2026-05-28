# Plugin agent instructions

Extends the root [`AGENTS.md`](../AGENTS.md). When they conflict, the
root file wins.

## Scope

This file applies to work under `plugins/` — provider, downloader, and
export plugins plus `plugin-tester`.

Full policy: [`docs/provider-policy.md`](../docs/provider-policy.md) and
`AGENTS.md` Section 18.

## Path-specific rules

- Declare Habitv **phase** before editing (Section 18.1).
- Do not rewrite providers without a scoped tracker item and ADR.
- Update provider **status** when changing behavior (Section 18.2).
- Preserve/improve **metadata** when validated (Section 18.3).
- Plugin version bumps must match `plugin-versioning-policy` triggers in
  [`CONTRIBUTING.md`](../CONTRIBUTING.md).
- Internal reactor deps in a bumped plugin MUST use
  `${project.parent.version}`, never `${project.version}`.
- Prefer offline fixtures and opt-in network tests; do not add live
  provider tests to the default lifecycle.
- Do not rely on live-network-only validation as proof of correctness.
- Distinguish offline unit tests, live/network tests, and manual tests
  (Section 15.24).
- If live tests are skipped (geoblocking, auth, DRM, rate limits), state
  why clearly.
- Keep changes scoped to one provider or plugin module when possible.
- No secrets, binaries, or manual JAXB/XML generated-file edits unless
  explicitly required (root `AGENTS.md` Sections 15.9, 15.12–15.13).

## Validation

When Java or POM files under `plugins/` change, run root `AGENTS.md`
Section 14.2 validation plus targeted module tests:

```bash
mvn -B -ntp -pl plugins/<module> -am test
```

## Instruction loading

At task start, report **Instruction files loaded** including this file
and root `AGENTS.md` Section 15.1.
