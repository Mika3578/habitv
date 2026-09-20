# Cloud Agent overlay

Cloud and Background Agents read this after `AGENTS.md`. Local IDE
chat ignores it.

## Branch name (platform)

The harness often creates `cursor/...` before repo rules run. That
prefix is not controlled by `AGENTS.md`. Do **not** rename an existing
PR head on GitHub: GitHub closes the PR.

- If you create a branch yourself before a PR exists, use
  `<type>/<short-scope>` (`feat/tf1plus`).
- If you are already on `cursor/...` with an open PR, stay on it.

## Still required

- PR title: `feat(tf1plus): add provider` (lowercase kebab-case scope)
- PR body: `.github/pull_request_template.md`, short and generic
- No DRM, Widevine, Gigya, VPN, geo, login, cookies, or stream recipes
- No AI/tool footers or auto-summaries
