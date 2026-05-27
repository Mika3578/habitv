# Claude Code instructions for Habitv

`AGENTS.md` is the single source of truth for repository-wide AI agent
workflow policy.

Before making changes, read and follow `AGENTS.md` for branch naming,
duplicate branch/PR prevention, commit style, PR structure, validation,
PR target policy, and documentation sync.

If this file conflicts with `AGENTS.md`, `AGENTS.md` wins.

Use English for code comments, commit messages, PR text, and
documentation.

## Branch naming (Claude Code)

Never let Claude Code pick a branch name. Forbidden prefixes and
recovery steps are in `AGENTS.md` §4.

When using Claude Code worktrees, always pass an explicit branch name:

```bash
claude --worktree feat/descriptive-scope
```

Never run `claude --worktree` without a branch argument — that produces
random `claude/**` names that violate repository policy.
