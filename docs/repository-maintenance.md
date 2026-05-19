# Repository maintenance automation

This repository uses conservative automation for dependency hygiene and
triage while keeping merge control with maintainers.

## Dependency updates

- Dependabot opens pull requests for:
  - Maven dependency updates
  - GitHub Actions updates
- Dependabot pull requests must pass Maven CI before merge.
- Auto-merge is intentionally disabled.
- Bots open pull requests only and must not push directly to `develop`.

## Security checks

- Dependency Review blocks pull requests that introduce new `high` or
  `critical` vulnerabilities in dependencies.
- CodeQL default setup runs separately from Maven CI and reports code
  scanning alerts.

## Triage automation

- Pull request labeler applies labels from changed file paths to support
  review routing and queue management.
- Stale triage labels inactive issues and pull requests.
- Stale triage does not auto-close issues.
- Stale triage does not auto-close pull requests.

## Compatibility policy note

Modern Java compatibility lanes (`java11+`) remain diagnostic until JAXB
and JavaFX modernization work is complete.
