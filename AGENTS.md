# HabiTV Agent Instructions

Canonical repository instructions for coding agents. Humans: see
[`CONTRIBUTING.md`](CONTRIBUTING.md). Architecture and build:
[`docs/architecture.md`](docs/architecture.md),
[`docs/development.md`](docs/development.md).

Portable task procedures live under [`.agents/skills/`](.agents/skills/).
Agent architecture overview: [`docs/development.md`](docs/development.md#agent-instructions).

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

## Technical routing

| Topic | Document |
|-------|----------|
| Java, Maven, CI, run commands | [`docs/development.md`](docs/development.md) |
| Providers and fixtures | [`docs/providers.md`](docs/providers.md) |
| Architecture | [`docs/architecture.md`](docs/architecture.md) |
| Modernization roadmap | [`docs/modernization.md`](docs/modernization.md) |
| Public git text procedure | [`.agents/skills/public-git-text/SKILL.md`](.agents/skills/public-git-text/SKILL.md) |
| Validation workflow | [`.agents/skills/code-change-verification/SKILL.md`](.agents/skills/code-change-verification/SKILL.md) |
| Provider diagnostics | [`.agents/skills/provider-diagnostics/SKILL.md`](.agents/skills/provider-diagnostics/SKILL.md) |
| PR review loop | [`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md) |

## Engineering Baseline

- Language: stay on the **current Java 8** baseline unless the task is
  an explicit JDK migration. See [`docs/development.md`](docs/development.md).
- GUI modules need a JavaFX-capable JDK 8 at runtime (`jfxrt`).
- Do not add, remove, or upgrade dependencies unless the task requires it.
- Plugin version overrides must follow [`CONTRIBUTING.md`](CONTRIBUTING.md).
  Internal plugin deps use `${project.parent.version}`.
- English for branches, commits, comments, docs, and PR text
  (**Public git text**).
- No secrets, tokens, credentials, or machine paths in git.
- No AI/tool attribution in commits or PRs.
- Scratch work goes in `agent_space/` (gitignored). Do not commit it.

## Download and Provider Rules

See [`docs/providers.md`](docs/providers.md).

- Prefer offline fixtures; live network tests are opt-in
  (`-Plive-provider-tests`), never the default proof.
- Do not claim a provider works without code/tests/evidence.
- One provider/plugin module per change when possible.
- Public git text stays short and generic. See **Public git text**.

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
Record the command and result. Do not claim success without output.
Do not paste full logs in PR text.

**Final gate (executable changes):** before a PR may leave Draft, run the
full Maven reactor validation appropriate to the active baseline defined in
[`docs/development.md`](docs/development.md) (required CI: validate,
deterministic test subset, and `mvn -B -ntp -DskipTests package`). Narrow
commands during development do not replace this gate.

Windows is the primary dev OS: quote paths with spaces. If both `.sh`
and `.ps1` exist, update both or say why not.

## Public git text

Applies to branch names, PR titles, PR bodies, PR comments, and commit
subjects. English only. Detail belongs in the diff, not in public git
text.

Do not mention DRM, Widevine, CDM, Gigya, VPN, geo bypass, login,
cookies, sessions, stream recipes, or host/selector internals. Do not
write "no unlock / no bypass / no account" lists. Do not add AI/tool
footers or auto-summaries.

**Branches:** `<type>/<short-scope>` kebab-case. Types: `feat`, `fix`,
`docs`, `test`, `refactor`, `chore`, `ci`. Scope is the module or
topic only. No `cursor/`, `claude/`, `ai/`, `codex/`, or other tool
prefixes. No `replay`, `download`, or diagnostic suffixes.

Name the branch correctly at creation. Do not rename an existing PR
head on GitHub: that closes the PR.

Cursor Cloud Agents often start on a platform `cursor/...` branch
before these rules run. Stay on that head if a PR already exists.
Keep the PR title and body generic anyway.

| Avoid | Use |
|-------|-----|
| `cursor/lemanbleu-replay-1a2e` | `feat/lemanbleu` |
| `fix/clubic-shorts-listing` | `fix/clubic` |
| `fix/download-daemon-resilience` | `fix/background-daemon` |

**PR titles and commits:** Conventional Commits, required scope,
imperative, ≤ 72 characters. Scope is lowercase kebab-case
(`lemanbleu`, `rtbf-auvio`), not Maven camelCase. Same generic
wording.

| Avoid | Use |
|-------|-----|
| `feat(tf1plus): add replay provider` | `feat(tf1plus): add provider` |
| `fix(6play): diagnose obsolete SPA listing and DRM limits` | `fix(6play): diagnose listing` |

**PR bodies:** Fill `.github/pull_request_template.md`. Keep Summary,
Changes, and Notes generic. Validation is the command and result. Do
not add strategy, access-model, or manual-check sections.

## Git Workflow

Never work on `develop`, `main`, or `master`. Branch format follows
**Public git text**. Follow [`CONTRIBUTING.md`](CONTRIBUTING.md).

Before creating a branch, search open PRs and existing branches for
the same scope. Do not open a second PR that covers the same module
or topic.

Never run without explicit approval **in this conversation**:
`git commit`, `git push`, `git push --force` / `--force-with-lease`,
`gh pr create` / `merge` / `review` / `comment`, review resolution,
mark a PR Ready, merge, delete branches, rebase when it would rewrite
remote history, or destructive git/fs commands.

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
Titles, bodies, and review comments follow **Public git text**.
Handle Copilot/review comments (fix, or reject with a reason). Ask before
posting GitHub comments or resolving threads.

## Pull request lifecycle

Keep every pull request in **Draft** until all review comments and
unresolved threads have been individually addressed, all required checks
pass on the **latest commit**, and no actionable review finding remains.
Do not mark a pull request Ready for review while any of these conditions
is unmet.

Always evaluate the **current PR HEAD**. A previous successful review,
approval, build, or CI run does not validate later commits. After any new
commit, repeat validation and review on that commit.

### Draft → Ready (all applicable on current HEAD)

1. Scope is complete.
2. Required focused tests pass.
3. Full reactor validation passes when the PR touches executable code,
   build logic, runtime behavior, providers, download logic, packaging,
   startup, or UI (see [`docs/development.md`](docs/development.md)).
4. All required GitHub checks pass on the current commit.
5. Every review comment has been individually considered.
6. Every valid review finding has been fixed.
7. Every rejected finding has a documented technical reason where
   appropriate.
8. No unresolved actionable review thread remains.
9. No actionable AI-review finding remains.
10. Required real-user functional testing has succeeded when applicable.
11. The user explicitly confirmed the functional test in the current
    conversation.
12. No newer commit has invalidated any of the above.

If any condition becomes false after another commit, return to the relevant
step. Do not declare a PR ready, good to merge, or validated while a gate
remains incomplete.

### Real-user functional validation

When a change may affect observable application behavior (UI, provider
discovery, listing, downloading, yt-dlp integration, subtitles, quality,
startup, daemon, packaging, plugin loading, updates, or runtime config),
keep the PR in Draft until the user performs a real test in HabiTV and
**explicitly confirms success in the current conversation**. Automated
tests, fixtures, CI, and agent inspection are not substitutes.

Provide a concise manual test procedure. Do not infer confirmation from
silence. While pending, report:

```text
Functional validation: PENDING USER TEST
```

Pure documentation or agent-configuration changes that cannot affect
executable behavior are exempt from full reactor build and real-user
testing when that exemption is stated and `git diff --check` (and
config validation) pass.

Procedure detail: [`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md).

## Documentation

One topic, one page. Link instead of copying. Root `AGENTS.md` is the
only full repository-wide agent policy. Do not add substantive policy to
`CLAUDE.md`, `GEMINI.md`, nested `AGENTS.md`, or tool-specific
constitution files.

Tool adapters (`.cursor/`, `.continue/rules/`, `.github/copilot-instructions.md`)
must stay short, point here, and hold only compatibility or activation
behavior.

Cursor loads this file on every Agent session. Cloud Agents also load
repo `.cursor/rules/*.mdc` and `.cursor/CLOUD.md` (after this file).
Keep those files short and point here. Do not put `description` on
always-on `.mdc` rules: Cursor has mapped that to requestable instead
of injected.
Portable skills live under `.agents/skills/`; Cursor may mirror discovery
paths but must not hold a second canonical copy.

## Definition of Done

- Change matches the requested scope only.
- Validation for that scope ran (or is explicitly skipped with reason).
- Links and claims match the repository.
- Applicable Draft → Ready gates satisfied on the latest commit before
  Ready (with explicit authorization).
- Developer was asked to test real behavior when runtime/UI is affected.
- Commit/push/PR wait for explicit approval.
