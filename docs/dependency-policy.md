# Dependency update policy

Canonical source: [`AGENTS.md`](../AGENTS.md) Section 16.3 and Section 2
hard rules.

## Scope

This policy applies to Maven dependencies, Maven plugins, GitHub Actions,
and tooling dependencies managed through the repository.

## Principles

- **Focused PRs** — one dependency topic per PR when possible.
- **No mixing** — do not combine dependency updates with feature work.
- **Java 8 first** — preserve Java 8 compatibility unless a dedicated
  migration task and ADR authorize otherwise.
- **Full validation** — run full Maven validation after any dependency
  change.

## Update categories

| Category | Example | Typical risk |
|----------|---------|--------------|
| security patch | CVE fix | high priority |
| patch update | bugfix release | low |
| minor update | new features, compatible API | medium |
| major update | breaking API | high — needs release notes |
| plugin/tooling update | Maven plugin bump | medium–high |
| GitHub Actions update | `actions/checkout@vN` | medium |

## Dependabot

Configuration: [`.github/dependabot.yml`](../.github/dependabot.yml)

- targets `develop`
- limits open PRs to reduce noise
- groups Maven minor/patch and GitHub Actions updates
- ignores major JAXB/mail bumps that would violate Java 8 / no-jakarta rules
- **no auto-merge** unless developer explicitly enables it

Inspect Dependabot config before changing dependency automation.

## Required report

Before commit approval on dependency PRs, provide the **Dependency update
report** (Section 16.3):

- dependency name, old/new version, update type, reason
- Java 8 compatibility assessment
- release notes/changelog checked (yes/no)
- security impact
- validation commands with exact results
- known risks

## Analysis commands

```bash
mvn -B -ntp dependency:tree
mvn -B -ntp versions:display-dependency-updates
mvn -B -ntp versions:display-plugin-updates
mvn -B -ntp -DskipTests clean package
mvn -B -ntp test
```

## Forbidden without tracker + ADR

See `AGENTS.md` Section 2: reactor restructure, Java baseline bump,
JavaFX migration, JAXB regeneration, youtube-dl → yt-dlp behavior
replacement, and similar high-blast-radius changes.

## Dependency Review

PRs that change dependencies should pass
[`.github/workflows/dependency-review.yml`](../.github/workflows/dependency-review.yml)
when available (fails on high severity).

If Dependency Review is unavailable on the repository plan, document that
limitation in the PR body.

## Dependency provenance (Section 19.6)

Document name, ecosystem, source, license, versions, Java 8 compatibility,
security reason, release notes, migration notes, validation. Template in
[`docs/maintainability-policy.md`](maintainability-policy.md).

## Renovate vs Dependabot (Section 19.5)

Current: Dependabot only (`.github/dependabot.yml`). Renovate requires a
dedicated PR. Never auto-merge without explicit approval.

## Plugin versioning

Plugin module version bumps must follow `plugin-versioning-policy` in
[`CONTRIBUTING.md`](../CONTRIBUTING.md).
