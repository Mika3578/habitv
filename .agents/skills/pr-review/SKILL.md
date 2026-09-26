---
name: pr-review
description: Canonical PR orchestrator — Draft through Ready on current HEAD. Use when continuing, reviewing, or finishing a specific pull request.
---

# PR orchestrator

Constitutional invariants: [`AGENTS.md`](../../../AGENTS.md) (**Pull request lifecycle**).
Template: [`.github/pull_request_template.md`](../../../.github/pull_request_template.md).
Public replies: [public-git-text](../public-git-text/SKILL.md).
Verification: [code-change-verification](../code-change-verification/SKILL.md).
New PR setup: [git-workflow](../git-workflow/SKILL.md).

This skill is the **only** canonical end-to-end PR procedure. Do not add
parallel PR-management skills.

## Operating model

```text
small global invariants (AGENTS.md)
    → this orchestrator
    → deterministic scripts
    → live GitHub reconciliation
    → independent review (read-only)
    → bounded feedback loop
    → explicit Ready gate
```

Mechanical facts (HEAD SHA, thread counts, body markers, check names) come
from scripts and `gh` — not from model memory.

## Single-writer rule

During a review cycle for one PR, **only the orchestrating agent** may:

- edit PR title/body or Draft/Ready;
- post disposition replies on review threads;
- resolve threads;
- request reviewers.

**Independent reviewers** (subagents, Copilot, humans as consulted) are
**read-only** on GitHub. They may inspect diff, tests, and context and
return findings. They must not post duplicate PR comments, resolve threads,
change PR metadata, request reviewers, or merge.

The orchestrator deduplicates, adjudicates, batches fixes, publishes,
replies, resolves, and reconciles live state.

## Local durable state (resumability)

Store operational metadata only under `agent_space/pr-<number>/` (gitignored).
Never commit it. Never cite it in public git text.

Live GitHub state **overrides** stale local files.

Example `state.json` shape:

```json
{
  "repository": "Mika3578/habitv",
  "pr_number": 0,
  "base": "develop",
  "head_sha": "",
  "review_round": 1,
  "review_head": "",
  "findings": [
    {
      "id": "thread-or-comment-id",
      "source": "copilot|human|bot",
      "classification": "BLOCKING",
      "disposition": "FIXED",
      "fix_commit": "",
      "validation": "pass|fail|pending",
      "replied": true,
      "resolved": true
    }
  ]
}
```

No credentials, tokens, or session data.

## Authorization

When the user authorizes **finish this PR**, **address this PR's reviews**,
or **prepare this PR for final review**, that includes for **that PR only**:

- adjudicating and fixing findings;
- concise replies on existing threads;
- resolving verified threads;
- re-fetching GitHub state.

Still requires separate approval: merge, unrelated issues/PRs, branch
deletion, unrelated force operations, creating GitHub issues (unless
explicitly authorized).

## Orchestration phases

```text
INTAKE → SNAPSHOT → INVENTORY → ADJUDICATE → FIX_BATCH → VERIFY
    → PUBLISH → REPLY → RESOLVE → LIVE_RECONCILE → FINAL_REVIEW → READY_GATE
```

| Phase | Purpose |
|-------|---------|
| INTAKE | Confirm repo, PR number, user scope, risk tier, round number |
| SNAPSHOT | Record `REVIEW_HEAD=<40-char SHA>`; run [`scripts/pr-gh-snapshot.sh`](../../../scripts/pr-gh-snapshot.sh) (or `.ps1`) |
| INVENTORY | Classify all feedback surfaces (see below) |
| ADJUDICATE | Assign disposition per finding; deduplicate |
| FIX_BATCH | Implement accepted fixes in one coherent batch |
| VERIFY | Run applicable validation ([code-change-verification](../code-change-verification/SKILL.md)) |
| PUBLISH | Commit/push; confirm remote HEAD contains fixes |
| REPLY | Concise disposition in **existing** thread (commit SHA when useful) |
| RESOLVE | GraphQL/REST resolve; re-query `isResolved` (not `isOutdated`) |
| LIVE_RECONCILE | Full live PR fetch before claiming cleanliness |
| FINAL_REVIEW | One independent review on exact current HEAD (tier-dependent) |
| READY_GATE | All gates on current HEAD; explicit user authorization to mark Ready |

## Finding lifecycle

```text
DISCOVERED → EVALUATED → FIXED | REJECTED | DUPLICATE | FOLLOW_UP
    → VALIDATED → PUBLISHED → REPLIED → RESOLVED
```

Editing a file does **not** complete a finding.

**FIXED (valid):** fix → validate → publish → verify on remote HEAD → reply →
resolve → confirm `isResolved=true`.

**REJECTED (invalid):** technical evidence → reply → resolve → confirm.

