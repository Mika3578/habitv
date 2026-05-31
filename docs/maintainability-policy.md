# Habitv maintainability policy

Canonical source: [`AGENTS.md`](../AGENTS.md) Section 19.

## Definition of Done

Declare before implementation (Section 19.2). If any item cannot be met,
status is **Not ready to commit**.

## Dependency provenance

Required when adding or updating dependencies or external tools:

| Field | Required |
|-------|:--------:|
| name | yes |
| ecosystem (Maven, npm, binary) | yes |
| source repository or official site | yes |
| license | yes |
| old version | if update |
| new version | if update |
| Java 8 compatibility | yes |
| security reason | if applicable |
| release notes checked | yes/no |
| known migration notes | if any |
| validation performed | yes |

No random mirrors or untrusted binary downloads.

## Manual test evidence

Before commit approval on behavior changes:

```markdown
* Scenario tested:
* Command or UI path:
* Input:
* Expected result:
* Actual result:
* Screenshot/log needed: yes/no
* Remaining uncertainty:
```

If not done: **Ready for developer manual testing** (not commit approval).

## PR risk levels

| Level | Examples | Validation |
|-------|----------|------------|
| low | docs, narrow isolated fix | focused validation |
| medium | provider, Maven config, CI, dep patch/minor | targeted + full Maven |
| high | dep major, Java migration, packaging, config migration | full + manual + review |
| critical | security, release, protected content/auth, destructive migration | dedicated plan + decision |

## Rollback plan (medium+)

```markdown
* Files to revert:
* Command or PR strategy:
* User impact:
* Data compatibility concerns:
* Safe after release: yes/no
```

## Test failure classification

- related to current change
- unrelated existing failure
- flaky
- environment/network-dependent
- skipped by design
- needs investigation

Do not disable tests without developer approval.

## XML / user config

See Section 19.10–19.11. Preserve backward compatibility; test old config
files; document default changes.

## Scripts

Windows-first validation (19.13); cross-platform `.sh`/`.ps1` parity (19.14).
