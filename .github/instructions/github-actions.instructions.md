---
applyTo: ".github/workflows/**,.github/actions/**"
---

# Habitv GitHub Actions instructions (Copilot)

Extends `AGENTS.md` and `.github/AGENTS.md`. Do not contradict them.

## Scope

GitHub Actions workflows and related CI configuration under `.github/`.

## Rules

- Do not weaken branch protection or required checks without explicit
  maintainer approval.
- Do not use `secrets: inherit` unless explicitly justified and
  documented.
- Never commit secrets, tokens, or credentials in workflow files.
- Keep workflow changes scoped; document validation commands run.
- Align AI instruction files with root `AGENTS.md` when changing
  governance docs.

## Validation

For workflow-only changes:

- verify YAML syntax manually or with `actionlint` if available;
- complete **Workflow safety checklist** (Section 16.8);
- verify required checks remain aligned with `docs/required-checks-roadmap.md`
  when applicable;
- state whether CI was run locally or why it was skipped;
- use least-privilege `permissions: contents: read` by default.

For code-impacting CI changes, follow root `AGENTS.md` Section 14.2.

## Safety

No chained commands that bypass commit/push/PR approval. No AI
attribution in commits or PR bodies. See Section 16.7–16.9 for CodeQL,
dependency review, and CI speed rules.
