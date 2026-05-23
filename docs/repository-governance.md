# Repository governance

## Purpose

Habitv is a Java 8 Maven multi-module application that downloads French TV
catch-up content via pluggable provider plugins. This document defines how
the `Mika3578/habitv` repository is governed: branches, merges, validation,
security, and automation boundaries for the modernization restart.

## Branch model

| Branch | Role |
|--------|------|
| `develop` | Default integration branch. All active work merges here first. |
| `master` / `main` | Legacy stable baseline when present. Protected from direct pushes. |
| Short-lived branches | Created from `develop`: `fix/*`, `feat/*`, `chore/*`, `docs/*`, `test/*`, `ci/*`, `refactor/*` (see `AGENTS.md`). |

Do not base modernization work branches on `master` unless a tracker item
explicitly requires it.

## Merge model

- **Pull requests only** — no direct pushes to protected branches.
- **Linear history required** — merge commits are disabled.
- **Prefer squash merge** for a single logical change per PR.
- **Rebase merge** is allowed only when the PR already contains clean, atomic
  commits that should land individually.
- **Merge commits** are disabled at the repository level.

## Required local validation before opening a PR

Run from the repository root on the work branch:

```bash
git diff --check
mvn -B -ntp -DskipTests validate
```

Document any baseline failure honestly in the PR body (module name and first
meaningful error line). Do not fix unrelated baseline failures inside a
governance-only PR.

## Required PR checks

The **CI** workflow (`.github/workflows/ci.yml`) must pass on pull requests
targeting `develop`. Use the expected required ruleset status check context:

- `CI / validate (zulu-8)`

If GitHub displays a different check name in the ruleset UI (for example
`validate (zulu-8)` without the workflow prefix), align the ruleset with the
exact name shown on a completed workflow run.

## Security policy summary

- **No secrets** in the repository, issues, PRs, logs, or attachments.
- **Dependabot** is enabled for Maven (`/`) and GitHub Actions (`/`), targeting
  `develop` (see `.github/dependabot.yml`).
- **GitHub Actions** workflows use least privilege: default `contents: read`
  at the workflow level unless a documented exception requires more.
- **Dependabot alerts** and **Dependabot security updates** should stay enabled.
- **Secret scanning** and **push protection** should be enabled when the account
  plan supports them.
- Report vulnerabilities per `SECURITY.md` (private triage before public
  disclosure).

## Rulesets summary

Repository rulesets (preferred over legacy branch protection):

| Ruleset | Targets | Intent |
|---------|---------|--------|
| `protect-develop` | `refs/heads/develop` | PR required, linear history, required CI check, up-to-date branch, conversation resolution, no force-push or deletion. |
| `protect-stable-branches` | `refs/heads/master` | PR required, linear history, no force-push or deletion. |
| `protect-release-tags` | `refs/tags/v*` | Prevent tag deletion and non-fast-forward updates; restrict tag changes where supported. |

See `docs/github-rulesets/` for API payloads when rulesets must be applied
manually.

## Out of scope for governance PRs

- Java baseline migration beyond Java 8.
- Plugin or provider modernization.
- Maven artifact repository migration (HBTV-004).
- Runtime updater URL or layout changes (HBTV-005).

Tracker items and ADRs in `docs/dev-tracker.md` and `docs/decision-log.md`
govern when those areas may change.

## Related documentation

- `AGENTS.md` — agent and contributor workflow.
- `docs/github-repository-settings.md` — historical bootstrap notes (superseded
  in part by this document for day-to-day governance).
- `SECURITY.md` — vulnerability reporting.
- `.github/pull_request_template.md` — PR checklist.

## Manual ruleset setup

If `gh api` rejects a ruleset payload, apply rules in the GitHub UI:

1. Open **Settings → Rules → Rulesets**.
2. Create or edit each ruleset using the JSON files under `docs/github-rulesets/`.
3. For `protect-develop`, under **Require status checks**, add the check name
   exactly as shown on a green CI run after merging the CI workflow.
4. Set **Required approving reviews** to `0` for a single-maintainer repository
   while keeping **Require a pull request before merging** enabled.
5. Enable **Require conversation resolution before merging** on `develop`.

Record any API error message in the PR body under **Manual settings still required**.
