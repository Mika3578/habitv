# Documentation agent instructions

Extends the root [`AGENTS.md`](../AGENTS.md). When they conflict, the
root file wins.

## Scope

This file applies to work under `docs/` and other documentation-only
changes (Markdown, tracker JSON mirrors, ADRs, audit baselines).

## Path-specific rules

- Update `docs/dev-tracker.md` AND `docs/dev-tracker.json` together when
  tracker state changes (root `AGENTS.md` Section 12).
- Keep slug ids aligned between `.md` and `.json` tracker mirrors.
- Append-only for `docs/risk-register.md` and `docs/decision-log.md`;
  supersede rather than rewrite ADRs.
- English only for all new documentation.
- Verify version/Java/workflow claims match repository state (Section 16.21).
- Link to `docs/provider-policy.md`, `docs/release-policy.md`,
  `docs/modernization-backlog.md`, `docs/obsolescence-register.md`, and
  `docs/maintainability-policy.md` for Habitv-specific agent topics
  (Sections 18–19).
- AI rule changes require version bump and changelog entry (Section 17).
- For docs-only AI-rule work, use speed mode (Section 20.8); run drift
  audit when rules change (Section 17.6).
- Prefer backlog over new mandatory rules (Section 20.10).
- Do not use deprecated `hbtv-*` or `HBTV*` IDs in new headings or
  active tracker slugs.
- No runtime behavior claims without citing source code or tracker items.
- No AI attribution in documentation (root `AGENTS.md` Section 15.10).

## Validation (docs-only)

When only documentation changes:

- verify Markdown formatting manually;
- verify no contradictory duplicate AI rules;
- verify instruction files still point to the same workflow;
- confirm no Java source, POM, dependency, workflow, or runtime file
  changed.

Maven is not required unless build files changed. Set repo health status
per root `AGENTS.md` Section 15.4.

## Instruction loading

At task start, report **Instruction files loaded** including this file
and root `AGENTS.md` Section 15.1.
