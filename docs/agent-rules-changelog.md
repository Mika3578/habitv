# Agent rules changelog

Version history for Habitv AI agent rules. Canonical source: [`AGENTS.md`](../AGENTS.md).

## 2026-05-28 — v1.4.0

### Added

* Section 20 — Productivity and anti-bloat guardrails (minimum viable
  ruleset, rule profiles, docs-only lightweight path, heavy-gate scope,
  conflict precedence, rule budget, archive policy, speed/deep mode, stop
  condition, cleanup cadence, enforcement-over-expansion).
* `docs/agent-rule-profiles.md` — profile matrix and speed/deep mode detail.
* `docs/archived-agent-rules.md` — retired rules archive.
* `.cursor/rules/agent-productivity.mdc` — always-on profile guidance.

### Changed

* Section 16.2 — cross-reference to rule profiles (Section 20.2).

### Removed

* None (anti-bloat policy defers demotion to future cleanup PRs).

### Reason

* Rule surface grew across Sections 14–19; agents need scope control so
  docs-only work stays fast and detail stays in companion docs.

### Follow-up

* Propose `docs/cleanup-agent-rules` after ~10 merged PRs (Section 20.11).
* Shift effort to enforcement (CI, branch protection) per Section 20.12.

---

## 2026-05-28 — v1.3.0

### Added

* Section 19 — Habitv maintainability guardrails (ADR/decisions, DoD,
  reproducible builds, toolchains, Renovate/Dependabot strategy, dependency
  provenance, EditorConfig, flaky/network tests, XML config, user data,
  downloader safety, Windows-first, script parity, manual test evidence,
  risk levels, rollback, feature flags, logging, performance, UI, errors,
  worktrees, maintenance dashboard, maintainability report).
* `docs/maintainability-policy.md`, `docs/maintenance-dashboard.md`,
  `docs/adr/README.md`.
* `.cursor/rules/habitv-maintainability.mdc`.

### Changed

* `docs/dependency-policy.md` — provenance and Renovate/Dependabot notes.
* `scripts/AGENTS.md` — cross-platform and Windows-first rules.

### Removed

* None.

### Reason

* Second-level rules needed for reproducibility, config compatibility, test
  reliability, and decision tracking without bloating Section 18.

### Follow-up

* Propose `.editorconfig` in dedicated tooling PR (backlog).
* Populate `maintenance-dashboard.md` during planning reviews.

---

## 2026-05-28 — v1.2.0

### Added

* Section 18 — Habitv-specific modernization (phases, provider status matrix,
  metadata contract, provider testing, external tools, Java/JavaFX, security
  tooling lanes, workflow tiers, IDE setup, runtime diagnostics, obsolescence
  register, release readiness, modernization backlog, Habitv final report).
* `docs/provider-policy.md`, `docs/release-policy.md`,
  `docs/obsolescence-register.md`, `docs/modernization-backlog.md`.
* `.cursor/rules/habitv-providers.mdc`.

### Changed

* Section 17.13 phases now reference Section 18.1 canonical list.
* `docs/security-policy.md` — SBOM, OWASP, Scorecard, workflow tiers.
* `plugins/AGENTS.md`, `plugins.instructions.md` — Section 18 alignment.

### Removed

* None.

### Reason

* Habitv needs project-specific rules for providers, external tools, JavaFX
  wording, security tooling maturity, and release readiness — without
  duplicating generic gates in Sections 14–17.

### Follow-up

* Populate `obsolescence-register.md` from `provider-inventory.md` audits.
* Execute modernization backlog items in focused PRs.

---

## 2026-05-28 — v1.1.0

### Added

* Section 17 — evolutionary rules governance (lifecycle states, review
  cadence, improvement triggers, simplification, drift audit, retirement,
  automation maturity levels, rule scoring, experimental rules,
  progress-aware phases).
* Agent rules metadata block in `AGENTS.md` (version, last updated,
  related files).
* `docs/agent-rules-changelog.md` and `docs/agent-rules-backlog.md`.
* `.cursor/rules/README.md`, `00-canonical-source.mdc`, `90-rule-evolution.mdc`.
* `.github/instructions/docs.instructions.md`.

### Changed

* Workflow index extended with Section 17 and hard-rule ADR distinction
  (Section 13 vs Section 17).
* Cursor rules documentation maps legacy filenames to recommended numbered
  modules.

### Removed

* None.

### Reason

* Rules accumulated across multiple instruction files during modernization;
  governance was needed so rules evolve through reviewable PRs, stay
  auditable, and can be simplified or retired without silent drift.

### Follow-up

* Consider numbered Cursor rule renames (`10-git-safety.mdc`, etc.) in a
  dedicated tooling PR.
* Review experimental backlog items after 5 merged agent-rules PRs.

---

## 2026-05-28 — v1.0.0

### Added

* Initial consolidated AI agent rules: Sections 14–16 (manual validation
  gates, instruction governance, fast safe modernization).
* Cursor rules, Copilot instructions, nested `AGENTS.md` files.
* Companion docs: `dev-workflow.md`, `dependency-policy.md`,
  `security-policy.md`.
* VS Code extension recommendations and `settings.example.json`.
* `agent_space/` gitignore entry.

### Changed

* Tool-specific files aligned to reference `AGENTS.md` as canonical source.

### Removed

* None (first versioned baseline).

### Reason

* Replace ad hoc agent behavior with explicit, enforceable workflow gates
  while preserving manual commit/push/PR approval and Java 8 compatibility.

### Follow-up

* Add evolutionary governance (completed in v1.1.0).
