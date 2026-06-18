#!/bin/bash
# Calculate SemVer bump based on Conventional Commit type
#
# Usage: calculate-version-bump.sh <commit-type> <current-version>
#
# Outputs: <BUMP_TYPE>|<NEW_VERSION>
#   where BUMP_TYPE = MAJOR|MINOR|PATCH|NONE
#   and NEW_VERSION = X.Y.Z (or X.Y.Z-SNAPSHOT if input was SNAPSHOT)
#
# Examples:
#   calculate-version-bump.sh "feat" "4.2.0" → MINOR|4.3.0
#   calculate-version-bump.sh "fix" "2.1.0-SNAPSHOT" → PATCH|2.1.1-SNAPSHOT
#   calculate-version-bump.sh "breaking-change" "4.2.0" → MAJOR|5.0.0

set -e

COMMIT_TYPE="$1"
CURRENT_VERSION="$2"

# Validate inputs
if [[ -z "$COMMIT_TYPE" ]] || [[ -z "$CURRENT_VERSION" ]]; then
    echo "❌ Usage: calculate-version-bump.sh <type> <current-version>" >&2
    exit 1
fi

# Remove -SNAPSHOT suffix for parsing
CLEAN_VERSION=$(echo "$CURRENT_VERSION" | sed 's/-SNAPSHOT$//')

# Check if version is in X.Y.Z format
if ! echo "$CLEAN_VERSION" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
    echo "❌ Invalid version format: $CURRENT_VERSION" >&2
    echo "   Expected format: X.Y.Z or X.Y.Z-SNAPSHOT" >&2
    exit 1
fi

# Parse major.minor.patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CLEAN_VERSION"

# Determine bump type and calculate new version
case "$COMMIT_TYPE" in
    feat)
        BUMP_TYPE="MINOR"
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$((MINOR + 1))
        NEW_PATCH=0
        ;;
    fix|refactor|perf)
        BUMP_TYPE="PATCH"
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$MINOR
        NEW_PATCH=$((PATCH + 1))
        ;;
    breaking-change|breaking_change)
        BUMP_TYPE="MAJOR"
        NEW_MAJOR=$((MAJOR + 1))
        NEW_MINOR=0
        NEW_PATCH=0
        ;;
    chore|docs|test|ci|build|style|revert)
        BUMP_TYPE="NONE"
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$MINOR
        NEW_PATCH=$PATCH
        ;;
    *)
        echo "❌ Unknown commit type: $COMMIT_TYPE" >&2
        exit 1
        ;;
esac

# Build new version
NEW_VERSION="$NEW_MAJOR.$NEW_MINOR.$NEW_PATCH"

# Preserve -SNAPSHOT suffix if original had it
if [[ "$CURRENT_VERSION" == *"-SNAPSHOT" ]]; then
    NEW_VERSION="$NEW_VERSION-SNAPSHOT"
fi

# Output in format: BUMP_TYPE|NEW_VERSION
echo "$BUMP_TYPE|$NEW_VERSION"
