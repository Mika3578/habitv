# Cloud Agent overlay

Cloud and Background Agents read this after [`AGENTS.md`](../AGENTS.md).
Local IDE chat ignores it.

## Branch name (platform)

The harness often creates `cursor/...` before repo rules run. That
prefix is not controlled by `AGENTS.md`. Do **not** rename an existing
PR head on GitHub: GitHub closes the PR.

- If you create a branch yourself before a PR exists, use
  `<type>/<short-scope>` (`feat/tf1plus`).
- If you are already on `cursor/...` with an open PR, stay on it.

## Public git text

Follow **Public git text** in `AGENTS.md`. Procedure:
[`.agents/skills/public-git-text/SKILL.md`](../.agents/skills/public-git-text/SKILL.md).
Fill [`.github/pull_request_template.md`](../.github/pull_request_template.md).
