#!/usr/bin/env sh
set -eu

get_repo_root() {
  script_dir=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
  CDPATH= cd -- "$script_dir/../.." && pwd
}

evaluate_static_repo_path() {
  repo_root=$1
  override_path=${2-}
  CDPATH= cd -- "$repo_root"
  if [ -n "$override_path" ]; then
    normalized=$(printf '%s' "$override_path" | tr '\\' '/')
    mvn -Dhabitv.static.repo.path="$normalized" \
      help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout | tail -n 1
  else
    mvn help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout | tail -n 1
  fi
}

repo_root=$(get_repo_root)
static_repo_path=$(evaluate_static_repo_path "$repo_root" "${1-}")
habitv_repo_root=$(dirname "$static_repo_path")

mkdir -p "$static_repo_path"

echo "habitv.static.repo.path = $static_repo_path"
echo "Deploying with Maven property-based file repository (no hardcoded machine path)."

CDPATH= cd -- "$repo_root"
if [ -n "${1-}" ]; then
  normalized=$(printf '%s' "$1" | tr '\\' '/')
  mvn -Dhabitv.static.repo.path="$normalized" \
    -B -ntp -DskipTests clean deploy \
    '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}'
else
  mvn -B -ntp -DskipTests clean deploy \
    '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}'
fi

cat <<EOF
Deploy complete. Publish with:
  cd "$habitv_repo_root"
  git status --short
  git add repository
  git commit -m "repo: publish habitv artifacts"
  git push
EOF
