#!/usr/bin/env sh
set -eu

get_repo_root() {
  script_dir=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
  CDPATH= cd -- "$script_dir/../.." && pwd
}

print_resolution_help_and_fail() {
  cat <<'EOF' >&2
Unable to resolve habitv-repo checkout.
Resolution order:
  1. first script argument
  2. HABITV_STATIC_REPO_LOCAL_PATH
  3. $HOME/dev/habitv-repo
  4. ../habitv-repo
Expected standard layout:
  $HOME/dev/habitv
  $HOME/dev/habitv-repo
EOF
  exit 1
}

resolve_static_repo_path() {
  explicit_path=${1-}
  repo_root=${2}
  sibling_default=$(dirname "$repo_root")/habitv-repo

  if [ -n "$explicit_path" ] && [ -d "$explicit_path" ]; then
    CDPATH= cd -- "$explicit_path" && pwd
    return
  fi

  if [ -n "${HABITV_STATIC_REPO_LOCAL_PATH-}" ] && [ -d "${HABITV_STATIC_REPO_LOCAL_PATH}" ]; then
    CDPATH= cd -- "$HABITV_STATIC_REPO_LOCAL_PATH" && pwd
    return
  fi

  if [ -n "${HOME-}" ] && [ -d "$HOME/dev/habitv-repo" ]; then
    CDPATH= cd -- "$HOME/dev/habitv-repo" && pwd
    return
  fi

  if [ -d "$sibling_default" ]; then
    CDPATH= cd -- "$sibling_default" && pwd
    return
  fi

  print_resolution_help_and_fail
}

repo_root=$(get_repo_root)
resolved_static_repo_path=$(resolve_static_repo_path "${1-}" "$repo_root")

if [ ! -d "$resolved_static_repo_path" ]; then
  echo "Static repository path does not exist: $resolved_static_repo_path" >&2
  exit 1
fi

resolved_repository_path="$resolved_static_repo_path/repository"
mkdir -p "$resolved_repository_path"

echo "Deploying Maven artifacts to: file://$resolved_repository_path"
(
  CDPATH= cd -- "$repo_root"
  mvn -B -ntp -DskipTests clean deploy \
    -DaltDeploymentRepository=habitv-local::default::file://../habitv-repo/repository
)

cat <<EOF
Deploy complete. Publish with:
  cd "$resolved_static_repo_path"
  git status --short
  git add repository
  git commit -m "repo: publish habitv artifacts"
  git push
EOF
