#!/usr/bin/env bash
# Apply (or check) the parent/reactor SemVer bump without clobbering plugin
# module version overrides.
#
# Usage:
#   apply-parent-version-bump.sh --type feat
#   apply-parent-version-bump.sh --type fix --check
#   apply-parent-version-bump.sh --to 4.2.0-SNAPSHOT
#   apply-parent-version-bump.sh --type feat --dry-run
#
# Updates:
#   - root pom.xml project <version>
#   - every child <parent><version> matching the previous parent version
# Leaves alone:
#   - module-level <version> overrides (e.g. plugins/6play 4.1.1-SNAPSHOT)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CALC="$SCRIPT_DIR/calculate-version-bump.sh"

COMMIT_TYPE=""
TO_VERSION=""
CHECK_ONLY=0
DRY_RUN=0

usage() {
  sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'
  exit 2
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --type)
      COMMIT_TYPE="${2:-}"
      shift 2
      ;;
    --to)
      TO_VERSION="${2:-}"
      shift 2
      ;;
    --root)
      REPO_ROOT="$(cd "${2:-}" && pwd)"
      shift 2
      ;;
    --check)
      CHECK_ONLY=1
      shift
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      ;;
  esac
done

if [[ -z "$COMMIT_TYPE" && -z "$TO_VERSION" ]]; then
  echo "Provide --type <conventional-type> and/or --to <version>." >&2
  usage
fi

resolve_python() {
  local candidate user out
  for candidate in python3 python; do
    if command -v "$candidate" >/dev/null 2>&1; then
      candidate="$(command -v "$candidate")"
      # Reject Windows Store python stubs that print an install prompt.
      out="$("$candidate" -c "print(1)" 2>/dev/null || true)"
      out="${out//$'\r'/}"
      if [[ "$out" == "1" ]]; then
        printf '%s\n' "$candidate"
        return 0
      fi
    fi
  done
  if command -v py >/dev/null 2>&1; then
    out="$(py -3 -c "print(1)" 2>/dev/null || true)"
    out="${out//$'\r'/}"
    if [[ "$out" == "1" ]]; then
      # Wrap via a tiny executable path is awkward; call py -3 through env.
      printf '%s\n' "py"
      return 0
    fi
  fi
  user="${USERNAME:-${USER:-}}"
  if [[ -n "$user" ]]; then
    for candidate in \
      /c/Users/"$user"/AppData/Local/Programs/Python/Python*/python.exe \
      /mnt/c/Users/"$user"/AppData/Local/Programs/Python/Python*/python.exe
    do
      if [[ -x "$candidate" ]] || [[ -f "$candidate" ]]; then
        out="$("$candidate" -c "print(1)" 2>/dev/null || true)"
        out="${out//$'\r'/}"
        if [[ "$out" == "1" ]]; then
          printf '%s\n' "$candidate"
          return 0
        fi
      fi
    done
  fi
  return 1
}

PY_BIN="$(resolve_python || true)"
if [[ -z "${PY_BIN:-}" ]]; then
  echo "python3 or python is required" >&2
  exit 1
fi

run_python() {
  if [[ "$PY_BIN" == "py" ]]; then
    py -3 "$@"
  else
    "$PY_BIN" "$@"
  fi
}

root_version() {
  # First <version>…</version> after <artifactId>parent</artifactId> in root POM.
  run_python - "$REPO_ROOT/pom.xml" <<'PY'
import re, sys
text = open(sys.argv[1], encoding="utf-8").read()
m = re.search(
    r"<artifactId>\s*parent\s*</artifactId>\s*<version>\s*([^<]+)\s*</version>",
    text,
    re.I | re.S,
)
if not m:
    # Fallback: first project-level version under project coordinates.
    m = re.search(r"<version>\s*([^<]+)\s*</version>", text)
if not m:
    sys.exit("Could not read root parent version from pom.xml")
print(m.group(1).strip())
PY
}

FROM_VERSION="$(root_version)"

if [[ -n "$COMMIT_TYPE" ]]; then
  OUTPUT="$("$CALC" "$COMMIT_TYPE" "$FROM_VERSION")"
  BUMP_TYPE="$(printf '%s' "$OUTPUT" | cut -d'|' -f1)"
  EXPECTED="$(printf '%s' "$OUTPUT" | cut -d'|' -f2)"
else
  BUMP_TYPE="MANUAL"
  EXPECTED="$TO_VERSION"
