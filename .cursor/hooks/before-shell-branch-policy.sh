#!/usr/bin/env bash
# beforeShellExecution: block publishing from non-canonical branches unless
# the branch already has a published upstream (continuing an open PR head).
# See .agents/skills/git-workflow/SKILL.md
#
# Best-effort Cursor shell guard, not a full argv parser. Direct publish forms
# are classified; ambiguous wrappers fail closed.

set -euo pipefail

_HABITV_HOOK_RESPONDED=0

if [[ "${HABITV_SKIP_BRANCH_HOOK:-}" == "1" ]]; then
  printf '%s\n' '{"permission":"allow"}'
  exit 0
fi

emit_response() {
  _HABITV_HOOK_RESPONDED=1
  printf '%s\n' "$1"
}

json_escape() {
  local s="$1"
  s="${s//\\/\\\\}"
  s="${s//\"/\\\"}"
  s="${s//$'\n'/\\n}"
  s="${s//$'\r'/\\r}"
  s="${s//$'\t'/\\t}"
  printf '%s' "$s"
}

allow() {
  emit_response '{"permission":"allow"}'
  exit 0
}

policy_deny() {
  local msg
  msg="$(json_escape "$1")"
  emit_response "{\"permission\":\"deny\",\"agent_message\":\"$msg\"}"
  exit 0
}

runtime_deny() {
  local msg
  msg="$(json_escape "Branch policy hook runtime: $1")"
  emit_response "{\"permission\":\"deny\",\"agent_message\":\"$msg\"}"
  exit 0
}

dependency_deny() {
  local msg
  msg="$(json_escape "Branch policy hook dependency: $1")"
  emit_response "{\"permission\":\"deny\",\"agent_message\":\"$msg\"}"
  exit 0
}

_hook_on_err() {
  if [[ "${_HABITV_HOOK_RESPONDED}" -eq 1 ]]; then
    return 0
  fi
  runtime_deny "internal error near line ${BASH_LINENO[0]}."
}

_hook_on_exit() {
  local ec=$?
  if [[ "${_HABITV_HOOK_RESPONDED}" -eq 1 ]]; then
    exit 0
  fi
  if [[ "$ec" -ne 0 ]]; then
    runtime_deny "unexpected exit (code $ec)."
  fi
}

trap _hook_on_err ERR
trap _hook_on_exit EXIT

