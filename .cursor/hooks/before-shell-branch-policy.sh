#!/usr/bin/env bash
# beforeShellExecution: block publishing from non-canonical branches unless
# the branch already has a published upstream (continuing an open PR head).
# See .agents/skills/git-workflow/SKILL.md

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
  # WinGet jq package path varies by version; glob the common root.
  local winget_jq
  winget_jq="$(ls /mnt/c/Users/*/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_*/jq.exe 2>/dev/null | head -n 1 || true)"
  if [[ -n "$winget_jq" && -x "$winget_jq" ]]; then
    printf '%s\n' "$winget_jq"
    return 0
  fi
  return 1
}

JQ_BIN="$(resolve_jq || true)"
if [[ -z "$JQ_BIN" ]]; then
  printf '%s\n' '{"permission":"deny","agent_message":"Branch policy hook requires jq for safe JSON parsing. Install jq or use a canonical branch from the repository root."}'
  exit 0
fi

command="$("$JQ_BIN" -r '.command // empty' <<<"$input")"
cwd="$("$JQ_BIN" -r '.cwd // empty' <<<"$input")"
if [[ -n "$command" && "$command" =~ (^|[[:space:];&|])cd[[:space:]]+ ]]; then
  printf '%s\n' '{"permission":"deny","agent_message":"Publishing blocked: do not combine cd with git commit, git push, or gh pr create in one shell command. Run publish commands from the repository working directory."}'
  exit 0
fi

if [[ -z "$cwd" || ! -d "$cwd" ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

cd "$cwd"

branch="$(git branch --show-current 2>/dev/null || true)"
if [[ -z "$branch" ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
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
  printf '%s\n' '{"permission":"deny","agent_message":"Publishing blocked: platform-generated branch. Before first publish, determine canonical <type>/<scope>, create/switch branch from origin/develop, verify with git branch --show-current. Procedure: .agents/skills/git-workflow/SKILL.md"}'
  exit 0
fi

printf '%s\n' '{"permission":"deny","agent_message":"Publishing blocked: branch name must match <type>/<scope> (feat|fix|docs|test|refactor|chore|ci). See .agents/skills/git-workflow/SKILL.md"}'
exit 0
