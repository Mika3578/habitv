# GitHub Copilot instructions for Habitv

Habitv is a legacy Java 8 Maven multi-module application for downloading
French TV catch-up content via pluggable provider plugins. Modernization
is staged through tracker items in `docs/dev-tracker.md` (last refresh
2026-05-31 on `develop`). Supported **runtime** remains Java 8; planned
LTS **runtime target** is Java 21, then Java 25 — see
`docs/java-runtime-policy.md`.

Treat the codebase as production legacy: prefer conservative,
narrowly-scoped suggestions over rewrites.

## Workflow policy source of truth

`AGENTS.md` is the canonical source for all AI agent behavior. These
companion files mirror or point to the same mandatory rules:

- `.cursor/rules/git-safety.mdc` — Git workflow and approval gates
- `.cursor/rules/maven-validation.mdc` — pre-commit Maven validation
- `.cursor/rules/pr-review.mdc` — PR policy and Copilot review handling
- `.cursor/rules/java8-compatibility.mdc` — Java 8 and dependency rules
- `.github/instructions/maven-java.instructions.md` — Java/POM path rules
- `.github/instructions/plugins.instructions.md` — plugin path rules
- `.github/instructions/github-actions.instructions.md` — GitHub Actions
- `.github/instructions/github-pr.instructions.md` — PR/GitHub path rules
- `.github/instructions/packaging.instructions.md` — packaging path rules
- nested `AGENTS.md` files — path-specific extensions (Section 15.2)
- `docs/dev-workflow.md`, `docs/dependency-policy.md`, `docs/security-policy.md`
- `docs/provider-policy.md`, `docs/release-policy.md`, `docs/modernization-backlog.md`
- `.cursor/rules/habitv-providers.mdc`, `90-rule-evolution.mdc`

When any file conflicts with `AGENTS.md`, `AGENTS.md` wins.

At task start, report **Instruction files loaded** (Section 15.1).

When changing AI rules, run drift audit (Section 17.6), update changelog,
and bump `AGENTS.md` metadata version (Section 17).

Never chain approval-gated commands (Section 15.8). Stage only intentional
files by path (Section 15.20). Inspect diffs for secrets (Section 15.9).
No AI attribution (Section 15.10). Provide exact validation evidence
(Section 15.15). Verify status checks before PR merge readiness (15.18).
Ask approval before PR comment actions (Section 15.25).

## Fast safe modernization

See `AGENTS.md` Section 16 and `docs/dev-workflow.md`. Classify tasks
before editing (16.2). Prefer small focused PRs. Security fixes outrank
cosmetic work (16.19). Dependency updates need focused PRs and the
Dependency update report (16.3).

## Rule evolution

See Section 17. AI rules change through dedicated PRs with changelog entry.
Automation maturity: Level 1 — Assisted.

## Habitv-specific modernization

See Section 18 and `docs/provider-policy.md`. Declare phase before work.
Provider status, metadata, offline tests, external tools, Java/JavaFX,
release readiness — see companion docs in metadata block.

## Maintainability

See Section 19 and `docs/maintainability-policy.md`. Declare Definition of
Done; classify PR risk; document ADRs for durable decisions; manual test
evidence before commit on behavior changes.

## Productivity and anti-bloat

See Section 20 and `docs/agent-rule-profiles.md`. Pick a rule profile at
task start; use speed mode for low-risk docs-only work; prefer backlog
over new mandatory rules.

AI agents must never commit, push, create PRs, merge PRs, or resolve
review threads without explicit developer approval in the current
conversation. See `AGENTS.md` Section 14. Never resolve GitHub review
conversations silently (Section 15.7).

## Compatibility constraints

See `.github/instructions/maven-java.instructions.md` and
`.cursor/rules/java8-compatibility.mdc` for Java 8 and Maven constraints.

## Key modules

- `fwk/api` — public API contracts shared by core and plugins.
- `fwk/framework` — base utilities, retriever, updater helpers.
- `application/core` — config, DAOs, JAXB-bound entities, updater.
- `application/consoleView` — CLI front end.
- `application/trayView` — JavaFX 2.x tray UI.
- `application/habiTv` — application bundling entry point.
- `plugins/*` — provider, downloader, and tool wrapper plugins
  (e.g. youtube, ffmpeg, curl, RSS, canalPlus, arte, pluzz, 6play).
- `plugins/plugin-tester` — shared test harness for plugin modules.

## Default validation

Pre-commit validation for AI agents is defined in `AGENTS.md` Section
14.2 (tiered). Do **not** require root `clean package` for every code change.

**Standard code** tier:

```
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test   # when module-specific
```

**Risky / code-wide** tier:

```
mvn -B -ntp validate
mvn -B -ntp test
```

**Packaging / full-app** tier (only when in scope and JavaFX/`jdk.home`
environment supports it):

```
mvn -B -ntp -DskipTests clean package
```

**Docs-only:** Maven may be skipped with a clear note.

CI-safe sanity check (not sufficient alone before commit approval unless
the developer explicitly relaxes Section 14.2):

```
mvn -B -ntp -DskipTests validate
```

Do not invent richer commands (e.g. `verify`, `install`) unless a
tracker item explicitly requires them — the reactor is not yet
stabilized (see `docs/audit-master-baseline.md`).

## Things to avoid suggesting

- Replacing `javax.xml.bind` with `jakarta.xml.bind`.
- Replacing `youtube-dl` with `yt-dlp` (tracked under `ytdlp-migration`).
- Upgrading or replacing JavaFX dependencies (tracked under
  `javafx-modernization`).
- Adding network-dependent tests to the default lifecycle.
- Adding OWASP, SBOM, or static-analysis plugins in this phase.
- Reformatting files, renaming variables outside a change, or moving
  packages.
- Bumping a `plugins/*/pom.xml` `<version>` for changes that do not
  match a `plugin-versioning-policy` trigger (downloader/parser
  behaviour, user-facing endpoint, user-facing configuration). See
  the "Plugin versioning" section of `CONTRIBUTING.md`. Inside a
  bumped plugin, internal reactor deps (`api`, `framework`,
  `plugin-tester`) MUST use `${project.parent.version}`, never
  `${project.version}`.

## Things to encourage

- Small, scoped diffs tied to one tracker item.
- English-only comments and identifiers in new code.
- Explicit error handling at meaningful boundaries (no swallow,
  no broad catch).
- Updating `docs/dev-tracker.md`, `docs/dev-tracker.json`,
  `docs/risk-register.md`, and `docs/decision-log.md` when the
  change is meaningful.
