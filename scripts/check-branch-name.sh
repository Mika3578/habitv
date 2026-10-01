#!/usr/bin/env bash
# Optional local/CI helper: report non-canonical current branch names.
# Not a required merge gate in PR #242. Set HABITV_STRICT_BRANCH=1 to exit 1.
# Usage: scripts/check-branch-name.sh

set -euo pipefail

branch="$(git branch --show-current 2>/dev/null || true)"
if [[ -z "$branch" ]]; then
  echo "check-branch-name: no current branch"
  if [[ "${HABITV_STRICT_BRANCH:-}" == "1" ]]; then
    exit 1
  fi
  exit 0
fi

canonical='^(feat|fix|docs|test|refactor|chore|ci)/[a-z0-9]+(-[a-z0-9]+)*$'
if [[ "$branch" =~ $canonical ]]; then
  # Reject agent-looking trailing hex suffixes such as topic-1a2e.
  # Require a leading digit so dictionary scopes (cafe, dead) stay valid.
  scope="${branch#*/}"
  if [[ "$scope" =~ -[0-9][0-9a-f]{3}$ ]]; then
    echo "check-branch-name: WARN non-canonical generated-looking suffix on '$branch'"
    if [[ "${HABITV_STRICT_BRANCH:-}" == "1" ]]; then
      exit 1
    fi
    exit 0
  fi
  echo "check-branch-name: OK ($branch)"
  exit 0
fi

echo "check-branch-name: WARN non-canonical branch '$branch' (expected <type>/<scope>)"
if [[ "${HABITV_STRICT_BRANCH:-}" == "1" ]]; then
  exit 1
fi
exit 0
