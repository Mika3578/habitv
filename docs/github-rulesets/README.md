# GitHub rulesets (admin payloads)

JSON under this directory documents the intended `protect-develop.json`
ruleset for the `develop` branch only. Apply changes in the GitHub UI or
Rulesets API; files here are not applied automatically.

## `protect-develop.json` — recommended follow-ups

After agent-policy CI is stable on `develop`, consider adding required
status checks (exact context names from a green PR):

- `agent-policy`
- `agent-policy (windows)`

Recommended pull-request rule adjustments (verify current GitHub semantics
before enabling):

| Setting | Current payload | Recommended |
|---------|-----------------|-------------|
| `required_review_thread_resolution` | `true` | keep `true` |
| `dismiss_stale_reviews_on_push` | `false` | `true` |
| `require_last_push_approval` | `false` | keep `false` unless a second approval gate is desired |
| `review_on_push` (Copilot) | `false` | **keep `false`** (Copilot is one final review after Ready-prep, not every push) |
| `review_draft_pull_requests` (Copilot) | `false` | **keep `false`** |

Do not enable Copilot automatic review on every push or on Draft PRs.

## CodeRabbit

Iterative Draft review uses the in-repo `.coderabbit.yaml` (drafts on,
incremental on, finishing-touch extra PRs off). Until the repository has
10 GitHub stars, automatic reviews skip; agents comment `@coderabbitai
review` after each published fix batch. Do **not** add CodeRabbit as a
required status check (skip or queued suites would strand PRs). Amazon Q
remains optional for HIGH_RISK only; it is not a merge gate.

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
