#!/bin/bash
# Validate Conventional Commits format for Habitv
#
# Usage: validate-conventional-commit.sh "<commit message>"
# Exit 0 if valid, 1 if invalid
#
# Expected format:
#   type(scope): subject
#   type(scope)!: subject
#
#   [optional body]
#
#   [optional footer: BREAKING CHANGE: ...]
#
# Valid types: feat, fix, docs, test, refactor, perf, chore, ci, build
# Valid scopes: core, framework, ffmpeg-exporter, francetv-provider, etc.

set -e

MSG="$1"

# Check if message is empty
if [[ -z "$MSG" ]]; then
    echo "❌ Commit message is empty"
    exit 1
fi

# Extract first line
FIRST_LINE=$(echo "$MSG" | head -1)

# Check format: type(scope): subject or type(scope)!: subject
# Pattern: type(scope)[!]: subject (non-empty)
COMMIT_HEADER_RE='^(feat|fix|docs|test|refactor|perf|chore|ci|build)\([a-z0-9\-]+\)!?: .+'
if ! echo "$FIRST_LINE" | grep -qE "$COMMIT_HEADER_RE"; then
    echo "❌ Invalid commit format"
    echo ""
    echo "Got: '$FIRST_LINE'"
    echo ""
    echo "Expected format:"
    echo "  type(scope): subject"
    echo "  type(scope)!: subject"
    echo ""
    echo "Valid types: feat, fix, docs, test, refactor, perf, chore, ci, build"
    echo "Example: feat(francetv-provider): add series description extraction"
    echo "Example: feat(core)!: rename provider API"
    echo ""
    exit 1
fi

# Warn if breaking change via ! syntax
if echo "$FIRST_LINE" | grep -qE '^(feat|fix|docs|test|refactor|perf|chore|ci|build)\([a-z0-9\-]+\)!:'; then
    echo "⚠️  BREAKING CHANGE detected via ! syntax"
    echo "   → This commit will bump MAJOR version"
fi

# Warn if BREAKING CHANGE footer present
if echo "$MSG" | grep -q "BREAKING CHANGE:"; then
    echo "⚠️  BREAKING CHANGE detected"
    echo "   → This commit will bump MAJOR version"
fi

echo "✅ Valid Conventional Commit format"
exit 0
