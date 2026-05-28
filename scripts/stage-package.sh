#!/usr/bin/env bash
# Collect built Maven artifacts into target/package-staging/Habitv/.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n 1)"
STAGING="$REPO_ROOT/target/package-staging/Habitv"
LIB_DIR="$STAGING/lib"
PLUGINS_DIR="$LIB_DIR/plugins"
BIN_DIR="$STAGING/bin"

rm -rf "$REPO_ROOT/target/package-staging"
mkdir -p "$LIB_DIR" "$PLUGINS_DIR" "$BIN_DIR"

MAIN_JAR="$REPO_ROOT/application/habiTv/target/habiTv-${VERSION}.jar"
if [[ ! -f "$MAIN_JAR" ]]; then
  echo "Main JAR not found: $MAIN_JAR" >&2
  echo "Run: mvn -B -ntp -DskipTests package" >&2
  exit 1
fi

cp "$MAIN_JAR" "$LIB_DIR/"

shopt -s nullglob
for module_dir in "$REPO_ROOT"/plugins/*/; do
  module_name="$(basename "$module_dir")"
  if [[ "$module_name" == "plugin-tester" ]]; then
    continue
  fi
  for jar in "$module_dir"/target/"${module_name}"-*.jar; do
    [[ -f "$jar" ]] || continue
    base_name="$(basename "$jar")"
    case "$base_name" in
      *-sources.jar|*-javadoc.jar|*-tests.jar|original-*)
        continue
        ;;
    esac
    cp "$jar" "$PLUGINS_DIR/"
  done
done

if [[ -f "$REPO_ROOT/packaging/common/README.txt" ]]; then
  cp "$REPO_ROOT/packaging/common/README.txt" "$STAGING/README.txt"
fi

mkdir -p "$REPO_ROOT/target/packages"
printf 'VERSION=%s\nSTAGING=%s\n' "$VERSION" "$STAGING" > "$REPO_ROOT/target/package-staging/.env"

plugin_count="$(find "$PLUGINS_DIR" -maxdepth 1 -name '*.jar' | wc -l | tr -d ' ')"
echo "Staged Habitv ${VERSION} at ${STAGING} (${plugin_count} plugins)"
