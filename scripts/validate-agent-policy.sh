#!/usr/bin/env bash
# Deterministic checks for repository agent-policy layout.
# Usage: scripts/validate-agent-policy.sh
# Exit 0 when valid, 1 on failure.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

failures=0
fail() {
  echo "agent-policy: $1"
  failures=$((failures + 1))
}

ADAPTER_MAX_BYTES=8192
SKILL_DESC_MAX=500

if [[ ! -f AGENTS.md ]]; then
  fail "missing root AGENTS.md"
fi

while IFS= read -r f; do
  case "$f" in
    ./AGENTS.md | AGENTS.md) ;;
    *) fail "unexpected nested AGENTS.md: $f" ;;
  esac
done < <(find . -name AGENTS.md -not -path './.git/*' -not -path './agent_space/*')

for banned in CLAUDE.md GEMINI.md .cursorrules .windsurfrules; do
  if [[ -f "$banned" ]]; then
    size=$(wc -c <"$banned")
    if [[ "$size" -gt 200 ]]; then
      fail "competing substantive file present: $banned ($size bytes)"
    fi
  fi
done

check_adapter() {
  local path="$1"
  [[ -f "$path" ]] || return 0
  local size
  size=$(wc -c <"$path")
  if [[ "$size" -gt "$ADAPTER_MAX_BYTES" ]]; then
    fail "adapter too large ($size bytes): $path"
  fi
  if ! grep -q 'AGENTS.md' "$path"; then
    fail "adapter must reference AGENTS.md: $path"
  fi
}

check_adapter .continue/rules/00-habitv.md
check_adapter .github/copilot-instructions.md
check_adapter .cursor/CLOUD.md

check_thin_public_git_cursor_rule() {
  local path=".cursor/rules/public-git-text.mdc"
  if [[ ! -f "$path" ]]; then
    fail "missing thin Cursor rule: $path"
    return
  fi
  if ! grep -qE '\.agents/skills/public-git-text' "$path"; then
    fail "public-git-text Cursor rule must reference portable skill: $path"
  fi
  local body
  body="$(awk 'BEGIN { n = 0; show = 0 } /^---$/ { n++; if (n >= 2) { show = 1 }; next } show { print }' "$path")"
  if printf '%s\n' "$body" | grep -qE '^[[:space:]]*- '; then
    fail "thin Cursor rule must not duplicate policy bullets: $path"
  fi
}
check_thin_public_git_cursor_rule

if [[ ! -f .cursor/hooks.json ]]; then
  fail "missing .cursor/hooks.json"
elif [[ ! -f .cursor/hooks/before-shell-branch-policy.sh ]]; then
  fail "missing branch policy hook script"
elif ! grep -q 'before-shell-branch-policy' .cursor/hooks.json; then
  fail ".cursor/hooks.json must register before-shell-branch-policy hook"
fi

if [[ -f .agents/skills/git-workflow/SKILL.md ]]; then
  if ! grep -q 'workOnCurrentBranch' .agents/skills/git-workflow/SKILL.md; then
    fail "git-workflow skill must document Cloud workOnCurrentBranch workflow"
  fi
fi

if [[ -d .cursor/skills ]]; then
  while IFS= read -r -d '' skill; do
    rel="${skill#./}"
    if ! grep -qE '\.agents/skills/' "$skill"; then
      fail "Cursor skill must point to .agents/skills/: $rel"
    fi
  done < <(find .cursor/skills -name 'SKILL.md' -print0 2>/dev/null)
fi

required_skills=(
  public-git-text
  git-workflow
  code-change-verification
  provider-diagnostics
  pr-review
)

if [[ ! -d .agents/skills ]]; then
  fail "missing .agents/skills directory"
else
  skill_count=0
  while IFS= read -r -d '' _; do
    skill_count=$((skill_count + 1))
  done < <(find .agents/skills -name 'SKILL.md' -print0 2>/dev/null)
  if [[ "$skill_count" -eq 0 ]]; then
    fail "no SKILL.md files under .agents/skills"
  fi
  for req in "${required_skills[@]}"; do
    if [[ ! -f ".agents/skills/$req/SKILL.md" ]]; then
      fail "missing required skill: .agents/skills/$req/SKILL.md"
    fi
  done
fi

if [[ -d .agents/skills ]]; then
  declare -A skill_names=()
  while IFS= read -r -d '' skill; do
    rel="${skill#./}"
    metadata="$(awk 'NR == 1 && $0 ~ /^---[[:space:]]*$/ { frontmatter = 1; next }
      frontmatter && $0 ~ /^---[[:space:]]*$/ { closed = 1; exit }
      frontmatter { print }
      END { if (!frontmatter || !closed) exit 1 }' "$skill" 2>/dev/null)" || metadata=""
    if [[ -z "$metadata" ]]; then
      fail "skill missing YAML frontmatter: $rel"
      continue
    fi
    name=$(printf '%s\n' "$metadata" | sed -n 's/^name:[[:space:]]*//p' | head -1)
    desc=$(printf '%s\n' "$metadata" | sed -n 's/^description:[[:space:]]*//p' | head -1)
    if [[ -z "$name" ]]; then
      fail "empty skill name: $rel"
    fi
    if [[ -z "$desc" ]]; then
      fail "empty skill description: $rel"
    fi
    if [[ "${#desc}" -gt "$SKILL_DESC_MAX" ]]; then
      fail "skill description too long (${#desc} chars): $rel"
    fi
    if [[ -n "${skill_names[$name]:-}" ]]; then
      fail "duplicate skill name '$name': ${skill_names[$name]} and $rel"
    fi
    skill_names[$name]="$rel"
  done < <(find .agents/skills -name 'SKILL.md' -print0 2>/dev/null)
fi

require_phrase() {
  local phrase="$1"
  if ! grep -qF "$phrase" AGENTS.md; then
    fail "AGENTS.md missing required phrase: $phrase"
  fi
}

require_phrase "Keep every pull request in **Draft**"
require_phrase "current PR HEAD"
require_phrase "explicitly confirms success in the current conversation"

if [[ "$failures" -gt 0 ]]; then
  echo "agent-policy: FAILED ($failures check(s))"
  exit 1
fi

echo "agent-policy: OK"
exit 0
