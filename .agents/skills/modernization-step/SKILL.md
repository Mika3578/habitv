---
name: modernization-step
description: >-
  Run one clear HabiTV modernization step (API, module, warning group, or
  defined JavaFX/JDK slice). Use for progressive migration work; never a
  whole-repo modernization in one session.
disable-model-invocation: true
---

# Modernization step

**One modernization step = one clear objective.**

Do not attempt a general repository modernization in a single session.

## Choose one objective

Examples of a valid step:

- migrate one concrete API or dependency call site group;
- modernize one component or module slice;
- clear one coherent warning group;
- JavaFX/OpenJFX or JDK work for a **defined** perimeter only.

## Workflow

1. State the single objective and out-of-scope list up front.
2. Keep the file/module perimeter small and reviewable.
3. Prefer a dedicated branch/PR when the change is migration-sized
   ([git-workflow](../git-workflow/SKILL.md)).
4. Implement only what the objective requires; no drive-by refactors.
5. Validate with targeted builds/tests for that perimeter.
6. Stop when the stated step is done and verified. Further steps wait for
   a new task.

## Guardrails

- Current merge baseline remains **Java 8** until an explicit migration
  changes compiler and required CI (`docs/development.md`,
  `docs/modernization.md`).
- Single-concern migrations may span the modules that concern requires;
  still one concern per PR/session.
- Policy: [`AGENTS.md`](../../../AGENTS.md).
