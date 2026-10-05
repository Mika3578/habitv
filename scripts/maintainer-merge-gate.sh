#!/usr/bin/env bash
# Fail closed unless GitHub Environment merge-develop requires a trusted user.
# Usage:
#   scripts/maintainer-merge-gate.sh --check-env <environment.json> [login]
#   scripts/maintainer-merge-gate.sh --self-test
# Exit 0 when the named user is a required reviewer; 1 otherwise.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
EXPECTED_LOGIN="${MAINTAINER_MERGE_GATE_LOGIN:-Mika3578}"

has_required_reviewer() {
  local json_file="$1"
  local login="$2"
  python3 - "$json_file" "$login" <<'PY'
import json, sys
path, login = sys.argv[1], sys.argv[2]
with open(path, encoding="utf-8") as fh:
    data = json.load(fh)
login_l = login.lower()
rules = data.get("protection_rules") or []
for rule in rules:
    if rule.get("type") != "required_reviewers":
        continue
    for entry in rule.get("reviewers") or []:
        reviewer = entry.get("reviewer") or entry
        name = (reviewer.get("login") or "")
        if name.lower() == login_l:
            sys.exit(0)
sys.exit(1)
PY
}

self_test() {
  local td="$ROOT/scripts/testdata/maintainer-merge-gate"
  if ! has_required_reviewer "$td/protected.json" "$EXPECTED_LOGIN"; then
    echo "maintainer-merge-gate: expected protected.json to pass" >&2
    exit 1
  fi
  if has_required_reviewer "$td/unprotected.json" "$EXPECTED_LOGIN"; then
    echo "maintainer-merge-gate: expected unprotected.json to fail" >&2
    exit 1
  fi
  echo "maintainer-merge-gate: self-test OK"
}

usage() {
  echo "usage: $0 --check-env <environment.json> [login]" >&2
  echo "       $0 --self-test" >&2
  exit 2
}

cmd="${1:-}"
case "$cmd" in
  --self-test)
    self_test
    ;;
  --check-env)
    file="${2:-}"
    login="${3:-$EXPECTED_LOGIN}"
    [[ -n "$file" && -f "$file" ]] || usage
    if has_required_reviewer "$file" "$login"; then
      echo "maintainer-merge-gate: required reviewer ${login} present"
      exit 0
    fi
    echo "maintainer-merge-gate: environment is missing required reviewer ${login}" >&2
    exit 1
    ;;
  *)
    usage
    ;;
esac
