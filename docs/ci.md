# CI and security checks

## Baseline and intent

- Java 8 is the current required baseline for merge-blocking checks.
- Java 11, 17, 21, and 25 are diagnostic compatibility checks only.
- Java 11+ may fail until JAXB and JavaFX migration is complete.
- No deployment, credentials, publishing, or auto-merge behavior is part of this workflow set.

**Staged required-checks plan:** see [`required-checks-roadmap.md`](required-checks-roadmap.md)
(tracker `required-checks-roadmap`, HBTV-017) for Phases 0–5, prerequisites,
and branch-protection targets.

## Workflow coverage

Workflow: `.github/workflows/ci-maven.yml` (`Maven CI`)

### Phase 0 — Required checks (branch protection target)

Current required check for `develop` may still be:

- `CI / validate (zulu-8)` (from `.github/workflows/ci.yml`)

The `Maven CI` workflow does not supersede the legacy `CI` check until
repository governance and the ruleset are updated together
(`branch-protection`, `required-checks-roadmap`).

**Target required checks** (stable Java 8 baseline — Liberica JDK 8 + JavaFX):

- `Maven CI / validate-java8`
- `Maven CI / deterministic-tests-java8`
- `Maven CI / compile-and-package-java8`
- `Dependency Review / dependency-review`

### Not required yet (Phases 1–4)

Do not require until the roadmap prerequisites are met:

- `Maven CI / compatibility-java11` through `compatibility-java25` (modern JDK
  **validation** in Phase 1; **package** only after OpenJFX in Phase 2)
- `Maven CI / full-test-suite` (Phase 3 — deterministic default tests only)
- Live provider tests (`-Plive-tests` when introduced)
- Dependency Review / CodeQL as required gates (Phase 4)

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

## Validation results

### Command: `mvn -B -ntp -DskipTests validate`

Exit status: `0`

Relevant output excerpt:

```text
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Command: `mvn -B -ntp -pl fwk/api,fwk/framework,application/core,plugins/plugin-tester -am test`

Exit status: `0`

Relevant output excerpt:

```text
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Command: `mvn -B -ntp -DskipTests package`

Exit status: `0`

Relevant output excerpt:

```text
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

If one of these commands fails because of a pre-existing baseline blocker
that reproduces on latest `develop` and is unrelated to this CI workflow or
documentation change, record the exact failing command, exit status, and
relevant error excerpt here and in the pull request body.
