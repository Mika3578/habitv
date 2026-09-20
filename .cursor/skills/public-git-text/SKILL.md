---
name: public-git-text
description: Writes short generic git branches, commit subjects, and GitHub pull request titles and bodies. Use when creating or renaming a branch, writing a commit, opening or editing a pull request, or drafting a PR title, body, or review comment.
---

# Public git text

Read **Public git text** in `AGENTS.md` before any branch, commit, or PR
text. Fill `.github/pull_request_template.md` for PR bodies.

## Do

- Branch: `<type>/<short-scope>` kebab-case (`feat/tf1plus`)
- Title/commit: `type(scope): subject` with lowercase kebab scope
  (`feat(tf1plus): add provider`)
- PR body: Summary, Scope, Changes, Validation (command + result),
  Risk / rollback. Keep wording generic.

## Do not

- Tool prefixes: `cursor/`, `claude/`, `ai/`, `codex/`
- Suffixes: `replay`, `download`, `diagnostics`
- DRM, Widevine, Gigya, VPN, geo, login, cookies, stream recipes
- "No unlock / no bypass / no account" lists
- AI/tool footers or auto-summaries
- Rename an existing GitHub PR head branch (that closes the PR)
- Fight a Cloud Agent platform `cursor/...` branch after the PR exists;
  keep the title and body generic instead

## Examples

```text
❌ cursor/lemanbleu-replay-1a2e
✅ feat/lemanbleu
❌ feat(lemanBleu): add replay provider
✅ feat(lemanbleu): add provider
```
