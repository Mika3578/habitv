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

assert_git_checkout() {
  repo_path=$1
  if ! git -C "$repo_path" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "Path is not a Git checkout: $repo_path" >&2
    exit 1
  fi
}

assert_habitv_repo_remote() {
  repo_path=$1
  remote_url=$(git -C "$repo_path" remote get-url origin 2>/dev/null || true)
  if [ -z "$remote_url" ]; then
    echo "Unable to read origin remote for $repo_path" >&2
    exit 1
  fi

  case "$remote_url" in
    *Mika3578/habitv-repo|*Mika3578/habitv-repo.git) ;;
    *)
      echo "Static repository origin must target Mika3578/habitv-repo. Found: $remote_url" >&2
      exit 1
      ;;
  esac
}

repo_root=$(get_repo_root)
resolved_static_repo_path=$(resolve_static_repo_path "${1-}" "$repo_root")

if [ ! -d "$resolved_static_repo_path" ]; then
  echo "Static repository path does not exist: $resolved_static_repo_path" >&2
  exit 1
fi

assert_git_checkout "$resolved_static_repo_path"
assert_habitv_repo_remote "$resolved_static_repo_path"

resolved_static_repo_maven_path="$resolved_static_repo_path/maven"
mkdir -p "$resolved_static_repo_maven_path"

echo "Deploying Maven artifacts to: habitv-static-repo::default::file:///$resolved_static_repo_maven_path"
mvn -B -ntp -DskipTests deploy \
  "-DaltDeploymentRepository=habitv-static-repo::default::file:///$resolved_static_repo_maven_path"

git -C "$resolved_static_repo_path" add .
if git -C "$resolved_static_repo_path" diff --cached --quiet; then
  echo "No artifact changes to publish."
  exit 0
fi

git -C "$resolved_static_repo_path" commit -m "repo: publish Habitv Maven artifacts"
git -C "$resolved_static_repo_path" push
