#!/usr/bin/env bash
# Deterministic live PR snapshot for orchestration (read-only GitHub).
# Usage: scripts/pr-gh-snapshot.sh <owner/repo> <pr_number>
# Prints JSON to stdout. Exit 0 on success, 2 on usage, 1 on gh failure.

set -euo pipefail

repo="${1:-}"
pr="${2:-}"

if [[ -z "$repo" || -z "$pr" ]]; then
  echo "usage: scripts/pr-gh-snapshot.sh <owner/repo> <pr_number>" >&2
  exit 2
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "pr-gh-snapshot: gh CLI required" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "pr-gh-snapshot: jq required" >&2
  exit 1
fi

owner="${repo%%/*}"
name="${repo#*/}"

pr_json="$(gh pr view "$pr" --repo "$repo" --json \
  number,title,isDraft,baseRefName,headRefName,headRefOid,body,reviewRequests,reviews,statusCheckRollup)"

threads_json="$(gh api graphql -f query="
query(\$owner: String!, \$name: String!, \$number: Int!) {
  repository(owner: \$owner, name: \$name) {
    pullRequest(number: \$number) {
      reviewThreads(first: 100) {
        nodes { isResolved isOutdated path }
      }
    }
  }
}" -f owner="$owner" -f name="$name" -F number="$pr")"

body="$(jq -r '.body' <<<"$pr_json")"
body_ok=0
if PR_BODY="$body" bash "$(dirname "$0")/validate-pr-public-body.sh" >/dev/null 2>&1; then
  body_ok=1
fi

unresolved="$(jq '[.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved == false)] | length' <<<"$threads_json")"
outdated="$(jq '[.data.repository.pullRequest.reviewThreads.nodes[] | select(.isOutdated == true)] | length' <<<"$threads_json")"
total="$(jq '.data.repository.pullRequest.reviewThreads.nodes | length' <<<"$threads_json")"

jq -n \
  --arg repo "$repo" \
  --argjson pr "$pr_json" \
  --argjson unresolved "$unresolved" \
  --argjson outdated "$outdated" \
  --argjson total "$total" \
  --argjson body_policy_ok "$body_ok" \
  '{
    repository: $repo,
    pr_number: $pr.number,
    title: $pr.title,
    is_draft: $pr.isDraft,
    base: $pr.baseRefName,
    head_branch: $pr.headRefName,
    head_sha: $pr.headRefOid,
    body_policy_ok: ($body_policy_ok == 1),
    review_threads: {
      total: $total,
      unresolved: $unresolved,
      outdated: $outdated
    },
    review_requests: $pr.reviewRequests,
    reviews: $pr.reviews,
    status_check_rollup: $pr.statusCheckRollup
  }'

exit 0
