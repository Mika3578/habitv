# Packaging agent instructions

Extends the root [`AGENTS.md`](../AGENTS.md). When they conflict, the
root file wins.

## Scope

Packaging, installers, platform bundling, and staging scripts that
produce distributable artifacts.

## Path-specific rules

- No installer or binary scope creep unless explicitly requested.
- No platform packaging unless the task requires it.
- Do not add binaries (`.exe`, `.zip`, `.dll`, generated `.jar`) unless
  explicitly requested and developer-approved before staging.
- JavaFX packaging requires a Java 8 JDK with JavaFX; document
  constraints in validation evidence.
- Do not commit `target/` outputs, downloaded tools, or local logs.

## Validation

Document exact packaging commands, pass/fail results, and manual tests
for the produced artifact. Follow root `AGENTS.md` Section 15.15.

## Instruction loading

At task start, report **Instruction files loaded** including this file,
`.github/instructions/packaging.instructions.md`, and root `AGENTS.md`
Section 15.1.
