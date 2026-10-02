#!/usr/bin/env bash
# Offline deterministic tests for .cursor/hooks/before-shell-branch-policy.sh
# Usage: scripts/test-branch-policy-hook.sh

set -euo pipefail

if [[ "${BASH_VERSINFO[0]}" -lt 4 ]]; then
  echo "branch-policy-hook-tests: Bash 4+ required"
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOK="$ROOT/.cursor/hooks/before-shell-branch-policy.sh"
if [[ ! -f "$HOOK" ]]; then
  echo "branch-policy-hook-tests: missing hook script"
  exit 1
fi

TMP_ROOT="$(mktemp -d)"
MOCK_BIN="$TMP_ROOT/mockbin"
mkdir -p "$MOCK_BIN"

cleanup() {
  rm -rf "$TMP_ROOT"
}
trap cleanup EXIT

write_mock_git() {
  cat >"$MOCK_BIN/git" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
case "${1:-}" in
  branch)
    if [[ "${2:-}" == "--show-current" ]]; then
      printf '%s\n' "${HABITV_HOOK_TEST_BRANCH:-}"
      exit 0
    fi
    ;;
  rev-parse)
    if [[ "${2:-}" == "@{u}" ]] || [[ "${2:-}" == "--abbrev-ref" && "${3:-}" == "@{u}" ]]; then
      printf '%s\n' "${HABITV_HOOK_TEST_UPSTREAM:-}"
      exit 0
    fi
    ;;
  ls-remote)
    if [[ "${HABITV_HOOK_TEST_LSREMOTE_OK:-}" == "1" ]]; then
      exit 0
    fi
    exit 2
    ;;
esac
exit 1
EOF
  chmod +x "$MOCK_BIN/git"
  cp "$MOCK_BIN/git" "$MOCK_BIN/git.exe"
  chmod +x "$MOCK_BIN/git.exe"
}

write_mock_git

REPO="$TMP_ROOT/repo"
mkdir -p "$REPO"
# Hook only needs a directory; git is mocked.
REPO_JSON="${REPO//\\/\\\\}"

failures=0
pass() {
  printf 'branch-policy-hook-tests: PASS %s\n' "$1"
}
fail() {
  printf 'branch-policy-hook-tests: FAIL %s\n' "$1"
  failures=$((failures + 1))
}

run_hook() {
  local json="$1"
  local branch="${2:-}"
  local upstream="${3:-}"
  local lsremote="${4:-}"
  local path_prefix="${5:-$MOCK_BIN}"
  printf '%s' "$json" | env PATH="$path_prefix:$PATH" \
    HABITV_SKIP_BRANCH_HOOK=0 \
    HABITV_HOOK_TEST_BRANCH="$branch" \
    HABITV_HOOK_TEST_UPSTREAM="$upstream" \
    HABITV_HOOK_TEST_LSREMOTE_OK="$lsremote" \
    bash "$HOOK" 2>/dev/null || true
}

