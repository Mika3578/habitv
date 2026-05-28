# GitHub workflow agent instructions

Extends the root [`AGENTS.md`](../AGENTS.md). When they conflict, the
root file wins.

## Scope

This file applies to work under `.github/` — workflows, branch protection
docs, PR templates, Copilot instructions, and repository governance.

## Path-specific rules

- PRs must target `Mika3578/habitv` → `develop` (root `AGENTS.md`
  Section 14.10).
- Keep `.github/copilot-instructions.md` and
  `.github/instructions/*.instructions.md` aligned with root `AGENTS.md`.
- When changing AI workflow rules, run the rule drift check (root
  `AGENTS.md` Section 15.3).
- Do not use `secrets: inherit` in workflows unless explicitly justified
  and documented.
- Be aware of branch protection and required checks in
  `docs/repository-governance.md` (Section 15.17).
- Inspect `.github/CODEOWNERS` when governance files change (Section 15.19).
- Verify PR status checks before declaring ready; never say "CI is green"
  without verification (Section 15.18).
- Do not suggest bypassing branch protection unless developer asks for
  admin guidance.
- Ask approval before PR comments, labels, review requests, or resolution
  (Section 15.25).
- Fill every required section of `.github/pull_request_template.md`.
- No chained approval-gated commands (Section 15.8).

## Instruction loading

At task start, report **Instruction files loaded** including this file,
`.github/copilot-instructions.md`, `.github/instructions/github-actions.instructions.md`,
and `.github/instructions/github-pr.instructions.md`.

## Fast modernization

Follow Section 16 for CI/workflow changes: provide Workflow safety
checklist (16.8), least-privilege permissions, no auto-merge for deps.
## Copilot and review

Follow root `AGENTS.md` Sections 14.4 and 15.7 for Copilot and GitHub
review resolution. Never resolve conversations silently.
