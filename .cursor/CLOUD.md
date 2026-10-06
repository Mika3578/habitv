# Cloud Agent overlay

Cloud and Background Agents read this after [`AGENTS.md`](../AGENTS.md).
Local IDE chat ignores it.

## Branch name (platform)

Cloud harnesses may assign a platform branch before repository policy runs.
Do **not** rename an open PR head on GitHub (GitHub closes the PR).

New Cloud tasks and branch recovery:
[`.agents/skills/git-workflow/SKILL.md`](../.agents/skills/git-workflow/SKILL.md)
(Cloud Agents section). Dashboard branch prefix is static; see
[`docs/development.md`](../docs/development.md).

## Public git text

Follow **Public git text** in `AGENTS.md`. Procedure:
[`.agents/skills/public-git-text/SKILL.md`](../.agents/skills/public-git-text/SKILL.md).
Fill [`.github/pull_request_template.md`](../.github/pull_request_template.md).

## Pull request review feedback

Cloud Agents working a same-repository PR follow **Pull request lifecycle**
in `AGENTS.md` and [pr-review](../.agents/skills/pr-review/SKILL.md).
Load policy from `origin/develop` when the task is event-driven triage.
Never merge, never enable GitHub auto-merge, and never approve the
`merge-develop` environment unless the conversation explicitly
authorizes that exact action. Post sequenced Draft reviewer-trigger
comments without asking the maintainer to type them. Do not request
Copilot until after Ready (FINAL_REVIEW). CodeRabbit `APPROVED` is not
merge authorization.
