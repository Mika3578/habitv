# Architecture Decision Records (ADR)

Habitv uses ADR-style decision records for durable architectural and
process decisions.

## Where ADRs live

| Location | Use |
|----------|-----|
| [`docs/decision-log.md`](../decision-log.md) | **Primary** append-only log with dashboard (existing ADRs) |
| `docs/adr/<slug>.md` | Optional standalone ADR files for large decisions |

New standalone files should be linked from the dashboard in
`decision-log.md` in the same PR.

## When to write an ADR

- Java baseline or migration strategy
- Provider/platform strategy
- Packaging or release approach
- Dependency automation (Dependabot/Renovate)
- CI/security workflow changes
- AI workflow policy changes (see also Section 13 meta-rules)
- Config migration or breaking user-facing defaults

Do **not** use ADRs for temporary implementation notes or PR-only rationale.

## ADR template

```markdown
# Title

| | |
|---|---|
| **Status** | proposed \| accepted \| superseded \| rejected |
| **Date** | YYYY-MM-DD |
| **Related PRs** | #123 |

## Context

## Decision

## Consequences

## Alternatives considered

## Follow-up
```

## Status workflow

- **proposed** — under review
- **accepted** — in force
- **superseded** — replaced; link to successor ADR
- **rejected** — explicitly not adopted

Supersede rather than rewrite. Canonical rule: `AGENTS.md` Section 19.1.