**DUPLICATE:** map to canonical thread/finding; no second comment.

**FOLLOW_UP:** legitimate but out of scope — record locally; open a GitHub
issue only when authorized; do not expand the PR.

## Classification vocabulary

| Class | Meaning |
|-------|---------|
| `BLOCKING` | Must fix or reject with evidence before Ready |
| `NON_BLOCKING` | May defer with documented reason if policy allows |
| `INVALID` | Reject with concise technical reason |
| `DUPLICATE` | Same concern as an existing thread |
| `FOLLOW_UP` | Out of scope for this PR |

Review tools supply evidence; the orchestrator adjudicates. Do not implement
every AI suggestion blindly.

## Batch fixes (default)

```text
collect complete round → adjudicate all → fix accepted batch
    → validate → publish once → reply → resolve eligible threads
    → request next substantive review once
```

Do not loop: one bot comment → one commit → one review request.

Exceptions: urgent blockers or findings that change implementation strategy.

## HEAD-bound review rounds

At SNAPSHOT set `REVIEW_HEAD` to the live PR HEAD.

Before reporting a clean review, green eligibility, or Ready: fetch HEAD
again. If `current HEAD != REVIEW_HEAD`, the round is stale — re-run
invalidated gates. Never report historical reviews as current.

Track `review_round` in local state. Increment when HEAD changes materially
after a published fix batch.

## Bounded review loops

If ~3 correction rounds target the same root rule/design area without
stability, **escalate**: question the condition, layer, or architecture;
restate policy instead of patching endlessly. This is not permission to
ignore valid defects.

## Risk-proportional independent review

Pick the **smallest** tier that safely covers the diff.

| Tier | Typical scope | Independent review |
|------|---------------|------------------|
| LIGHTWEIGHT | Formatting, spelling, non-semantic docs | Self-review acceptable |
| ORDINARY | Normal code, providers, tests, ordinary CI/config | One fresh-context independent review |
| HIGH_RISK | Credentials, process execution, updater/security, packaging/runtime, major Maven/JDK, shared cross-provider logic | Stronger independent coverage |

Independent reviewers must not inherit the implementer's conclusions.
Launch with fresh context and diff evidence only.

## Inventory (every round, start and end)

Fetch live GitHub state:

- HEAD SHA, base, branch, draft flag, title, body;
- review submissions and requested reviewers;
- inline threads (`isResolved`, `isOutdated`, comments);
- top-level issue/PR comments;
- check runs / statuses.

Use [`scripts/pr-gh-snapshot.sh`](../../../scripts/pr-gh-snapshot.sh) for a
deterministic baseline, then classify each item.

Deduplicate before acting on new bot output.

## Four different concepts (do not conflate)

| Concept | Meaning |
|---------|---------|
| **Green check** | CI/policy/static-analysis status succeeded |
| **Review executed** | A reviewer bot or human submitted a review or explicit skip/rate-limit comment |
| **Substantive review** | The reviewer examined the diff on the recorded SHA and reported findings or explicit no-findings |
| **Approval** | GitHub review state `APPROVED` (may be routing-only) |

`success` on CodeRabbit, Sonar, or Snyk does **not** prove a code review ran.
Read the **comment body**, not only the status context.

## Review execution state (per source)

Separate from **finding** classification (`BLOCKING`, etc.). For each
reviewer integration, record:

| State | Meaning |
|-------|---------|
| `SUBSTANTIVE` | Review ran on the SHA; findings or inline threads to adjudicate |
| `NO_FINDINGS` | Review ran on the SHA; explicit clean substantive result |
| `SKIPPED` | Tool declined (e.g. star threshold, draft policy) — **not** a clean review |
| `RATE_LIMITED` | Quota exhausted — **not** a gate |
| `SUMMARY_ONLY` | Generated guide/context — **not** a code review |
| `STATIC_ANALYSIS` | Quality gate / scanner — track separately |
| `APPROVAL_ONLY` | `APPROVED` or comment without diff review (e.g. “checks only”) |
| `PENDING` | Requested or advertised but no outcome yet |
| `STALE` | Review or finding tied to an older commit than current HEAD |
| `MISSING` | No submission on this PR / HEAD when one is required |

[`scripts/pr-gh-snapshot.sh`](../../../scripts/pr-gh-snapshot.sh) (`.ps1`)
applies deterministic hints via `pr-classify-review-sources.*`. The
orchestrator must still read live review and issue-comment bodies when
classifying edge cases.

Record `execution_state` per source in `agent_space/pr-<n>/state.json`.

### HabiTV defaults (from real PR experience)

- **Amazon Q:** valuable in Draft rounds; treat as substantive when findings
  exist; becomes `STALE` when `commit_id ≠ HEAD`.