assert_permission() {
  local name="$1" expected="$2" json="$3"
  local branch="${4:-}"
  local upstream="${5:-}"
  local lsremote="${6:-}"
  local out perm
  out="$(run_hook "$json" "$branch" "$upstream" "$lsremote")"
  if [[ -z "$out" ]]; then
    fail "$name (no hook output)"
    return
  fi
  if [[ "$out" =~ \"permission\"[[:space:]]*:[[:space:]]*\"(allow|deny)\" ]]; then
    perm="${BASH_REMATCH[1]}"
  else
    fail "$name (invalid JSON: $out)"
    return
  fi
  if [[ "$perm" == "$expected" ]]; then
    pass "$name"
  else
    fail "$name (expected $expected, got $perm; $out)"
  fi
}

base_json() {
  local cmd="$1"
  printf '{"command":"%s","cwd":"%s"}' "$cmd" "$REPO_JSON"
}

# Canonical branch allow
assert_permission "canonical fix branch" allow "$(base_json 'git push origin fix/foo')" "fix/foo"

# Invalid branch deny
assert_permission "invalid branch" deny "$(base_json 'git push origin main')" "main"

# Generated suffix deny
assert_permission "generated suffix" deny "$(base_json 'git commit -m test')" "fix/foo-1a2e"

# Platform first publish deny
assert_permission "platform first publish" deny "$(base_json 'git push -u origin cursor/agent-1')" "cursor/agent-1" "" "0"

# Platform published upstream allow
assert_permission "platform published upstream" allow "$(base_json 'git push origin cursor/agent-1')" "cursor/agent-1" "origin/cursor/agent-1" "1"

# Detached HEAD deny
assert_permission "detached HEAD" deny "$(base_json 'git push origin HEAD')" ""

# Direct commit / push / gh pr create evaluated (canonical)
assert_permission "direct git commit" allow "$(base_json 'git commit -m msg')" "chore/cursor-hooks"
assert_permission "direct git push" allow "$(base_json 'git push origin chore/cursor-hooks')" "chore/cursor-hooks"
assert_permission "gh pr create" allow "$(base_json 'gh pr create --title t')" "chore/cursor-hooks"

# Unrelated shell (hook invoked directly; should allow when not a publish)
assert_permission "unrelated command" allow "$(base_json 'mvn -version')" "chore/cursor-hooks"

# cd && publish: different directory deny
OTHER="$TMP_ROOT/other"
mkdir -p "$OTHER"
OTHER_JSON="${OTHER//\\/\\\\}"
assert_permission "cd other && push" deny "{\"command\":\"cd \\\"$OTHER_JSON\\\" && git push\",\"cwd\":\"$REPO_JSON\"}" "chore/cursor-hooks"

# cd && publish: same directory allow
out_same="$(run_hook "{\"command\":\"cd \\\"$REPO_JSON\\\" && git push\",\"cwd\":\"$REPO_JSON\"}" "chore/cursor-hooks")"
if [[ "$out_same" =~ \"permission\"[[:space:]]*:[[:space:]]*\"allow\" ]]; then
  pass "cd same repo && push"
else
  fail "cd same repo && push (expected allow; got $out_same)"
fi

# Malformed / empty input
out_empty="$(printf '' | env PATH="$MOCK_BIN:$PATH" bash "$HOOK" 2>/dev/null || true)"
if [[ "$out_empty" =~ \"permission\"[[:space:]]*:[[:space:]]*\"deny\" ]]; then
  pass "empty input"
else
  fail "empty input (got: $out_empty)"
fi

out_bad="$(printf '{"command":' | env PATH="$MOCK_BIN:$PATH" bash "$HOOK" 2>/dev/null || true)"
if [[ -n "$out_bad" && "$out_bad" =~ \"permission\"[[:space:]]*:[[:space:]]*\"deny\" ]]; then
  pass "malformed input yields deny JSON"
else
  fail "malformed input (got: $out_bad)"
fi

# Missing JSON parser (reproducible when neither jq nor python works)
write_broken_python_mock() {
  cat >"$MOCK_BIN/python3" <<'EOF'
#!/usr/bin/env bash
exit 1
EOF
  cat >"$MOCK_BIN/python" <<'EOF'
#!/usr/bin/env bash
exit 1
EOF
  chmod +x "$MOCK_BIN/python3" "$MOCK_BIN/python"
  cat >"$MOCK_BIN/jq" <<'EOF'
#!/usr/bin/env bash
exit 1
EOF
  chmod +x "$MOCK_BIN/jq"
}

write_broken_python_mock
simple_json='{"command":"git push","cwd":"'"$REPO_JSON"'"}'
out_nodep="$(run_hook "$simple_json" "fix/foo" "" "" "$MOCK_BIN")"
if [[ "$out_nodep" =~ \"permission\"[[:space:]]*:[[:space:]]*\"(allow|deny)\" ]]; then
  pass "missing parser still returns protocol JSON"
else
  fail "missing parser (got: $out_nodep)"
fi

# Restore mock git only on PATH for ambiguous wrapper test
write_mock_git
assert_permission "ambiguous pwsh wrapper" deny "$(base_json 'pwsh -Command git push')" "chore/cursor-hooks"

if [[ "$failures" -gt 0 ]]; then
  echo "branch-policy-hook-tests: FAILED ($failures)"
  exit 1
fi

echo "branch-policy-hook-tests: OK"
exit 0
