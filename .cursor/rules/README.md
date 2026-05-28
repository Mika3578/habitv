# Cursor rules index

Canonical source: [`AGENTS.md`](../../AGENTS.md). Cursor rules **extend** it;
they must not contradict it or hold unique critical policy.

## Purpose

Modular, path-scoped rules for Cursor agents. Update through dedicated
AI-rules PRs with changelog entry (Section 17).

## Module map

| File | Recommended # | Applies | AGENTS.md |
|------|---------------|---------|-----------|
| `00-canonical-source.mdc` | 00 | always | metadata, §17 |
| `habitv-master.mdc` | — | always | §1–2, §16 |
| `git-safety.mdc` | 10 | always | §14, §15.5–15.8, §15.20 |
| `maven-validation.mdc` | 20 | Java/POM | §6, §14.2, §16.11 |
| `java8-compatibility.mdc` | 30 | Java/POM | §2, §14.9, §16.10 |
| `pr-review.mdc` | 40 | PR/docs | §5, §14.4, §15.16–15.19 |
| `pr-style.mdc` | 40 | PR metadata | §5, §14.12 |
| `dependencies.mdc` | 50 | POM/deps | §16.3–16.6 |
| `workflows.mdc` | 60 | `.github/workflows` | §16.8–16.9 |
| `cursor-ide.mdc` | — | `.vscode`, `.cursor` | §16.13–16.16 |
| `agent-productivity.mdc` | — | always | §20 |
| `habitv-maintainability.mdc` | — | code/build/config | §19 |
| `habitv-providers.mdc` | 70 | `plugins/**` | §18 |
| `90-rule-evolution.mdc` | 90 | AI-rule PRs | §17 |

Path-specific: `plugins/AGENTS.md`, `.github/AGENTS.md`, etc.

## Updating rules

1. Change `AGENTS.md` first.
2. Update related `.mdc` / Copilot files.
3. Bump version metadata and `docs/agent-rules-changelog.md`.
4. Run **Rule drift audit** (Section 17.6).
5. End with **Agent rules update report** (Section 17.14).

## Lifecycle

Rules may be **mandatory**, **recommended**, **experimental**,
**deprecated**, or **removed** (Section 17.2).
