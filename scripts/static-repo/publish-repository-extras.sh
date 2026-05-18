#!/usr/bin/env sh
# Manual fallback: metadata via Python; tool zips require publish-repository-extras.ps1 (Windows/PowerShell).
set -eu

get_repo_root() {
  script_dir=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
  CDPATH= cd -- "$script_dir/../.." && pwd
}

evaluate_repository_root() {
  repo_root=$1
  override=${2-}
  CDPATH= cd -- "$repo_root"
  if [ -n "$override" ]; then
    normalized=$(printf '%s' "$override" | tr '\\' '/')
    mvn -Dhabitv.static.repo.path="$normalized" \
      help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout | tail -n 1
  else
    mvn help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout | tail -n 1
  fi
}

repo_root=$(get_repo_root)
repository_root=$(evaluate_repository_root "$repo_root" "${1-}")

if command -v pwsh >/dev/null 2>&1; then
  pwsh -File "$repo_root/scripts/static-repo/publish-repository-extras.ps1" \
    -RepositoryPath "$repository_root"
  exit 0
fi

if command -v powershell.exe >/dev/null 2>&1; then
  powershell.exe -ExecutionPolicy Bypass -File \
    "$repo_root/scripts/static-repo/publish-repository-extras.ps1" \
    -RepositoryPath "$repository_root"
  exit 0
fi

echo "Tool downloads require PowerShell. Generating metadata only with python3." >&2
python3 "$repo_root/scripts/static-repo/generate_repository_metadata.py" "$repository_root"
echo "Done. Repository root: $repository_root"
