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

if command -v jq >/dev/null 2>&1; then
  command="$(jq -r '.command // empty' <<<"$input")"
  cwd="$(jq -r '.cwd // empty' <<<"$input")"
else
  printf '%s\n' '{"permission":"deny","agent_message":"Branch policy hook requires jq for safe JSON parsing. Install jq or use a canonical branch from the repository root."}'
  exit 0
fi

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
