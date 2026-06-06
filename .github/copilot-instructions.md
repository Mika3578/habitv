# GitHub Copilot instructions for Habitv

Habitv is a Java 21 Maven multi-module app for French TV catch-up via
pluggable provider plugins. Treat the codebase as production legacy: prefer
narrow, conservative suggestions over rewrites.

## Source of truth

`AGENTS.md` is the **canonical** source for mandatory AI agent rules.
These files are **mirrors only** — they must not introduce unique mandatory
rules (`AGENTS.md` Section 20.16):

- `.cursor/rules/*.mdc` — topic summaries
- `.github/instructions/*.instructions.md` — path-specific extensions
- nested `AGENTS.md` — scoped extensions
- `CLAUDE.md`, `GEMINI.md` — pointers
- `docs/dev-workflow.md`, `docs/agent-rule-profiles.md` — detail and ownership

When any file conflicts with `AGENTS.md`, `AGENTS.md` wins.

At task start: **Instruction files loaded** (Section 15.1). Pick a rule
profile (Section 20.2).

## Workflow gates (summary)

- Never commit, push, create/merge PRs, or resolve review threads without
  explicit developer approval in the **current** conversation (Section 14).
- Never chain approval-gated commands (Section 15.8).
- Stage only intentional paths (Section 15.20); no secrets (Section 15.9);
  no AI attribution (Section 15.10).
- Rebase on `origin/develop` only; `--force-with-lease` after rebase with
  approval — never plain `--force`.
- PR target: `Mika3578/habitv` → `develop` (Section 14.10).

Details: [`docs/dev-workflow.md`](../docs/dev-workflow.md).

## Validation

Pre-commit tiers: `AGENTS.md` Section 14.2 and
[`docs/dev-workflow.md`](../docs/dev-workflow.md#validation).

Default CI-safe baseline (not sufficient alone for commit approval):

```bash
mvn -B -ntp -DskipTests validate
```

Do not require root `clean package` for every code change. Do not add live
network tests to the default lifecycle.

## Review and communication

- Copilot review loop: Section 20.15; details in
  [`docs/dev-workflow.md`](../docs/dev-workflow.md#copilot-review-loop).
- Concise PR/commit text: Section 20.13.
- Provider public text: Section 20.14 → [`docs/provider-policy.md`](../docs/provider-policy.md).

Ask approval before PR comment actions (Section 15.25).

## Habitv modernization (pointers)

- Providers: Section 18, [`docs/provider-policy.md`](../docs/provider-policy.md)
- Maintainability: Section 19, [`docs/maintainability-policy.md`](../docs/maintainability-policy.md)
- Profiles / drift: Section 20, [`docs/agent-rule-profiles.md`](../docs/agent-rule-profiles.md)
- Planning: [`docs/dev-tracker.md`](../docs/dev-tracker.md), [`docs/maintenance-dashboard.md`](../docs/maintenance-dashboard.md)

## Key modules

`fwk/api`, `fwk/framework`, `application/core`, `application/consoleView`,
`application/trayView`, `application/habiTv`, `plugins/*`, `plugins/plugin-tester`.

## Things to avoid suggesting

- Jakarta JAXB migration, JavaFX replacement, `youtube-dl` → `yt-dlp` rewrites
  (tracked items exist).
- OWASP/SBOM/static-analysis plugins in this phase.
- Broad reformatting or opportunistic refactors.
- Plugin version bumps without `plugin-versioning-policy` trigger
  (`CONTRIBUTING.md`).

## Things to encourage

- Small scoped diffs; English in new code and comments.
- Meaningful doc sync: `dev-tracker.{md,json}`, `risk-register.md`,
  `decision-log.md` when state changes (Section 12).
- AI rule changes: version bump, changelog, drift audit v2 (Section 20.16).
