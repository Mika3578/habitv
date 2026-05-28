---
applyTo: ".github/**,AGENTS.md,CONTRIBUTING.md,docs/**,*.md"
---

# Habitv GitHub and PR instructions (Copilot)

Canonical workflow policy: `AGENTS.md`. Do not contradict it.

## PR target

- Repository: `Mika3578/habitv`
- Base branch: `develop`
- Never target `ikfon10/habitv` or other forks

## Git workflow

Never commit, push, create PRs, merge PRs, or resolve review threads
without explicit developer approval in the current conversation.

Approval prompts (exact wording):

- **Approve commit?**
- **Approve push?**
- **Approve force-with-lease push?** (after rebase only)

Branch updates: rebase on `origin/develop` only. Never merge
`origin/develop`, plain `git pull`, or GitHub Update branch when it
creates a merge commit. After rebase, push with `--force-with-lease`
only (never `--force`).

## Copilot review comments

Before PR readiness:

1. Inspect all Copilot review comments and unresolved conversations.
2. Classify each: accepted and fixed; intentionally rejected with reason;
   not applicable; outside current PR scope; needs developer decision.
3. Implement accepted items; reply in English to each comment.
4. Resolve only after handling and reply. Never resolve to silence review.
5. Ask developer approval before GitHub commands that resolve threads.
6. Never resolve conversations silently (`AGENTS.md` Section 15.7).

## Instruction alignment

At task start, report **Instruction files loaded**. When changing AI
rules, run drift check (`AGENTS.md` Section 15.3).

No AI attribution in commits or PRs (Section 15.10). No chained
approval-gated commands (Section 15.8). Stage only by explicit path
(Section 15.20). Provide exact validation evidence (Section 15.15).

Verify branch protection alignment, status checks, and CODEOWNERS before
PR merge readiness (Sections 15.17–15.19). Ask approval before PR
comments, labels, review requests, or resolution (Section 15.25).

## PR content

- English only for titles, bodies, and comments
- Conventional Commits for PR titles
- No `hbtv-*` or `HBTV*` IDs in branch names, commits, or PR text
- Fill required sections of `.github/pull_request_template.md`
- Include exact validation results in the PR body

## Branch naming

Allowed prefixes: `feat/`, `fix/`, `docs/`, `test/`, `refactor/`,
`chore/`, `ci/`, `build/`. No random tool-generated branch names.
