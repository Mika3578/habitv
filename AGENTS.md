# HabiTV Agent Instructions

Canonical repository instructions for coding agents. Humans: see
[`CONTRIBUTING.md`](CONTRIBUTING.md). Architecture and build:
[`docs/architecture.md`](docs/architecture.md),
[`docs/development.md`](docs/development.md).

## Repository

HabiTV is a Maven multi-module replay downloader (`fwk/`,
`application/`, `plugins/`). Canonical remote: `Mika3578/habitv`.
Integration branch: `develop`.

Current **build/runtime baseline is Java 8**; Java 21 is the
modernization target, Java 25 next. Details:
[`docs/development.md`](docs/development.md). Do not treat 21/25 as
supported until compiler and required CI change.

Do not rewrite architecture, migrate Java, replace JavaFX, regenerate
JAXB, restructure the Maven reactor, or rewrite providers unless the
current task explicitly asks for that scoped work.

## Current Priorities

Preferred work order: build/CI health, download diagnostics, yt-dlp
reliability, then provider repairs, then JDK/packaging migration.

## Engineering Baseline

- Language: stay on the **current Java 8** baseline unless the task is
  an explicit JDK migration. See [`docs/development.md`](docs/development.md).
- GUI modules need a JavaFX-capable JDK 8 at runtime (`jfxrt`).
- Do not add, remove, or upgrade dependencies unless the task requires it.
- Plugin version overrides must follow [`CONTRIBUTING.md`](CONTRIBUTING.md).
  Internal plugin deps use `${project.parent.version}`.
- English for branches, commits, comments, docs, and PR text.
- No secrets, tokens, credentials, or machine paths in git.
- No AI/tool attribution in commits or PRs.
- Scratch work goes in `agent_space/` (gitignored). Do not commit it.

## Download and Provider Rules

See [`docs/providers.md`](docs/providers.md).

- Prefer offline fixtures; live network tests are opt-in
  (`-Plive-provider-tests`), never the default proof.
- Do not claim a provider works without code/tests/evidence.
- One provider/plugin module per change when possible.
- Keep public PR/commit text high-level (no bypass recipes, no session
  extraction steps).

## Safety and Legal Constraints

- No DRM, encryption, paywall, or license bypass.
- No credential, cookie, or browser-session extraction into the repo.
- User secrets stay in local config or environment variables.
- Do not re-enable legacy hosts (`dabiboo.free.fr`, `ftpperso.free.fr`).
- Do not delete user downloads, indexes, or configs.

## Testing and Validation

Docs-only: `git diff --check`. Maven optional if no Java/POM/workflow
change.

Default code validation:

```bash
mvn -B -ntp -DskipTests validate
```

Add targeted `mvn -B -ntp -pl <module> -am test` when Java changes.
Do not treat `mvn verify` or full `package` as the default merge bar.
Quote exact commands and results. Do not claim success without output.

Windows is the primary dev OS: quote paths with spaces. If both `.sh`
and `.ps1` exist, update both or say why not.

## Git Workflow

Never work on `develop`, `main`, or `master`. Branch format:
`<type>/<short-scope>` with `feat/`, `fix/`, `docs/`, `test/`,
`refactor/`, `chore/`, `ci/`. No AI/tool prefixes.

Conventional Commits, English, imperative, required scope, ≤ 72 char
subject. Follow [`CONTRIBUTING.md`](CONTRIBUTING.md).

Never run without explicit approval **in this conversation**:
`git commit`, `git push`, `git push --force` / `--force-with-lease`,
`gh pr create` / `merge` / `review` / `comment`, review resolution,
or destructive git/fs commands.

Do not chain those actions. Do not `git add -A`, `git add .`, or
`git add --all`. Stage explicit paths only.

Linear history: rebase onto `origin/develop`; never merge `develop`
into the work branch. After rebase, `--force-with-lease` only, with
approval. Never `--force`.

Do not overwrite unrelated local changes.

## Pull Requests and Reviews

- Repository: `Mika3578/habitv`
- Base: `develop`
- Never open PRs against `ikfon10/habitv`

Fill `.github/pull_request_template.md`. Keep PRs small and single-topic.
Handle Copilot/review comments (fix, or reject with a reason). Ask before
posting GitHub comments or resolving threads.

## Documentation

One topic, one page. Link instead of copying. Do not add agent-rule
mirrors (`CLAUDE.md`, nested `AGENTS.md`, Cursor rulebooks, Copilot
`copilot-instructions.md`). Root `AGENTS.md` is the only agent rulebook.

## Definition of Done

- Change matches the requested scope only.
- Validation for that scope ran (or is explicitly skipped).
- Links and claims match the repository.
- Developer was asked to test real behavior when runtime/UI is affected.
- Commit/push/PR wait for explicit approval.
