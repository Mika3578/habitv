#!/usr/bin/env bash
# Build macOS portable archive, app bundle, and DMG.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

bash "$REPO_ROOT/scripts/stage-package.sh"

# shellcheck disable=SC1091
source "$REPO_ROOT/target/package-staging/.env"

STAGING="$STAGING"
PACKAGES="$REPO_ROOT/target/packages"
mkdir -p "$PACKAGES/bin"

cp "$REPO_ROOT/packaging/macos/bin/habitv" "$STAGING/bin/habitv"
chmod 755 "$STAGING/bin/habitv"
cp "$STAGING/bin/habitv" "$PACKAGES/bin/habitv"
chmod 755 "$PACKAGES/bin/habitv"

ARCHIVE="$PACKAGES/habitv-macos.tar.gz"
tar -C "$REPO_ROOT/target/package-staging" -czf "$ARCHIVE" Habitv

if [[ ! -s "$ARCHIVE" ]]; then
  echo "Archive is empty: $ARCHIVE" >&2
  exit 1
fi

APP_DIR="$PACKAGES/Habitv.app"
rm -rf "$APP_DIR"
mkdir -p "$APP_DIR/Contents/MacOS" "$APP_DIR/Contents/Resources"

sed "s/@VERSION@/${VERSION}/" "$REPO_ROOT/packaging/macos/app/Info.plist" > "$APP_DIR/Contents/Info.plist"
cp "$REPO_ROOT/packaging/macos/app/Habitv.command" "$APP_DIR/Contents/MacOS/Habitv"
chmod 755 "$APP_DIR/Contents/MacOS/Habitv"

cp -a "$STAGING/." "$APP_DIR/Contents/Resources/Habitv/"

if [[ ! -d "$APP_DIR/Contents/Resources/Habitv/lib" ]]; then
  echo "App bundle is missing packaged libraries." >&2
  exit 1
fi

DMG_STAGING="$PACKAGES/dmg-staging"
rm -rf "$DMG_STAGING"
mkdir -p "$DMG_STAGING"
cp -R "$APP_DIR" "$DMG_STAGING/"
cp "$REPO_ROOT/packaging/macos/dmg/README.md" "$DMG_STAGING/README.txt"

DMG_FILE="$PACKAGES/Habitv.dmg"
rm -f "$DMG_FILE"
hdiutil create -volname "Habitv" -srcfolder "$DMG_STAGING" -ov -format UDZO "$DMG_FILE"

if [[ ! -s "$DMG_FILE" ]]; then
  echo "DMG is empty: $DMG_FILE" >&2
  exit 1
fi

echo "macOS packages written to $PACKAGES"
echo "  - habitv-macos.tar.gz"
echo "  - Habitv.app"
echo "  - Habitv.dmg"
