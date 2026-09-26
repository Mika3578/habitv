# GitHub rulesets (admin payloads)

JSON under this directory documents the intended `develop` protection
settings. Apply changes in the GitHub UI or Rulesets API; files here are
not applied automatically.

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
| `review_on_push` (Copilot) | `false` | optional; enable when Draft PRs should receive Copilot on each push |

Hosted Copilot rule in the payload sets `review_draft_pull_requests: false`.

## External PR description tools

- **Sourcery:** disable **Enable pull request summary** in Sourcery Review
  Settings (dashboard) to stop mutating the GitHub PR description.
- **cubic:** no `cubic.yaml` in this repository; use the cubic dashboard or
  supported repo config if available. CI runs
  `scripts/validate-pr-public-body.*` on `pull_request` `edited` events.

## Merge queue (deferred)

GitHub merge queues can reduce integration friction when many provider PRs
land in parallel. Enabling a queue is **out of scope for agent-rules PRs**;
workflows would need `merge_group` event support and a dedicated governance
change. Document only until a maintainer adopts it.