fi

if [[ -n "$TO_VERSION" && "$TO_VERSION" != "$EXPECTED" && -n "$COMMIT_TYPE" ]]; then
  echo "Note: --to $TO_VERSION overrides calculated $EXPECTED from type $COMMIT_TYPE" >&2
  EXPECTED="$TO_VERSION"
elif [[ -n "$TO_VERSION" && -z "$COMMIT_TYPE" ]]; then
  EXPECTED="$TO_VERSION"
fi

echo "Parent version: $FROM_VERSION"
echo "Bump: $BUMP_TYPE → $EXPECTED"

if [[ "$BUMP_TYPE" == "NONE" && -z "$TO_VERSION" ]]; then
  echo "No parent version change required for this commit type."
  if [[ "$FROM_VERSION" != "$EXPECTED" ]]; then
    echo "ERROR: root version $FROM_VERSION differs from expected unchanged $EXPECTED" >&2
    exit 1
  fi
  exit 0
fi

if [[ "$CHECK_ONLY" -eq 1 ]]; then
  if [[ "$FROM_VERSION" == "$EXPECTED" ]]; then
    echo "OK: parent version matches expected bump."
    exit 0
  fi
  echo "ERROR: parent version mismatch." >&2
  echo "  expected: $EXPECTED" >&2
  echo "  actual:   $FROM_VERSION" >&2
  echo "Apply with: bash scripts/apply-parent-version-bump.sh --type ${COMMIT_TYPE:-fix}" >&2
  exit 1
fi

if [[ "$FROM_VERSION" == "$EXPECTED" ]]; then
  echo "Already at $EXPECTED; nothing to do."
  exit 0
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "Dry-run: would update root project version and child <parent><version> from $FROM_VERSION to $EXPECTED"
  exit 0
fi

run_python - "$REPO_ROOT" "$FROM_VERSION" "$EXPECTED" <<'PY'
import os, re, sys

root, old, new = sys.argv[1], sys.argv[2], sys.argv[3]
old_re = re.escape(old)
new_esc = new
changed = []

def update_root(path):
    text = open(path, encoding="utf-8").read()
    # Project coordinates: groupId, artifactId parent, version
    updated, n = re.subn(
        r"(<artifactId>\s*parent\s*</artifactId>\s*<version>\s*)"
        + old_re
        + r"(\s*</version>)",
        r"\g<1>" + new_esc + r"\2",
        text,
        count=1,
        flags=re.I | re.S,
    )
    if n == 0:
        updated, n = re.subn(
            r"(<version>\s*)" + old_re + r"(\s*</version>)",
            r"\g<1>" + new_esc + r"\2",
            text,
            count=1,
            flags=re.I,
        )
    if n:
        open(path, "w", encoding="utf-8", newline="\n").write(updated)
        changed.append(os.path.relpath(path, root))

def update_child(path):
    text = open(path, encoding="utf-8").read()

    def repl_parent(m):
        block = m.group(0)
        block2, n = re.subn(
            r"(<version>\s*)" + old_re + r"(\s*</version>)",
            r"\g<1>" + new_esc + r"\2",
            block,
            count=1,
            flags=re.I,
        )
        return block2 if n else block

    updated, n = re.subn(
        r"<parent\b[^>]*>.*?</parent>",
        repl_parent,
        text,
        flags=re.I | re.S,
    )
    # Only write when parent version actually changed.
    if updated != text:
        open(path, "w", encoding="utf-8", newline="\n").write(updated)
        changed.append(os.path.relpath(path, root))

update_root(os.path.join(root, "pom.xml"))
for dirpath, dirnames, filenames in os.walk(root):
    # Skip build outputs and VCS
    dirnames[:] = [
        d
        for d in dirnames
        if d not in (".git", "target", "node_modules", "agent_space", ".venv")
    ]
    if "pom.xml" not in filenames:
        continue
    path = os.path.join(dirpath, "pom.xml")
    if os.path.normpath(path) == os.path.normpath(os.path.join(root, "pom.xml")):
        continue
    update_child(path)

print("Updated {} file(s):".format(len(changed)))
for rel in sorted(changed):
    print("  " + rel.replace("\\", "/"))
if not changed:
    sys.exit("No POM files were updated; refuse empty apply.")
PY

echo "Parent version bump applied: $FROM_VERSION → $EXPECTED"
