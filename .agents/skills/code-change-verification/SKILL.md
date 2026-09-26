---
name: code-change-verification
description: Runs focused then full Maven validation and records evidence before a PR may leave Draft. Use when validating Java, POM, workflow, provider, or packaging changes.
---

# Code change verification

Canonical build baseline and commands: [`docs/development.md`](../../../docs/development.md).
Policy gates: [`AGENTS.md`](../../../AGENTS.md) (**Testing and Validation**,
**Pull request lifecycle**). PR orchestration:
[pr-review](../pr-review/SKILL.md).

## Workflow

```text
focused test (module or narrow command)
    ↓
broader test as needed
    ↓
final full-reactor validation (when applicable)
    ↓
required GitHub checks on current HEAD
    ↓
real-user functional test when runtime behavior changed
```

## Rules

- Focused commands accelerate development; they do **not** replace the final
  full-reactor gate when the PR touches executable code, build logic, runtime
  behavior, providers, download logic, packaging, startup, or UI.
- Use the canonical full-reactor commands from
  [`docs/development.md`](../../../docs/development.md) (CI mirrors
  `mvn -B -ntp -DskipTests validate`, deterministic tests, and
  `mvn -B -ntp -DskipTests package` on required checks).
- Record the exact command and outcome in the PR body. Do not claim success
  without actual output.
- Real-user functional validation is separate; see **Pull request lifecycle**
  in `AGENTS.md` and [`.agents/skills/pr-review/SKILL.md`](../../../.agents/skills/pr-review/SKILL.md).

## Docs-only exemption

Only documentation or agent-configuration changes that cannot affect
executable behavior may use `git diff --check` (and relevant config
validation) instead of the full-reactor gate. Workflow, build, or CI logic
changes are not exempt. State the exemption explicitly in the PR.
