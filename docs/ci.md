# CI and security checks

## Baseline and intent

- **Java 8** is the required compile and merge baseline for `develop`
  (`source`/`target` **1.8**; supported **runtime** baseline).
- Java 11, 17, 21, and 25 jobs are **diagnostic only** (build-host checks;
  not proof of Java 11/17/21 **runtime** support). JDK 11+ may compile GUI
  modules using the provided OpenJFX profile ([PR #100](https://github.com/Mika3578/habitv/pull/100));
  end-user runtime migration is tracked under `javafx-modernization`. See
  [`java-runtime-policy.md`](java-runtime-policy.md).
- No deployment, credential publishing, or auto-merge from bots.

## Workflows

| Workflow | File | Role |
|----------|------|------|
| Maven CI | `.github/workflows/ci-maven.yml` | `develop` PR validation: validate, deterministic tests, package |
| Build | `.github/workflows/build.yml` | Legacy `master` push/PR coverage only; not part of the `develop` merge baseline |
| Dependency Review | `.github/workflows/dependency-review.yml` | Blocks new high/critical dependency issues on PRs |
| CodeQL | (repository default setup) | Code scanning alerts |
| Labeler | `.github/workflows/labeler.yml` | Path-based PR labels (not required) |
| Stale | `.github/workflows/stale.yml` | Inactivity labels (no auto-close) |

### Required checks today

The live `protect-develop` ruleset requires these exact status check contexts:

- `validate-java8`
- `deterministic-tests-java8`
- `compile-and-package-java8`
- `dependency-review`

The first three are Maven CI checks (`ci-maven.yml`). `dependency-review` is
provided by Dependency Review (`dependency-review.yml`). Do not use
workflow-prefixed check names (for example `Maven CI / validate-java8`) unless
GitHub Settings later displays them that way. The removed legacy check was
`validate (zulu-8)` / `CI / validate (zulu-8)`.

### Diagnostic checks (do not require)

- `compatibility-java11` (and 17, 21, 25)
- `full-test-suite` (workflow_dispatch / schedule only)
- `validate-macos` (cross-platform Java 8 validate signal; complements the
  Windows `build.yml` signal — informational only, not in `protect-develop`)

## Reusable composite actions

Shared Maven CI steps live under `.github/actions/` so the jobs stay DRY
(inspired by the `spring-petclinic` / Apache Commons reusable-build pattern):

| Action | Role |
|--------|------|
| `setup-build-jdk` | Set up a JDK (`distribution`, `java-version`, `java-package` inputs) with the Maven cache, then print `java`/`mvn` versions and `git status` |
| `upload-maven-artifacts` | Upload Surefire reports and built jars under a per-job `name` |

## Runner hardening

Every `actions/checkout` step sets `persist-credentials: false`. No CI job
pushes to git or reuses the workflow token after checkout, so the persisted
credential is dropped to shrink the supply-chain surface (Apache Commons /
OpenSSF Scorecard practice).

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
