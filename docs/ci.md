# Maven CI

## Baseline and intent

- Java 8 is the current required baseline for merge-blocking checks.
- Java 11, 17, 21, and 25 are diagnostic compatibility checks only.
- Java 11+ may fail until JAXB and JavaFX migration is complete.
- No deployment, credentials, publishing, or auto-merge behavior is part of this workflow.

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

### Diagnostic checks (do not require)

Do not require:

- `Maven CI / compatibility-java11`
- `Maven CI / compatibility-java17`
- `Maven CI / compatibility-java21`
- `Maven CI / compatibility-java25`
- `Maven CI / full-test-suite`

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
