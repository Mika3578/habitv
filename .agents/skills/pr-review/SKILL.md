---
name: pr-review
description: Keeps a pull request in Draft until review, CI, full validation, and user functional gates pass on the latest commit. Use when addressing review comments or preparing a PR for Ready.
---

# Pull request review loop

Authoritative policy: [`AGENTS.md`](../../../AGENTS.md) (**Pull request lifecycle**,
**Git Workflow**). Template: [`.github/pull_request_template.md`](../../../.github/pull_request_template.md).

Before opening a new pull request, complete branch setup in
[`git-workflow`](../git-workflow/SKILL.md).

## Loop

```text
PR stays Draft
    ↓
collect all review sources (human, Copilot, bots)
    ↓
inspect every comment/thread/finding individually
    ↓
fix valid findings
    ↓
document/reject invalid findings with evidence
    ↓
run focused validation
    ↓
run final required full validation when applicable
    ↓
verify required GitHub checks on latest HEAD
    ↓
re-check unresolved threads
    ↓
request/perform real-user functional test if applicable
    ↓
wait for explicit user confirmation in the current conversation
    ↓
re-verify current HEAD after any new commit
    ↓
only then eligible for Ready (with explicit authorization)
```

## Latest HEAD

Evaluate the **current PR head commit**, not historical checks or approvals.
A prior green CI run or approval does not validate later commits.

## Forbidden without conversation authorization

Posting GitHub comments, resolving threads, marking Ready, merging, pushing,
or force-pushing. See **Git Workflow** in `AGENTS.md`.

## Do not declare completion while

- Any review item is unexamined.
- Any actionable finding remains unfixed or undocumented.
- Required checks are pending or failing on the latest commit.
- Full-reactor validation is required but missing.
- Functional testing is required but unconfirmed.
- New commits landed after validation without re-running gates.

## Functional validation

When runtime behavior may change, provide a concise manual test procedure and
report `Functional validation: PENDING USER TEST` until the user explicitly
confirms success in the current conversation. Automated tests and agent
inspection are not substitutes.

Verification commands: [`code-change-verification`](../code-change-verification/SKILL.md).
