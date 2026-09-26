# HabiTV Agent Instructions

Canonical repository instructions for coding agents. Humans: see
[`CONTRIBUTING.md`](CONTRIBUTING.md). Architecture and build:
[`docs/architecture.md`](docs/architecture.md),
[`docs/development.md`](docs/development.md).

Portable task procedures live under [`.agents/skills/`](.agents/skills/).
Agent architecture overview: [`docs/development.md`](docs/development.md#agent-instructions).

## Repository

HabiTV is a Maven multi-module replay application (`fwk/`,
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

Preferred work order: build/CI health, retrieval diagnostics, yt-dlp
reliability, then provider repairs, then JDK/packaging migration.

## Technical routing

| Topic | Document |
|-------|----------|
| Java, Maven, CI, run commands | [`docs/development.md`](docs/development.md) |
| Providers and fixtures | [`docs/providers.md`](docs/providers.md) |
| Architecture | [`docs/architecture.md`](docs/architecture.md) |
| Modernization roadmap | [`docs/modernization.md`](docs/modernization.md) |
| Public git text procedure | [`.agents/skills/public-git-text/SKILL.md`](.agents/skills/public-git-text/SKILL.md) |
| Branch and worktree setup | [`.agents/skills/git-workflow/SKILL.md`](.agents/skills/git-workflow/SKILL.md) |
| Validation workflow | [`.agents/skills/code-change-verification/SKILL.md`](.agents/skills/code-change-verification/SKILL.md) |
| Provider diagnostics | [`.agents/skills/provider-diagnostics/SKILL.md`](.agents/skills/provider-diagnostics/SKILL.md) |
| PR orchestration | [`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md) |

## Engineering Baseline

- Language: stay on the **current Java 8** baseline unless the task is
  an explicit JDK migration. See [`docs/development.md`](docs/development.md).
- GUI modules need a JavaFX-capable JDK 8 at runtime (`jfxrt`).
- Do not add, remove, or upgrade dependencies unless the task requires it.
- Plugin version overrides must follow [`CONTRIBUTING.md`](CONTRIBUTING.md).
  Internal plugin deps use `${project.parent.version}`.
- English for branches, commits, comments, docs, and PR text
  (**Public git text**).
- Keep **code comments** concise: explain non-obvious *why*; do not narrate
  parsing, hosts, URLs, or integration mechanics (use `docs/` or tests).
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

See [`docs/providers.md`](docs/providers.md).

- Do not implement or document circumvention of technical protection
  or license restrictions.
- No credential, cookie, or browser-session extraction into the repo.
- User secrets stay in local config or environment variables.
- Do not re-enable legacy hosts (`dabiboo.free.fr`, `ftpperso.free.fr`).
- Do not delete user media files, indexes, or configs.

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

Keep public git text **implementation-neutral**: use concise user-facing
wording and avoid unnecessary detail about access mechanisms,
authentication flows, delivery internals, provider-specific selectors, or
host implementation specifics. Do not add AI/tool footers or auto-summaries.
Do not publish negated topic checklists in public git text.

**Branches:** `<type>/<short-scope>` kebab-case. Types: `feat`, `fix`,
`docs`, `test`, `refactor`, `chore`, `ci`. Scope is the module or
topic only. No `cursor/`, `claude/`, `ai/`, `codex/`, or other tool
prefixes. No `replay`, `download`, or diagnostic suffixes.

Name the branch correctly at creation. Do not rename an existing PR
head on GitHub: that closes the PR.

Cursor Cloud Agents may create a platform-prefixed branch before
repository policy loads; `AGENTS.md` cannot change that initial step.
For new work, follow **Cloud agents** in
[`.agents/skills/git-workflow/SKILL.md`](.agents/skills/git-workflow/SKILL.md).
An open pull request on a platform-created head stays on that head
unless the maintainer explicitly authorizes replacement (renaming the
head closes the PR on GitHub).

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
| `fix(6play): diagnose obsolete SPA listing and access limits` | `fix(6play): diagnose listing` |

**PR bodies:** Fill `.github/pull_request_template.md`. Keep Summary,
Changes, and Notes generic. Validation is the command and result. Do
not add strategy, access-model, or manual-check sections.

**PR review comments:** Short and factual. State the fix, rejection
reason, or requested change with evidence. Do not restate the diff, do
not narrate runtime or provider behavior, and do not write tutorial-length
replies.

## Git Workflow

Never work on `develop`, `main`, or `master`. Branch format follows
**Public git text**. Follow [`CONTRIBUTING.md`](CONTRIBUTING.md).

Before an agent creates a branch or worktree for a **new** task, determine
the canonical branch name (`<type>/<short-scope>`) and follow
[`.agents/skills/git-workflow/SKILL.md`](.agents/skills/git-workflow/SKILL.md).
When the environment allows explicit naming, use the compliant name from the
start and verify with `git branch --show-current` before the first commit or
push. Do not intentionally create a tool-prefixed or randomly suffixed public
branch when the branch name is under agent control.

Cloud Agents: repository files do not run before the platform may assign an
initial branch. Use strict pre-branch API launch or in-workspace recovery in
`git-workflow` before the first publish. If a pull request already exists on a
platform-created head, do not rename that head on GitHub.

Before creating a branch, search open PRs and existing branches for
the same scope. Do not open a second PR that covers the same module
or topic.

Never run without explicit approval **in this conversation**:
`git commit`, `git push`, `git push --force` / `--force-with-lease`,
`gh pr create`, `gh pr merge`, mark a PR Ready, merge, delete branches,
rebase when it would rewrite remote history, or destructive git/fs commands.

When the user authorizes finishing or reviewing a **specific** pull request,
that authorization covers the bounded orchestration loop for that PR only
([`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md)).
It does not authorize merge or unrelated GitHub mutations.

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

End-to-end PR work uses the **pr-review** orchestrator skill. Do not
duplicate that procedure here.

## Pull request lifecycle (invariants)

Keep every pull request in **Draft** until gates on the **current PR HEAD**
are satisfied.

- Evaluate every PR against its **current PR HEAD**; new commits invalidate
  prior reviews and checks that do not apply to that HEAD.
- Every actionable review finding receives a disposition (fix, reject with
  evidence, duplicate, or follow-up).
- Addressed inline threads get a concise reply, then resolution only after
  the fix or rejection is verified; re-fetch GitHub and confirm
  `isResolved`. Do not resolve unanswered or unexamined threads.
- **Live GitHub PR state is authoritative** over local memory or ledgers.
- During a review cycle, **only the PR orchestrator** mutates GitHub PR
  metadata (body, draft/ready, replies, resolution, reviewer requests).
  Independent reviewers are read-only.
- **Review rigor is proportional to risk** (see pr-review skill).
- No actionable unresolved feedback remains at Ready.

When runtime behavior may change, keep the PR in Draft until the user
**explicitly confirms success in the current conversation** after a real
HabiTV test. Automated checks are not a substitute.

Ready gate, batching, Copilot final review, and live reconciliation:
[`.agents/skills/pr-review/SKILL.md`](.agents/skills/pr-review/SKILL.md).

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
