---
name: pr-review
description: Keeps a pull request in Draft until review, CI, full validation, and user functional gates pass on the latest commit. Use when addressing review comments or preparing a PR for Ready.
---

# Pull request review loop

Authoritative invariants: [`AGENTS.md`](../../../AGENTS.md) (**Pull request lifecycle**).
Template: [`.github/pull_request_template.md`](../../../.github/pull_request_template.md).
Public replies: [public-git-text](../public-git-text/SKILL.md).

Before opening a new pull request, complete branch setup in
[`git-workflow`](../git-workflow/SKILL.md).

## Authorization (this PR)

When the user asks to finish a PR, address reviews, process findings, or
prepare for Ready **for a named PR**, that includes for that PR only:

- concise replies on existing review threads;
- resolving threads after the resolution gate below;
- re-fetching thread state to confirm `isResolved=true`.

Separate approval is still required for merge, closing unrelated issues/PRs,
branch deletion, and force operations outside the approved workflow.

## Review item lifecycle

Each finding moves through:

```text
DISCOVERED → EVALUATED → FIXED or REJECTED_WITH_REASON
    → VALIDATED → PUSHED (when remote) → REPLIED → RESOLVED (verified)
```

Code findings (typical):

```text
edit → focused validation → commit/push → verify fix on remote HEAD
    → reply (with commit SHA when useful) → resolve → verify isResolved
```

Rejected findings: `evaluate → evidence → reply → resolve → verify`.

Never resolve without a disposition reply. Never resolve because a bot
comment looks stale. Never open a duplicate thread when the same concern
already has one.

## Review round (HEAD-bound)

Record `REVIEW_HEAD=<40-char SHA>` at the start of a round.

Before reporting “review complete”, “ready”, or “approved on HEAD”, fetch
the live PR HEAD again. If `current HEAD != REVIEW_HEAD`, the round is
stale; repeat validation and review for the new SHA.

After fixing findings in a round: validate, push once, reply, resolve
qualifying threads, confirm CI on that HEAD, then **re-request the final
reviewer once** for that HEAD. Do not re-request after every intermediate
commit.

## Inventory (start and end of each round)

Fetch live GitHub state for the target PR:

- metadata, title, body, draft flag, branch, HEAD SHA;
- review submissions and requested reviewers;
- inline review threads (`isResolved`, comments);
- top-level issue/PR conversation comments;
- check runs / statuses.

Classify each item. Do not ignore top-level comments because they are not
inline threads.

Deduplicate: if a new bot finding matches an existing thread, continue that
thread; do not count it twice.

## Status checks vs substantive review

Green CI, policy validators, or a skipped bot status does **not** prove a
substantive code review ran. Track separately:

`checks` · `review submissions` · `thread findings` · `approvals`

## Live reconciliation before Ready

Immediately before recommending or marking Ready, re-fetch the PR from
GitHub. Confirm:

- no unresolved actionable review threads;
- live body passes [`scripts/validate-pr-public-body.sh`](../../../scripts/validate-pr-public-body.sh)
  (or `.ps1`) — remove forbidden generated blocks if maintenance is in scope;
- required checks green on **current** HEAD;
- final substantive review (when required) targets **current** HEAD.

Local files alone are not sufficient.

## Loop (summary)

```text
PR stays Draft
    ↓
inventory all feedback (round opens: REVIEW_HEAD)
    ↓
evaluate each finding; fix or reject with evidence
    ↓
validate; push when needed; reply; resolve; verify threads
    ↓
CI green on current HEAD
    ↓
one final review request for that HEAD (when applicable)
    ↓
functional test + explicit user confirmation when applicable
    ↓
live reconciliation gate
    ↓
Ready only with explicit authorization
```

## Latest HEAD

Evaluate the **current PR head commit**, not historical checks or approvals.

## Forbidden without conversation authorization

Merge, marking Ready without completing gates, closing unrelated PRs,
destructive branch operations, and force-push outside approved workflow.
See **Git Workflow** in `AGENTS.md`.

## Do not declare completion while

- Any review item is unexamined.
- Any actionable finding is unfixed and not rejected with evidence.
- Any qualifying thread lacks a reply or remains unresolved.
- Required checks are pending or failing on the latest commit.
- Full-reactor validation is required but missing.
- Functional testing is required but unconfirmed.
- Live PR body contains forbidden generated summary blocks.
- `REVIEW_HEAD` does not match live PR HEAD when claiming review completeness.

## Functional validation

When runtime behavior may change, provide a concise manual test procedure and
report `Functional validation: PENDING USER TEST` until the user explicitly
confirms success in the current conversation.

Verification commands: [`code-change-verification`](../code-change-verification/SKILL.md).
