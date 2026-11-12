---
name: public-git-text
description: >-
  Writes short generic git branches, commit subjects, and GitHub pull request
  titles and bodies. Use when creating or renaming a branch, writing a commit,
  opening or editing a pull request, or drafting a PR title, body, or review
  comment.
---

# Public git text

Project pointer: [`AGENTS.md`](../../../AGENTS.md). This skill holds the
procedure and examples (loaded only when publishing git/PR text).

## Rules

- English only. Detail belongs in the diff, not in public git text.
- Keep wording **implementation-neutral** and user-facing. No AI/tool
  footers or auto-summaries authored by agents. Third-party appended
  blocks are tolerated and are not Ready/merge blockers.
- Do not publish negated topic checklists in public git text.

## Procedure

1. Apply this skill before any branch, commit, or PR text.
2. Fill [`.github/pull_request_template.md`](../../../.github/pull_request_template.md)
   for PR bodies.
3. Record validation as the exact command and honest result (no log dumps).
4. **Review comments:** one or two short sentences; fix or reject with
   evidence. Do not restate the diff or narrate runtime/provider behavior.
5. On an existing PR whose head is a platform `cursor/...` branch: keep
   title/body generic; do not rename the PR head on GitHub (closes the PR).

## Branches

Format: `<type>/<short-scope>` kebab-case. Types: `feat`, `fix`, `docs`,
`test`, `refactor`, `chore`, `ci`. Scope = module or topic only. No tool
prefixes (`cursor/`, `claude/`, `ai/`, …). No `replay` / `download` /
diagnostic suffixes.

Name correctly at creation. Do not rename an open PR head on GitHub.

| Avoid | Use |
|-------|-----|
| `cursor/lemanbleu-replay-1a2e` | `feat/lemanbleu` |
| `fix/clubic-shorts-listing` | `fix/clubic` |
| `fix/download-daemon-resilience` | `fix/background-daemon` |

Cloud Agents may get a platform-prefixed branch before policy loads —
recovery: [git-workflow](../git-workflow/SKILL.md).

## PR titles and commits

Conventional Commits, required scope, imperative, ≤ 72 characters. Scope
is lowercase kebab-case (`lemanbleu`, `rtbf-auvio`), not Maven camelCase.

| Avoid | Use |
|-------|-----|
| `feat(tf1plus): add replay provider` | `feat(tf1plus): add provider` |
| `fix(6play): diagnose obsolete SPA listing and access limits` | `fix(6play): diagnose listing` |

**PR bodies:** template sections only; Summary/Changes/Notes generic;
Validation = command + result. No strategy or access-model sections.
