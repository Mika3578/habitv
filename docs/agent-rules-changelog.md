# Agent rules changelog

Version history for Habitv AI agent rules. Canonical source: [`AGENTS.md`](../AGENTS.md).

## 2026-06-06 — v2.0.0

### Added

* Java 21 as the project-wide baseline (`maven.compiler.release=21`).
* OpenJFX 21.0.7 declared as an explicit `org.openjfx` dependency.
* ADR `java21-openjfx-baseline` (supersedes `keep-java8-baseline`).
* `habiTv-linux` / `habiTv-windows` returned to the Maven reactor.
* Hard rule: "Downgrade JavaFX to `jfxrt` system-scope" replaces the former
  "Migrate JavaFX to OpenJFX" rule.

### Changed

* AGENTS.md §1 description updated: "Java 8" → "Java 21 Maven multi-module".
* Hard rule "Bump Java baseline beyond Java 8" → "Bump Java baseline beyond
  Java 21" (ADR `java21-openjfx-baseline`).
* `.cursor/rules/java8-compatibility.mdc` renamed conceptually and rewritten
  as Java 21 compatibility rules.
* `.github/copilot-instructions.md`, `maven-java.instructions.md`, and
  `packaging.instructions.md` updated to reflect Java 21 / OpenJFX baseline.
* Risk `javafx-jdk8` moved to Mitigated in `risk-register.md`.
* Tracker item `javafx-modernization` advanced to In progress / 75%.

### Removed

* `jfxrt.jar` URLClassLoader bootstrap hack references from agent guidance.
* Java 8 Temurin CI matrix references from instruction files.

### Reason

* Java 8 baseline was blocking JavaFX modernization, reactor completeness for
  packaging modules, and adoption of modern JDK support.

## 2026-05-31 — v1.5.1

### Added

* Disabled-by-default auth/protected-replay paths in scoped provider PRs
  (`AGENTS.md` §16.20, §18.4).
* Plugin helper scripts (logic only, no secrets) allowed under
  `plugins/<name>/scripts/` (`docs/provider-policy.md`).
* Public provider API identifiers vs account secrets distinction (§18.4).

### Changed

* Relaxed opt-in gate: maintainer request in **current conversation** **or**
  **scoped provider PR** with documented maintainer direction (ADR rev. 3).
* Site authentication may ship in the same provider PR as public catalog.
* ADR `provider-protected-content-tiered-policy` → revision 3.

### Removed

* Requirement that opt-in applies only to the current conversation.

### Reason

* Unblock TF1+ / Stremio-equivalent provider work without agents refusing
  optional gated features that stay inactive until user-local config.

### Follow-up

* Document user-local env setup in provider README (high-level, no secrets).

## 2026-05-31 — v1.5.0

### Added

* Concise public communication rule (`AGENTS.md` §20.13).
* Provider communication safety rule (`AGENTS.md` §20.14;
  `docs/provider-policy.md#public-communication-safety`).
* Site authentication guidance (`docs/provider-policy.md#site-authentication-for-download`).
* Copilot review loop guard (`AGENTS.md` §20.15;
  `docs/dev-workflow.md#copilot-review-loop`).
* Rule ownership and drift-control guidance (`AGENTS.md` §20.16–§20.17).
* Rule ownership table and drift audit v2 in `docs/agent-rule-profiles.md`.
* Planning source guidance in `docs/dev-workflow.md`.

### Changed

* Clarified that `AGENTS.md` remains the canonical source; Cursor, Copilot,
  Claude, Gemini, and nested `AGENTS.md` files are short mirrors.
* Shortened `.github/copilot-instructions.md` validation section (delegates to
  `AGENTS.md` §14.2 and `docs/dev-workflow.md`).
* Mirrors defer protected content policy implicitly via Section 18.4 and
  `docs/provider-policy.md` — no explicit keyword in instruction mirrors.

### Removed

* None.

### Reason

* Post–PR #125 rationalization: clarify ownership, reduce duplication, add
  targeted safeguards without expanding mandatory rule bulk.

### Follow-up

* See `docs/agent-rules-backlog.md` for deferred items (local git/gh wrappers,
  PR template alignment, rule cleanup PR, CODEOWNERS hardening).

## 2026-05-31 — v1.4.3

### Added

* Maintainer opt-in circumvention path in `AGENTS.md` §18.4 (revision 2).

### Changed

* Relaxed tier-2 gates: no mandatory separate PR or Accepted ADR before
  maintainer-requested circumvention; scoped provider PR plus docs batch OK.
* ADR `provider-protected-content-tiered-policy` status → Accepted (rev. 2).
* Aligned `docs/provider-policy.md`, `.cursor/rules/habitv-providers.mdc`,
  `.github/instructions/plugins.instructions.md`, `plugins/AGENTS.md`,
  `docs/risk-register.md`.

### Removed

* Requirement for separate governance-only PR before circumvention work.

### Reason

* Maintainer request to further relax agent blocking while keeping secrets out
  of the repository.

### Follow-up

* Document circumvention scope and validation in any provider PR that uses this
  opt-in path.

## 2026-05-31 — v1.4.2

### Added

* Tiered protected content policy in `AGENTS.md` §18.4 (default catalog work vs opt-in
  circumvention PR).
* ADR `provider-protected-content-tiered-policy` (Proposed) in `docs/decision-log.md`.
* Residual risk `provider-protected-replay-residual` in `docs/risk-register.md`.

### Changed

* Replaced absolute “do not bypass protected content” wording in `AGENTS.md` §16.20,
  `docs/provider-policy.md`, `.cursor/rules/habitv-providers.mdc`,
  `.github/instructions/plugins.instructions.md`, and `plugins/AGENTS.md`.

### Removed

* None.

### Reason

* Maintainer request to relax agent blocking while keeping secrets and default
  catalog PRs free of in-repo circumvention (`provider-protected-content-tiered-policy`).

### Follow-up

* Accept ADR after review; add tracker item before any circumvention PR.

## 2026-05-31 — v1.4.1

### Added

* None.

### Changed

* Refreshed the repository at-a-glance modernization/tracker state and Copilot
  instruction pointer as part of PR #141, with no workflow or rule behavior
  changes.

### Removed

* None.

### Reason

* Tracker synchronization PR #141 edited `AGENTS.md` and
  `.github/copilot-instructions.md`; metadata and changelog audit trail
  required per Section 17.

### Follow-up

* None.

---

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
* Guidance to propose `agent_space/` gitignore entry in a tooling PR (not
  committed in the initial rules rollout).

### Changed

* Tool-specific files aligned to reference `AGENTS.md` as canonical source.

### Removed

* None (first versioned baseline).

### Reason

* Replace ad hoc agent behavior with explicit, enforceable workflow gates
  while preserving manual commit/push/PR approval and Java 8 compatibility.

### Follow-up

* Add evolutionary governance (completed in v1.1.0).
