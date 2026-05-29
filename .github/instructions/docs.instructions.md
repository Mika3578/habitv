---
applyTo: "docs/**,*.md,AGENTS.md,CONTRIBUTING.md,CHANGELOG.md"
---

# Habitv documentation instructions (Copilot)

Extends `AGENTS.md` and `docs/AGENTS.md`. Do not contradict them.

## Applies to

Documentation under `docs/`, root policy files, and Markdown contributing
guides.

## Extends AGENTS.md

- Section 16.21 — documentation freshness; verify claims against repo state.
- Section 17 — rule changes need changelog + version bump.
- Section 20 — docs-only speed mode; no Maven unless build/runtime docs
  change; prefer backlog over new mandatory rules.
- Section 15.20 — no blind staging; explicit paths only.

## Validation expected

- `git diff --check` for docs-only PRs.
- No runtime claims without source or tracker reference.
- Rule doc changes: drift audit + changelog entry.

## Do not change

- Java source, POMs, or workflows in a docs-only PR without reclassification.
- Do not document future Java/support as current without CI proof.
- Do not add AI attribution (Section 15.10).

## Agent rules docs

When editing `AGENTS.md` or agent instruction files, update
`docs/agent-rules-changelog.md` and bump metadata version.
