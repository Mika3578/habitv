#!/usr/bin/env bash
# Optional local/CI helper: report non-canonical current branch names.
# Not a required merge gate in PR #242. Set HABITV_STRICT_BRANCH=1 to exit 1.
# Usage: scripts/check-branch-name.sh

set -euo pipefail

branch="$(git branch --show-current 2>/dev/null || true)"
if [[ -z "$branch" ]]; then
  echo "check-branch-name: no current branch"
  exit 0
fi

canonical='^(feat|fix|docs|test|refactor|chore|ci)/[a-z0-9][a-z0-9-]*$'
if [[ "$branch" =~ $canonical ]]; then
  echo "check-branch-name: OK ($branch)"
  exit 0
fi

echo "check-branch-name: WARN non-canonical branch '$branch' (expected <type>/<scope>)"
if [[ "${HABITV_STRICT_BRANCH:-}" == "1" ]]; then
  exit 1
fi
exit 0
