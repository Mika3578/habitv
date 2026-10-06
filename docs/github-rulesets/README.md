# GitHub rulesets (admin payloads)

JSON under this directory documents the intended `protect-develop.json`
ruleset for the `develop` branch only. Apply changes in the GitHub UI or
Rulesets API; files here are not applied automatically.

Live snapshot (2026-10-05): `protect-develop` requires one generic
approving review (any actor, including review apps), dismisses stale
reviews on push, requires thread resolution, squash only, and required
checks `validate-java8`, `deterministic-tests-java8`,
`compile-and-package-java8`, and `dependency-review`. Bypass is off.

A generic review count is **not** a trusted merge gate. CodeRabbit may
`APPROVE` as an iterative reviewer; that must not complete merge.

## Maintainer GitHub clicks (required)

This repository is a user-owned repo: ruleset `required_reviewers` is
team-only and cannot pin a human. Copilot `APPROVED` is not reliable.
Do **not** add bypass actors or paid review products.

Apply all of the following in the GitHub UI (Settings). The in-repo JSON
does not change hosted rules by itself.

### 1. Turn off repository auto-merge

1. Open **Settings → General → Pull Requests**.
2. Uncheck **Allow auto-merge**.
3. Keep squash as the only allowed merge method (matches the ruleset).

Agents must not re-enable auto-merge.

### 2. Protect environment `merge-develop`

1. Open **Settings → Environments → New environment**.
2. Name it exactly `merge-develop`.
3. Enable **Required reviewers** and add `Mika3578` only.
4. Do **not** enable wait timer unless you want extra delay.
5. Do **not** allow administrators or apps to bypass this environment.
6. Save.

Until this environment exists with that required reviewer, the
`maintainer-merge-gate` job must fail closed.

### 3. Add the required check (do not drop existing checks)

1. Open **Settings → Rules → `protect-develop`**.
2. Under required status checks, **add** `maintainer-merge-gate`.
3. **Keep** `validate-java8`, `deterministic-tests-java8`,
   `compile-and-package-java8`, and `dependency-review`.
4. Keep **Dismiss stale pull request approvals when new commits are pushed**.
5. Keep **Require conversation resolution before merging**.
6. Keep **Required approvals: 1** (do not set to 0).
7. Keep squash only. Do **not** add bypass actors.
8. Save.

After this, a Ready PR still needs CI plus one GitHub approval (which
may be CodeRabbit) **and** a maintainer approval of the `merge-develop`
deployment for the current HEAD. A new commit starts a new run; the
previous environment approval does not apply.

### 4. Cursor automations

1. Open the **Pull Request Router and Approver** automation.
2. Disable any merge or auto-merge action.
3. Do not run agents whose task is to arm auto-merge.

## `protect-develop.json` — other follow-ups

After agent-policy CI is stable on `develop`, consider also adding:

- `agent-policy`
- `agent-policy (windows)`

| Setting | Intended payload |
|---------|------------------|
| `required_approving_review_count` | `1` (not a trusted final gate) |
| `required_review_thread_resolution` | `true` |
| `dismiss_stale_reviews_on_push` | `true` |
| `require_last_push_approval` | `false` |
| Copilot `review_on_push` | `false` |
| Copilot `review_draft_pull_requests` | `false` |

Do not enable Copilot automatic review on every push or on Draft PRs.
The PR orchestrator requests Copilot **once after Ready** on that HEAD.
Do **not** add CodeRabbit as a required status check.

Draft review commands (`@coderabbitai review`, `@coderabbitai full review`,
`/q review`, optional `@sourcery-ai review`) are posted by the PR
orchestrator. They are not maintainer chores and not merge gates.

Amazon Q is a Draft secondary reviewer on a **stabilized** HEAD after
CodeRabbit full review. Sourcery is opportunistic. Neither is a merge
gate.

## Merge method

Default: maintainer **squash merge** in the GitHub UI after
`maintainer-merge-gate` is green on the current HEAD. Leave auto-merge
off so an app cannot complete merge when a review bot approves.

## External PR description tools

Third-party PR description footers are tolerated (not a CI or Ready gate).
Optional: disable Sourcery **Enable pull request summary** in the dashboard
to reduce noise. cubic: no in-repo `cubic.yaml`; use the cubic dashboard if
needed.

## Merge queue (deferred)

GitHub merge queues can reduce integration friction when many provider PRs
land in parallel. Enabling a queue is **out of scope for agent-rules PRs**;
workflows would need `merge_group` event support and a dedicated governance
change. Document only until a maintainer adopts it.
