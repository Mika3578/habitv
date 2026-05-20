# Repository maintenance

Day-to-day maintenance tasks for the Habitv repository: merge hygiene,
documentation sync, and contributor-facing metadata.

## PR metadata policy

Pull request metadata must stay accurate and readable for reviewers and for
future history readers after squash merge.

- PR titles and bodies use English.
- PR bodies document real validation commands and honest outcomes.
- Tracker references use descriptive slugs from `docs/dev-tracker.md`,
  optionally paired with the legacy `HBTV-XXX` code from the same entry
  (for example `clean-squash-merge-policy` (HBTV-018)).
- Squash merge titles follow Conventional Commits and include the PR number.
- Squash merge bodies summarize the merged outcome, not intermediate commits.

See `docs/pull-request-style-guide.md` for examples and the full squash merge
commit policy.

### Squash merge metadata

The person merging a PR is responsible for cleaning the final squash commit
message.

- The final squash commit must summarize the PR outcome, not list every
  intermediate commit.
- Generated `Co-authored-by` trailers should be removed unless intentionally
  kept.
- The final commit title should follow Conventional Commits and include the PR
  number.

## Repository automation checks

This repository uses conservative automation for dependency hygiene and triage
while keeping merge control with maintainers.

- Dependabot opens pull requests for Maven and GitHub Actions updates.
- Dependabot is configured to ignore semver-major updates.
- Auto-merge is intentionally disabled.
- Bots open pull requests only and must not push directly to `develop`.
- Dependency Review blocks pull requests that introduce new `high` or
  `critical` vulnerabilities in dependencies.
- CodeQL runs separately from Maven CI and reports code scanning alerts.
- Stale triage labels inactive issues and pull requests without auto-closing
  them.

## Documentation sync

When a change updates process or governance documentation, keep these files
aligned in the same commit when applicable:

- `docs/dev-tracker.md` and `docs/dev-tracker.json`
- `docs/risk-register.md` when risks change
- `docs/decision-log.md` when architecture decisions change

## CI governance

When changing required checks, branch protection, or workflow job semantics,
update `docs/required-checks-roadmap.md` and tracker item
`required-checks-roadmap` in the same PR when the staged plan changes.

## Related documentation

- `docs/required-checks-roadmap.md` — phased path to required CI and security checks
- `docs/ci.md` — Maven CI workflow and local command parity
- `docs/repository-governance.md` — branch protection and rulesets
- `docs/pull-request-style-guide.md` — PR and squash merge style
- `AGENTS.md` — agent and contributor rules
- `docs/ci.md` — CI, security checks, and required/diagnostic lanes
