# Claude Code instructions for Habitv

AGENTS.md is the single source of truth for repository-wide AI agent workflow policy.

Before making changes, read and follow AGENTS.md for:
- PR target policy
- branch naming
- commit style
- pull request structure
- validation commands
- duplicate branch and PR prevention
- linear Git history
- documentation sync requirements

Do not use random Claude-generated branch names.

Allowed branch prefixes only:
- fix/
- feat/
- chore/
- docs/
- test/
- ci/
- refactor/

Deprecated branch prefixes:
- feature/
- build/
- runtime/
- provider/

Before creating any branch, run:
git fetch --all --prune
git branch -a --list "*[short-scope]*"
gh pr list --repo Mika3578/habitv --state open --search "[short-scope]"
git check-ref-format --branch "[branch-name]"

If an existing branch or PR already covers the same scope, do not create a duplicate. Reuse or update the existing branch/PR instead.

Use English for:
- branch names
- commit messages
- PR titles
- PR bodies
- code comments
- AI rule files

Keep Git history linear.

For this workflow, agent-created PRs must target `Mika3578/habitv`
and must not be opened directly against `ikfon10/habitv`.

Do not use local tracker IDs (e.g. `hbtv-*` / `HBTV-*`) in new branch
names, PR titles, commit subjects, or active workflow documentation.
