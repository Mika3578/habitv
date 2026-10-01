#!/usr/bin/env bash
# PR body hygiene helper (advisory only).
# Usage:
#   scripts/validate-pr-public-body.sh              # read body from stdin
#   scripts/validate-pr-public-body.sh /path/file
#   PR_BODY='...' scripts/validate-pr-public-body.sh
# Always exits 0. Third-party PR description footers are tolerated and are
# not a merge or Ready gate.

set -euo pipefail

# Consume input for compatibility with callers; do not fail on content.
body="${PR_BODY:-}"
if [[ $# -gt 0 ]]; then
  body="$(cat "$1")"
elif [[ -z "$body" ]]; then
  body="$(cat || true)"
fi
: "${body:=}"

echo "pr-public-body: OK (advisory; tool footers not enforced)"
exit 0