- **CodeRabbit:** often `SKIPPED` on this repo (&lt;10 stars) — never count as
  clean review because status is green.
- **Sourcery:** `SUMMARY_ONLY` / `RATE_LIMITED` — opportunistic, not a gate.
- **SonarCloud:** `STATIC_ANALYSIS` only.
- **Cursor Approval Agent:** `APPROVAL_ONLY` when it approves from checks
  without Bugbot/substantive diff review — **does not** satisfy final review.
- **Copilot:** required **substantive** final reviewer when policy applies;
  must appear as a review submission with `commit_id == HEAD`.

## PR body ownership

The orchestrator owns the canonical PR description (template sections).
External tools may comment; do not rely on multiple tools rewriting the body.

Hygiene: live body must pass
[`scripts/validate-pr-public-body.sh`](../../../scripts/validate-pr-public-body.sh)
(`.ps1`). CI runs this on `pull_request` events including `edited`.

**Sourcery:** disable **Enable pull request summary** in Sourcery Review
Settings when possible ([`docs/github-rulesets/README.md`](../../../docs/github-rulesets/README.md)).

**cubic:** no in-repo `cubic.yaml`; use official dashboard/repo config only —
do not invent keys. If auto-description cannot be disabled, keep reconciling
the live body.

## Copilot final review (HabiTV model)

**Draft phase:** implementation, validation, auxiliary rounds (e.g. Amazon Q),
batch fixes. Auxiliary tools are not substitutes for the final reviewer.

**Final phase** — do not assume Ready triggers Copilot automatically:

```text
PRE_REVIEW_GATE  (threads, checks, body, scope)
    ↓
Ready  (only when PRE_REVIEW_GATE passes + user authorizes)
    ↓
REQUEST COPILOT EXPLICITLY  (gh pr edit --add-reviewer @copilot)
    ↓
verify a Copilot review submission exists
    ↓
verify review.commit_id == current HEAD
    ↓
classify execution_state (SUBSTANTIVE or NO_FINDINGS)
    ↓
FINAL_REVIEW_GATE
```

If Copilot is `MISSING` or `PENDING` after request, wait and re-fetch; do not
declare final review complete. If `SKIPPED` / `RATE_LIMITED`, record and
escalate to maintainer — do not treat as clean.

Do not request Copilot after every intermediate commit.

## Live reconciliation (READY_GATE prerequisites)

On current HEAD, confirm:

- scope complete and validation recorded;
- full reactor validation when required ([`docs/development.md`](../../../docs/development.md));
- required GitHub checks pass;
- zero actionable unresolved threads;
- PR body policy-clean;
- **final substantive review** on this HEAD when required: source actually
  ran, `execution_state` is `SUBSTANTIVE` or `NO_FINDINGS`, and
  `commit_id == HEAD` (not `APPROVAL_ONLY`, `SKIPPED`, or stale);
- functional test: user **explicitly confirms success in the current
  conversation** when runtime behavior may change — else report
  `Functional validation: PENDING USER TEST`;
- explicit user authorization to mark Ready.

```bash
scripts/pr-gh-snapshot.sh Mika3578/habitv <pr>
```

Local files alone are insufficient.

## Draft → Ready checklist (operational)

All items apply to **current PR HEAD** only:

1. Scope complete.
2. Focused tests pass when code changed.
3. Full reactor validation when applicable.
4. Required checks green on HEAD.
5. Every feedback item inventoried and adjudicated.
6. Every `BLOCKING` item fixed or rejected with evidence.
7. Qualifying threads replied and resolved (`isResolved` verified).
8. No actionable unresolved threads.
9. Live body passes `validate-pr-public-body`.
10. Final substantive review on HEAD: Copilot (or tier-equivalent) with
    `execution_state` ∈ {`SUBSTANTIVE`, `NO_FINDINGS`} and `commit_id == HEAD`.
11. User functional confirmation when applicable.
12. No newer commit invalidates the above.
13. Explicit authorization to mark Ready.

## Continuing an existing PR

1. INTAKE — user authorized this PR.
2. SNAPSHOT — `REVIEW_HEAD`, `pr-gh-snapshot`, update `agent_space/pr-<n>/state.json`.
3. INVENTORY + ADJUDICATE — all sources; map duplicates.
4. If work remains: FIX_BATCH through RESOLVE; one review request when round complete.
5. LIVE_RECONCILE + FINAL_REVIEW + READY_GATE.

## Deferred (not in every PR)

- **Merge queue:** future governance option for parallel provider PRs; requires
  `merge_group` workflow support — see
  [`docs/github-rulesets/README.md`](../../../docs/github-rulesets/README.md).

## Forbidden without conversation authorization

Merge, marking Ready before READY_GATE, unrelated GitHub mutations,
destructive branch ops, force-push outside approved workflow.