resolve_jq() {
  if command -v jq >/dev/null 2>&1; then
    command -v jq
    return 0
  fi
  local candidate user
  user="${USERNAME:-${USER:-}}"
  for candidate in \
    "/mnt/c/Program Files/Git/usr/bin/jq.exe" \
    "/c/Program Files/Git/usr/bin/jq.exe" \
    "/mnt/c/ProgramData/chocolatey/bin/jq.exe" \
    "/c/ProgramData/chocolatey/bin/jq.exe" \
    /usr/bin/jq \
    /usr/local/bin/jq
  do
    if [[ -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done
  if [[ -n "$user" ]]; then
    for candidate in \
      /mnt/c/Users/"$user"/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_*/jq.exe \
      /c/Users/"$user"/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_*/jq \
      /c/Users/"$user"/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_*/jq.exe
    do
      if [[ -x "$candidate" ]]; then
        printf '%s\n' "$candidate"
        return 0
      fi
    done
  fi
  return 1
}

resolve_python() {
  local candidate user out
  for candidate in python3 python; do
    if command -v "$candidate" >/dev/null 2>&1; then
      candidate="$(command -v "$candidate")"
      if out="$("$candidate" -c "import json; print(json.dumps({'ok': True}))" 2>/dev/null || true)"; then
        out="${out//$'\n'/}"
        if [[ "$out" == *ok* ]]; then
          printf '%s\n' "$candidate"
          return 0
        fi
      fi
    fi
  done
  user="${USERNAME:-${USER:-}}"
  for candidate in \
    /usr/bin/python3 \
    /usr/local/bin/python3
  do
    if [[ -x "$candidate" ]]; then
      out="$("$candidate" -c "import json; print(1)" 2>/dev/null || true)"
      out="${out//$'\n'/}"
      if [[ "$out" == "1" ]]; then
        printf '%s\n' "$candidate"
        return 0
      fi
    fi
  done
  if [[ -n "$user" ]]; then
    for candidate in \
      /mnt/c/Users/"$user"/AppData/Local/Programs/Python/Python3*/python.exe \
      /c/Users/"$user"/AppData/Local/Programs/Python/Python3*/python.exe
    do
      if [[ -x "$candidate" ]]; then
        out="$("$candidate" -c "import json; print(1)" 2>/dev/null || true)"
        out="${out//$'\n'/}"
        if [[ "$out" == "1" ]]; then
          printf '%s\n' "$candidate"
          return 0
        fi
      fi
    done
  fi
  return 1
}

json_field() {
  local key="$1"
  if [[ -n "${JQ_BIN:-}" ]]; then
    "$JQ_BIN" -r --arg k "$key" '.[$k] // empty' <<<"$input" 2>/dev/null | tr -d '\r\n' || true
    return 0
  fi
  if [[ -n "${PY_BIN:-}" ]]; then
    "$PY_BIN" -c 'import json,sys; d=json.loads(sys.stdin.read()); v=d.get(sys.argv[1],""); print("" if v is None else v)' "$key" <<<"$input" 2>/dev/null | tr -d '\r\n' || true
    return 0
  fi
  printf ''
}

json_field_fallback() {
  local key="$1"
  local value=""
  if [[ "$input" =~ \"$key\"[[:space:]]*:[[:space:]]*\"([^\"]+)\" ]]; then
    value="${BASH_REMATCH[1]}"
  fi
  printf '%s' "$value"
}

normalize_path_token() {
  local p="$1"
  p="${p#\"}"
  p="${p%\"}"
  p="${p#\'}"
  p="${p%\'}"
  p="${p//\\//}"
  while [[ "$p" == */ && "$p" != "/" ]]; do
    p="${p%/}"
  done
  if [[ "$p" =~ ^([A-Za-z]):/(.*)$ ]]; then
    local drive rest
    drive="$(printf '%s' "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')"
    rest="${BASH_REMATCH[2]}"
    p="${drive}:/${rest}"
  fi
  printf '%s' "$p"
}

paths_same() {
  local a="$1" b="$2"
  local na nb
  na="$(normalize_path_token "$a")"
  nb="$(normalize_path_token "$b")"
  if [[ -n "$na" && "$na" == "$nb" ]]; then
    return 0
  fi
  if [[ -d "$a" && -d "$b" ]]; then
    local ra rb
    ra="$(cd "$a" 2>/dev/null && pwd)" || return 1
    rb="$(cd "$b" 2>/dev/null && pwd)" || return 1
    [[ "$ra" == "$rb" ]]
    return
  fi
  return 1
}

cd_blocks_publish() {
  local cmd_u="$1" cmd_n="$2" repo="$3" hook_cwd="$4"
  if [[ ! "$cmd_u" =~ (^|[[:space:];&|])cd[[:space:]] ]] && [[ ! "$cmd_n" =~ (^|[[:space:];&|])cd[[:space:]] ]]; then
    return 1
  fi
  local cd_target=""
  if [[ "$cmd_n" =~ (^|[[:space:];&|])cd[[:space:]]+\"([^\"]+)\" ]]; then
    cd_target="${BASH_REMATCH[2]}"
  elif [[ "$cmd_n" =~ (^|[[:space:];&|])cd[[:space:]]+\'([^\']+)\' ]]; then
    cd_target="${BASH_REMATCH[2]}"
  elif [[ "$cmd_u" =~ (^|[[:space:];&|])cd[[:space:]]+([^;&|]+) ]]; then
    cd_target="${BASH_REMATCH[2]}"
    cd_target="${cd_target%%[[:space:]]&&*}"
    cd_target="$(printf '%s' "$cd_target" | sed -E 's/[[:space:]]+$//')"
  fi
  if [[ -z "$cd_target" ]]; then
    return 0
  fi
  if paths_same "$cd_target" "$repo" || paths_same "$cd_target" "$hook_cwd"; then
    return 1
  fi
  return 0
}

input="$(cat || true)"
if [[ -z "$input" ]]; then
  policy_deny "Branch policy hook received empty input."
fi

JQ_BIN="$(resolve_jq || true)"
PY_BIN="$(resolve_python || true)"

command="$(json_field command)"
cwd="$(json_field cwd)"
if [[ -z "$command" && -z "$JQ_BIN" && -z "$PY_BIN" ]]; then
  command="$(json_field_fallback command)"
  cwd="$(json_field_fallback cwd)"
fi
if [[ -z "$command" && ( -n "$JQ_BIN" || -n "$PY_BIN" ) ]]; then
  runtime_deny "could not parse command field from hook input."
fi
if [[ -z "$command" && -z "$JQ_BIN" && -z "$PY_BIN" ]]; then
  dependency_deny "install jq or Python in the hook bash environment, or simplify the shell command JSON."
fi

# Normalize quoted -C paths, quoted publish verbs, and git -c config flags.
cmd_norm="$(printf '%s' "$command" | sed -E \
  -e 's/-C[[:space:]]+"[^"]+"/-C _PATH_/g' \
  -e "s/-C[[:space:]]+'[^']+'/-C _PATH_/g" \
  -e 's/"push"/push/g; s/'\''push'\''/push/g' \
  -e 's/"commit"/commit/g; s/'\''commit'\''/commit/g' \
  -e 's/"create"/create/g; s/'\''create'\''/create/g')"
_strip_i=0
while [[ $_strip_i -lt 16 ]]; do
  _cmd_next="$(printf '%s' "$cmd_norm" | sed -E \
    's#(^|[[:space:];&|])([^[:space:]]*/)?git(\.exe)?([[:space:]]+-C[[:space:]]+[^[:space:]]+)*[[:space:]]+-c[[:space:]]+[^[:space:]]+#\1\2git\3\4#g')"
  [[ "$_cmd_next" == "$cmd_norm" ]] && break
  cmd_norm="$_cmd_next"
  _strip_i=$((_strip_i + 1))
done
unset _strip_i _cmd_next
cmd_unquoted="$(printf '%s' "$cmd_norm" | sed -E 's/"[^"]*"//g; s/'\''[^'\'']*'\''//g')"

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
  policy_deny "Publishing blocked: ambiguous wrapper around git/gh publish. Run a direct git commit, git push, or gh pr create from the repository working directory."
fi

repo_dir="${cwd:-}"
if [[ "$looks_publish" -eq 1 ]] && cd_blocks_publish "$cmd_unquoted" "$cmd_norm" "$repo_dir" "$cwd"; then
  policy_deny "Publishing blocked: do not combine cd with git commit, git push, or gh pr create in one shell command. Run publish commands from the repository working directory."
fi

is_publish=0
if [[ "$cmd_norm" =~ (^|[[:space:];&|])([^[:space:]]*/)?git(\.exe)?([[:space:]]+-C[[:space:]]+[^[:space:]]+)*([[:space:]]+[^[:space:]]+)*[[:space:]]+(push|commit)([[:space:]]|$) ]]; then
  is_publish=1
fi
if [[ "$cmd_unquoted" =~ (^|[[:space:];&|])([^[:space:]]*/)?gh(\.exe)?[[:space:]]+pr[[:space:]]+create ]]; then
  is_publish=1
fi

if [[ "$is_publish" -eq 1 && "$ambiguous" -eq 0 ]]; then
  if [[ "$command" =~ (^|[[:space:]])([^[:space:]]*/)?git(\.exe)?[[:space:]]+-C[[:space:]]+\"([^\"]+)\" ]]; then
    repo_dir="${BASH_REMATCH[4]}"
  elif [[ "$command" =~ (^|[[:space:]])([^[:space:]]*/)?git(\.exe)?[[:space:]]+-C[[:space:]]+\'([^\']+)\' ]]; then
    repo_dir="${BASH_REMATCH[4]}"
  elif [[ "$cmd_norm" =~ (^|[[:space:]])([^[:space:]]*/)?git(\.exe)?[[:space:]]+-C[[:space:]]+([^[:space:]]+) ]]; then
    repo_dir="${BASH_REMATCH[4]}"
  fi
fi

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
    policy_deny "Publishing blocked: working directory missing or invalid; cannot validate branch policy."
  fi
  allow
fi

if [[ "$is_publish" -eq 0 ]]; then
  allow
fi

cd "$repo_dir"

branch="$(git branch --show-current 2>/dev/null || true)"
branch="${branch//$'\r'/}"
if [[ -z "$branch" ]]; then
  policy_deny "Publishing blocked: detached HEAD or empty branch name. Check out a canonical <type>/<scope> branch before git commit, git push, or gh pr create."
fi

canonical='^(feat|fix|docs|test|refactor|chore|ci)/[a-z0-9]+(-[a-z0-9]+)*$'
if [[ "$branch" =~ $canonical ]]; then
  scope="${branch#*/}"
  if [[ "$scope" =~ -[0-9][0-9a-f]{3}$ ]]; then
    policy_deny "Publishing blocked: generated-looking branch suffix on '$branch'. Use a canonical <type>/<scope> without agent hex suffixes."
  fi
  allow
fi

platform='^(cursor|claude|codex|ai)/'
if [[ "$branch" =~ $platform ]]; then
  upstream="$(git rev-parse --abbrev-ref '@{u}' 2>/dev/null || true)"
  upstream="${upstream//$'\r'/}"
  if [[ -n "$upstream" && "$upstream" == */* ]]; then
    remote="${upstream%%/*}"
    remote_branch="${upstream#*/}"
    if [[ "$remote_branch" == "$branch" ]] && git ls-remote --exit-code --heads "$remote" "$remote_branch" >/dev/null 2>&1; then
      allow
    fi
  fi
  policy_deny "Publishing blocked: platform-generated branch. Before first publish, determine canonical <type>/<scope>, create/switch branch from origin/develop, verify with git branch --show-current. Procedure: .agents/skills/git-workflow/SKILL.md"
fi

policy_deny "Publishing blocked: branch name must match <type>/<scope> (feat|fix|docs|test|refactor|chore|ci). See .agents/skills/git-workflow/SKILL.md"
