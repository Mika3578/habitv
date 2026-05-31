# Agent rules backlog

Future AI rule improvements. Do not bloat `AGENTS.md` — track candidates
here. Canonical source: [`AGENTS.md`](../AGENTS.md) Sections 17 and 20.

When unsure about a new rule, add here instead of making it mandatory
(Section 20.10).

## Enforcement over expansion (Section 20.12)

Prefer implementing guardrails through tooling rather than new text:

| Gap | Enforcement target | Status |
|-----|-------------------|--------|
| Commit/push without approval | branch protection, human review | partial |
| Maven validate on code PRs | CI required check | exists |
| Dependency security | Dependabot + Dependency Review | exists |
| CodeQL Java 8 | workflow (see candidate below) | proposed |
| Small PR size | review convention | partial |
| Provider offline fixtures | CI policy (future) | proposed |

## Candidate rules

### Embedded browser for provider login (CEF / JavaFX)

* **Problem:** Some providers (TF1+, etc.) require a web login before replay
  URLs are available; Habitv has no in-app browser today.
* **Proposed rule:** Dedicated tracker + ADR before any JavaFX/CEF integration;
  reference `habitv-references/captvty` (CefSharp) and
  `habitv-references/TF1-Downloader` (Selenium) for patterns only.
* **Priority:** P3
* **Target file:** `application/trayView` or new tooling module
* **Trigger:** maintainer request after `docs/provider-policy.md` auth section
* **Status:** proposed

### Optional local git/gh wrappers for Cursor automation

* **Problem:** Agents prepare commits/PRs but developer runs Git/GitHub manually;
  no shared safe wrapper scripts for common validate-then-diff flows.
* **Proposed rule:** Optional `scripts/agent-git-*.ps1` / `.sh` helpers in a
  dedicated tooling PR; never auto-commit or auto-push.
* **Priority:** P3
* **Target file:** `scripts/`
* **Trigger:** developer request
* **Status:** proposed

### PR template alignment with concise communication

* **Problem:** PR template sections may encourage verbose bodies inconsistent
  with §20.13.
* **Proposed rule:** Align `.github/pull_request_template.md` with
  `docs/dev-workflow.md#concise-communication` in a dedicated docs PR.
* **Priority:** P3
* **Target file:** `.github/pull_request_template.md`
* **Trigger:** after v1.5.0 merge
* **Status:** proposed

### Future rule cleanup PR (`docs/cleanup-agent-rules`)

* **Problem:** Mirror files may accumulate duplicate bullets over time.
* **Proposed rule:** Periodic cleanup per Section 20.11; archive retired wording
  in `docs/archived-agent-rules.md`.
* **Priority:** P3
* **Target file:** `.cursor/rules/`, `.github/instructions/`, nested `AGENTS.md`
* **Trigger:** ~10 merged PRs after v1.5.0
* **Status:** proposed

### Optional CODEOWNERS / rulesets / security hardening

* **Problem:** Some gates documented in rules are not yet fully enforced in
  GitHub UI.
* **Proposed rule:** Dedicated governance PR for CODEOWNERS, rulesets, and
  required checks — not mixed with provider or rule-text PRs.
* **Priority:** P2
* **Target file:** `.github/CODEOWNERS`, repository settings docs
* **Trigger:** `branch-protection` tracker item
* **Status:** proposed

### Provider communication review checklist

* **Problem:** v1.5.0 adds policy text but no optional PR checklist artifact.
* **Proposed rule:** Add checklist to `docs/provider-policy.md` or PR template
  if repeated review misses occur.
* **Priority:** P3
* **Target file:** `docs/provider-policy.md` or PR template
* **Trigger:** after first provider PR using §20.14
* **Status:** proposed

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

* v1.5.0 rule governance streamline — see changelog
* v1.4.0 productivity and anti-bloat guardrails — see changelog
* v1.3.0 maintainability guardrails — see changelog
* v1.1.0 evolutionary governance — see [`agent-rules-changelog.md`](agent-rules-changelog.md)
