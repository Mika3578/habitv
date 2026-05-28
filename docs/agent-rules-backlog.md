# Agent rules backlog

Future AI rule improvements. Do not bloat `AGENTS.md` — track candidates
here. Canonical source: [`AGENTS.md`](../AGENTS.md) Section 17.

## Candidate rules

### Numbered Cursor rule module rename

* **Problem:** Legacy Cursor rule filenames (`git-safety.mdc`, etc.) differ
  from recommended numbered modules (`10-git-safety.mdc`).
* **Proposed rule:** Rename modules in a dedicated PR; keep README mapping
  until complete.
* **Priority:** P3
* **Target file:** `.cursor/rules/`
* **Trigger:** dedicated tooling PR
* **Status:** proposed

### VS Code tasks.json safe Maven/git tasks

* **Problem:** No shared tasks for validate/test without commit/push actions.
* **Proposed rule:** Add `.vscode/tasks.json` per Section 16.16 in tooling PR.
* **Priority:** P3
* **Target file:** `.vscode/tasks.json`
* **Trigger:** developer request or tooling PR
* **Status:** proposed

### CodeQL workflow for Java 8 Maven

* **Problem:** CodeQL referenced in governance docs but workflow may not
  exist yet.
* **Proposed rule:** Add CodeQL workflow with Java 8 build mode; document in
  `security-policy.md`.
* **Priority:** P2
* **Target file:** `.github/workflows/`
* **Trigger:** CI hardening phase
* **Status:** proposed

## Experimental rules

### Targeted provider fixture requirement

* **Start date:** (not started)
* **Reason:** reduce live-network-only validation for plugin PRs
* **Success criteria:** plugin PRs include offline fixture tests when parser
  logic changes
* **Review after:** 5 plugin PRs
* **Owner:** repository maintainer
* **Rollback condition:** fixtures add more noise than value

## Deprecated rules to remove later

*(none yet)*

### Propose root `.editorconfig`

* **Problem:** No repository-wide EditorConfig; IDE formatting may drift.
* **Proposed rule:** Add `.editorconfig` in dedicated tooling PR (19.7).
* **Priority:** P3
* **Target file:** `.editorconfig`
* **Trigger:** tooling PR
* **Status:** proposed

## Recently completed

* v1.3.0 maintainability guardrails — see changelog
* v1.1.0 evolutionary governance — see [`agent-rules-changelog.md`](agent-rules-changelog.md)
