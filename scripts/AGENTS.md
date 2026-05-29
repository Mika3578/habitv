# Scripts agent instructions

Extends the root [`AGENTS.md`](../AGENTS.md). When they conflict, the
root file wins.

## Scope

This file applies to work under `scripts/` — maintenance, packaging
helpers, static-repo tooling, and security inventory scripts.

## Path-specific rules

- Do not commit secrets, tokens, local absolute paths, or credentials.
- Prefer deterministic, documented commands; state OS assumptions
  (PowerShell vs bash) in script headers or companion docs.
- Do not re-enable runtime calls to legacy hosts (`dabiboo.free.fr`,
  `ftpperso.free.fr`) without explicit tracker approval.
- Keep script changes scoped; do not mix unrelated refactors.
- No destructive scripts without explicit developer approval.
- Maintain PowerShell/Bash parity when a script has both `.ps1` and
  `.sh` variants.
- When a script affects packaging or deployment layout, coordinate with
  `packaging/AGENTS.md` and `.github/instructions/packaging.instructions.md`.
- **Cross-platform parity:** if both `.sh` and `.ps1` exist, update both or
  explain why not (Section 19.14).
- **Windows-first:** quote paths with spaces; do not assume Linux-only
  behavior (Section 19.13).

## Validation

- Shell/Python script changes: run the script in a safe dry-run or
  document why execution was skipped.
- If scripts invoke Maven or touch POMs indirectly, follow root
  `AGENTS.md` Section 14.2.

## Instruction loading

At task start, report **Instruction files loaded** including this file
and root `AGENTS.md` Section 15.1.
