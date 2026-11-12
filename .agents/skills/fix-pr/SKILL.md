---
name: fix-pr
description: >-
  Address a specific open pull request (diff, checks, or review comments)
  without auditing the whole HabiTV repository. Use when fixing CI, review
  findings, or a named PR issue.
disable-model-invocation: true
---

# Fix PR

Work one named PR. Do not expand into unrelated cleanup.

## Workflow

1. Read the PR description, current HEAD diff, failing checks, and relevant
   review comments only.
2. Identify exactly what blocks the requested outcome.
3. Stay inside that PR’s scope and files.
4. Do not fix unrelated problems discovered along the way (report them).
5. Make the minimal coherent change; follow **Public git text** for any
   commit or reply.
6. Run targeted validation for the touched modules.
7. If review threads are in scope, follow
   [pr-review](../pr-review/SKILL.md) (reply before resolve).
8. Stop when the requested PR problem is resolved.

## Notes

- Full Draft→Ready orchestration belongs in [pr-review](../pr-review/SKILL.md).
- Merge / Ready still need explicit user authorization
  ([`AGENTS.md`](../../../AGENTS.md)).
