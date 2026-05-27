# Claude Code instructions for Habitv

`AGENTS.md` is the single source of truth for repository-wide AI agent
workflow policy. If this file conflicts with `AGENTS.md`, `AGENTS.md` wins.

Read `AGENTS.md` before making changes (hard rules, L0/L1/L2 governance,
branch naming, PR policy, validation, doc sync at PR readiness).

## Claude Code–specific

- Use English for code comments, commit messages, PR text, and docs.
- **Never** let Claude Code pick a branch name. Forbidden prefixes and
  recovery: `AGENTS.md` branch naming.
- Always pass an explicit branch when using worktrees:

```bash
claude --worktree feat/descriptive-scope
```

Never run `claude --worktree` without a branch argument — that produces
invalid `claude/**` names.
