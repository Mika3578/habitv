# HabiTV Agent Instructions

Canonical **project** policy for coding agents. Humans:
[`CONTRIBUTING.md`](CONTRIBUTING.md). Build/architecture:
[`docs/development.md`](docs/development.md),
[`docs/architecture.md`](docs/architecture.md).

Context-economy habits belong in Cursor **User Rules** (global). Do not
duplicate them here.

Portable procedures: [`.agents/skills/`](.agents/skills/). Cursor file
rules: [`.cursor/rules/`](.cursor/rules/). Adapter overview:
[`docs/development.md`](docs/development.md#agent-instructions).

## Repository

Maven multi-module replay app (`fwk/`, `application/`, `plugins/`).
Remote: `Mika3578/habitv`. Integration branch: `develop`.

Build/runtime baseline: **Java 8**. Modernization target: **Java 21**
(then 25). Do not treat 21/25 as supported until compiler and required CI
change. Module-scoped migrations only — one concern per task.

Priorities: CI health → retrieval diagnostics → yt-dlp → providers →
JDK/packaging migration.

## Hard constraints

- Stay on Java 8 unless the task is an explicit JDK migration.
- Personal-use download scope and provider rules: [`docs/providers.md`](docs/providers.md).
- Prefer offline fixtures; live tests only with `-Plive-provider-tests`.
- No secrets, tokens, credentials, or machine paths in git. Scratch:
  `agent_space/` (gitignored).
- English, short, generic **public git text** — procedure:
  [`.agents/skills/public-git-text/SKILL.md`](.agents/skills/public-git-text/SKILL.md).
- Never work directly on `develop` / `main` / `master`. Branching:
  [`.agents/skills/git-workflow/SKILL.md`](.agents/skills/git-workflow/SKILL.md).
- PRs: base `develop`, repo `Mika3578/habitv`. Lifecycle/orchestration:
  [`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md)
  (load only for PR work).
- Default validate: `mvn -B -ntp -DskipTests validate`. Targeted:
  `mvn -B -ntp -pl <module> -am test`. Docs-only: `git diff --check`.
- Tool adapters (`.cursor/`, `.continue/`, `.github/copilot-instructions.md`)
  stay thin and point here. No nested `AGENTS.md` / competing constitutions.

## Routing

| Topic | Where |
|-------|--------|
| Java / Maven / CI | [`docs/development.md`](docs/development.md) |
| Providers | [`docs/providers.md`](docs/providers.md) |
| Modernization | [`docs/modernization.md`](docs/modernization.md) + [modernization-step](.agents/skills/modernization-step/SKILL.md) |
| Public git text | [public-git-text](.agents/skills/public-git-text/SKILL.md) |
| Branch / worktree | [git-workflow](.agents/skills/git-workflow/SKILL.md) |
| Validation gate | [code-change-verification](.agents/skills/code-change-verification/SKILL.md) |
| Provider diagnostics | [provider-diagnostics](.agents/skills/provider-diagnostics/SKILL.md) |
| PR orchestration | [pr-review](.agents/skills/pr-review/SKILL.md) |
| Bug investigation | [investigate-bug](.agents/skills/investigate-bug/SKILL.md) |
| Single-PR fix | [fix-pr](.agents/skills/fix-pr/SKILL.md) |
