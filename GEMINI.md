# Gemini instructions for Habitv

`AGENTS.md` is the canonical source of truth for repository-wide AI
agent behavior.

Before making changes, read and follow `AGENTS.md`. At task start, report
the **Instruction files loaded** checklist (`AGENTS.md` Section 15.1).

Tool-specific mirrors:

- `.cursor/rules/git-safety.mdc`
- `.cursor/rules/maven-validation.mdc`
- `.cursor/rules/pr-review.mdc`
- `.cursor/rules/java8-compatibility.mdc`
- `.github/copilot-instructions.md`
- `.github/instructions/*.instructions.md`
- `docs/agent-rules-changelog.md`

AI rule changes: drift audit (Section 17.6), changelog, version bump.

Never commit, push, create PRs, merge PRs, or resolve review threads
without explicit developer approval in the current conversation.

Never chain approval-gated commands (Section 15.8). Inspect diffs for
secrets (Section 15.9). No AI attribution (Section 15.10).

Use English for code comments, commit messages, PR text, and
documentation.
