#!/usr/bin/env bash
set -euo pipefail

BUNDLE_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RESOURCES="$BUNDLE_ROOT/Resources/Habitv"
LIB_DIR="$RESOURCES/lib"

if ! command -v java >/dev/null 2>&1; then
  osascript -e 'display alert "Java required" message "Java 8 with JavaFX support is required. Install a JDK 8 distribution that includes JavaFX (for example Liberica JDK 8 Full)." as critical' >/dev/null 2>&1 || true
  echo "Java 8 with JavaFX support is required but 'java' was not found on PATH." >&2
  exit 1
fi

JAR=""
shopt -s nullglob
for candidate in "$LIB_DIR"/habiTv-*.jar; do
  JAR="$candidate"
  break
done

if [[ -z "$JAR" ]]; then
  echo "Habitv application JAR not found in $LIB_DIR" >&2
  exit 1
fi

exec java -jar "$JAR" "$@"
