#!/usr/bin/env bash
# beforeShellExecution: block publishing from non-canonical branches unless
# the branch already has a published upstream (continuing an open PR head).
# See .agents/skills/git-workflow/SKILL.md
#
# This is a best-effort Cursor shell guard, not a full argv parser. Direct
# publish forms are classified; ambiguous wrappers fail closed.

set -euo pipefail

if [[ "${HABITV_SKIP_BRANCH_HOOK:-}" == "1" ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

input="$(cat)"
command=""
cwd=""

resolve_jq() {
  if command -v jq >/dev/null 2>&1; then
    command -v jq
    return 0
  fi
  local candidate
  for candidate in \
    "/mnt/c/Program Files/Git/usr/bin/jq.exe" \
    "/mnt/c/ProgramData/chocolatey/bin/jq.exe" \
    /usr/bin/jq \
    /usr/local/bin/jq
  do
    if [[ -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done
  local winget_jq
  winget_jq="$(ls /mnt/c/Users/*/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_*/jq.exe 2>/dev/null | head -n 1 || true)"
  if [[ -n "$winget_jq" && -x "$winget_jq" ]]; then
    printf '%s\n' "$winget_jq"
    return 0
  fi
  return 1
}

deny() {
  printf '%s\n' "{\"permission\":\"deny\",\"agent_message\":\"$1\"}"
  exit 0
}

JQ_BIN="$(resolve_jq || true)"
if [[ -z "$JQ_BIN" ]]; then
  deny "Branch policy hook requires jq for safe JSON parsing. Install jq or use a canonical branch from the repository root."
fi

command="$("$JQ_BIN" -r '.command // empty' <<<"$input" | tr -d '\r')"
cwd="$("$JQ_BIN" -r '.cwd // empty' <<<"$input" | tr -d '\r')"

# Normalize quoted -C paths and git -c config flags so verb detection still works.
cmd_norm="$(printf '%s' "$command" | sed -E \
  -e 's/-C[[:space:]]+"[^"]+"/-C _PATH_/g' \
  -e "s/-C[[:space:]]+'[^']+'/-C _PATH_/g")"
_strip_i=0
while [[ $_strip_i -lt 16 ]]; do
  _cmd_next="$(printf '%s' "$cmd_norm" | sed -E \
    's#(^|[[:space:];&|])([^[:space:]]*/)?git(\.exe)?([[:space:]]+-C[[:space:]]+[^[:space:]]+)*[[:space:]]+-c[[:space:]]+[^[:space:]]+#\1\2git\3\4#g')"
  [[ "$_cmd_next" == "$cmd_norm" ]] && break
  cmd_norm="$_cmd_next"
  _strip_i=$((_strip_i + 1))
done
unset _strip_i _cmd_next
# Strip remaining simple quotes for cd / wrapper checks.
cmd_unquoted="$(printf '%s' "$cmd_norm" | sed -E 's/"[^"]*"//g; s/'\''[^'\'']*'\''//g')"

# Ambiguous indirection: fail closed when publish verbs appear.
ambiguous=0
if [[ "$cmd_unquoted" =~ (^|[[:space:];&|])([^[:space:]]*/)?(bash|sh|zsh|dash|pwsh|powershell)([[:space:]]|\.exe) ]] || \
   [[ "$cmd_unquoted" =~ (^|[[:space:];&|])(eval|source)[[:space:]] ]]; then
  ambiguous=1
fi

looks_publish=0
if [[ "$cmd_unquoted" =~ (push|commit|pr[[:space:]]+create) ]] || \
   [[ "$cmd_norm" =~ (push|commit|pr[[:space:]]+create) ]]; then
  looks_publish=1
fi

if [[ "$ambiguous" -eq 1 && "$looks_publish" -eq 1 ]]; then
  deny "Publishing blocked: ambiguous wrapper around git/gh publish. Run a direct git commit, git push, or gh pr create from the repository working directory."
fi

# Deny cd combined with a publish verb in the same shell statement.
if [[ "$cmd_unquoted" =~ (^|[[:space:];&|])cd[[:space:]] ]] && [[ "$looks_publish" -eq 1 ]]; then
  deny "Publishing blocked: do not combine cd with git commit, git push, or gh pr create in one shell command. Run publish commands from the repository working directory."
fi

# Direct publish: git / git.exe (optional absolute path) with optional -C, then push|commit.
is_publish=0
if [[ "$cmd_norm" =~ (^|[[:space:];&|])([^[:space:]]*/)?git(\.exe)?([[:space:]]+-C[[:space:]]+[^[:space:]]+)*([[:space:]]+[^[:space:]]+)*[[:space:]]+(push|commit)([[:space:]]|$) ]]; then
  is_publish=1
fi
if [[ "$cmd_unquoted" =~ (^|[[:space:];&|])([^[:space:]]*/)?gh(\.exe)?[[:space:]]+pr[[:space:]]+create ]]; then
  is_publish=1
fi

# Prefer trusted hook cwd. Only honor git -C when the command is a clear direct publish.
repo_dir="$cwd"
if [[ "$is_publish" -eq 1 && "$ambiguous" -eq 0 ]]; then
  if [[ "$command" =~ (^|[[:space:]])git(\.exe)?[[:space:]]+-C[[:space:]]+\"([^\"]+)\" ]]; then
    repo_dir="${BASH_REMATCH[3]}"
  elif [[ "$command" =~ (^|[[:space:]])git(\.exe)?[[:space:]]+-C[[:space:]]+\'([^\']+)\' ]]; then
    repo_dir="${BASH_REMATCH[3]}"
  elif [[ "$cmd_norm" =~ (^|[[:space:]])git(\.exe)?[[:space:]]+-C[[:space:]]+([^[:space:]]+) ]]; then
    repo_dir="${BASH_REMATCH[3]}"
  fi
fi

# Normalize Windows drive paths when the hook runs under WSL bash.
if [[ -n "$repo_dir" && ! -d "$repo_dir" && "$repo_dir" =~ ^[A-Za-z]:[\\/] ]]; then
  drive="$(printf '%s' "${repo_dir:0:1}" | tr '[:upper:]' '[:lower:]')"
  rest="${repo_dir:2}"
  rest="${rest//\\//}"
  wsl_path="/mnt/${drive}${rest}"
  if [[ -d "$wsl_path" ]]; then
    repo_dir="$wsl_path"
  fi
fi

if [[ -z "$repo_dir" || ! -d "$repo_dir" ]]; then
  if [[ "$is_publish" -eq 1 || "$looks_publish" -eq 1 ]]; then
    deny "Publishing blocked: working directory missing or invalid; cannot validate branch policy."
  fi
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

# Non-publish commands: no branch gate.
if [[ "$is_publish" -eq 0 ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

cd "$repo_dir"

branch="$(git branch --show-current 2>/dev/null || true)"
if [[ -z "$branch" ]]; then
  deny "Publishing blocked: detached HEAD or empty branch name. Check out a canonical <type>/<scope> branch before git commit, git push, or gh pr create."
fi

canonical='^(feat|fix|docs|test|refactor|chore|ci)/[a-z0-9]+(-[a-z0-9]+)*$'
if [[ "$branch" =~ $canonical ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

platform='^(cursor|claude|codex|ai)/'
if [[ "$branch" =~ $platform ]]; then
  upstream="$(git rev-parse --abbrev-ref '@{u}' 2>/dev/null || true)"
  if [[ -n "$upstream" && "$upstream" == */* ]]; then
    remote="${upstream%%/*}"
    remote_branch="${upstream#*/}"
    if git ls-remote --exit-code --heads "$remote" "$remote_branch" >/dev/null 2>&1; then
      printf '%s\n' '{"permission":"allow"}'
      exit 0
    fi
  fi
  deny "Publishing blocked: platform-generated branch. Before first publish, determine canonical <type>/<scope>, create/switch branch from origin/develop, verify with git branch --show-current. Procedure: .agents/skills/git-workflow/SKILL.md"
fi

deny "Publishing blocked: branch name must match <type>/<scope> (feat|fix|docs|test|refactor|chore|ci). See .agents/skills/git-workflow/SKILL.md"
