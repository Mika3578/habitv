---
name: git-workflow
description: Chooses a compliant HabiTV branch name, checks for duplicate scope, and creates or verifies a worktree before new agent work. Use before the first commit, push, or pull request for a new task.
---

# Git workflow (branch and worktree)

Authoritative branch naming: **Public git text** in [`AGENTS.md`](../../../AGENTS.md).
Repository workflow: **Git Workflow** in `AGENTS.md`.

## Canonical branch name

Format: `<type>/<short-scope>`

- **type:** `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, or `ci`
- **scope:** lowercase kebab-case module or topic (for example `agent-rules`, `arte`, `tf1plus`)
- No tool/vendor prefix (`cursor/`, `claude/`, `ai/`, `codex/`, …)
- No random numeric suffix or generated agent label
- No unnecessary implementation detail in the scope

Example: `chore/agent-rules`

Trust **`git branch --show-current`** over Cursor UI labels for the effective Git branch.

## Worktree directory vs Git branch

- **Filesystem worktree path** (for example under `~/.cursor/worktrees/…`) is local and need not match the public branch name.
- **Git branch name** is the public repository concern.

## Local / desktop agent

When the agent controls branch creation, create the canonical branch directly (Git worktree or `git switch -c`) and verify before the first commit or push.

## Cursor Cloud Agents — platform limit

Cloud Agents may create a **platform-prefixed** branch **before** `AGENTS.md`, skills, or `.cursor/rules` load. A static Dashboard branch prefix cannot express HabiTV’s dynamic `<type>/<scope>` policy (see [`docs/development.md`](../../../docs/development.md)).

Repository rules **do not** control that initial platform branch creation.

### Strict naming (preferred for new Cloud work)

1. Classify task type and scope; build `<type>/<scope>`.
2. Search open PRs and branches for duplicate scope.
3. Create the branch from `origin/develop` locally.
4. Push the branch to `origin` (`git push -u origin <type>/<scope>`) so the Cloud API `startingRef` exists on the remote.
5. Start the Cloud Agent with **`workOnCurrentBranch: true`** and **`repos[].startingRef`** set to that branch name (Cloud Agents API). The v1 API does not accept a separate custom branch name field; pushing on the named ref requires `workOnCurrentBranch`.
6. Maintainer helper (optional): [`scripts/launch-cloud-agent-strict.ps1`](../../../scripts/launch-cloud-agent-strict.ps1) with `CURSOR_API_KEY` from local secrets only.

The standard Cloud Agent UI (without API) defaults to **`workOnCurrentBranch: false`**, which creates a new `cursor/…` branch from the base ref.

### Continuing an existing pull request

Pass **`repos[].prUrl`** and set **`workOnCurrentBranch: true`**. The agent works on the PR head branch; `startingRef` is ignored.

Do **not** rename an open PR head on GitHub (GitHub closes the pull request).

### Recovery (platform branch, no PR yet)

After the workspace is writable, **before the first published commit**:

1. Run `git branch --show-current`.
2. If the branch is platform-generated or otherwise non-canonical, determine the canonical name.
3. Check for conflicts (`git branch -a`, `gh pr list`).
4. Create/switch: for example `git fetch origin develop` then
   `git switch -c <type>/<scope> origin/develop` (or move commits with `git switch -c` from the current tip when appropriate).
5. Re-run `git branch --show-current` and continue only when canonical.

### Startup check (all agents)

Before the first `git commit`, `git push`, or `gh pr create`:

```bash
git branch --show-current
```

Project hook [`.cursor/hooks/before-shell-branch-policy.sh`](../../../.cursor/hooks/before-shell-branch-policy.sh) blocks publishing from unpublished platform-generated branches but allows branches that already have a published upstream (continuing an existing PR head).

## Procedure (new task)

1. Determine task **type** and short **scope**; build the canonical branch name.
2. Search open pull requests and remote branches for the same scope.
3. Do not open a second PR for the same scope when one already exists.
4. Create checkout with the canonical name where supported (Git, Cursor CLI `--worktree`, IDE `/worktree` when available).
5. Verify `git branch --show-current` and `git status`.
6. Apply Cloud recovery or strict API workflow above when using Cloud Agents.
7. Base new work on `origin/develop`; keep linear history per `AGENTS.md`.

## Cleanup

Remove temporary local worktrees with `git worktree remove` when finished. Do not delete the checkout that backs an open pull request.

Related: [public-git-text](../public-git-text/SKILL.md), [pr-review](../pr-review/SKILL.md).
