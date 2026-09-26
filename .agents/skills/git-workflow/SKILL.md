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

## Worktree directory vs Git branch

Cursor and Git may use different labels:

- **Filesystem worktree path** (for example under `~/.cursor/worktrees/…`) is local
  and need not match the public branch name.
- **Git branch name** is the public repository concern and must follow the canonical
  format when the agent controls creation.

After any automated worktree creation, run `git branch --show-current` and confirm the
branch matches the canonical name before committing or pushing.

## Procedure (new task)

1. Determine task **type** and short **scope**; build the canonical branch name.
2. Search open pull requests and remote branches for the same scope (`gh pr list`,
   `git branch -a`, `git ls-remote --heads origin`).
3. Do not open a second PR for the same scope when one already exists.
4. Create or request an isolated checkout using the canonical name where supported:
   - **Git:** `git fetch origin develop` then
     `git worktree add <path> -b <type>/<scope> origin/develop` (or checkout an
     existing compliant branch).
   - **Cursor CLI:** `agent --worktree <label> --worktree-base origin/develop "…"`.
     If the resulting Git branch is not compliant and no PR exists yet, create or
     rename the branch locally before the first push (`git checkout -b <type>/<scope>`
     from the correct base).
   - **Cursor IDE:** when supported, `/worktree branch=<type>/<scope> …` (see current
     Cursor documentation).
5. Verify `git branch --show-current` and `git status`.
6. If the platform assigned a non-compliant branch **and no pull request exists yet**,
   correct the branch before push or PR creation when safe.
7. If a **pull request already exists** on a platform-created head, do **not** rename
   that head on GitHub (official GitHub behavior closes the open PR). Continue on the
   existing head unless the maintainer explicitly authorizes replacement.
8. Base new work on `origin/develop` (or the integration branch in `AGENTS.md`); keep
   linear history per `AGENTS.md`.
9. Before first push, re-check duplicate scope and branch name compliance.

## Cleanup

Remove temporary local worktrees with `git worktree remove` when finished. Do not delete
the checkout that backs an open pull request.

Related: [public-git-text](../public-git-text/SKILL.md) (commits and PR text),
[pr-review](../pr-review/SKILL.md) (review loop after a PR exists).
