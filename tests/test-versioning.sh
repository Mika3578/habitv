#!/bin/bash
# Unit tests for versioning scripts
#
# Run with: bash tests/test-versioning.sh
# Exit 0 if all tests pass, 1 if any fail

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VALIDATE_SCRIPT="$SCRIPT_DIR/scripts/validate-conventional-commit.sh"
BUMP_SCRIPT="$SCRIPT_DIR/scripts/calculate-version-bump.sh"

TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to run a test
run_test() {
    local test_name="$1"
    local command="$2"
    local expected_exit="$3"  # 0 for success, 1 for failure

    echo -n "Testing: $test_name ... "

    if eval "$command" > /dev/null 2>&1; then
        actual_exit=0
    else
        actual_exit=1
    fi

    if [[ $actual_exit -eq $expected_exit ]]; then
        echo "✅"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo "❌ (expected exit $expected_exit, got $actual_exit)"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

# Helper function for version bump tests
test_bump() {
    local test_name="$1"
    local type="$2"
    local current="$3"
    local expected_output="$4"

    echo -n "Testing: $test_name ... "

    set +e
    output="$("$BUMP_SCRIPT" "$type" "$current" 2>&1)"
    status=$?
    set -e

    if [[ $status -ne 0 ]]; then
        echo "❌ (script exited $status)"
        echo "   Output: $output"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return
    fi

    if [[ "$output" == "$expected_output" ]]; then
        echo "✅"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo "❌"
        echo "   Expected: $expected_output"
        echo "   Got:      $output"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

# Helper for bump calculator failures (invalid input must not abort suite)
test_bump_failure() {
    local test_name="$1"
    local type="$2"
    local current="$3"

    echo -n "Testing: $test_name ... "

    set +e
    output="$("$BUMP_SCRIPT" "$type" "$current" 2>&1)"
    status=$?
    set -e

    if [[ $status -ne 0 ]]; then
        echo "✅"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo "❌ (expected non-zero exit, got 0)"
        echo "   Output: $output"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

echo ""
echo "=== Conventional Commits Validation Tests ==="
echo ""

# Valid commits
run_test "Valid: feat(core)" \
    "$VALIDATE_SCRIPT 'feat(core): add new feature'" \
    0

run_test "Valid: fix(ffmpeg-exporter)" \
    "$VALIDATE_SCRIPT 'fix(ffmpeg-exporter): handle codec error'" \
    0

run_test "Valid: refactor(framework)" \
    "$VALIDATE_SCRIPT 'refactor(framework): simplify plugin loader'" \
    0

run_test "Valid: with BREAKING CHANGE footer" \
    "$VALIDATE_SCRIPT \$'feat(core): rename IProvider interface\n\nBREAKING CHANGE: IProvider.getEpisodes() renamed to retrieve()'" \
    0

run_test "Valid: breaking via ! syntax" \
    "$VALIDATE_SCRIPT 'feat(core)!: rename API interface'" \
    0

run_test "Valid: fix breaking via ! syntax" \
    "$VALIDATE_SCRIPT 'fix(download)!: change retry contract'" \
    0

run_test "Valid: style(scope)" \
    "$VALIDATE_SCRIPT 'style(format): normalize whitespace'" \
    0

run_test "Valid: revert(scope)" \
    "$VALIDATE_SCRIPT 'revert(versioning): revert previous versioning change'" \
    0

run_test "Valid: style breaking via ! syntax" \
    "$VALIDATE_SCRIPT 'style(format)!: change formatting contract'" \
    0

run_test "Valid: revert breaking via ! syntax" \
    "$VALIDATE_SCRIPT 'revert(versioning)!: revert public behavior change'" \
    0

# Invalid commits
run_test "Invalid: missing scope" \
    "$VALIDATE_SCRIPT 'feat: add feature'" \
    1

run_test "Invalid: empty subject" \
    "$VALIDATE_SCRIPT 'fix(core):'" \
    1

run_test "Invalid: unknown type" \
    "$VALIDATE_SCRIPT 'unknown(core): subject'" \
    1

run_test "Invalid: empty message" \
    "$VALIDATE_SCRIPT ''" \
    1

echo ""
echo "=== Version Bump Calculation Tests ==="
echo ""

# MINOR bumps (feat)
test_bump "MINOR: 4.2.0 → 4.3.0" \
    "feat" "4.2.0" \
    "MINOR|4.3.0"

test_bump "MINOR: 1.0.0 → 1.1.0" \
    "feat" "1.0.0" \
    "MINOR|1.1.0"

# PATCH bumps (fix, refactor, perf)
test_bump "PATCH (fix): 2.0.0 → 2.0.1" \
    "fix" "2.0.0" \
    "PATCH|2.0.1"

test_bump "PATCH (refactor): 4.2.5 → 4.2.6" \
    "refactor" "4.2.5" \
    "PATCH|4.2.6"

test_bump "PATCH (perf): 1.2.3 → 1.2.4" \
    "perf" "1.2.3" \
    "PATCH|1.2.4"

# MAJOR bumps (breaking-change)
test_bump "MAJOR: 4.2.0 → 5.0.0" \
    "breaking-change" "4.2.0" \
    "MAJOR|5.0.0"

test_bump "MAJOR: 1.5.3 → 2.0.0" \
    "breaking-change" "1.5.3" \
    "MAJOR|2.0.0"

# NO bump (chore, docs, test)
test_bump "NONE (chore): 4.2.0 stays 4.2.0" \
    "chore" "4.2.0" \
    "NONE|4.2.0"

test_bump "NONE (docs): 1.0.0 stays 1.0.0" \
    "docs" "1.0.0" \
    "NONE|1.0.0"

test_bump "NONE (style): 4.2.0 stays 4.2.0" \
    "style" "4.2.0" \
    "NONE|4.2.0"

test_bump "NONE (revert): 1.0.0 stays 1.0.0" \
    "revert" "1.0.0" \
    "NONE|1.0.0"

test_bump_failure "Invalid type aborts calculator with non-zero exit" \
    "unknown" "4.2.0"

# Preserve -SNAPSHOT
test_bump "PATCH with -SNAPSHOT: 4.2.0-SNAPSHOT → 4.2.1-SNAPSHOT" \
    "fix" "4.2.0-SNAPSHOT" \
    "PATCH|4.2.1-SNAPSHOT"

test_bump "MINOR with -SNAPSHOT: 4.2.0-SNAPSHOT → 4.3.0-SNAPSHOT" \
    "feat" "4.2.0-SNAPSHOT" \
    "MINOR|4.3.0-SNAPSHOT"

test_bump "MAJOR with -SNAPSHOT: 4.2.0-SNAPSHOT → 5.0.0-SNAPSHOT" \
    "breaking-change" "4.2.0-SNAPSHOT" \
    "MAJOR|5.0.0-SNAPSHOT"

echo ""
echo "=== Test Summary ==="
echo "Passed: $TESTS_PASSED"
echo "Failed: $TESTS_FAILED"
echo ""

if [[ $TESTS_FAILED -eq 0 ]]; then
    echo "✅ All tests passed!"
    exit 0
else
    echo "❌ Some tests failed"
    exit 1
fi
