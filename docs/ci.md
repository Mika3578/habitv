# CI and security checks

This repository uses independent GitHub workflows for build validation,
dependency security, and maintenance triage.

## Required checks recommendation

Require before merge:

- `Maven CI / validate-java8`
- `Maven CI / deterministic-tests-java8`
- `Maven CI / compile-and-package-java8`
- `Dependency Review / dependency-review`

Do not require yet (diagnostic compatibility lanes and broad suite):

- `Maven CI / compatibility-java11`
- `Maven CI / compatibility-java17`
- `Maven CI / compatibility-java21`
- `Maven CI / compatibility-java25`
- `Maven CI / full-test-suite`

## Security workflows

- **Dependency Review** (`.github/workflows/dependency-review.yml`) runs
  on pull requests to `develop` and `master` and fails when newly
  introduced dependencies have `high` or `critical` known
  vulnerabilities.
- **CodeQL** default setup runs as repository code scanning and reports
  alerts in GitHub code scanning.

## Maintenance automation

- **Dependabot** (`.github/dependabot.yml`) opens pull requests for
  Maven and GitHub Actions updates on a weekly schedule.
- **Pull Request Labeler** (`.github/workflows/labeler.yml`) applies
  labels based on changed files to help triage and routing.
- **Stale Triage** (`.github/workflows/stale.yml`) labels inactive
  issues and pull requests; it is not configured to auto-close them.

Labeler and stale triage are operational helpers and are not required
status checks for merging.
