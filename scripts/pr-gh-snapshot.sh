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
tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

pr_json="$(gh pr view "$pr" --repo "$repo" --json \
  number,title,isDraft,baseRefName,headRefName,headRefOid,body,reviewRequests,reviews,statusCheckRollup)"

# Paginate all review threads.
: >"$tmpdir/nodes.jsonl"
cursor=""
while true; do
  if [[ -n "$cursor" ]]; then
    threads_json="$(gh api graphql -f query="
query(\$owner: String!, \$name: String!, \$number: Int!, \$after: String!) {
  repository(owner: \$owner, name: \$name) {
    pullRequest(number: \$number) {
      reviewThreads(first: 100, after: \$after) {
        pageInfo { hasNextPage endCursor }
        nodes { isResolved isOutdated path }
      }
    }
  }
}" -f owner="$owner" -f name="$name" -F number="$pr" -f after="$cursor")"
  else
    threads_json="$(gh api graphql -f query="
query(\$owner: String!, \$name: String!, \$number: Int!) {
  repository(owner: \$owner, name: \$name) {
    pullRequest(number: \$number) {
      reviewThreads(first: 100) {
        pageInfo { hasNextPage endCursor }
        nodes { isResolved isOutdated path }
      }
    }
  }
}" -f owner="$owner" -f name="$name" -F number="$pr")"
  fi
  jq -c '.data.repository.pullRequest.reviewThreads.nodes[]' <<<"$threads_json" >>"$tmpdir/nodes.jsonl"
  has_next="$(jq -r '.data.repository.pullRequest.reviewThreads.pageInfo.hasNextPage' <<<"$threads_json")"
  if [[ "$has_next" != "true" ]]; then
    break
  fi
  cursor="$(jq -r '.data.repository.pullRequest.reviewThreads.pageInfo.endCursor' <<<"$threads_json")"
done

if [[ -s "$tmpdir/nodes.jsonl" ]]; then
  nodes_json="$(jq -s '.' "$tmpdir/nodes.jsonl")"
else
  nodes_json='[]'
fi

body="$(jq -r '.body' <<<"$pr_json")"
body_ok=0
if PR_BODY="$body" bash "$(dirname "$0")/validate-pr-public-body.sh" >/dev/null 2>&1; then
  body_ok=1
fi

unresolved="$(jq '[.[] | select(.isResolved == false)] | length' <<<"$nodes_json")"
outdated="$(jq '[.[] | select(.isOutdated == true)] | length' <<<"$nodes_json")"
total="$(jq 'length' <<<"$nodes_json")"

# Fail loudly if comments cannot be fetched (do not pretend there are none).
comments_raw="$(gh api "repos/$repo/issues/$pr/comments" --paginate)"
# --paginate may concatenate JSON arrays; slurp into one array.
comments_json="$(printf '%s' "$comments_raw" | jq -s 'add // []')"

head_sha="$(jq -r '.headRefOid' <<<"$pr_json")"
jq '.reviews' <<<"$pr_json" >"$tmpdir/reviews.json"
printf '%s' "$comments_json" >"$tmpdir/comments.json"
classify_json="$(bash "$(dirname "$0")/pr-classify-review-sources.sh" "$head_sha" "$tmpdir/reviews.json" "$tmpdir/comments.json")"

jq -n \
  --arg repo "$repo" \
  --argjson pr "$pr_json" \
  --argjson unresolved "$unresolved" \
  --argjson outdated "$outdated" \
  --argjson total "$total" \
  --argjson body_policy_ok "$body_ok" \
  --argjson classify "$classify_json" \
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
    status_check_rollup: $pr.statusCheckRollup,
    review_sources: $classify.review_sources,
    reviewer_requests: ($classify.reviewer_requests // []),
    substantive_review_on_head: $classify.substantive_review_on_head,
    final_review_gate_eligible: $classify.final_review_gate_eligible,
    iterative_review_on_head: $classify.iterative_review_on_head
  }'

exit 0
