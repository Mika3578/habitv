# CI and security checks

## Baseline and intent

- Java 8 is the current required baseline for merge-blocking checks.
- Java 11, 17, 21, and 25 are diagnostic compatibility checks only.
- Java 11+ may fail until JAXB and JavaFX migration is complete.
- No deployment, credentials, publishing, or auto-merge behavior is part of this workflow set.

## Workflow coverage

Workflow: `.github/workflows/ci-maven.yml` (`Maven CI`)

### Required checks (branch protection)

Current required check for `develop` remains:

- `CI / validate (zulu-8)` (from `.github/workflows/ci.yml`)

The `Maven CI` workflow is additive in this PR. It does not supersede the
existing required `CI` check until repository governance and the ruleset are
updated together.

After that governance/ruleset update, require:

- `Maven CI / validate-java8`
- `Maven CI / deterministic-tests-java8`
- `Maven CI / compile-and-package-java8`
- `Dependency Review / dependency-review`

### Diagnostic checks (do not require)

Do not require:

- `Maven CI / compatibility-java11`
- `Maven CI / compatibility-java17`
- `Maven CI / compatibility-java21`
- `Maven CI / compatibility-java25`
- `Maven CI / full-test-suite`

## Security workflows

- `Dependency Review` (`.github/workflows/dependency-review.yml`) runs on pull
  requests and fails when newly introduced dependencies have `high` or
  `critical` vulnerabilities.
- `CodeQL` default setup runs as repository code scanning and reports alerts in
  GitHub code scanning.

## Maintenance automation workflows

- `Dependabot` (`.github/dependabot.yml`) opens pull requests for Maven and
  GitHub Actions updates on a weekly schedule.
- `Pull request labeler` (`.github/workflows/labeler.yml`) applies labels by
  changed file paths.
- `Stale triage` (`.github/workflows/stale.yml`) labels inactive issues and
  pull requests without auto-closing them.

Labeler and stale triage are operational helpers and are not required status
checks for merging.

## Local command parity

Run the same required Java 8 commands locally:

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -pl fwk/api,fwk/framework,application/core,plugins/plugin-tester -am test
mvn -B -ntp -DskipTests package
```
