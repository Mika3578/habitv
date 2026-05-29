#!/usr/bin/env bash
# Build Linux portable archive and DEB package.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

bash "$REPO_ROOT/scripts/stage-package.sh"

# shellcheck disable=SC1091
source "$REPO_ROOT/target/package-staging/.env"

STAGING="$STAGING"
PACKAGES="$REPO_ROOT/target/packages"
mkdir -p "$PACKAGES/bin"

cp "$REPO_ROOT/packaging/linux/bin/habitv" "$STAGING/bin/habitv"
chmod 755 "$STAGING/bin/habitv"
cp "$STAGING/bin/habitv" "$PACKAGES/bin/habitv"
chmod 755 "$PACKAGES/bin/habitv"

ARCHIVE="$PACKAGES/habitv-linux.tar.gz"
tar -C "$REPO_ROOT/target/package-staging" -czf "$ARCHIVE" Habitv

if [[ ! -s "$ARCHIVE" ]]; then
  echo "Archive is empty: $ARCHIVE" >&2
  exit 1
fi

DEB_ROOT="$PACKAGES/deb-root"
rm -rf "$DEB_ROOT"
mkdir -p "$DEB_ROOT/DEBIAN"
mkdir -p "$DEB_ROOT/opt/habitv"
mkdir -p "$DEB_ROOT/usr/share/applications"

cp -a "$STAGING/." "$DEB_ROOT/opt/habitv/"
sed "s/@VERSION@/${VERSION}/" "$REPO_ROOT/packaging/linux/deb/control" > "$DEB_ROOT/DEBIAN/control"
cp "$REPO_ROOT/packaging/linux/deb/postinst" "$DEB_ROOT/DEBIAN/postinst"
cp "$REPO_ROOT/packaging/linux/deb/prerm" "$DEB_ROOT/DEBIAN/prerm"
cp "$REPO_ROOT/packaging/linux/deb/habitv.desktop" "$DEB_ROOT/usr/share/applications/habitv.desktop"

chmod 755 "$DEB_ROOT/DEBIAN/postinst" "$DEB_ROOT/DEBIAN/prerm" "$DEB_ROOT/opt/habitv/bin/habitv"

DEB_FILE="$PACKAGES/habitv.deb"
dpkg-deb --root-owner-group --build "$DEB_ROOT" "$DEB_FILE"

if [[ ! -s "$DEB_FILE" ]]; then
  echo "DEB package is empty: $DEB_FILE" >&2
  exit 1
fi

echo "Linux packages written to $PACKAGES"
echo "  - habitv-linux.tar.gz"
echo "  - habitv.deb"
