# CI and security checks

## Baseline and intent

- **Java 8** is the required compile and merge baseline for `develop`.
- Java 11, 17, 21, and 25 jobs are **diagnostic only** (may fail until JAXB and
  JavaFX migration completes).
- No deployment, credential publishing, or auto-merge from bots.

## Workflows

| Workflow | File | Role |
|----------|------|------|
| CI | `.github/workflows/ci.yml` | Legacy required check for `develop`: `CI / validate (zulu-8)` |
| Maven CI | `.github/workflows/ci-maven.yml` | `develop` PR validation: validate, deterministic tests, package |
| Build | `.github/workflows/build.yml` | Legacy `master` push/PR coverage only; not part of the `develop` merge baseline |
| Dependency Review | `.github/workflows/dependency-review.yml` | Blocks new high/critical dependency issues on PRs |
| CodeQL | (repository default setup) | Code scanning alerts |
| Labeler | `.github/workflows/labeler.yml` | Path-based PR labels (not required) |
| Stale | `.github/workflows/stale.yml` | Inactivity labels (no auto-close) |

### Required checks today

Branch protection on `develop` currently requires:

- `CI / validate (zulu-8)` from `.github/workflows/ci.yml`

### Planned required checks (after ruleset update)

When governance catches up (`branch-protection` tracker item), require:

- `Maven CI / validate-java8`
- `Maven CI / deterministic-tests-java8`
- `Maven CI / compile-and-package-java8`
- `Dependency Review / dependency-review`

### Diagnostic checks (do not require)

- `Maven CI / compatibility-java11` (and 17, 21, 25)
- `Maven CI / full-test-suite` (workflow_dispatch / schedule only)

## Live network tests

Default `mvn test` **excludes** live provider tests (`*PluginManagerTest`, etc.).
Use opt-in profile:

```bash
mvn -B -ntp test -Plive-provider-tests
```

Prefer offline fixture tests for provider changes; quarantine or replace live
tests over time (`provider-inventory`, `live-tests-flaky` risk).

## Maintenance automation

- **Dependabot** (`.github/dependabot.yml`) — weekly Maven and GitHub Actions
  update PRs; semver-major ignored; no auto-merge.
- **Dependency Review** — fails PRs that introduce new high/critical vulns in
  dependencies.
- **CodeQL** — separate from Maven CI.

Details: [`repository-maintenance.md`](repository-maintenance.md).

## Local command parity

Before opening a PR:

- Docs-only PR: run `git diff --check`.
- Default code PR: run `mvn -B -ntp -DskipTests validate`.
- Stronger local commands are scoped and optional unless the PR
  explicitly targets CI/build behavior.

Scoped Java 8 command set (when needed):

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -pl fwk/api,fwk/framework,application/core,plugins/plugin-tester -am test
mvn -B -ntp -DskipTests package
```

Paste **exact** command output in the PR body (exit code and relevant lines).

If a command fails on latest `develop` for a reason unrelated to your change,
document the failure honestly in the PR and link the tracker/risk item.
